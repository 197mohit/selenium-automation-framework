package scripts.api.UPI;

import com.paytm.api.PaymentService;
import com.paytm.api.RedisAPI;
import com.paytm.api.StaticQrUpiPSP;
import com.paytm.api.TxnStatus;
import com.paytm.api.boss.staticPrefUpdateApi;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.processTransactionV1.response.UpiLiteResponseData;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.ABHISHEK_KULKARNI;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

@Owner(ABHISHEK_KULKARNI)
public class UpiLite extends PGPBaseTest {

    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();


    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String mid, String orderId ,Boolean isLiteEligible) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setMid(mid).setGenerateOrderId(orderId).setIsLiteEligible(isLiteEligible).setTpap(true).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.paymentMode")).contains("UPI_LITE");
        return fetchPaymentOptionsJson;
    }

    public JsonPath Validate_FetchQRDetails(String qrCode, MerchantType mid, String tokenType, String token, String version, Boolean isLiteEligible) {
        Reporter.report.info("Validating fetch QR Code Details for the merchant and txn token");
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCode)
                .setMID(mid.getId())
                .setTokenType(tokenType)
                .setToken(token)
                .setIsLiteEligible(isLiteEligible)
                .build();
        fetchQRPaymentDetailsDTO.getHead().setVersion(version);
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO,"V2");
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode")).contains("UPI_LITE");
        return fetchQRResponse;
    }


    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Push Lite Transaction")
    public void validateUpiPushLiteTxn() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_LITE_CC).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,MerchantType.UPI_LITE_CC.getId(),initTxnDTO.getBody().getOrderId() ,true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPI_LITE_CC.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null,null,null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
       ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String arpc = response.getBody().getUpiLiteResponseData().getArpc();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();
        String pay = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.getBody().getOrderId(),"Payment Request");
       String esn = pay.substring(pay.indexOf("Payment Request")+54,pay.indexOf(" | URL"));
        String callBack = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,esn,"Payload received from UPI");
       Assertions.assertThat(callBack).contains("\"arpc\":\""+arpc+"\"");
    }


    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI PUSH Lite Offline Txn")
    public void validateUpiPushOffline() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_LITE_CC;
        String qrCodeId = QRHelper.generateQRViaWallet(merchantType);
       JsonPath fetchQRResponse = Validate_FetchQRDetails(qrCodeId,merchantType,"SSO",user.ssoToken(),"v2",true);
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId,"2",null,"UPI_LITE",null)
                .setQRCodeId(qrCodeId)
                .setExtendInfoStaticFlow()
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCKQR)
                .setRiskExtendInfo(PaymentDTO.LITE_RISKEXTENDEDINFO)
                .setChannelId("APP")
                .build();
        processTxnV1Request.getBody().getUpiLiteRequestData();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String arpc = response.getBody().getUpiLiteResponseData().getArpc();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success");
        String pay = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Payment Request");
        String esn = pay.substring(pay.indexOf("Payment Request")+54,pay.indexOf(" | URL"));
        String callBack = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,esn,"Payload received from UPI");
        Assertions.assertThat(callBack).contains("\"arpc\":\""+arpc+"\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Lite Paymode in FPO When UPI_LITE_ENABLED Enabled and isLiteEligible is not Pass in Request")
    public void validateUpiLitePaymodeByEnablePrefAndIsliteEligibleNotPassed() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        String mid = MerchantType.UPI_LITE_CC.getId();
        staticPrefUpdateApi ss = new staticPrefUpdateApi();
        ss.buildRequestStaticPref("UPI_LITE_ENABLED", "Y", mid);
        String res = ss.execute().asString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPI_LITE_CC).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,MerchantType.UPI_LITE_CC.getId(),initTxnDTO.getBody().getOrderId() ,false);
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Lite Paymode in FPO When UPI_LITE_ENABLED Disabled and isLiteEligible is True in Request")
    public void validateUpiLitePaymodeByDisablePrefAndIsliteEligibleTrue() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        String mid = MerchantType.UPILITE.getId();
        staticPrefUpdateApi ss = new staticPrefUpdateApi();
        ss.buildRequestStaticPref("UPI_LITE_ENABLED", "N", mid);
        String res = ss.execute().asString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPILITE).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO,MerchantType.UPILITE.getId(),initTxnDTO.getBody().getOrderId() ,true);
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Lite Paymode in FPO When UPI_LITE_ENABLED Disabled and isLiteEligible is Flase in Request")
    public void validateUpiLitePaymodeByDisablePrefAndIsliteEligibleFalse() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        String mid = MerchantType.UPILITE.getId();
        staticPrefUpdateApi ss = new staticPrefUpdateApi();
        ss.buildRequestStaticPref("UPI_LITE_ENABLED", "N", mid);
        String res = ss.execute().asString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPILITE).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setMid(mid).setGenerateOrderId(initTxnDTO.getBody().getOrderId()).setIsLiteEligible(false).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.paymentMode")).doesNotContain("UPI_LITE");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Lite Paymode in FetchQr Details When UPI_LITE_ENABLED Enabled and isLiteEligible is not Pass in Request")
    public void validateUpiLitePaymodeByEnablePrefAndIsliteEligibleNotPassedForOffline() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_LITE_CC;
        staticPrefUpdateApi ss = new staticPrefUpdateApi();
        ss.buildRequestStaticPref("UPI_LITE_ENABLED", "Y", merchantType.getId());
        String res = ss.execute().asString();
        String qrCodeId = QRHelper.generateQRViaWallet(merchantType);
        Validate_FetchQRDetails(qrCodeId, merchantType, "SSO", user.ssoToken(), "v2", false);
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Lite Paymode in FetchQr Details When UPI_LITE_ENABLED Disabled and isLiteEligible Passed in Request")
    public void validateUpiLitePaymodeByDisablePrefAndIsliteEligiblePassedForOffline() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPILITE;
        staticPrefUpdateApi ss = new staticPrefUpdateApi();
        ss.buildRequestStaticPref("UPI_LITE_ENABLED", "N", merchantType.getId());
        String res = ss.execute().asString();
        String qrCodeId = QRHelper.generateQRViaWallet(merchantType);
        Validate_FetchQRDetails(qrCodeId, merchantType, "SSO", user.ssoToken(), "v2", true);
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Lite Paymode in FetchQr Details When UPI_LITE_ENABLED Disabled and isLiteEligible false in Request")
    public void validateUpiLitePaymodeByDisablePrefAndIsliteEligibleFalseForOffline() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPILITE;
        staticPrefUpdateApi ss = new staticPrefUpdateApi();
        ss.buildRequestStaticPref("UPI_LITE_ENABLED", "N", merchantType.getId());
        String res = ss.execute().asString();
        String qrCodeId = QRHelper.generateQRViaWallet(merchantType);
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setIsLiteEligible(false)
                .build();
        fetchQRPaymentDetailsDTO.getHead().setVersion("v2");
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO,"V2");
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode")).doesNotContain("UPI_LITE");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-49741")
    @Test(description = "Validate UPI Push Lite Transaction Where Txn Amount is More Then Txn limit")
    public void validateUpiPushLiteTxnAmountMoreThenTxnLimit() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPILITE_LIMIT).setTxnValue("201").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, MerchantType.UPILITE_LIMIT.getId(), initTxnDTO.getBody().getOrderId(), true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPILITE_LIMIT.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null, null, null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
         Assertions.assertThat(resultMsg).isEqualTo("Merchant cannot accept payments on UPI Lite at the moment. Try using other options.");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"Merchant cannot accept payments on UPI Lite at the moment. Try using other options.\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110079\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-49741")
    @Test(description = "Validate UPI Push Lite Transaction Where Txn Amount is More Then Daily limit")
    public void validateUpiPushLiteTxnAmountMoreThenDailyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPILITE_LIMIT).setTxnValue("101").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, MerchantType.UPILITE_LIMIT.getId(), initTxnDTO.getBody().getOrderId(), true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPILITE_LIMIT.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null, null, null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        Assertions.assertThat(resultMsg).isEqualTo("Merchant cannot accept payments on UPI Lite at the moment. Try using other options.");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_LITE_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"Merchant cannot accept payments on UPI Lite at the moment. Try using other options.\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110077\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-49741")
    @Test(description = "Validate UPI Push Lite Transaction Where Txn Amount is More Then Monthly limit")
    public void validateUpiPushLiteTxnAmountMoreThenMonthlyLimit() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.UPILITE_LIMIT).setTxnValue("151").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, MerchantType.UPILITE_LIMIT.getId(), initTxnDTO.getBody().getOrderId(), true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.UPILITE_LIMIT.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null, null, null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        Assertions.assertThat(resultMsg).isEqualTo("Merchant cannot accept payments on UPI Lite at the moment. Try using other options.");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_LITE_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"Merchant cannot accept payments on UPI Lite at the moment. Try using other options.\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110078\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-49741")
    @Test(description = "Validate UPI Push Lite Transaction Where Txn Amount is More Then Txn limit For COTP")
    public void validateUpiPushLiteTxnAmountMoreThenTxnLimitCotp() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.LITE_LIMIT_COTP).setTxnValue("201").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, MerchantType.LITE_LIMIT_COTP.getId(), initTxnDTO.getBody().getOrderId(), true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.LITE_LIMIT_COTP.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null, null, null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        Assertions.assertThat(resultMsg).isEqualTo("Merchant cannot accept payments on UPI Lite at the moment. Try using other options.");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_LITE_LIMIT_CHECK_AMOUNT_OVER_MAX_SINGLE_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"Merchant cannot accept payments on UPI Lite at the moment. Try using other options.\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110079\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-49741")
    @Test(description = "Validate UPI Push Lite Transaction Where Txn Amount is More Then Daily limit For COTP")
    public void validateUpiPushLiteTxnAmountMoreThenDailyLimitCotp() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.LITE_LIMIT_COTP).setTxnValue("101").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, MerchantType.LITE_LIMIT_COTP.getId(), initTxnDTO.getBody().getOrderId(), true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.LITE_LIMIT_COTP.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null, null, null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        Assertions.assertThat(resultMsg).isEqualTo("Merchant cannot accept payments on UPI Lite at the moment. Try using other options.");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_LITE_LIMIT_CHECK_AMOUNT_OVER_DAILY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"Merchant cannot accept payments on UPI Lite at the moment. Try using other options.\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110077\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-49741")
    @Test(description = "Validate UPI Push Lite Transaction Where Txn Amount is More Then Monthly limit For COTP")
    public void validateUpiPushLiteTxnAmountMoreThenMonthlyLimitCotp() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.LITE_LIMIT_COTP).setTxnValue("151").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, MerchantType.LITE_LIMIT_COTP.getId(), initTxnDTO.getBody().getOrderId(), true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MerchantType.LITE_LIMIT_COTP.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), null, null, null)
                .setPaymentMode("UPI_LITE")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setUpiAccRefId("236077")
                .setCreditBlock(PaymentDTO.LITE_CREDITBLOCK)
                .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String resultMsg = response.getBody().getResultInfo().getResultMsg();
        Assertions.assertThat(resultMsg).isEqualTo("Merchant cannot accept payments on UPI Lite at the moment. Try using other options.");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultCode\":\"UPI_LITE_LIMIT_CHECK_AMOUNT_OVER_MONTHLY_LIMIT\"");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"Merchant cannot accept payments on UPI Lite at the moment. Try using other options.\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"12110078\"");

    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Risk Reject Error Msg")
    public void ValidateRiskReject() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "","UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse= generateResponse.replace("\\=","\\\\=");
        generateResponse =generateResponse.replace("\\&","\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "22.10",qrCodeId,"UPI_LITE");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_LITE");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\""+resultMsg+"\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\""+subResultCodeId+"\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Txn Limit Error Msg for UpiLite 3pPsp Flow")
    public void ValidateTxnLimit3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "","UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse= generateResponse.replace("\\=","\\\\=");
        generateResponse =generateResponse.replace("\\&","\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "201.00",qrCodeId,"UPI_LITE");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_LITE");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPRequest.getHeader().getRequestMsgId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\""+resultMsg+"\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\""+subResultCodeId+"\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Daily Limit Error Msg for UpiLite 3pPsp Flow")
    public void ValidateDailyLimit3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "","UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse= generateResponse.replace("\\=","\\\\=");
        generateResponse =generateResponse.replace("\\&","\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "101.00",qrCodeId,"UPI_LITE");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_LITE");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPRequest.getHeader().getRequestMsgId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\""+resultMsg+"\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\""+subResultCodeId+"\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Monthly Limit Error Msg for UpiLite 3pPsp Flow")
    public void ValidateMonthlyLimit3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "","UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse= generateResponse.replace("\\=","\\\\=");
        generateResponse =generateResponse.replace("\\&","\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "151.00",qrCodeId,"UPI_LITE");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_LITE");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPRequest.getHeader().getRequestMsgId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\""+resultMsg+"\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\""+subResultCodeId+"\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQR:Validate the Txn Limit Error Msg For UPILite 3pPsp Txn")
    public void ValidateTxnLimitForUPILite3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "201", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "201", qrCodeId, "UPI_LITE");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_LITE");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQR:Validate the Daily Limit Error Msg For UPILite 3pPsp Txn")
    public void ValidateDailyLimitForUPILite3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "101", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "101", qrCodeId, "UPI_LITE");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_LITE");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQR:Validate the Monthly Limit Error Msg For UPILite 3pPsp Txn")
    public void ValidateMonthlyLimitForUPILite3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "151", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "151", qrCodeId, "UPI_LITE");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_LITE");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-55491")
    @Test(description = "Verify UPI LITE is present when PUSH express disabled on mid")
    public void UPILITE_WithoutUPIPushExpress() throws Exception {
        FF4JFlags.enable("theia.allowUpiLiteForUpiPush");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.allowUpiLiteForUpiPush");
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        User user = userManager.getForWrite(Label.UPILITECC);
        String OrderId = CommonHelpers.generateOrderId();
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMerchantVpa("paytmd.946912797@axis")
                .setTpap(true)
                .setIsLiteEligible(true)
                .build();
        FetchQRPaymentDetails fqrDetail = new FetchQRPaymentDetails(paymentDetailsDTO,"V2");
        JsonPath fqrResponse = fqrDetail.execute().jsonPath();
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode")).contains("UPI_LITE");
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.payChannelOptions")).doesNotContain("UPIPUSHEXPRESS");
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-55491")
    @Test(description = "Verify UPI LITE is present when UPIPUSH and PUSH express enabled on mid")
    public void UPILITE_WithUPIPushExpress() throws Exception {
        FF4JFlags.disable("theia.allowUpiLiteForUpiPush");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.allowUpiLiteForUpiPush");
        //qa12ma22939255272061
        User user = userManager.getForWrite(Label.UPILITECC);
        String OrderId = CommonHelpers.generateOrderId();
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMerchantVpa("paytm-956917593@paytm")
                .setTpap(true)
                .setIsLiteEligible(true)
                .build();
        FetchQRPaymentDetails fqrDetail = new FetchQRPaymentDetails(paymentDetailsDTO,"V2");
        JsonPath fqrResponse = fqrDetail.execute().jsonPath();
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.paymentMode")).contains("UPI_LITE");
        Assertions.assertThat(fqrResponse.getString("body.paymentOptions.merchantPayOption.paymentModes.payChannelOptions")).contains("UPIPUSHEXPRESS");
    }
}