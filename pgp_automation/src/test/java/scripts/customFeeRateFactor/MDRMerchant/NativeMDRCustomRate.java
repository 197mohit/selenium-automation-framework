package scripts.customFeeRateFactor.MDRMerchant;

import com.paytm.LocalConfig;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.OrderStatus;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PrepaidHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.util.PayMethodType;
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
public class NativeMDRCustomRate extends PGPBaseTest {


    private static String successMessage = "Txn Success";
    private static String successStatus = "TXN_SUCCESS";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType mdrCustomRate = Constants.MerchantType.MDR_CUSTOM_RATE;
    Constants.MerchantType mdrCustom = Constants.MerchantType.MDR_INTL_MERC;
    private double txnAmount = 2.00;
//    private String theme = "enhancedweb";
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
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
    /*
    public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }

     */

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

    //FPO
    public JsonPath isMDRMerchant(String txnToken, InitTxnDTO initTxnDTO) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.pcfEnabled")).as("PCF is disabled").isEqualTo(false);
        return fetchPaymentOptionsJson;
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

    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful DC txn on MDR merchant having MASTER 10rs flat commission and default 1 %")
    public void successfulFeeFactorDCTxnForMDRNative(@Optional("false") Boolean isNativePlus) throws Exception {

        PcfHelpher.assertConvFee(mdrCustomRate, "N");

        String payMode = "DC";

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DC);

        User user = userManager.getForWrite(Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustomRate)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isMDRMerchant(txnToken, initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mdrCustomRate, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

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
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases bases slab bases >25 10 flat commission and default 1 %")
    public void successfulFeeFactorCCInternationalLessThan100TxnForMDRNative(@Optional("false") Boolean isNativePlus) throws Exception {


        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        User user = userManager.getForWrite(Label.BASIC);
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1("");
        extendInfo.setUdf2("");
        extendInfo.setUdf3("");
        extendInfo.setUdf4("");
        extendInfo.setUdf5("");
        extendInfo.setMercUnqRef("vivek4");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustom)
                .setTxnValue(String.valueOf(txnAmount)).setExtendInfo(extendInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isMDRMerchant(txnToken, initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mdrCustom, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponseMDRTxn(orderDTO,mdrCustom, payMode);


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
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "API");

        double flatCommission = 10;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

        //Successful Peon
        validateSuccessPeonPCFtxn(orderDTO, payMode);
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases<25 12 flat and percentage both commission and default 1 %")
    public void successfulFeeFactorCCInternationalMoreThan100TxnForMDR(@Optional("false") Boolean isNativePlus) throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");

        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        User user = userManager.getForWrite(Label.BASIC);
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1("");
        extendInfo.setUdf2("");
        extendInfo.setUdf3("");
        extendInfo.setUdf4("");
        extendInfo.setUdf5("");
        extendInfo.setMercUnqRef("vivek4");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustom)
                .setTxnValue("30").setExtendInfo(extendInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isMDRMerchant(txnToken, initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mdrCustom, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        validateSuccessResponseMDRTxn(orderDTO, mdrCustom, payMode);


        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, mdrCustom, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, 30.00);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "API");


        double flatCommission = 12.0;
        double percentCommission = 30 * 0.12;
        double TotalCommission = flatCommission + percentCommission;



        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(TotalCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

        //Successful Peon
        validateSuccessPeonPCFtxn(orderDTO, payMode);
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful DC PREPAID txn on MDR merchant having flat commission of 10 and default 1 %")
    public void successfulFeeFactorDCPrePaidCard(@Optional("false") Boolean isNativePlus) throws Exception {

        PcfHelpher.assertConvFee(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, "N");
        User user = userManager.getForWrite(Label.BASIC);

        String payMode = "DC";

    //    enablePrepaidCardFF4jFlag();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO, Constants.MerchantType.MASKED_MOBILE_ENABLED_PG2_RTDD);


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
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC","prepaidCard", "API");

        double flatCommission = 10.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful NB txn on MDR merchant having flat commission of 15 and default 1 %")
    public void successfulFeeFactorNBForSlabMDR(@Optional("false") Boolean isNativePlus) throws Exception {

        PcfHelpher.assertConvFee(mdrCustomRate, "N");

        User user = userManager.getForWrite(Label.BASIC);

        String payMode = "NB";

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrCustomRate)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isMDRMerchant(txnToken, initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mdrCustomRate, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.NET_BANKING)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setChannelCode("ICICI")
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);


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


    @Parameters({"isNativePlus"})
    @Test(description = "Verify a successful Hybrid CC txn on MDR merchant having flat commission of 10 and default 1 %")
    public void successfulFeeFactorHybridforMDR(@Optional("false") Boolean isNativePlus) throws Exception {


        PcfHelpher.assertConvFee(Constants.MerchantType.Hybrid_Retry, "N");
        User user = userManager.getForWrite(Label.BASIC);

        WalletHelpers.modifyBalance(user, txnAmount - 1);
        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_Retry)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Hybrid_Retry, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        
        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, Constants.MerchantType.Hybrid_Retry, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, txnAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "API");

        double flatCommission = 10.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

    }


}
