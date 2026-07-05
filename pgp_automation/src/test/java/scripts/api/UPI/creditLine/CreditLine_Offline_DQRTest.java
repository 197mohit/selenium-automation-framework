package scripts.api.UPI.creditLine;

import com.paytm.LocalConfig;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder;
import com.paytm.api.JustPayCallback;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.pg.crypto.AesEncryption;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

public class CreditLine_Offline_DQRTest {
    String payerVPA = "8840500363@ypay";
    String payeeVPA = "paytm.ud105956918112@pty";//qa12id27248540927529
    Random random = new Random();
    SoftAssertions softly = new SoftAssertions();
    @Test(description = "Verify Insta to accept new value payerPaymentInstrument = \"CREDITLINE_\"+ <NPCI Credit Line types> for JusPay")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void payerPaymentInstrument_On_Insta_JUSPay() throws InterruptedException {

        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId =
                "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "10.00", payerVPA, payeeVPA, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
        softly.assertThat(json.get("status")).isEqualTo("accept");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();

        String UPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY","REQUEST");
        softly.assertThat(UPIPspLogs.substring(UPIPspLogs.indexOf("upiModeSubType")+17,UPIPspLogs.indexOf("upiModeSubType")+31)).isEqualTo("UPI_CREDITLINE");
        softly.assertThat(UPIPspLogs.substring(UPIPspLogs.indexOf("upiCreditLineSubType")+23,UPIPspLogs.indexOf("upiCreditLineSubType")+35)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        softly.assertAll();
    }

    @Test(description = "UPI_CREDIT_LINE eligibility will be set as false if pref \"UPI_CREDITLINE_BLACKLISTED\" is ON and CREDITLINE_ON_UPI_RAILS_ENABLED is null")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void Ineligible_Small_MID_Blacklisted() throws InterruptedException {
        // mid: qa14mm53905562904596
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId = "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "10.00", payerVPA, "paytm.ud105956931677@pty", orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request failed in ReqAuth");
        softly.assertThat(json.get("status")).isEqualTo("reject" +
                "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest("paytm.ud105956931677@pty", npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", "paytm.ud105956931677@pty");
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

         String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data");
        softly.assertThat(instaUPIPspResponse.contains("Merchant Ineligible UPI_CREDITLINE"));
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();
    }

    @Test(description = "UPI_CREDIT_LINE eligibility will be set as false if pref \"UPI_CREDITLINE_BLACKLISTED\" is ON and CREDITLINE_ON_UPI_RAILS_ENABLED is ON")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void CreditLineEligible_Small_MID() throws InterruptedException {
        // mid: qa12id78167344257141
        //paytm.ud199956931749@pty
        String payeeVPA="paytm.ud199956931749@pty";
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId =
                "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "10.00", payerVPA, payeeVPA, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request failed in ReqAuth");
        softly.assertThat(json.get("status")).isEqualTo("reject" +
                "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data");
        softly.assertThat(instaUPIPspResponse.contains("Merchant Ineligible UPI_CREDITLINE"));
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();
    }

    @Test(description = "Verify txn amount>=2000 is not allowed on UPI_CREDIT_LINE when \"UPI_CREDITLINE_BLACKLISTED\" is OFF and CREDITLINE_ON_UPI_RAILS_ENABLED is OFF on small merchant")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void TxnAmt_2000_not_allowed_ON_CL() throws InterruptedException {
        // mid: qa14mm53905562904596
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId = "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "22100.00", payerVPA, payeeVPA, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request failed in ReqAuth");
        softly.assertThat(json.get("status")).isEqualTo("reject" +
                "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data");
        softly.assertThat(instaUPIPspResponse.contains("Merchant Ineligible UPI_CREDITLINE"));
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();

        //Validate UPI PSP  Logs
        String UPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY","Response");
        softly.assertThat(UPIPspResponse.contains("Payment failed. This merchant can accept Credit Line on UPI upto Rs. 2000."));
        softly.assertAll();
    }
    // Disabling below case due to MID issue
    @Test(description = "Verify txn amount>=2000 is not allowed on UPI_CREDIT_LINE when \"UPI_CREDITLINE_BLACKLISTED\" is OFF and CREDITLINE_ON_UPI_RAILS_ENABLED is ON on small merchant")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void TxnAmt_2200_allowed_ON_CL_SmallMID() throws InterruptedException {
        // mid: qa12id09176493184865
        //paytm.ud199956932019@pty
        String payeeVPA="paytm.ud199956932019@pty";
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId = "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "2200.00", payerVPA, payeeVPA, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
        softly.assertThat(json.get("status")).isEqualTo("accept" +
                "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data");
        softly.assertThat(instaUPIPspResponse.contains("Merchant Ineligible UPI_CREDITLINE"));
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
        softly.assertAll();

        //Validate UPI PSP  Logs
        String UPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY","Response");
        softly.assertThat(UPIPspResponse.contains("Payment failed. This merchant can accept Credit Line on UPI upto Rs. 2000."));
        softly.assertAll();
    }

    @Test(description = "Verify txn amount<=2000 is allowed on UPI_CREDIT_LINE when \"UPI_CREDITLINE_BLACKLISTED\" is OFF and CREDITLINE_ON_UPI_RAILS_ENABLED is OFF on small merchant")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void TxnAmt_2000_allowed_ON_CL() throws InterruptedException {
        // paytm.ud199956931745@pty
        //qa12id65895617804208
        String payeeVPA="paytm.ud199956931745@pty";
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId ="PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "1900.00", payerVPA, payeeVPA, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
        softly.assertThat(json.get("status")).isEqualTo("accept" + "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data");
        softly.assertThat(instaUPIPspResponse.contains("Merchant Ineligible UPI_CREDITLINE"));
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);

        //Validate UPI PSP  Logs
        String UPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY","Response");
        softly.assertThat(UPIPspResponse.contains("ACCEPTED_SUCCESS"));
        softly.assertAll();
    }

    @Test(description = "Verify no txn is allowed on UPI_CREDIT_LINE when \"UPI_CREDITLINE_BLACKLISTED\" is ON and CREDITLINE_ON_UPI_RAILS_ENABLED is OFF on BIG merchant")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void Txn_Not_allowed_ON_CL_BIG_MID() throws InterruptedException {
        //qa12id10103882120454
        //paytm.ud199956931746@pty
        String payeeVPA_Blacklisted="paytm.ud199956931746@pty";
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId =
                "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "1900.00", payerVPA, payeeVPA_Blacklisted, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request failed in ReqAuth");
        softly.assertThat(json.get("status")).isEqualTo("reject" + "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA_Blacklisted, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA_Blacklisted);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data");
        softly.assertThat(instaUPIPspResponse.contains("Merchant Ineligible UPI_CREDITLINE"));
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
    }

    @Test(description = "Verify no txn is allowed on UPI_CREDIT_LINE when \"UPI_CREDITLINE_BLACKLISTED\" is OFF/NULL and CREDITLINE_ON_UPI_RAILS_ENABLED is OFF/NULL on BIG merchant")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void Txn_Not_allowed_ON_CL_BIG_MID_NoPref() throws InterruptedException {
        //qa12id97593465068011
        String payeeVPA_Blacklisted="paytm.ud199956931750@pty";
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId =
                "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "1900.00", payerVPA, payeeVPA_Blacklisted, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request failed in ReqAuth");
        softly.assertThat(json.get("status")).isEqualTo("reject" + "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA_Blacklisted, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA_Blacklisted);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data");
        softly.assertThat(instaUPIPspResponse.contains("Merchant Ineligible UPI_CREDITLINE"));
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);
    }
    @Test(description = "Verify All amount txn is allowed on UPI_CREDIT_LINE when \"UPI_CREDITLINE_BLACKLISTED\" is OFF and CREDITLINE_ON_UPI_RAILS_ENABLED is ON on BIG merchant")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")

    public void AllTxnt_allowed_ON_CL_BIG_MID() throws InterruptedException {
        //qa12id86593844962445
        String payeeVPA_Enabled="paytm.ud199956931748@pty";
        String orderId = CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String npciOrderId =
                "PTMCR" + String.format("%011d%011d", random.nextInt((int) Math.pow(10, 11)),
                        random.nextInt((int) Math.pow(10, 11)));
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequest(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01, "1900.00", payerVPA, payeeVPA_Enabled, orderId, npciOrderId, "ABC",
                epochSeconds);

        Response upiCreateOrderResponse = upiCreateOrder.execute("PTYES");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
        softly.assertThat(json.get("status")).isEqualTo("accept" + "");
        softly.assertAll();

        JustPayCallback justPayCallback = new JustPayCallback().buildRequest(payeeVPA_Enabled, npciOrderId,
                "10.00", ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "AABD0000011", "ABC", payerVPA,
                "MERCHANT_CREDITED_VIA_PAY",ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        JsonPath callbackResponse = justPayCallback.execute().jsonPath();
        softly.assertThat(callbackResponse.getString("callbackStatus")).isEqualTo("Success");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA_Enabled);
        tokenMap.put("npciTxnId", npciOrderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted request from PTYES bank for create order:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+37)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());

        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," - Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);

        //Validate UPI PSP Response in Insta Logs
        String instaUPIPspResponse = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Exit Connect with response data : ");
        String extno=instaUPIPspResponse.substring(instaUPIPspResponse.indexOf("externalSerialNo")+19,instaUPIPspResponse.indexOf("externalSerialNo")+38);
        softly.assertThat(extno).isNotEmpty();
        softly.assertAll();
    }

    //Yes Bank

    @Test(description = "Verify Insta to accept new value payerPaymentInstrument = \"CREDITLINE_\"+ <NPCI Credit Line types> for YES Bank")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PAPR-4883")
    public void payerPaymentInstrument_On_Insta_YesBank() throws InterruptedException {
        String payeeVPA_Enabled="paytm-956917335@ptys";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE.name()+"_"+ ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name(), "1.00", payerVPA, payeeVPA_Enabled, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS", "qa12id27248540927529","DEFERRED_SETTLEMENT");

        Response upiCreateOrderResponse = upiCreateOrder.executeV2("PPBL");
        String response = upiCreateOrderResponse.asString();
        System.out.println("Encrypted response is+: " + response);
        AesEncryption aesEncryption = new AesEncryption();
        String decryptedResponse;
        try {
            decryptedResponse = aesEncryption.decrypt(response, LocalConfig.INSTA_CREATE_ORDER_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("decryptedResponse is: " + decryptedResponse);
        JSONObject json = new JSONObject(decryptedResponse);
        softly.assertThat(json.get("statusMsg")).isEqualTo("Request Acknowledged");
        softly.assertThat(json.get("status")).isEqualTo("accept");
        softly.assertAll();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("payeeVpa", payeeVPA_Enabled);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);

        String instLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId," Decrypted Request from Bank for order creation:");
        softly.assertThat(instLogs.substring(instLogs.indexOf("payerPaymentInstrument")+25,instLogs.indexOf("payerPaymentInstrument")+48)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.toString());
        String instaUPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"Order Creation Payload for UPI PSP:");
        softly.assertThat(instaUPIPspLogs.substring(instaUPIPspLogs.indexOf("payerPaymentInstrument")+24,instaUPIPspLogs.indexOf("payerPaymentInstrument")+47)).isEqualTo("CREDITLINE_"+ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01);

        String instaResultToRouter = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderId,"pg.router.paytm.upi.payment.result");
        softly.assertThat(instaResultToRouter.contains("\"thirdResultInfo\":{\"resultStatus\":\"01\", \"resultCodeId\":\"0\", \"resultCode\":\"SUCCESS\", \"resultMsg\":\"NA\", \"retryable\":\"false\"}"));

        String UPIPspLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderId,"ACQUIRING_CREATE_ORDER_PAY","REQUEST");
        softly.assertThat(UPIPspLogs.substring(UPIPspLogs.indexOf("upiModeSubType")+17,UPIPspLogs.indexOf("upiModeSubType")+31)).isEqualTo("UPI_CREDITLINE");
        softly.assertThat(UPIPspLogs.substring(UPIPspLogs.indexOf("upiCreditLineSubType")+23,UPIPspLogs.indexOf("upiCreditLineSubType")+35)).isEqualTo(ReqAuthUPICreateOrder.PayerInstrument.CREDITLINE01.name());
        softly.assertAll();
    }
}
