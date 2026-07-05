package scripts.api.PreAuthCapture;

import com.paytm.api.PreAuthCapture.Capture;
import com.paytm.api.PreAuthCapture.PreAuth;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.PreAuthCaptureBankPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreAuthCapture extends PGPBaseTest {
    
    PreAuthCaptureBankPage preAuthBankPage=new PreAuthCaptureBankPage();


    // Variables to store values for later use
    private String txnToken;
    private String preAuthId;

    private void extractTxnTokenAndPreAuthIdFromHtml(String htmlResponse) {
        txnToken = null;

        Pattern pattern1 = Pattern.compile("\"txnToken\"\\s*[:=]\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(htmlResponse);
        if (matcher1.find()) {
            txnToken = matcher1.group(1);
        }

        if (txnToken == null) {
            Pattern pattern2 = Pattern.compile("(?:name|id)\\s*=\\s*[\"']txnToken[\"']\\s+value\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(htmlResponse);
            if (matcher2.find()) {
                txnToken = matcher2.group(1);
            }
        }

        if (txnToken == null) {
            Pattern pattern3 = Pattern.compile("txnToken\\s*[:=]\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher matcher3 = pattern3.matcher(htmlResponse);
            if (matcher3.find()) {
                txnToken = matcher3.group(1);
            }
        }

        if (txnToken == null) {
            txnToken = PGPHelpers.extractFieldWithRegex(htmlResponse, "\"txnToken\"\\s*[:=]\\s*\"([^\"]+)\"");
        }

        preAuthId = null;

        Pattern preAuthPattern1 = Pattern.compile("\"preAuthId\"\\s*[:=]\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher preAuthMatcher1 = preAuthPattern1.matcher(htmlResponse);
        if (preAuthMatcher1.find()) {
            preAuthId = preAuthMatcher1.group(1);
        }

        if (preAuthId == null) {
            Pattern preAuthPattern2 = Pattern.compile("(?:name|id)\\s*=\\s*[\"']preAuthId[\"']\\s+value\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher preAuthMatcher2 = preAuthPattern2.matcher(htmlResponse);
            if (preAuthMatcher2.find()) {
                preAuthId = preAuthMatcher2.group(1);
            }
        }

        if (preAuthId == null) {
            Pattern preAuthPattern3 = Pattern.compile("preAuthId\\s*[:=]\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher preAuthMatcher3 = preAuthPattern3.matcher(htmlResponse);
            if (preAuthMatcher3.find()) {
                preAuthId = preAuthMatcher3.group(1);
            }
        }

        if (preAuthId == null) {
            preAuthId = PGPHelpers.extractFieldWithRegex(htmlResponse, "\"preAuthId\"\\s*[:=]\\s*\"([^\"]+)\"");
        }
    }

    @Feature("PGP-61253")
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "Verify the PreAuth success txn when multiCaptureAllowed as false and txn is of CC")
    public void verifyPreAuthWithMultiCaptureFalse_CC_Txn() throws Exception {
        // Generate random orderId
        String orderId = CommonHelpers.generateOrderId();
        
        // Get merchant ID from PRE_AUTH_CAPTURE_MID
        String mid = Constants.MerchantType.PRE_AUTH_CAPTURE_MID.getId();
        
        // Create PreAuth request with required parameters
        PreAuth preAuth = new PreAuth()
                .setMid(mid)
                .setOrderId(orderId)
                .setMultiCaptureAllowed(false);
        
        // Execute the API call - response will be in HTML format
        Response response = preAuth.execute();
        String htmlResponse = response.getBody().asString();
        
        System.out.println("PreAuth Response: " + htmlResponse);

        extractTxnTokenAndPreAuthIdFromHtml(htmlResponse);
        
        System.out.println("Extracted txnToken: " + txnToken);
        System.out.println("Extracted preAuthId: " + preAuthId);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|618|072026")
                .setAuthMode("otp")
                .setCardPreAuthType("STANDARD_AUTH")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        preAuthBankPage.waitUntilLoads();
        preAuthBankPage.submitButton_PreAuthCaptureTxn().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderId)
                .validateMid(mid)
                .assertAll();
    }

    @Feature("PGP-61253")
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "Verify the successful capture API call when PreAuth amount and capture amount is same | Single caputure Case CC")
    public void verifyCaptureTxn_CC_Txn() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String mid = Constants.MerchantType.PRE_AUTH_CAPTURE_MID.getId();
        String txnAmount = "40";

        PreAuth preAuth = new PreAuth()
                .setMid(mid)
                .setOrderId(orderId)
                .setMultiCaptureAllowed(false)
                .setTxnAmount(txnAmount);

        Response preAuthResponse = preAuth.execute();
        String htmlResponse = preAuthResponse.getBody().asString();
        System.out.println("PreAuth Response: " + htmlResponse);

        extractTxnTokenAndPreAuthIdFromHtml(htmlResponse);
        System.out.println("Extracted txnToken: " + txnToken);
        System.out.println("Extracted preAuthId: " + preAuthId);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|618|072026")
                .setAuthMode("otp")
                .setCardPreAuthType("STANDARD_AUTH")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        preAuthBankPage.waitUntilLoads();
        preAuthBankPage.submitButton_PreAuthCaptureTxn().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderId)
                .validateMid(mid)
                .assertAll();
        System.out.println("Success Preauth Bank txn ");        

        Response captureResponse = new Capture()
                .setMid(mid)
                .setPreAuthId(preAuthId)
                .setTxnAmount(txnAmount)
                .setOrderId(orderId)
                .setPayMode("CREDIT_CARD")
                .setTerminalCapture("true")
                .setMercUnqRef("online")
                .execute();

        JsonPath captureJson = captureResponse.jsonPath();
        String captureStatus = captureJson.getString("body.status");

        if ("PENDING".equalsIgnoreCase(captureStatus)) {
            TxnStatus txnStatus = new TxnStatus(mid, orderId);
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderId)
                    .validateTxnAmount(txnAmount)
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(mid)
                    .AssertAll();
            System.out.println("Success Capture txn ");   

        } else {
            Assertions.assertThat(captureJson.getString("body.responseCode")).isEqualTo("01");
            Assertions.assertThat(captureJson.getString("body.ResponseMessage")).isEqualTo("Txn Successful.");
            Assertions.assertThat(captureJson.getString("body.status")).isEqualTo("TXN_SUCCESS");
        }
    }

    @Feature("PGP-61253")
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "Verify partial capture flow: PreAuth 100, Capture 40,terminalCapture-true and validate Auto release of remaining amount")
    public void verifyPartialCapture_Release_Flow_CC_Txn() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String mid = Constants.MerchantType.PRE_AUTH_CAPTURE_MID.getId();
        String preAuthAmount = "100";
        String captureAmount = "40";

        PreAuth preAuth = new PreAuth()
                .setMid(mid)
                .setOrderId(orderId)
                .setMultiCaptureAllowed(false)
                .setTxnAmount(preAuthAmount);

        Response preAuthResponse = preAuth.execute();
        String htmlResponse = preAuthResponse.getBody().asString();
        extractTxnTokenAndPreAuthIdFromHtml(htmlResponse);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4718650100010336|618|072026")
                .setAuthMode("otp")
                .setCardPreAuthType("STANDARD_AUTH")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        preAuthBankPage.waitUntilLoads();
        preAuthBankPage.submitButton_PreAuthCaptureTxn().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderId)
                .validateMid(mid)
                .assertAll();

        Response captureResponse = new Capture()
                .setMid(mid)
                .setPreAuthId(preAuthId)
                .setTxnAmount(captureAmount)
                .setOrderId(orderId)
                .setPayMode("CREDIT_CARD")
                .setTerminalCapture("true")
                .setMercUnqRef("online")
                .execute();

        JsonPath captureJson = captureResponse.jsonPath();
       // Assertions.assertThat(captureJson.getString("body.responseCode")).isEqualTo("01");
       // Assertions.assertThat(captureJson.getString("body.ResponseMessage")).isEqualTo("Txn Successful.");

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(preAuthAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(mid)
                .AssertAll();

        System.out.println("Success Capture txn ");   


        String logger="FLUXNET_BANKCARD_PG2_RELEASE_RESULT";        

        String instaproxyLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy,
                orderId,logger,"REQUEST");
        System.out.println("Instaproxy Logs:--------------------------#########   " + instaproxyLogs);
 
        Assertions.assertThat(instaproxyLogs).contains("\"paidAmount\":\"6000\"");
        System.out.println("Success Release of remaining amount - Done");   
    }

    @Feature("PGP-61253")
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "Verify successful Down Payment flow where multiCaptureAllowed as TRUE: PreAuth - Rs 100, DownPayment - 20,EMI-60")
    public void verifyDownPayment_multiCaptureTrue_CC_EMI_Txn() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String mid = Constants.MerchantType.PRE_AUTH_CAPTURE_MID.getId();
        String preAuthAmount = "100";

        PreAuth preAuth = new PreAuth()
                .setMid(mid)
                .setOrderId(orderId)
                .setMultiCaptureAllowed(true)
                .setTxnAmount(preAuthAmount);

        Response preAuthResponse = preAuth.execute();
        extractTxnTokenAndPreAuthIdFromHtml(preAuthResponse.getBody().asString());

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setChannelCode("HDFC")
                .setCardInfo("|" + PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|618|072030")
                .setAuthMode("otp")
                .setCardPreAuthType("STANDARD_AUTH")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        preAuthBankPage.waitUntilLoads();
        preAuthBankPage.submitButton_PreAuthCaptureTxn().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderId)
                .validateMid(mid)
                .assertAll();

        System.out.println("Success Preauth Bank txn "); 

        Response captureCreditCardResponse = new Capture()
                .setMid(mid)
                .setPreAuthId(preAuthId)
                .setTxnAmount("20")
                .setOrderId(orderId)
                .setPayMode("CREDIT_CARD")
                .setTerminalCapture("false")
                .setMercUnqRef("ONLINE")
                .setPlanId("HDFC|3")
                .execute();

        JsonPath captureCcJson = captureCreditCardResponse.jsonPath();
        String captureCcStatus = captureCcJson.getString("body.status");

        if ("PENDING".equalsIgnoreCase(captureCcStatus)) {
            TxnStatus txnStatusAfterCcCapture = new TxnStatus(mid, orderId);
            txnStatusAfterCcCapture.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderId)
                    .validateTxnAmount(preAuthAmount)
                    .validateStatus("PENDING")
                    .validateTxnType("SALE")
                    .validateMid(mid)
                    .AssertAll();
        } else {
            Assertions.assertThat(captureCcJson.getString("body.responseCode")).isEqualTo("01");
            Assertions.assertThat(captureCcJson.getString("body.ResponseMessage")).isEqualTo("Txn Successful.");
            Assertions.assertThat(captureCcJson.getString("body.status")).isEqualTo("TXN_SUCCESS");
        }
        System.out.println("Success Capture CC txn ");   

        Response captureEmiResponse = new Capture()
                .setMid(mid)
                .setPreAuthId(preAuthId)
                .setTxnAmount("60")
                .setOrderId(orderId)
                .setPayMode("EMI")
                .setTerminalCapture("true")
                .setMercUnqRef("online")
                .setPlanId("HDFC|3")
                .execute();

        JsonPath captureEmiJson = captureEmiResponse.jsonPath();
        String captureEmiStatus = captureEmiJson.getString("body.status");

        if ("PENDING".equalsIgnoreCase(captureEmiStatus)) {
            TxnStatus txnStatusAfterEmiCapture = new TxnStatus(mid, orderId);
            txnStatusAfterEmiCapture.executeUntilNotPending();
            txnStatusAfterEmiCapture.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderId)
                    .validateTxnAmount(preAuthAmount)
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(mid)
                    .AssertAll();
        } else {
            Assertions.assertThat(captureEmiJson.getString("body.responseCode")).isEqualTo("01");
            Assertions.assertThat(captureEmiJson.getString("body.ResponseMessage")).isEqualTo("Txn Successful.");
            Assertions.assertThat(captureEmiJson.getString("body.status")).isEqualTo("TXN_SUCCESS");
        }

        System.out.println("Success Capture EMI txn ");   
    }

    @Feature("PGP-61253")
    @Owner(Constants.Owner.VIDHI)
    @Test(description = "Verify the successful auto release of remaining amount of multiCapture flow - PreAuth 100, DownPayment 20,EMI-60")
    public void verifyDownPayment_Release_Flow_multiCaptureTrue_CC_EMI_Txn() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String mid = Constants.MerchantType.PRE_AUTH_CAPTURE_MID.getId();
        String preAuthAmount = "100";

        PreAuth preAuth = new PreAuth()
                .setMid(mid)
                .setOrderId(orderId)
                .setMultiCaptureAllowed(true)
                .setTxnAmount(preAuthAmount);

        Response preAuthResponse = preAuth.execute();
        extractTxnTokenAndPreAuthIdFromHtml(preAuthResponse.getBody().asString());

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .setChannelCode("HDFC")
                .setCardInfo("|" + PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|618|072030")
                .setAuthMode("otp")
                .setCardPreAuthType("STANDARD_AUTH")
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        preAuthBankPage.waitUntilLoads();
        preAuthBankPage.submitButton_PreAuthCaptureTxn().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateOrderId(orderId)
                .validateMid(mid)
                .assertAll();

        System.out.println("Success Preauth Bank txn "); 

        Response captureCreditCardResponse = new Capture()
                .setMid(mid)
                .setPreAuthId(preAuthId)
                .setTxnAmount("20")
                .setOrderId(orderId)
                .setPayMode("CREDIT_CARD")
                .setTerminalCapture("false")
                .setMercUnqRef("ONLINE")
                .setPlanId("HDFC|3")
                .execute();

        JsonPath captureCcJson = captureCreditCardResponse.jsonPath();
        String captureCcStatus = captureCcJson.getString("body.status");

        if ("PENDING".equalsIgnoreCase(captureCcStatus)) {
            TxnStatus txnStatusAfterCcCapture = new TxnStatus(mid, orderId);
            txnStatusAfterCcCapture.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderId)
                    .validateTxnAmount(preAuthAmount)
                    .validateStatus("PENDING")
                    .validateTxnType("SALE")
                    .validateMid(mid)
                    .AssertAll();
        } else {
            Assertions.assertThat(captureCcJson.getString("body.responseCode")).isEqualTo("01");
            Assertions.assertThat(captureCcJson.getString("body.ResponseMessage")).isEqualTo("Txn Successful.");
            Assertions.assertThat(captureCcJson.getString("body.status")).isEqualTo("TXN_SUCCESS");
        }
        System.out.println("Success Capture CC txn ");   

        Response captureEmiResponse = new Capture()
                .setMid(mid)
                .setPreAuthId(preAuthId)
                .setTxnAmount("60")
                .setOrderId(orderId)
                .setPayMode("EMI")
                .setTerminalCapture("true")
                .setMercUnqRef("online")
                .setPlanId("HDFC|3")
                .execute();

        JsonPath captureEmiJson = captureEmiResponse.jsonPath();
        String captureEmiStatus = captureEmiJson.getString("body.status");

        if ("PENDING".equalsIgnoreCase(captureEmiStatus)) {
            TxnStatus txnStatusAfterEmiCapture = new TxnStatus(mid, orderId);
            txnStatusAfterEmiCapture.executeUntilNotPending();
            txnStatusAfterEmiCapture.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderId)
                    .validateTxnAmount(preAuthAmount)
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(mid)
                    .AssertAll();
        } else {
            Assertions.assertThat(captureEmiJson.getString("body.responseCode")).isEqualTo("01");
            Assertions.assertThat(captureEmiJson.getString("body.ResponseMessage")).isEqualTo("Txn Successful.");
            Assertions.assertThat(captureEmiJson.getString("body.status")).isEqualTo("TXN_SUCCESS");
        }

        System.out.println("Success Capture EMI txn ");   

        String logger="FLUXNET_BANKCARD_PG2_RELEASE_RESULT";        

        String instaproxyLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.instaproxy,
                orderId,logger,"REQUEST");
        System.out.println("Instaproxy Logs:--------------------------#########   " + instaproxyLogs);
 
        Assertions.assertThat(instaproxyLogs).contains("\"paidAmount\":\"2000\"");
        System.out.println("Success Release of remaining amount - Done");  
    }

}
