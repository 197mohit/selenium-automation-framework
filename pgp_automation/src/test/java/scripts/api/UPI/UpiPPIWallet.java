package scripts.api.UPI;



import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.CHAKSHU;
import static io.restassured.RestAssured.given;

public class UpiPPIWallet extends PGPBaseTest  {

    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();
    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }

    public void assertPTCCommonResponseForm(ProcessTxnV1Response ptcResponse){
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getActionUrl()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getMethod()).isEqualTo("POST");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getType()).isEqualTo("redirect");
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getHeaders()).isNotNull();
        Assertions.assertThat(ptcResponse.getBody().getBankForm().getRedirectForm().getContent()).isNotNull();
    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET in FPO , COP , Instaproxy Request and UPI callback  ")
    public void validateUpiPpiWalletCollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=PPI_WALLET";
        String paymentinstrumentresponse = "paymentInstrument=PPI_WALLET";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();


    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET in FPO , COP , Instaproxy UPi Intent Request")
    public void validateUpiPpiWalletIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiIntentRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"PPI_WALLET\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET , UPI CC in FPO , COP , Instaproxy Request and UPI Collect callback")
    public void validateUpiPpiWalletUpiCCCollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD,PPI_WALLET";
        String paymentinstrumentresponse = "paymentInstrument=PPI_WALLET";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_UPI_CC_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET , UPI CC in FPO , COP , Instaproxy Request and UPI Intent callback")
    public void validateUpiPpiWalletUpiCCIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiIntentRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"CREDIT_CARD\\\",\\\"PPI_WALLET\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_UPI_CC_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present , UPI CC in FPO , COP , Instaproxy Request and UPI Collect callback")
    public void validateUpiPpiWalletNotPresentUpiCCCollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_DISABLED_UPI_CC_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).doesNotContain("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present , UPI CC in FPO , COP , Instaproxy Request and UPI Intent callback ")
    public void validateUpiPpiWalletNotPresentUpiCCIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiIntentRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"CREDIT_CARD\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).doesNotContain("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }


    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET, UPI CC not present in  FPO , COP , Instaproxy Request and UPI Intent callback")
    public void validateUpiPpiWalletNotPresentUpiCCNotPresentIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiIntentRequestParam = "merchantAllowedUpiPaymentInstruments";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).doesNotContain("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present when Flag Off , UPI CC in FPO , COP , Instaproxy Request and UPI Collect callback")
    public void validateUpiPpiWalletUpiCCCollectTxnFlagOff() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_UPI_CC_ENABLED_FLAG_OFF_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")

                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present when Flag Off , UPI CC in FPO , COP , Instaproxy Request and UPI Intent callback")
    public void validateUpiPpiWalletUpiCCIntentTxnFlagOff() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"CREDIT_CARD\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }


    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present when Flag Off, in FPO , COP , Instaproxy Request and UPI Intent callback")
    public void validateUpiPpiWalletIntentTxnFlagOff() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstruments";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present when Flag Off, UPI CC in FPO , COP , Instaproxy Request and UPI Collect callback ")
    public void validateUpiPpiWalletDisabledUPICCCollectTxnFlagOff() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_DISABLED_UPI_CC_ENABLED_FLAG_OFF_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present when Flag Off, UPI CC in FPO , COP , Instaproxy Request and UPI Intent callback ")
    public void validateUpiPpiWalletDisabledUPICCIntentTxnFlagOff() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"CREDIT_CARD\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }


    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present when Flag Off, UPI CC Not Present in FPO , COP , Instaproxy Request and UPI Intent callback ")
    public void validateUpiPpiWalletDisabledUPICCDisabledIntentTxnFlagOff() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstruments";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET present in  FPO , COP but not in Instaproxy Request and UPI Intent callback for limit exceed")
    public void validateUpiPpiWalletEnabledwithLimitIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstruments";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET  ,UPI CC present in  FPO , COP but not in Instaproxy Request and UPI Collect callback for limit exceed")
    public void validateUpiPpiWalletEnabledwithLimitUPICCCollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_WITH_LIMIT_UPI_CC_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }


    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET  ,UPI CC present in  FPO , COP but not in Instaproxy Request and UPI Collect callback for limit exceed of PPI WALLET &  UPI CC")
    public void validateUpiPpiWalletEnabledwithLimitUPICCwithLimitCollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_WITH_LIMIT_UPI_CC_ENABLED_WITH_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET  ,UPI CC present in  FPO , COP but not in Instaproxy Request and UPI Intent callback for limit exceed of PPI WALLET &  UPI CC")
    public void validateUpiPpiWalletEnabledwithLimitUPICCwithLimitIntentTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstruments";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_WITH_LIMIT_UPI_CC_ENABLED_WITH_LIMIT_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET in FPO , COTP , Instaproxy Request and UPI callback  ")
    public void validateUpiPpiWalletCOTPCollectTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=PPI_WALLET";
        String paymentinstrumentresponse = "paymentInstrument=PPI_WALLET";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_COTP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();


    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET in FPO , COTP , Instaproxy UPi Intent Request")
    public void validateUpiPpiWalletIntentCOTPTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiIntentRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"PPI_WALLET\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_ENABLED_COTP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");
        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET , UPI CC in FPO , COTP , Instaproxy Request and UPI Collect callback")
    public void validateUpiPpiWalletUpiCCCollectCOTPTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD,PPI_WALLET";
        String paymentinstrumentresponse = "paymentInstrument=PPI_WALLET";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_UPI_CC_ENABLED_COTP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET , UPI CC in FPO , COTP , Instaproxy Request and UPI Intent callback")
    public void validateUpiPpiWalletUpiCCIntentCOTPTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiIntentRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"CREDIT_CARD\\\",\\\"PPI_WALLET\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_UPI_CC_ENABLED_COTP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).contains("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present , UPI CC in FPO , COTP , Instaproxy Request and UPI Collect callback")
    public void validateUpiPpiWalletNotPresentUpiCCCollectCOTPTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiCollectRequestParam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD";
        Constants.MerchantType merchant = Constants.MerchantType.UPIPPI_WALLET_DISABLED_UPI_CC_ENABLED_COTP_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).doesNotContain("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytmwallet")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        assertPTCCommonResponseForm(ptcResponse);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("UPI")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-51160")
    @Test(description = "Validate UPI_PPIWALLET not present , UPI CC in FPO , COTP , Instaproxy Request and UPI Intent callback ")
    public void validateUpiPpiWalletNotPresentUpiCCIntentCOTPTxn() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "2";
        String upiIntentRequestParam = "merchantAllowedUpiPaymentInstruments\\\":[\\\"CREDIT_CARD\\\"]";
        Constants.MerchantType merchant = Constants.MerchantType.REQAUTH_FLOW_UPI_ACQ_ID_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibility")).doesNotContain("UPI_PPIWALLET");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.upiSubTypeMerchantEligibilityAddnPay")).doesNotContain("UPI_PPIWALLET");

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI_INTENT")
                .setAuthMode("3D")
                .setRiskExtendInfo("userLBSLatitude:26.88|userLBSLongitude:81.01|userAgent:Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|networkType:4g|businessFlow:JS_CHECKOUT|amount:2|language:en-US|screenResolution:393X786|deviceType:mobile|channelId:WAP|platform:mWeb|osType:Android|deviceModel:Redmi Note 5|browserType:Chrome|browserVersion:96.0.4664.104|osVersion:8.1.0|deviceManufacturer:Xiaomi")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }
}
