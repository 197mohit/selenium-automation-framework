package scripts.Qr;
import com.paytm.LocalConfig;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder;
import com.paytm.api.Instaproxy.ReqAuthUPICreateOrder.PayerInstrument;
import com.paytm.api.JustPayCallback;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.ui.base.test.BaseTest;
import com.paytm.pg.crypto.AesEncryption;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Test;
public class PosIdQR extends BaseTest {
    SoftAssertions softly= new SoftAssertions();
    String payerVPA = "9999725804@ypay";

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify Success UPI PSP txn when notification pref=SMS and QR mobile number is enabled but MID level SMS is OFF")
    public void prefSMS_001() {
        String payeeVPA = "paytm.us7name7@pty";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "15.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.POS_ID_OCIL_YES_PTYES_MID_VPA.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.POS_ID_OCIL_YES_PTYES_MID_VPA.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("PTYEC1IN").AssertAll();
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify the COP request fields when notification pref=SMS and QR mobile number is enabled but MID level SMS is OFF" )
    public void prefSMS_002() throws InterruptedException {
        String payeeVPA = "paytm.us7name7@pty";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "15.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.POS_ID_OCIL_YES_PTYES_MID_VPA.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        String grepcmd = "ACQUIRING_CREATE_ORDER_PAY";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE, orderId, grepcmd,"REQUEST");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"mobileNumber\":\"6898980000\",\"notificationPreference\":\"SMS\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify the payment notify request fields when notification pref=SMS and QR mobile number is enabled but MID level SMS is OFF" )
    public void prefSMS_003() {
        String payeeVPA = "paytm.us7name7@pty";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "15.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.POS_ID_OCIL_YES_PTYES_MID_VPA.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        String grepcmd = "BUSINESS_FLOW_NAME=ACQUIRING_PAYMENT_NOTIFY";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"mobileNumber\":\"689***0000\",\"notificationPreference\":\"SMS\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify the NQH logs when notification pref=SMS and QR mobile number is enabled but MID level SMS is OFF" )
    public void prefSMS_004() {
        String payeeVPA = "paytm.us7name7@pty";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "15.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.POS_ID_OCIL_YES_PTYES_MID_VPA.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        String grepcmd = "StatusTypePreferenceDTO : StatusTypePreferenceDTO";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("notificationQRPreference=SMS");
        Assertions.assertThat(logs).contains("merchantQRPhoneNoList=[689***0000]");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify the CG logs when notification pref=SMS and QR mobile number is enabled but MID level SMS is OFF" )
    public void prefSMS_005() {
        String payeeVPA = "paytm.us7name7@pty";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "15.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.POS_ID_OCIL_YES_PTYES_MID_VPA.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        String grepcmd = "";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId, grepcmd);
        System.out.println(logs);
        Assertions.assertThat(logs).contains("Rs 15 paid by UPI 9999XX@ypay");
        Assertions.assertThat(logs).contains("It will settle to your bank by 7 am tomorrow or you can transfer now.Visit b.paytm.me :Paytm");
        Assertions.assertThat(logs).contains("\"phoneNo\":\"689***0000\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify the Success UPI psp txn when notification pref=WHATSAPP and QR mobile number is enabled but MID level SMS is ON")
    public void prefWHATSAPP_001() {
        String payeeVPA = "paytm.us7namdq@pty";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "15.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.POS_ID_OCIL_YES_PTYES_MID_WHATSAPP.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.POS_ID_OCIL_YES_PTYES_MID_WHATSAPP.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("PTYEC1IN").AssertAll();
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify the COP request fields when notification pref=WHATSAPP and QR mobile number is enabled but MID level SMS is ON")
    public void prefWHATSAPP_002() throws InterruptedException {
        String payeeVPA = "paytm.us7namdq@pty";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "15.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.POS_ID_OCIL_YES_PTYES_MID_WHATSAPP.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        String grepcmd = "ACQUIRING_CREATE_ORDER_PAY";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE, orderId, grepcmd,"REQUEST");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"mobileNumber\":\"8006006993\",\"notificationPreference\":\"WHATSAPP\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Success txn when notification pref=DEFAULT and MID level SMS is ON")
    public void prefDEFAULT_001() {
        String payeeVPA = "paytmqr10nxdu@paytm";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "2000.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.OCIL_YES.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        TxnStatus txnStatus = new TxnStatus(Constants.MerchantType.OCIL_YES.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS").
                validateGatewayName("PPBL").AssertAll();
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-58194")
    @Test(description = "Verify the COP request fields when notification pref=DEFAULT and MID level SMS is ON")
    public void prefDEFAULT_002() throws InterruptedException {
        String payeeVPA = "paytmqr10nxdu@paytm";
        String orderId = "YES"+LocalConfig.ENV_NAME+CommonHelpers.generateOrderId();
        System.out.println("OrdersId is: " + orderId);
        String epochSeconds = String.valueOf(Instant.now().toEpochMilli());
        ReqAuthUPICreateOrder upiCreateOrder = new ReqAuthUPICreateOrder().buildRequestV2(
                PayerInstrument.CREDITLINE.name()+"_"+PayerInstrument.CREDITLINE01.name(), "10.00", payerVPA, payeeVPA, orderId, orderId, "ABC",
                epochSeconds,"","SAVINGS",MerchantType.OCIL_YES.getId(),"DEFERRED_SETTLEMENT");
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
        tokenMap.put("payeeVpa", payeeVPA);
        tokenMap.put("npciTxnId", orderId);
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
                LocalConfig.VALID_VAULT_JWT_KEY);
        String grepcmd = "ACQUIRING_CREATE_ORDER_PAY";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE, orderId, grepcmd,"REQUEST");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("\"notificationPreference\":\"DEFAULT\"");
    }
}
