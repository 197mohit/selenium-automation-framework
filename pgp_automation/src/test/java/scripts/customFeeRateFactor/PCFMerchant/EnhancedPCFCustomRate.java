package scripts.customFeeRateFactor.PCFMerchant;


import com.paytm.LocalConfig;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.TxnStatusList;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.fetchPcfDetail.feeRateFactors;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

@Feature("PGP-21710")
@Owner("Gagandeep")
public class EnhancedPCFCustomRate extends PGPBaseTest {


    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType pcfRateFactor = Constants.MerchantType.CUSTOMFEEPCF;
    Constants.MerchantType pcfRateFactorPg2Rtdd = Constants.MerchantType.CUSTOMFEEPCF_PG2_RTDD;
    Constants.MerchantType pcfRate = Constants.MerchantType.PCF_MERCHANT;
    Constants.MerchantType pcfRatePg2Rtdd = Constants.MerchantType.PCF_MERCHANT_PG2_RTDD;
    private double txnAmount = 2.00;
//    private String theme = "enhancedweb";
    private static String successStatus = "TXN_SUCCESS";


    public static void validateSuccessTxnStatusListPCFTxn(OrderDTO orderDTO,String payMode,Double chargeAmount)
    {
        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatusList
                .validateTxnId(orderDTO.getTxnId())
                .validateBankTxnIdNonEmpty()
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(successStatus)
                .validateTXNTYPE("SALE")
                .validateResponseCode("01")
                .validateResponseMessage("Txn Successful.")
                .validateMID(orderDTO.getMID())
                .validateRefundId("")
                .assertAll();


    }
    /*
    public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }

     */

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC txn on PCF merchant having VISA 2% commission and default 1 %")
    public void successfulFeeFactorDCTxnForPCF(@Optional("enhancedweb") String theme) throws Exception {

        String payMode = "DC";

        PaymentDTO paymentDTO = new PaymentDTO();

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfRateFactorPg2Rtdd, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "VISA", user, "DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");
        Double dcTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse, "DEBIT_CARD");

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage, dcTxnAmount);

        cashierPage.payBy(Constants.PayMode.DC);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful International CC txn on PCF merchant having 10 Rs commission , VISA 2% commission and default 1 %")
    public void successfulFeeFactorInternationalCCTxnForPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setInternationalCardPayment(true);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfRatePg2Rtdd, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "VISA", user, "CREDIT_CARD", feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse, "CREDIT_CARD");

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage, ccTxnAmount);

        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRatePg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRatePg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "API");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountLessThan25ForPCF(@Optional("enhancedweb") String theme) throws Exception {

        String payMode = "NB";

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfRateFactorPg2Rtdd, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");
        Double nbTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse, "NET_BANKING");

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());

        PcfHelpher.validateCashierPageAmount(cashierPage, nbTxnAmount);
        cashierPage.closePMDetailBtn().click();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMoreThan100LessThan200ForPCF(@Optional("enhancedweb") String theme) throws Exception {


        String payMode = "NB";

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfRateFactorPg2Rtdd, theme)
                .setTXN_AMOUNT("30")
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");
        Double nbTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse, "NET_BANKING");

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());

        PcfHelpher.validateCashierPageAmount(cashierPage, nbTxnAmount);
        cashierPage.closePMDetailBtn().click();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMore200ForPCFTOMDR(@Optional("enhancedweb_revamp") String theme) throws Exception {


        String payMode = "NB";

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfRateFactorPg2Rtdd, theme)
                .setTXN_AMOUNT("60")
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");
        Double nbTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse, "NET_BANKING");


        Assertions.assertThat(chargeAmount).as("Charge amount is equal to zero").isEqualTo(0.00);

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());

        PcfHelpher.validateCashierPageAmount(cashierPage, nbTxnAmount);
        cashierPage.closePMDetailBtn().click();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful CC Master txn on PCF merchant having flat 6 rs MDR commission ")
    public void successfulFeeFactorCCMasterTxnForPCF_MDR(@Optional("enhancedweb_revamp") String theme) throws Exception {


        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfRateFactorPg2Rtdd, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "MASTER", user, "CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse, "CREDIT_CARD");

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage, ccTxnAmount);
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}

        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "API");
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC Prepaid card txn on PCF merchant having flat 15 flat PCF commission")
    public void successfulFeeFactorDCPrepaidTxnForPCF_MDR(@Optional("enhancedweb") String theme) throws Exception {


        String payMode = "DC";

        //enablePrepaidCardFF4jFlag();
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setPrepaidCard(true);

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        User user = userManager.getForWrite(Label.BASIC);


        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfRateFactorPg2Rtdd, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "VISA", user, "DEBIT_CARD", feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse, "DEBIT_CARD");

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage, ccTxnAmount);

        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);


        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC","prepaidCard", "API");
    }
}
