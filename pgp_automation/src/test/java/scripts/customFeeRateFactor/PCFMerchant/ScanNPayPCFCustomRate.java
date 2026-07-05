package scripts.customFeeRateFactor.PCFMerchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.TxnStatusList;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.api.wallet.GetQRCodeInfoApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.feeRateFactors;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.util.ArrayList;


@Feature("PGP-21710")
@Owner("Gagandeep")
public class ScanNPayPCFCustomRate extends PGPBaseTest {

    private static final String JSON_POST_URL = LocalConfig.JSON_POST_URL;
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final String txnAmount = "2.00";
    Constants.MerchantType pcfRateFactor = Constants.MerchantType.CUSTOMFEEPCF;
    Constants.MerchantType pcfRate = Constants.MerchantType.PCF_MERCHANT;
    Constants.MerchantType pcfRateFactorPg2Rtdd = Constants.MerchantType.CUSTOMFEEPCF_PG2_RTDD;
    Constants.MerchantType pcfRatePg2Rtdd = Constants.MerchantType.PCF_MERCHANT_PG2_RTDD;
//    private String theme = "enhancedweb";
    private static final String successStatus = "TXN_SUCCESS";

    private void completeTxnInNewTab(ProcessTxnV1Request processTxnV1Request) throws JsonProcessingException {
        ArrayList<String> tabs = null;
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        try {
            String json = null;
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(processTxnV1Response);
            PGPHelpers.launchNewTab();
            tabs = new ArrayList<String>(DriverManager.getDriver().getWindowHandles());
            if (tabs.size() == 1)
                throw new SkipException("Unable to launch new browser tab");
            DriverManager.getDriver().switchTo().window(tabs.get(1));
            new NativePlusHoldpayPage().
                    launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                    .fillAndSubmitJsonForm(json);
            ResponsePage responsePage = new ResponsePage();
            responsePage.validateStatus("TXN_SUCCESS")
                    .assertAll();
        } finally {
            DriverManager.getDriver().switchTo().window(tabs.get(0));
        }
    }

    private String generateQrCodeOrder(User user, Constants.MerchantType merchant, OrderDTO orderDTO, String theme) {
        checkoutPage.createOrder(orderDTO);
        checkoutPage.waitUntilLoads();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.imgScanPayQRCode().assertVisible();
        try {
            cashierPage.pause(2);
            String qrCodeText = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));

            GetQRCodeInfoApi getQRCodeInfoApi = new GetQRCodeInfoApi(qrCodeText);      //validating QR code with wallet
            JsonPath jsonPath = getQRCodeInfoApi.execute().jsonPath();

            Assertions.assertThat(jsonPath.getString("response.mappingId"))
                    .as("mid mismatched")
                    .isEqualToIgnoringCase(merchant.getId());
            Assertions.assertThat(jsonPath.getString("response.ORDER_ID"))
                    .as("orderId mismatched")
                    .isEqualToIgnoringCase(orderDTO.getORDER_ID());
            return qrCodeText;
        } catch (Throwable throwable) {
            throw new SkipException("scan n pay QR code is not visible or not readed by paytm app", throwable);
        }
    }

    /*
    public void enablePrepaidCardFF4jFlag() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE='1' WHERE FEAT_UID ='prepaidCard'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_prepaidCard");
    }

     */

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

    @Test(description = "Verify a successful DC txn on PCF merchant having VISA 2% commission and default 1 %")
    public void successfulFeeFactorDCTxnForPCF(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "DC";

        User user = userManager.getForWrite(Label.BASIC);


        OrderDTO orderDTO = new OrderFactory.Hybrid(pcfRateFactorPg2Rtdd, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT(txnAmount).build();


        String qrCodeId = generateQrCodeOrder(user, pcfRateFactorPg2Rtdd, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(pcfRateFactorPg2Rtdd.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();

        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true));


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(pcfRateFactorPg2Rtdd.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(new PaymentDTO().getDebitCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "VISA", user, "DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");


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
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "QR");
    }


    @Test(description = "Verify a successful International CC txn on PCF merchant having 10 Rs commission , VISA 2% commission and default 1 %")
    public void successfulFeeFactorInternationalCCTxnForPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRatePg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRatePg2Rtdd, "Y");

        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);


        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setInternationalCardPayment(true);
        User user = userManager.getForWrite(Label.BASIC);


        OrderDTO orderDTO = new OrderFactory.Hybrid(pcfRatePg2Rtdd, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT(txnAmount).build();


        String qrCodeId = generateQrCodeOrder(user, pcfRatePg2Rtdd, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(pcfRatePg2Rtdd.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();

        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true));


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(pcfRatePg2Rtdd.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "VISA", user, "CREDIT_CARD", feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");


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
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "IHDF","internationalCardPayment", "QR");
    }


    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountLessThan100ForPCF(@Optional("enhancedweb") String theme) throws Exception {


        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        User user = userManager.getForWrite(Label.BASIC);


        OrderDTO orderDTO = new OrderFactory.Hybrid(pcfRateFactorPg2Rtdd, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT(txnAmount).build();


        String qrCodeId = generateQrCodeOrder(user, pcfRateFactorPg2Rtdd, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(pcfRateFactorPg2Rtdd.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();

        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true));


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(pcfRateFactorPg2Rtdd.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");


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
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");
    }


    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission  0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMoreThan100LessThan200ForPCF(@Optional("enhancedweb") String theme) throws Exception {


        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        User user = userManager.getForWrite(Label.BASIC);


        OrderDTO orderDTO = new OrderFactory.Hybrid(pcfRateFactorPg2Rtdd, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("30").build();


        String qrCodeId = generateQrCodeOrder(user, pcfRateFactorPg2Rtdd, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(pcfRateFactorPg2Rtdd.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();

        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true));


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(pcfRateFactorPg2Rtdd.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

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
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");
    }

    @Test(description = "Verify a successful NB txn on PCF merchant having Slab based Commission  0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulFeeFactorNBTxnSlabBasedforAmountMore200ForPCFTOMDR(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "NB";

        User user = userManager.getForWrite(Label.BASIC);


        OrderDTO orderDTO = new OrderFactory.Hybrid(pcfRateFactorPg2Rtdd, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("60").build();


        String qrCodeId = generateQrCodeOrder(user, pcfRateFactorPg2Rtdd, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(pcfRateFactorPg2Rtdd.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();

        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true));


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(pcfRateFactorPg2Rtdd.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "ICICI", user, "NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "NET_BANKING");

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
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");
    }


    @Test(description = "Verify a successful CC MASTER txn on PCF merchant having flat 6 rs MDR commission ")
    public void successfulFeeFactorCCMasterTxnForPCF_MDR(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "CC";

        User user = userManager.getForWrite(Label.BASIC);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);

        OrderDTO orderDTO = new OrderFactory.Hybrid(pcfRateFactorPg2Rtdd, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT(txnAmount).build();


        String qrCodeId = generateQrCodeOrder(user, pcfRateFactorPg2Rtdd, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(pcfRateFactorPg2Rtdd.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();


        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true));


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(pcfRateFactorPg2Rtdd.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);


        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "MASTER", user, "CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "CREDIT_CARD");


        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode, chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO, pcfRateFactorPg2Rtdd, payMode, chargeAmount);
        //PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO, payMode, chargeAmount);
        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "QR");
    }

    @Test(description = "Verify a successful DC Prepaid card txn on PCF merchant having flat 20 flat PCF commission")
    public void successfulFeeFactorDCPrepaidTxnForPCF_MDR(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(pcfRateFactorPg2Rtdd, "Y");

        PcfHelpher.assertConvFee(pcfRateFactorPg2Rtdd, "Y");

        String payMode = "DC";

        //enablePrepaidCardFF4jFlag();

        PaymentDTO paymentDTO = new PaymentDTO();

        paymentDTO.setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setPrepaidCard(true);

        User user = userManager.getForWrite(Label.BASIC);


        OrderDTO orderDTO = new OrderFactory.Hybrid(pcfRateFactorPg2Rtdd, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT(txnAmount).build();


        String qrCodeId = generateQrCodeOrder(user, pcfRateFactorPg2Rtdd, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(pcfRateFactorPg2Rtdd.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();


        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        Response fetchQRResponse = fetchQRPaymentDetails.execute();

        fetchQRResponse.then()
                .statusCode(200)
                .body("body.resultInfo.resultMsg", Matchers.equalToIgnoringCase("Success"))
                .body("body.qrInfo.status", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.qrInfo.response.qrCodeId", Matchers.equalToIgnoringCase(qrCodeId))
                .body("body.qrInfo.response.orderQr", Matchers.equalTo(true));


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(pcfRateFactorPg2Rtdd.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO, "VISA", user, "DEBIT_CARD", feeRateFactors);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse, "DEBIT_CARD");


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
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC","prepaidCard", "QR");
    }


}
