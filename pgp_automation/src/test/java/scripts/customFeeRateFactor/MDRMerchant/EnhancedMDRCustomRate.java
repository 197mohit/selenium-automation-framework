package scripts.customFeeRateFactor.MDRMerchant;

import com.paytm.LocalConfig;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.OrderStatus;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.util.Date;

@Feature("PGP-21710")
@Owner("Gagandeep")
public class EnhancedMDRCustomRate extends PGPBaseTest {

    private static final String successMessage = "Txn Success";
    private static final String successStatus = "TXN_SUCCESS";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final double txnAmount = 2.00;
    Constants.MerchantType mdrCustomRate = Constants.MerchantType.MDR_CUSTOM_RATE;
    Constants.MerchantType mdrCustom = Constants.MerchantType.MDR_INTL_MERC;

    public static void validateSuccessPeonPCFtxn(OrderDTO orderDTO,String payMode)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "feeRateFactors", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.feeRateFactors().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals("vivek4"),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals(successMessage),
                peon.status().equals(successStatus),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    public static TxnStatus validateSuccessTxnStatusMDRTxn(OrderDTO orderDTO, String payMode) {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static TxnStatus validateSuccessNativeTxnStatusMDRTxn(OrderDTO orderDTO, String payMode) {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        return txnStatus;

    }


    public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }

    public static JsonPath validateSuccessPaymentStatusAPIMDRTxn(OrderDTO orderDTO, Constants.MerchantType merchantType, String payMode) {
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase(successStatus);
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase(successMessage);
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase(payMode);

        return getPaymentStatus;
    }

    public static void validateOrderStatusAPI(OrderDTO orderDTO, String payMode, Double chargeAmount) {
        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Assertions.assertThat(response.jsonPath().getString("body.txnId")).isNotEmpty().isNotNull();
        Assertions.assertThat(response.jsonPath().getString("body.orderId")).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(response.jsonPath().getString("body.txnStatus")).isEqualTo(successStatus);
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseMsg")).isEqualTo(successMessage);
        Assertions.assertThat(response.jsonPath().getString("body.paymentMode")).isEqualTo(payMode);
        Assertions.assertThat(Double.parseDouble(response.jsonPath().getString("body.txnAmount"))).isEqualTo(Double.parseDouble(orderDTO.getTXN_AMOUNT()));
        Assertions.assertThat(response.jsonPath().getString("body.mid")).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseCode")).isEqualTo("01");
    }

    public void validateSuccessResponseMDRTxn(OrderDTO orderDTO, Constants.MerchantType merchantType, String payMode) {

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateStatus(successStatus)
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchantType.getKey()).assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC txn on MDR merchant having MASTER 10rs flat commission and default 1 %")
    public void successfulFeeFactorDCTxnForMDR(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustomRate, "N");

        String payMode = "DC";

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DC);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrCustomRate, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();


        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage, txnAmount);
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);

        validateSuccessResponseMDRTxn(orderDTO, mdrCustomRate, payMode);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, mdrCustomRate, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, txnAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "MASTER", "HDFC", "API");

        double flatCommission = 10.0;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

        //Successful Peon
        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO, payMode);
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases >25 10 flat commission and default 1 %")
    public void successfulFeeFactorCCInternationalLessThan100TxnForMDR(@Optional("enhancedwap_revamp") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");

        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrCustom, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .setMERC_UNQ_REF("vivek4")
                .build();


        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage, txnAmount);

        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);

        validateSuccessResponseMDRTxn(orderDTO, mdrCustom, payMode);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, mdrCustom, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, txnAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "internationalCardPayment","IHDF", "API");

        double flatCommission = 10;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

        //Successful Peon
        validateSuccessPeonPCFtxn(orderDTO, payMode);
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases<25 12 flat and percentage both commission and default 1 %")
    public void successfulFeeFactorCCInternationalMoreThan25TxnForMDR(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");

        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrCustom, theme)
                .setTXN_AMOUNT(String.valueOf(30))
                .setSSO_TOKEN(user.ssoToken())
                .build();


        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage, 30.0);

        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);

        validateSuccessResponseMDRTxn(orderDTO, mdrCustom, payMode);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, mdrCustom, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, 30.0);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "internationalCardPayment","IHDF", "API");

        double flatCommission = 12.0;
        double percentCommission = 30 * 0.12;
        double TotalCommission = flatCommission + percentCommission;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(TotalCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful DC PREPAID txn on MDR merchant having flat commission of 10 and default 1 %")
    public void successfulFeeFactorDCPrePaidCard(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertConvFee(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, "N");
        User user = userManager.getForWrite(Label.BASIC);

        String payMode = "DC";

        enablePrepaidCardFF4jFlag();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);


        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();


        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        PcfHelpher.validateCashierPageAmount(cashierPage, txnAmount);

        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);

        validateSuccessResponseMDRTxn(orderDTO, Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, payMode);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, txnAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "prepaidCard","HDFC", "API");

        double flatCommission = 10.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on MDR merchant having flat commission of 15 and default 1 %")
    public void successfulFeeFactorNBForSlabMDR(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustomRate, "N");
        User user = userManager.getForWrite(Label.BASIC);

        String payMode = "NB";

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");


        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrCustomRate, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();


        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        PcfHelpher.validateCashierPageAmount(cashierPage, txnAmount);

        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);

        validateSuccessResponseMDRTxn(orderDTO, mdrCustomRate, payMode);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, mdrCustomRate, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, txnAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

        double flatCommission = 15.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

        //Successful Peon
        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO, payMode);
    }
}
