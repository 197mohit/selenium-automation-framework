package scripts.customFeeRateFactor.MDRMerchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.OrderStatus;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.api.wallet.GetQRCodeInfoApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.util.ArrayList;
import java.util.Date;

public class ScanNPayMDRCustomRate extends PGPBaseTest {

    private static final String successMessage = "Txn Success";
    private static final String successStatus = "TXN_SUCCESS";
    private static final String JSON_POST_URL = LocalConfig.JSON_POST_URL;
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final double txnAmount = 2.00;
    Constants.MerchantType mdrCustomRate = Constants.MerchantType.MDR_CUSTOM_RATE;
    Constants.MerchantType mdrCustom = Constants.MerchantType.MDR_INTL_MERC;
//    private String theme = "enhancedweb_revamp";

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
  /*  public void enablePrepaidCardFF4jFlag() {
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

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC txn on MDR merchant having MASTER 10rs flat commission and default 1 %")
    public void successfulFeeFactorDCTxnForMDRScanNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustomRate, "N");

        String payMode = "DC";

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.Hybrid(mdrCustomRate, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        WalletHelpers.modifyBalance(user, 1.00);

        String qrCodeId = generateQrCodeOrder(user, mdrCustomRate, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mdrCustomRate.getId())
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
                .Builder(mdrCustomRate.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);


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
                .containsExactlyInAnyOrder("DEBIT_CARD", "MASTER", "HDFC", "QR");

        double flatCommission = 10.0;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");
    }


    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases >25 10 flat commission and default 1 %")
    public void successfulFeeFactorCCInternationalLessThan25TxnForMDRScanNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");

        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.Hybrid(mdrCustom, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("2.00").build();

        WalletHelpers.modifyBalance(user, 1.00);


        String qrCodeId = generateQrCodeOrder(user, mdrCustom, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mdrCustom.getId())
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
                .Builder(mdrCustom.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);


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
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("DINERS");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("CREDIT_CARD", "DINERS", "QR","HDFC");

        double flatCommission = 0.02;


        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");
    }


    @Test(description = "Verify a successful CC International txn on MDR merchant having slab bases <25 12 flat and percentage both commission and default 1 %")
    public void successfulFeeFactorCCInternationalMoreThan25TxnForMDRScanNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");

        String payMode = "CC";

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.Hybrid(mdrCustom, theme, user)
                .setSSO_TOKEN("")
                .setTXN_AMOUNT("30.00").build();

        WalletHelpers.modifyBalance(user, 1.00);


        String qrCodeId = generateQrCodeOrder(user, mdrCustom, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mdrCustom.getId())
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
                .Builder(mdrCustom.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(paymentDTO.getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);


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
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("DINERS");

            Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                    .as("FEE Factor is Not coming from PGPLUS BO")
                    .containsExactlyInAnyOrder("CREDIT_CARD", "DINERS", "HDFC", "QR");

            double flatCommission = 12.0;
            double percentCommission = 30 * 0.12;
            double TotalCommission = flatCommission + percentCommission;



            Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(TotalCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

    }

    @Test(description = "Verify a successful DC PREPAID txn on MDR merchant having flat commission of 10 and default 1 %")
    public void successfulFeeFactorDCPrePaidCardScanNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertConvFee(Constants.MerchantType.MASKED_MOBILE_ENABLED, "N");
        User user = userManager.getForWrite(Label.BASIC);

        String payMode = "DC";

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);


      //  enablePrepaidCardFF4jFlag();

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MASKED_MOBILE_ENABLED, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN("")
                .build();


        checkoutPage.createOrder(orderDTO);

        String qrCodeId = generateQrCodeOrder(user, Constants.MerchantType.MASKED_MOBILE_ENABLED, orderDTO, theme);


        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(Constants.MerchantType.MASKED_MOBILE_ENABLED.getId())
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
                .Builder(Constants.MerchantType.MASKED_MOBILE_ENABLED.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(paymentDTO.getDebitCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusMDRTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIMDRTxn(orderDTO, Constants.MerchantType.MASKED_MOBILE_ENABLED, payMode);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO, payMode);
        validateOrderStatusAPI(orderDTO, payMode, txnAmount);

        //SearchTransaction PGPLUS BO

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC","prepaidCard", "QR");

        double flatCommission = 10.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");

        //Successful Peon
        PcfHelpher.validateSuccessPeonPrepaidPCFtxn(orderDTO, payMode);
    }


    @Test(description = "Verify a successful NB txn on MDR merchant having flat commission of 15 and default 1 %")
    public void successfulFeeFactorNBForSlabMDRScanNPay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertConvFee(mdrCustom, "N");
        User user = userManager.getForRead(Label.BASIC);


        String payMode = "NB";

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrCustom, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN("")
                .build();


        String qrCodeId = generateQrCodeOrder(user, mdrCustom, orderDTO, theme);

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(mdrCustom.getId())
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
                .Builder(mdrCustom.getId(), "SSO", user.ssoToken(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT())
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setExtendInfoOrderAlreadyCreated(true)
                .build();

        completeTxnInNewTab(processTxnV1Request);


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

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "QR");

        double flatCommission = 15.0;

        Assertions.assertThat(response.jsonPath().getString("result.acquiringServiceFee[0]")).isEqualTo(String.valueOf(flatCommission))
                .as("FEE Factor is Not coming from PGPLUS BO");
    }

}
