package scripts.api.UPI;

import com.paytm.LocalConfig;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.apphelpers.*;
import com.paytm.dto.GetPaymentStatusResponse.GetPaymentStatusResponseDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.RiskExtendInfo;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.response.BankForm.*;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.CreditCardInfo;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.dto.upiIntent.staticQR.UpiPspExtendInfo;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JClient;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.ABHISHEK_KULKARNI;
import static com.paytm.appconstants.Constants.Owner.MANISH_MISHRA;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

@Owner(ABHISHEK_KULKARNI)
public class UpiCreditCard extends PGPBaseTest {

    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();


    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        return fetchPaymentOptionsJson;
    }

        public JsonPath Validate_FetchQRDetails(String qrCode, Constants.MerchantType mid, String tokenType, String token , String version ) {
            Reporter.report.info("Validating fetch QR Code Details for the merchant and txn token");
            FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                    .setQRCodeId(qrCode)
                    .setMID(mid.getId())
                    .setTokenType(tokenType)
                    .setToken(token)
                    .setVersion(version)
                    .build();
            fetchQRPaymentDetailsDTO.getHead().setVersion("v2");
            FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO,"V2");
            JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
            Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
            return fetchQRResponse;
    }

    @Step()
    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;
    }


    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Push Credit Card Transaction")
    public void validateUpiPushCCTxn() throws Exception{
        User user = userManager.getForRead(Label.UPILITECC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_LITE_CC).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        Constants.MerchantType.UPI_LITE_CC.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),null)
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(PaymentDTO.CC_MPIN)
                .setRiskExtendInfo(PaymentDTO.CC_RISKEXTENDEDINFO)
                .setCreditBlock(PaymentDTO.CC_CREDITBLOCK)
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
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
        Assertions.assertThat(callBack).contains("\"paymentInstrument\":\"CREDIT_CARD\"");
        Assertions.assertThat(callBack).contains("\"creditCardInfo\":\"{\"binNumber\":\"857775\", \"creditAccountReferenceNumber\":\"857XXXXXXXX5199\", \"cardType\":\"GOLD\"}\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Collect Credit Card Transaction")
    public void validateUpiCollectCCTxn() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        String txnamount = "90";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_LITE_CC).setTxnValue(txnamount)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        String mid = Constants.MerchantType.UPI_LITE_CC.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setTxnValue(txnamount)
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String pay = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Payment Request");
        String esn = pay.substring(pay.indexOf("Payment Request")+54,pay.indexOf(" | URL"));
        String callBack = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,esn,"Payload received from UPI");
        Assertions.assertThat(callBack).contains("\"paymentInstrument\":\"CREDIT_CARD\"");
        Assertions.assertThat(callBack).contains("\"creditCardInfo\":\"{\"binNumber\":\"857775\", \"creditAccountReferenceNumber\":\"857XXXXXXXX5199\", \"cardType\":\"GOLD\"}\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Push Credit Card Offline Transaction")
    public void validateUpiPushCCTxnOffline() throws Exception{
            User user = userManager.getForWrite(Label.UPILITECC);
            Constants.MerchantType merchantType = Constants.MerchantType.UPI_LITE_CC;
            String qrCodeId = QRHelper.generateQRViaWallet(merchantType);
            JsonPath fetchQRResponse = Validate_FetchQRDetails(qrCodeId,merchantType,"SSO",user.ssoToken(),"v2");
            String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
            ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId, "2")
                .setPreferredOtpPage("bank")
                    .setPaymentMode("UPI")
                    .setUpiAccRefId("242393")
                    .setQRCodeId(qrCodeId)
                .setChannelCode("push")
                .setChannelId("APP")
                    .setSeqNumber("PTM7cd75a30f600471f8eeada94a80cf1bd")
                .setMpin(PaymentDTO.CC_MPIN)
                .setRiskExtendInfo(PaymentDTO.CC_RISKEXTENDEDINFO)
                .setCreditBlock(PaymentDTO.CC_CREDITBLOCK)
                    .setExtendInfoStaticFlow()
                    .setCardInfo(null)
                    .build();
        ProcessTxnV1Response response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
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
        Assertions.assertThat(callBack).contains("\"paymentInstrument\":\"CREDIT_CARD\"");
        Assertions.assertThat(callBack).contains("\"creditCardInfo\":\"{\"binNumber\":\"857775\", \"creditAccountReferenceNumber\":\"857XXXXXXXX5199\", \"cardType\":\"GOLD\"}\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-47752")
    @Test(description = "Validate UPI Collect Credit Card Offline Transaction")
    public void validateUpiCollectCCTxnOffline() throws Exception {
        String txnamount = "90";
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_LITE_CC;
        String qrCodeId = QRHelper.generateQRViaWallet(merchantType);
        JsonPath fetchQRResponse = Validate_FetchQRDetails(qrCodeId,merchantType,"SSO",user.ssoToken(),"v2");
        String orderId = fetchQRResponse.getString("body.paymentOptions.orderId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), "SSO", user.ssoToken(), orderId, txnamount)
                .setPaymentMode("UPI")
                .setQRCodeId(qrCodeId)
                .setTxnValue(txnamount)
                .setSeqNumber("PTM7cd75a30f600471f8eeada94a80cf1bd")
                .setPayerAccount("test9972746530@paytm")
                .setExtendInfoStaticFlow()
                .setCardInfo(null)
                .setChannelId("APP")
                .setChannelCode(null)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
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
        Assertions.assertThat(callBack).contains("\"paymentInstrument\":\"CREDIT_CARD\"");
        Assertions.assertThat(callBack).contains("\"creditCardInfo\":\"{\"binNumber\":\"857775\", \"creditAccountReferenceNumber\":\"857XXXXXXXX5199\", \"cardType\":\"GOLD\"}\"");
    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y")
    public void validateccOnUPIAllowedTrue() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y")
    public void validateccOnUPIAllowedTrue2() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_OFF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is not present , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y")
    public void validateccOnUPIAllowedTrue3() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_NO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token")
    public void validateccOnUPIAllowedTrue4() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Double txnAmount = 1.0;
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token")
    public void validateccOnUPIAllowedTrue5() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Double txnAmount = 1.0;
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_OFF;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is not present , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token")
    public void validateccOnUPIAllowedTrue6() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Double txnAmount = 1.0;
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_NO;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y, v5/fpo")
    public void validateccOnUPIAllowedTrue7() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y :v5/fpo")
    public void validateccOnUPIAllowedTrue8() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_OFF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is not present , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y: v5/fpo")
    public void validateccOnUPIAllowedTrue9() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_NO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: v5/fpo")
    public void validateccOnUPIAllowedTrue10() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Double txnAmount = 1.0;
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token:v5/fpo")
    public void validateccOnUPIAllowedTrue11() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Double txnAmount = 1.0;
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_OFF;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubhamm Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate ccOnUPIAllowed is true When CONVERT_TO_ADDANDPAY_TXN is not present , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: v5/fpo")
    public void validateccOnUPIAllowedTrue12() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Double txnAmount = 1.0;
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_CC_ADDNPAY_NO;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionResponse.get("body.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: order/pay/upiPsp")
    public void validateccOnUPIAllowedTrue13() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: order/pay/upiPsp")
    public void validateccOnUPIAllowedTrue14() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY_OFF;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is not present , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: order/pay/upiPsp")
    public void validateccOnUPIAllowedTrue15() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY_NO;
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0","yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "DYNAIMC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: order/pay/upiPsp")
    public void validateccOnUPIAllowedTrue16() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "DYNAIMC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: order/pay/upiPsp")
    public void validateccOnUPIAllowedTrue17() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY_OFF;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "DYNAIMC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is not present , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: order/pay/upiPsp")
    public void validateccOnUPIAllowedTrue18() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY_NO;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "200.0",qrCodeId,"UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,staticQrUpiPSPResponse.getBody().getOrderId(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"QR\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "DYNAIMC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: v2/fetchQR")
    public void validateccOnUPIAllowedTrue19() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");

        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.get("body.paymentOptions.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchQRResponse.get("body.paymentOptions.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "DYNAIMC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: v2/fetchQR")
    public void validateccOnUPIAllowedTrue20() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY_OFF;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");

        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.get("body.paymentOptions.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchQRResponse.get("body.paymentOptions.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "DYNAIMC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is not present , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y for sso Token: v2/fetchQR")
    public void validateccOnUPIAllowedTrue21() throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_ADDNPAY_NO;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant,"200",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        FetchQRPaymentDetailsDTO paymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder().
                setQRCodeId(qrCodeId).setChannelId("APP").setTokenType("SSO").setToken(user.ssoToken())
                .build();

        FetchQRPaymentDetails qr = new FetchQRPaymentDetails(paymentDetailsDTO);
        qr.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        qr.setContext("body.fetchPaytmInstrumentsBalance", "False");
        qr.setContext("head.version", "v2");

        JsonPath fetchQRResponse = qr.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.get("body.paymentOptions.ccOnUPIAllowed").toString()).isEqualTo("true");
        Assertions.assertThat(fetchQRResponse.get("body.paymentOptions.ccOnUPIAllowedForAddNPay").toString()).isEqualTo("false");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "PTC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is ON , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y ")
    public void validateccOnUPIAllowedTrue22() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.ZEROWALLET);
        String txmAmount = "2.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_CC_ADDNPAY)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String defaccRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCredit.AccRefId");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.find { it.accountType == 'CREDIT' }.accRefId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.UPI_CC_ADDNPAY.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .setCreditBlock("{\"accRefId\":\""+accRefID+"\",\"accountType\":\"CREDIT\",\"bank\":\"" +
                        "My Bene\",\"bankLogoUrl\":\"https://static.paytmbank.com/upi/images/" +
                        "bank-logo/000000.png\",\"bankMetaData\":{\"bankHealth\":{\"category\":\"GREEN" +
                        "\",\"displayMsg\":\"\"},\"perTxnLimit\":\"100000\"},\"credsAllowed\":[{\"" +
                        "CredsAllowedDLength\":\"6\",\"CredsAllowedDType\":\"Numeric\",\"CredsAllowedSubType" +
                        "\":\"SMS\",\"CredsAllowedType\":\"OTP\",\"dLength\":\"6\"},{\"CredsAllowedDLength\"" +
                        ":\"6\",\"CredsAllowedDType\":\"Numeric\",\"CredsAllowedSubType\":\"MPIN\",\"CredsAllowedType\"" +
                        ":\"PIN\",\"dLength\":\"6\"},{\"CredsAllowedDLength\":\"6\",\"CredsAllowedDType\":\"Numeric\",\"" +
                        "CredsAllowedSubType\":\"ATMPIN\",\"CredsAllowedType\":\"PIN\",\"dLength\":\"6\"}],\"" +
                        "ifsc\":\"AABE0877543\",\"maskedAccountNumber\":\"XXXXXXXXXX355199\",\"mpinSet\":\"Y\",\"name\"" +
                        ":\"ABC\",\"pgBankCode\":\"CON3\",\"txnAllowed\":\"ALL\",\"vpaDetail\":{\"defaultCreditAccRefId\":" +
                        "\""+defaccRefID+"\",\"defaultDebitAccRefId\":\""+defaccRefID+"\",\"name\":\""+upiID+"\",\"primary\":true}}")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String theia_facade = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"API\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");
        String passThroughExtendInfologs = theia_facade.substring(theia_facade.indexOf("passThroughExtendInfo")+24,theia_facade.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"true\"");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "PTC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y ")
    public void validateccOnUPIAllowedTrue23() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.ZEROWALLET);
        String txmAmount = "2.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_CC_ADDNPAY_OFF)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String defaccRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCredit.AccRefId");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.find { it.accountType == 'CREDIT' }.accRefId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.UPI_CC_ADDNPAY_OFF.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .setCreditBlock("{\"accRefId\":\""+accRefID+"\",\"accountType\":\"CREDIT\",\"bank\":\"" +
                        "My Bene\",\"bankLogoUrl\":\"https://static.paytmbank.com/upi/images/" +
                        "bank-logo/000000.png\",\"bankMetaData\":{\"bankHealth\":{\"category\":\"GREEN" +
                        "\",\"displayMsg\":\"\"},\"perTxnLimit\":\"100000\"},\"credsAllowed\":[{\"" +
                        "CredsAllowedDLength\":\"6\",\"CredsAllowedDType\":\"Numeric\",\"CredsAllowedSubType" +
                        "\":\"SMS\",\"CredsAllowedType\":\"OTP\",\"dLength\":\"6\"},{\"CredsAllowedDLength\"" +
                        ":\"6\",\"CredsAllowedDType\":\"Numeric\",\"CredsAllowedSubType\":\"MPIN\",\"CredsAllowedType\"" +
                        ":\"PIN\",\"dLength\":\"6\"},{\"CredsAllowedDLength\":\"6\",\"CredsAllowedDType\":\"Numeric\",\"" +
                        "CredsAllowedSubType\":\"ATMPIN\",\"CredsAllowedType\":\"PIN\",\"dLength\":\"6\"}],\"" +
                        "ifsc\":\"AABE0877543\",\"maskedAccountNumber\":\"XXXXXXXXXX355199\",\"mpinSet\":\"Y\",\"name\"" +
                        ":\"ABC\",\"pgBankCode\":\"CON3\",\"txnAllowed\":\"ALL\",\"vpaDetail\":{\"defaultCreditAccRefId\":" +
                        "\""+defaccRefID+"\",\"defaultDebitAccRefId\":\""+defaccRefID+"\",\"name\":\""+upiID+"\",\"primary\":true}}")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String theia_facade = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"API\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");
        String passThroughExtendInfologs = theia_facade.substring(theia_facade.indexOf("passThroughExtendInfo")+24,theia_facade.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"true\"");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-48065")
    @Test(description = "PTC :Validate UpiCC is true When CONVERT_TO_ADDANDPAY_TXN is OFF , UPI_CC_BLACKLISTED is N & CC_ON_UPI_RAILS_ENABLED is Y ")
    public void validateccOnUPIAllowedTrue24() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.ZEROWALLET);
        String txmAmount = "2.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_CC_ADDNPAY_NO)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String defaccRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCredit.AccRefId");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts.find { it.accountType == 'CREDIT' }.accRefId");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.UPI_CC_ADDNPAY_NO.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .setCreditBlock("{\"accRefId\":\""+accRefID+"\",\"accountType\":\"CREDIT\",\"bank\":\"" +
                        "My Bene\",\"bankLogoUrl\":\"https://static.paytmbank.com/upi/images/" +
                        "bank-logo/000000.png\",\"bankMetaData\":{\"bankHealth\":{\"category\":\"GREEN" +
                        "\",\"displayMsg\":\"\"},\"perTxnLimit\":\"100000\"},\"credsAllowed\":[{\"" +
                        "CredsAllowedDLength\":\"6\",\"CredsAllowedDType\":\"Numeric\",\"CredsAllowedSubType" +
                        "\":\"SMS\",\"CredsAllowedType\":\"OTP\",\"dLength\":\"6\"},{\"CredsAllowedDLength\"" +
                        ":\"6\",\"CredsAllowedDType\":\"Numeric\",\"CredsAllowedSubType\":\"MPIN\",\"CredsAllowedType\"" +
                        ":\"PIN\",\"dLength\":\"6\"},{\"CredsAllowedDLength\":\"6\",\"CredsAllowedDType\":\"Numeric\",\"" +
                        "CredsAllowedSubType\":\"ATMPIN\",\"CredsAllowedType\":\"PIN\",\"dLength\":\"6\"}],\"" +
                        "ifsc\":\"AABE0877543\",\"maskedAccountNumber\":\"XXXXXXXXXX355199\",\"mpinSet\":\"Y\",\"name\"" +
                        ":\"ABC\",\"pgBankCode\":\"CON3\",\"txnAllowed\":\"ALL\",\"vpaDetail\":{\"defaultCreditAccRefId\":" +
                        "\""+defaccRefID+"\",\"defaultDebitAccRefId\":\""+defaccRefID+"\",\"name\":\""+upiID+"\",\"primary\":true}}")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String theia_facade = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_PAY");
        Assertions.assertThat(theia_facade).contains("feeRateFactorsInfo\":\"{\"solutionWiseMdr\":\"API\",\"upiCc\":\"TRUE\"}");
        Assertions.assertThat(theia_facade).contains("upiCC\":true,\"deepLinkFlow\":false");
        String passThroughExtendInfologs = theia_facade.substring(theia_facade.indexOf("passThroughExtendInfo")+24,theia_facade.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"merchantEligibleUPICC\":\"true\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When CCOnUPIBlacklisted Enabled")
    public void validateUpiCCCCOnUPIBlacklistedEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPICC_BLACKLIST;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPICC_BLACKLIST).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
    }


    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When CCOnUPIBlacklisted Disabled")
    public void validateUpiCCCCOnUPIBlacklistedDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_LITE_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_LITE_CC).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When P2PMerchant Enabled")
    public void validateUpiCcP2pMerchantEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPICC_BLACKLIST;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPICC_BLACKLIST).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When P2PMerchant Disabled")
    public void validateUpiCcP2pMerchantDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_LITE_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_LITE_CC).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When mid is onus and UPI_ON_CC_ONUS ff4j Disabled")
    public void validateUpiCcOnusAndPrefDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPICC_ONUS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When mid is onus and UPI_ON_CC_ONUS ff4j Enabled")
    public void validateUpiCcOnusAndPrefEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        FF4JFlags.enable("theia.upi.on.cc.onus");
        Constants.MerchantType merchantType = Constants.MerchantType.UPICC_ONUSENABLE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When CC_ON_UPI_RAILS_ENABLED Enabled")
    public void validateUpiccRailsEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_LITE_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_LITE_CC).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When CC_ON_UPI_RAILS_ENABLED Disabled")
    public void validateUpiCconupiRailsDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPICC_RALES_DISABLE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPICC_RALES_DISABLE).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo For AddAndPay Txn")
    public void validateUpiCcAddNPayEnabled() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Double txnAmount=WalletHelpers.getWalletBalance(user)+1;
        Constants.MerchantType merchantType = Constants.MerchantType.UPICC_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setTxnValue(txnAmount.toString()).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.paymentFlow")).isEqualTo("ADDANDPAY");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowedForAddNPay")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.addMoneyPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo AddMoney Txn")
    public void validateUpiccUpiCcNativeAddMoneyEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPICCADDANDPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType).setIsNativeAddMoney("true").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.paymentFlow")).isEqualTo("NONE");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo and upiCcSubsEnabled ff4j disabled For Subscription Txn")
    public void validateUpiccUpiCcSubsDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID3;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setSsoToken(user.ssoToken())
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo and upiCcSubsEnabled ff4j Enabled For Subscription Txn")
    public void validateUpiccUpiCcSubsEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        FF4JFlags.enable("theia.upiCcSubsEnabled");
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant.getId(), merchant.getKey(), "")
                .setSsoToken(user.ssoToken())
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5.58")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionEnableRetry("0")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When FetchAddMoneyOptions is True for Offus merchant")
    public void validateUpiccWhenFetchAddMoneyOptionsTrue() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.HIGH_PRIORITY_SMS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.HIGH_PRIORITY_SMS).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setFetchAddMoneyOptions(true).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fpo = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.paymentFlow")).isEqualTo("NONE");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Validate UPI Credit Card Accounts in Fpo When FetchAddMoneyOptions is False for Offus merchant")
    public void validateUpiccWhenFetchAddMoneyOptionsFalse() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.HIGH_PRIORITY_SMS;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.HIGH_PRIORITY_SMS).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setFetchAddMoneyOptions(false).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fpo = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.paymentFlow")).isEqualTo("NONE");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Check the display Account number and bank logo url in FPO for UPI CC")
    public void validateDisplayAccountNoAndBankLogoUrl() throws Exception {
        //User user = userManager.getForRead(Label.UPILITECC);
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.UPI_CC_PPI_WALLET_Eligibility;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_CC_PPI_WALLET_Eligibility).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        //Commenting as it is not in scope anymore
        // Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0]")).contains("accountType:CREDIT");
        // Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0]")).contains("bankLogoUrl").isNotEmpty();
        // Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0]")).contains("displayAccountNo").isNotEmpty();
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Check the display Account number and bank logo url in FPO for UPI CC Offline Txn")
    public void validateDisplayAccountNoAndBankLogoUrlOffline() throws Exception {
        User user = userManager.getForWrite(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.HIGH_PRIORITY_SMS;
        String qrCodeId = QRHelper.generateQRViaWallet(merchantType);
        JsonPath fetchQRResponse = Validate_FetchQRDetails(qrCodeId, merchantType, "SSO", user.ssoToken(), "v2");
        //Commenting as it is not in scope anymore
        // Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0]")).contains("accountType:CREDIT");
        // Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0]")).contains("bankLogoUrl").isNotEmpty();
        // Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[0]")).contains("displayAccountNo").isNotEmpty();
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount less than 2000 for Online merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOnlineMidLessThen2000() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_ONLINE_NULL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_ONLINE_NULL).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount More than 2000 for Online merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOnlineMidMoreThen2000() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_ONLINE_NULL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_ONLINE_NULL).setTxnValue("2500").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount Equal to 2000 for Online merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOnlineMidEquals2000() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_ONLINE_NULL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_ONLINE_NULL).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount less than 2000 for Online merchant, UPI_CC_BLACKLISTED :Y & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOnlineMidLessThen2000ForSmallIndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_ONLINE_SMALL).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount More than 2000 for Online merchant, UPI_CC_BLACKLISTED :Y & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOnlineMidMoreThen2000ForSmallIndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_ONLINE_SMALL).setTxnValue("2500").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount Equal to 2000 for Online merchant, UPI_CC_BLACKLISTED :Y & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOnlineMidEquals2000ForSmallIndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_ONLINE_SMALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_ONLINE_SMALL).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount less than 2000 for Offline merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOfflineMidLessThen2000ForSmallIndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_SMALL).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount More than 2000 for Offline merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOfflineMidMoreThen2000ForSmallIndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_SMALL).setTxnValue("2500").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction Equal to 2000 for Offline merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOfflineMidEqualTo2000ForSmallIndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_SMALL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_SMALL).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount less than 2000 for Offline merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : N")
    public void validateUpiCCOfflineMidLessThen2000ForNullndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_NULL).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount More than 2000 for Offline merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : N")
    public void validateUpiCCOfflineMidMoreThen2000ForNullndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_NULL).setTxnValue("2500").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");

    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction Equal to 2000 for Offline merchant, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : N")
    public void validateUpiCCOfflineMidEqual2000ForNullndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_NULL;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_NULL).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount less than 2000 for Offline merchant Big Industry, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOfflineMidLessThen2000ForBigndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_BIG).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount More than 2000 for Offline merchant Big Industry, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOfflineMidMoreThen2000ForBigndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_BIG).setTxnValue("2500").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction Equal to 2000 for Offline merchant Big Industry, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : Y")
    public void validateUpiCCOfflineMidEqual2000ForBigndustry() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_BIG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_BIG).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("true");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).contains("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount less than 2000 for Offline merchant Big Industry, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : N")
    public void validateUpiCCOfflineMidLessThen2000ForBigndustryCconupirailsDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_BIG1).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction amount More than 2000 for Offline merchant Big Industry, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : N")
    public void validateUpiCCOfflineMidMoreThen2000ForBigndustryCconupirailsDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_BIG1).setTxnValue("2500").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-48458")
    @Test(description = "Verify Success UPI CC transaction when transaction Equal to 2000 for Offline merchant Big Industry, UPI_CC_BLACKLISTED :N & CC_ON_UPI_RAILS_ENABLED : N")
    public void validateUpiCCOfflineMidEqual2000ForBigndustryCconupirailsDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchantType = Constants.MerchantType.WALINTER_OFFLINE_BIG1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_OFFLINE_BIG1).setTxnValue("2000").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        JsonPath fpo = Validate_FetchPayInstrument(txnToken, initTxnDTO, "UPI", "false");
        Assertions.assertThat(fpo.getString("body.ccOnUPIAllowed")).isEqualTo("false");
        Assertions.assertThat(fpo.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts")).doesNotContain("accountType:CREDIT");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Risk Reject Error Msg For UPICC Txn")
    public void ValidateRiskRejectForUPICc() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_LITE_CC;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "22.10", qrCodeId, "UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Merchant Ineligible Msg For UPICC Txn")
    public void ValidateMerchantIneligibleForUPICc() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPICC_BLACKLIST;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2.0", qrCodeId, "UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg())
                .as("Result msg mismatch")
                .isEqualToIgnoringCase("Merchant Ineligible UPI_CREDIT_CARD");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId())
                .as("subResultCodeId mismatch")
                .isEqualToIgnoringCase("002");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQr: Validate the Merchant Ineligible Msg For UPICC Txn")
    public void ValidateMerchantIneligibleForUPICcDynamicQr() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPICC_BLACKLIST_NEW;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant, "2", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "2", qrCodeId, "UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg())
                .as("Result msg mismatch")
                .isEqualToIgnoringCase("Merchant Ineligible UPI_CREDIT_CARD");
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getSubResultCodeId())
                .as("subResultCodeId mismatch")
                .isEqualToIgnoringCase("002");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Txn Limit Error Msg For UPICC 3pPsp Txn")
    public void ValidateTxnLimitForUPICc3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "10.00", qrCodeId, "UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Daily Limit Error Msg For UPICC 3pPsp Txn")
    public void ValidateDailyLimitForUPICc3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "11.00", qrCodeId, "UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "Validate the Monthly Limit Error Msg For UPICC 3pPsp Txn")
    public void ValidateMonthlyLimitForUPICc3pPsp() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        GenerateQR generateQR = new GenerateQR(merchant.getId(), "", "UPI_QR_CODE");
        String generateResponse = generateQR.execute().asString();
        generateResponse = generateResponse.replace("\\=", "\\\\=");
        generateResponse = generateResponse.replace("\\&", "\\\\&");
        JsonPath generateResponseJson = new JsonPath(generateResponse);
        Assertions.assertThat(generateResponseJson.getString("status")).isEqualTo("SUCCESS");
        String qrCodeId = generateResponseJson.getString("response[0].qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "21.00", qrCodeId, "UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        String resultMsg = staticQrUpiPSPResponse.getBody().getResultMsg();
        String subResultCodeId = staticQrUpiPSPResponse.getBody().getSubResultCodeId();
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode())
                .as("Result code mismatch")
                .isEqualToIgnoringCase("FAIL");
        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, staticQrUpiPSPRequest.getHeader().getRequestMsgId(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade).contains("\"resultMsg\":\"" + resultMsg + "\"");
        Assertions.assertThat(theia_facade).contains("\"resultCodeId\":\"" + subResultCodeId + "\"");
    }

    @Owner(ABHISHEK_KULKARNI)
    @Feature("PGP-52310")
    @Test(description = "DynamicQR:Validate the Txn Limit Error Msg For UPICC 3pPsp Txn")
    public void ValidateTxnLimitForUPICc3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPILITE_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant, "10", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "10", qrCodeId, "UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
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
    @Test(description = "DynamicQR:Validate the Daily Limit Error Msg For UPICC 3pPsp Txn")
    public void ValidateDailyLimitForUPICc3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant, "11", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "11", qrCodeId, "UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
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
    @Test(description = "DynamicQR:Validate the Monthly Limit Error Msg For UPICC 3pPsp Txn")
    public void ValidateMonthlyLimitForUPICc3pPspDynamicQR() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CC_LIMIT;
        String OrderId = CommonHelpers.generateOrderId();

        PaymentService paymentService = new PaymentService(merchant, "21", OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        String qrCodeId = jsonPath.getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "21", qrCodeId, "UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
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

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Offline Small Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateCConUPIAllowedforOfflineSmallMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Offline Small Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateCConUPIAllowedforOfflineSmallMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Offline Null Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateCConUPIAllowedforOfflineNullMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Offline Null Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateCConUPIAllowedforOfflineNullMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Offline Big Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateCConUPIAllowedforOfflineBigMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Offline Big Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateCConUPIAllowedforOfflineBigMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Online Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateCConUPIAllowedforOnlineMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Online Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateCConUPIAllowedforOnlineMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Onus Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateCConUPIAllowedforOnusMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate CConUPIAllowed for Onus Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateCConUPIAllowedforOnusMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("true");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Offline Small Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPforOfflineSmallMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Offline Small Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPforOfflineSmallMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Offline Null Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPforOfflineNullMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Offline Null Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPforOfflineNullMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Offline Big Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPforOfflineBigMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Offline Big Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPforOfflineBigMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Online Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPforOnlineMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for ONline Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPforOnlineMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI_CC_RISK_BLACKLIST Disabled")
    public void validateUPIPSPforOnusMerchantforUPI_CC_RISK_BLACKLISTDisabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57030")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI_CC_RISK_BLACKLIST Enabled")
    public void validateUPIPSPforOnusMerchantforUPI_CC_RISK_BLACKLISTEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_ENABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("risk reject"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("00000011"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP UPI CC for Offline Small Merchant for  amount greater than 2000")
    public void validateUPIPSPUPICCforOfflineSmallMerchantforAmountGreaterThan2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed. This merchant can accept Credit Card on UPI upto Rs. 2000."))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("002"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP UPICC for Offline Small Merchant for  amount less than 2000")
    public void validateUPIPSPUPICCforOfflineSmallMerchantforAmountLessThan2000() throws Exception {
        String txnAmount = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP UPI PPI WALLET for Offline Small Merchant for  amount greater than 2000")
    public void validateUPIPSPUPIPPIWalletforOfflineSmallMerchantforAmountGreaterThan2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed. This merchant can accept Wallet on UPI upto Rs. 2000."))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.requestMsgId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP UPI PPI WALLET for Offline Small Merchant for  amount less than 2000")
    public void validateUPIPSPUPIPPIWALLETforOfflineSmallMerchantforAmountLessThan2000() throws Exception {
        String txnAmount = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP UPI CreditLine for Offline Small Merchant for  amount greater than 2000")
    public void validateUPIPSPUPICreditLineforOfflineSmallMerchantforAmountGreaterThan2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed. This merchant can accept Credit Line on UPI upto Rs. 2000."))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP UPI CreditLine for Offline Small Merchant for  amount less than 2000")
    public void validateUPIPSPUPICreditLineforOfflineSmallMerchantforAmountLessThan2000() throws Exception {
        String txnAmount = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-55719")
    @Test(description = "validate CConUPIAllowed for Onus Merchant when no UPICC Pref is Enabled")
    public void validateCConUPIAllowedforOnusMerchantwhennoUPICCPrefisEnabled() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccOnUPIAllowed")).isEqualTo("false");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI_CC")
    public void validateUPIPSPforOnusMerchantforUPI_CC() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, qrCodeId, "UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);


        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI PPI Wallet")
    public void validateUPIPSPforOnusMerchantforUPIPPIWALLET() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, qrCodeId, "PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);


        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI CREDITLINE")
    public void validateUPIPSPforOnusMerchantforUPICREDITLINE() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, qrCodeId, "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);


        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI_CC Not Enabled")
    public void validateUPIPSPforOnusMerchantforUPI_CCNotEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, qrCodeId, "UPI_CREDIT_CARD");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDIT_CARD"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("002"))
                .extract().as(StaticQrUpiPSPResponse.class);

    }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI PPI Wallet Not Enabled")
    public void validateUPIPSPforOnusMerchantforUPIPPIWALLETNotEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, qrCodeId, "PPI_WALLET");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_PPIWALLET"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("003"))
                .extract().as(StaticQrUpiPSPResponse.class);


    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-57025")
    @Test(description = "validate UPIPSP for Onus Merchant for UPI CREDITLINE NotEnabled")
    public void validateUPIPSPforOnusMerchantforUPICREDITLINENotEnabled() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED;
        String mid = merchant.getId();
        int posId = 192737392;
        String orderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(merchant, "100", orderId, posId);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, qrCodeId, "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.orderId", Matchers.equalTo(null))
                .body("body.txnAmount", Matchers.equalTo(null))
                .body("body.mid", Matchers.equalTo(null))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);


    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP  for Offline Small Merchant Free Bear CreditLine Super Blacklisted")
    public void validateTheiaUPIPSofflinesmallInterestFreeCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP  for Offline Small Merchant Interest Bear CreditLine Super Blacklisted")
    public void validateTheiaUPIPSPofflinesmallInterestBearCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP  for Offline Small Merchant Free Bear CreditLine Super Blacklisted N")
    public void validateTheiaUPIPSPofflinesmallInterestFreeCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Small Merchant Interest Bear CreditLine Super Blacklisted N")
    public void validateTheiaUPIPSPofflinesmallInterestBearCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Small Merchant Free Bear CreditLine Super Blacklisted N above 2000")
    public void validateTheiaUPIPSPofflinesmallInterestFreeCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed. This merchant can accept Credit Line on UPI upto Rs. 2000."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Small Merchant Interest Bear CreditLine Super Blacklisted N above 2000")
    public void validateTheiaUPIPSPofflinesmallInterestBearCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Small Merchant Free Bear CreditLine Blacklisted Y ")
    public void validateTheiaUPIPSPofflinesmallInterestFreeCreditLineBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Small Merchant Interest Bear CreditLine Blacklisted Y")
    public void validateTheiaUPIPSPofflinesmallInterestBearCreditLineBlacklistedY() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Small Merchant Free Bear  CreditLine Rails Y above 2000")
    public void validateTheiaUPIPSPofflinesmallInterestFreeCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_CREDITLINE_ON_UPI_RAILS_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Small Merchant Interest Bear  CreditLine Rails Y above 2000")
    public void validateTheiaUPIPSPofflinesmallInterestBearCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_SMALL_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_SMALL_CREDITLINE_ON_UPI_RAILS_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big  Merchant Free Bear CreditLine Super Blacklisted")
    public void validateTheiaUPIPSPofflineBigInterestFreeCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Interest Bear CreditLine Super Blacklisted")
    public void validateTheiaUPIPSPofflineBigInterestBearCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Free Bear CreditLine Super Blacklisted N")
    public void validateTheiaUPIPSPofflineBigInterestFreeCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Interest Bear CreditLine Super Blacklisted N")
    public void validateTheiaUPIPSPofflineBigInterestBearCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Free Bear CreditLine Super Blacklisted N above 2000")
    public void validateTheiaUPIPSPofflineBigInterestFreeCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Interest Bear CreditLine Super Blacklisted N above 2000")
    public void validateTheiaUPIPSPofflineBigInterestBearCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Free Bear CreditLine Blacklisted Y ")
    public void validateTheiaUPIPSPofflineBigInterestFreeCreditLineBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Interest Bear CreditLine Blacklisted Y")
    public void validateTheiaUPIPSPofflineBigInterestBearCreditLineBlacklistedY() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Free Bear  CreditLine Rails Y above 2000")
    public void validateTheiaUPIPSPofflineBigInterestFreeCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }
    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Offline Big Merchant Interest Bear  CreditLine Rails Y above 2000")
    public void validateTheiaUPIPSPofflineBigInterestBearCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.OFFLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big  Merchant Free Bear CreditLine Super Blacklisted")
    public void validateTheiaUPIPSPonlineBigInterestFreeCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Interest Bear CreditLine Super Blacklisted")
    public void validateTheiaUPIPSPonlineBigInterestBearCreditLineSuperBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Merchant Ineligible UPI_CREDITLINE"))
                .body("body.externalSerialNo", Matchers.equalToIgnoringCase(""))
                .body("body.subResultCodeId", Matchers.equalTo("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Free Bear CreditLine Super Blacklisted N")
    public void validateTheiaUPIPSPonlineBigInterestFreeCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Interest Bear CreditLine Super Blacklisted N")
    public void validateTheiaUPIPSPonlineBigInterestBearCreditLineSuperBlacklistedN() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Free Bear CreditLine Super Blacklisted N above 2000")
    public void validateTheiaUPIPSPonlineBigInterestFreeCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Interest Bear CreditLine Super Blacklisted N above 2000")
    public void validateTheiaUPIPSPonlineBigInterestBearCreditLineSuperBlacklistedNabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Free Bear CreditLine Blacklisted Y ")
    public void validateTheiaUPIPSPonlineBigInterestFreeCreditLineBlacklistedY() throws Exception {
        String txnAmount = "100.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("FAIL"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("009"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("Payment failed as this merchant is not accepting Credit Line on UPI. Try using other options."))
                .body("body.subResultCodeId", Matchers.equalToIgnoringCase("004"))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Interest Bear CreditLine Blacklisted Y")
    public void validateTheiaUPIPSPonlineBigInterestBearCreditLineBlacklistedY() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Free Bear  CreditLine Rails Y above 2000")
    public void validateTheiaUPIPSPonlineBigInterestFreeCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CREDITLINE01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CREDITLINE01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Theia UPIPSP for Online Big Merchant Interest Bear  CreditLine Rails Y above 2000")
    public void validateTheiaUPIPSPonlineBigInterestBearCreditLineRailsYabove2000() throws Exception {
        String txnAmount = "2001.00";
        Constants.MerchantType merchant = Constants.MerchantType.ONLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ONLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "CREDITLINE_CL01");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"CREDITLINE_CL01");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
    }



    @Owner(MANISH_MISHRA)
    @Feature("PGP-58937")
    @Test(description = "DynamicQr: Verify callback url is present in response when ff4j is enabled for MID")
    public void callback_present_FF4J_Enabled() throws Exception {
        FF4JFlags.enable(FF4JFeatures.PASS_CALLBACK_URL_FOR_ONETIME_FLOW);
        RedisAPI.deleteKey("FF4J_FEATURE_theia.passCallbackurlForOnetimeFlow");
        Constants.MerchantType merchant = Constants.MerchantType.CALLBACK_IN_THEIA_ORDERPAY;
        String OrderId = CommonHelpers.generateOrderId();
        RiskExtendInfo riskExtendInfo=new RiskExtendInfo();
        riskExtendInfo.setAmount("10")
                .setBusinessType("Mandate")
                .setPayeeVpa("paytm.d956913490@ptys")
                .setIsVerifiedMerchant("true")
                .setPurposeCode("00")
                .setInitiationMode("01")
                .setMerchantGenre("OFFLINE");

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),OrderId, "10",riskExtendInfo);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        System.out.println("staticQrUpiPSPRequest: "+staticQrUpiPSPRequest.getBody());
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.callbackUrl",Matchers.notNullValue())
                .extract().as(StaticQrUpiPSPResponse.class);


    }

    @Owner(MANISH_MISHRA)
    @Feature("PGP-58937")
    @Test(description = "DynamicQr: Verify callback url is not present in response when ff4j is disabled for MID")
    public void callback_not_present_FF4J_Disabled() throws Exception {
        FF4JFlags.disable(FF4JFeatures.PASS_CALLBACK_URL_FOR_ONETIME_FLOW);
        RedisAPI.deleteKey("FF4J_FEATURE_theia.passCallbackurlForOnetimeFlow");
        System.out.println("FF4j Value: "+FF4JFlags.getFeatureValue("theia.passCallbackurlForOnetimeFlow"));
        Constants.MerchantType merchant = Constants.MerchantType.CALLBACK_NOT_IN_THEIA_ORDERPAY;
        String OrderId = CommonHelpers.generateOrderId();
        RiskExtendInfo riskExtendInfo=new RiskExtendInfo();
        riskExtendInfo.setAmount("10")
                .setBusinessType("Mandate")
                .setPayeeVpa("paytm.d956913490@ptys")
                .setIsVerifiedMerchant("true")
                .setPurposeCode("00")
                .setInitiationMode("01")
                .setMerchantGenre("OFFLINE");

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),OrderId, "10",riskExtendInfo);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"PPI_WALLET");
        System.out.println("staticQrUpiPSPRequest: "+staticQrUpiPSPRequest.getBody());
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body",Matchers.not(Matchers.hasKey("callbackUrl")))
                .extract().as(StaticQrUpiPSPResponse.class);

   }


    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Theia UPIPSP  DQR for VOUCHER")
    public void validateTheiaUPIPSDQRforVOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_VOUCHER_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "VOUCHER");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"VOUCHER");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_VOUCHER");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Theia UPIPSP SQR for VOUCHER")
    public void validateTheiaUPIPSSQRforVOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","VOUCHER");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"VOUCHER");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_VOUCHER");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_CREATE_ORDER_AND_PAY");
        String upiModeSubTypeValue  = PG2LogsValidationHelper.getKeyParameterValueFromLogs("upiModeSubType",logs);
        Assertions.assertThat(upiModeSubTypeValue).contains("UPI_VOUCHER");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Theia UPIPSP  DQR for UPI_VOUCHER")
    public void validateTheiaUPIPSDQRforUPI_VOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.UPI_VOUCHER_ENABLED_MID, "enhancedweb")
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        String orderId = orderDTO.getORDER_ID();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder("SEAMLESS_3D_FORM",merchant.getId(),orderId, txnAmount, "yuioty", "UPI_VOUCHER");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_VOUCHER");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).doesNotContain("UPI_VOUCHER");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Theia UPIPSP SQR for UPI_VOUCHER")
    public void validateTheiaUPIPSSQRforUPI_VOUCHER() throws Exception {
        String txnAmount = "220.55";
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        String mid = merchant.getId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), txnAmount,"yuioty","UPI_VOUCHER");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder,"UPI_VOUCHER");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .extract().as(StaticQrUpiPSPResponse.class);
        String orderId = staticQrUpiPSPResponse.getBody().getOrderId();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderId,"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).doesNotContain("UPI_VOUCHER");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Theia UPI Collect for VOUCHER")
    public void validateTheiaUPICollectforVOUCHER() throws Exception {
        String txnAmount = "220.55";
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String mid = merchant.getId();
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("9759417329@paytm")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
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

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "merchantAllowedUpiPaymentInstrumentsCommaSeparated=VOUCHER";
        Assertions.assertThat(logs).contains(validateparam);

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_VOUCHER");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59084")
    @Test(description = "validate Theia UPI Intent for VOUCHER")
    public void validateTheiaUPIIntentforVOUCHER() throws Exception {
        String txnAmount = "220.55";
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_VOUCHER_ENABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnAmount).build();
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

        String deeplink = ptcResponse.getBody().getDeepLinkInfo().getDeepLink();
        String esn = deeplink.substring(deeplink.indexOf("tr")+3,deeplink.indexOf("tr")+22);

        System.out.println(esn);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderId)
                .setExternalSerialNo(esn)
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(mid)
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId);
        String validateparam = "\"merchantAllowedUpiPaymentInstruments\":[\"VOUCHER\"";
        Assertions.assertThat(logs).contains(validateparam);

        Assertions.assertThat(txnStatus.execute().getBody().prettyPrint()).contains("UPI_VOUCHER");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR Theia UPIPSP Upi savings when acquirementId is passed in orderId parameter for tr as acquirementId when preference UPI_TR_ACQ_ID_ENABLE is Y")
    public void validateOnlineReqAuthDQRUPIPSPUpisavingswhenacquirementIdispassedinorderIdparameterfortrasacquirementIdwhenpreferenceUPI_TR_ACQ_ID_ENABLEisY() throws Exception {
        String txnAmount = "200.00";
        SoftAssertions softAssert = new SoftAssertions();
        String txnAmountinUpiPspRequest = "200.00";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PLATFORM_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = null ;
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus())
                .isEqualTo("S");
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode())
                .isEqualTo("0000");
        softAssert.assertAll();

        // 2. Hit fetchPaymentOptions API
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .setWorkFlow("checkout")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId,
                fetchPaymentOptionsDTO);
        JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softAssert.assertAll();

        // 3. Get QR data and decode deeplink
        String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
        String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
        System.out.println("Decoded deeplink: " + deeplink);



        String acqId = deeplink.substring(deeplink.indexOf("&tr")+4,deeplink.indexOf("&tr")+4+35);
        System.out.println("acqId is : " + acqId);

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, acqId,
                txnAmountinUpiPspRequest, "paytm-9759417324@ptybl", "paytmTest@ptys", "", payerPaymentInstrumentFee);
        builder.setPayerName("test");
        builder.setPayerPSP("Phonepe");
        builder.build();
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.orderId", Matchers.equalTo(acqId))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-58328")
    @Test(description = "validate Online ReqAuth DQR Theia UPIPSP MDRPCF Upi Credit Card for payerPaymentInstrumentFee when acquirementId is passed in orderId parameter for tr as acquirementId when preference UPI_TR_ACQ_ID_ENABLE is Y")
    public void validateOnlineReqAuthDQRTheiaUPIPSPMDRPCFUpiCreditCardforpayerPaymentInstrumentFeewhenacquirementIdispassedinorderIdparameterfortrasacquirementIdwhenpreferenceUPI_TR_ACQ_ID_ENABLEisY() throws Exception {
        String txnAmount = "200.00";
        SoftAssertions softAssert = new SoftAssertions();
        String txnAmountinUpiPspRequest = "211.80";
        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE;
        String mid = merchant.getId();
        String payerPaymentInstrumentFee = "11.80" ;
        String callbackUrl = LocalConfig.PGP_HOST + "/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.getBody().getOrderId();
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus())
                .isEqualTo("S");
        softAssert.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode())
                .isEqualTo("0000");
        softAssert.assertAll();

        // 2. Hit fetchPaymentOptions API
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setGenerateOrderId("false")
                .setDeepLinkRequiedField(true)
                .setWorkFlow("checkout")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId,
                fetchPaymentOptionsDTO);
        JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softAssert.assertThat(fpoResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softAssert.assertAll();

        // 3. Get QR data and decode deeplink
        String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
        String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
        System.out.println("Decoded deeplink: " + deeplink);

        String acqId = deeplink.substring(deeplink.indexOf("&tr")+4,deeplink.indexOf("&tr")+4+35);
        System.out.println("acqId is : " + acqId);

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(mid, acqId,
                txnAmountinUpiPspRequest, "paytm-9759417324@ptybl", "paytmTest@ptys", "UPI_CREDIT_CARD", payerPaymentInstrumentFee);
        builder.setPayerName("test");
        builder.setPayerPSP("Phonepe");
        builder.build();
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder);
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.txnAmount", Matchers.equalTo(txnAmountinUpiPspRequest))
                .body("body.mid", Matchers.equalTo(merchant.getId()))
                .body("body.orderId", Matchers.equalTo(acqId))
                .extract().as(StaticQrUpiPSPResponse.class);
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGQA-636")
    @Test(description = "Validate UPI PSP QR_SUBSCRIPTION response when payerPaymentInstrument is UPI_CREDIT_CARD")
    public void validateUpiPspQrSubscriptionUpiCreditCard() {
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        CreditCardInfo creditCardInfo = new CreditCardInfo("857775", "857222222225199", "CARD");
        UpiPspExtendInfo extendInfo = new UpiPspExtendInfo("comment:NA");

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
                merchant.getId(), "359620", "200.00", merchant.getVpa(), "8840500363@ptyes",
                "QR_SUBSCRIPTION", creditCardInfo, extendInfo);
        builder.setPayerPaymentInstrument("UPI_CREDIT_CARD");

        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(builder.build());
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg())
                .as("Result msg mismatch")
                .isEqualTo("Order doesn't exist");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGQA-636")
    @Test(description = "Validate UPI PSP QR_SUBSCRIPTION response when payerPaymentInstrument is invalid")
    public void validateUpiPspQrSubscriptionInvalidPayerPaymentInstrument() {
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        CreditCardInfo creditCardInfo = new CreditCardInfo("857775", "857222222225199", "CARD");
        UpiPspExtendInfo extendInfo = new UpiPspExtendInfo("comment:NA");

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
                merchant.getId(), "359620", "200.00", merchant.getVpa(), "8840500363@ptyes",
                "QR_SUBSCRIPTION", creditCardInfo, extendInfo);
        builder.setPayerPaymentInstrument("ABC");

        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(builder.build());
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg())
                .as("Result msg mismatch")
                .isEqualTo("payment Failure");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGQA-636")
    @Test(description = "Validate UPI PSP QR_SUBSCRIPTION response when payerPaymentInstrument is empty")
    public void validateUpiPspQrSubscriptionEmptyPayerPaymentInstrument() {
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        CreditCardInfo creditCardInfo = new CreditCardInfo("857775", "857222222225199", "CARD");
        UpiPspExtendInfo extendInfo = new UpiPspExtendInfo("comment:NA");

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
                merchant.getId(), "359620", "200.00", merchant.getVpa(), "8840500363@ptyes",
                "QR_SUBSCRIPTION", creditCardInfo, extendInfo);
        builder.setPayerPaymentInstrument("");

        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(builder.build());
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg())
                .as("Result msg mismatch")
                .isEqualTo("Order doesn't exist");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGQA-636")
    @Test(description = "Validate UPI PSP QR_SUBSCRIPTION response when payerPaymentInstrument is null")
    public void validateUpiPspQrSubscriptionNullPayerPaymentInstrument() {
        Constants.MerchantType merchant = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL;
        CreditCardInfo creditCardInfo = new CreditCardInfo("857775", "857222222225199", "CARD");
        UpiPspExtendInfo extendInfo = new UpiPspExtendInfo("comment:NA");

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(
                merchant.getId(), "359620", "200.00", merchant.getVpa(), "8840500363@ptyes",
                "QR_SUBSCRIPTION", creditCardInfo, extendInfo);

        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(builder.build());
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultMsg())
                .as("Result msg mismatch")
                .isEqualTo("Order doesn't exist");
    }

}
