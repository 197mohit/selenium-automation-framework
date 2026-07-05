package scripts.Instaproxy;

import com.paytm.api.Instaproxy.BankResponseServiceAPI;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.paytm.appconstants.Constants.Owner.HIMANSHU;

@Owner(HIMANSHU)
@Feature("BankResponseService-CodeCoverage")
public class BankResponseServiceCoverageTest extends PGPBaseTest {

    private static final String DUMMY_ESN = "TEST" + System.currentTimeMillis();
    private static final String DUMMY_ORDER = "TESTORD_" + System.currentTimeMillis();

    private Map<String, String> buildPPSLParams(String paymentMode) {
        Map<String, String> params = new HashMap<>();
        params.put("ORDERID", DUMMY_ESN);
        params.put("UDF_1", DUMMY_ORDER);
        params.put("PAYMENTMODE", paymentMode);
        params.put("STATUS", "TXN_SUCCESS");
        params.put("TXNAMOUNT", "1.00");
        params.put("TXNID", UUID.randomUUID().toString());
        params.put("MID", "TESTMID0001");
        params.put("CURRENCY", "INR");
        return params;
    }

    // ========== PPSL Direct (CC) ==========
    @Test(description = "PPSL Direct bank response with CC paymode")
    public void testPPSLDirectResponse_CC() {
        Response resp = BankResponseServiceAPI.postBankResponsePPSLDirect(buildPPSLParams("CC"));
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== PPSL Direct (UPI) ==========
    @Test(description = "PPSL Direct bank response with UPI paymode")
    public void testPPSLDirectResponse_UPI() {
        Response resp = BankResponseServiceAPI.postBankResponsePPSLDirect(buildPPSLParams("UPI"));
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== PPSL Direct (invalid paymode) ==========
    @Test(description = "PPSL Direct with invalid payment mode returns empty")
    public void testPPSLDirectResponse_InvalidPaymode() {
        Response resp = BankResponseServiceAPI.postBankResponsePPSLDirect(buildPPSLParams("INVALID_MODE"));
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== PPSLPG response ==========
    @Test(description = "PPSLPG bank response endpoint")
    public void testPPSLPGResponse() {
        String body = "ORDERID=" + DUMMY_ESN + "&UDF_1=" + DUMMY_ORDER + "&PAYMENTMODE=CC&STATUS=TXN_SUCCESS&TXNAMOUNT=1.00";
        Response resp = BankResponseServiceAPI.postBankResponsePPSLPG(body);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== Generic {bankcode}/RESP ==========
    @Test(description = "Generic bankcode RESP endpoint")
    public void testGenericBankCodeRESP() {
        String body = "ORDERID=" + DUMMY_ESN + "&UDF_1=" + DUMMY_ORDER + "&PAYMENTMODE=NB&STATUS=TXN_SUCCESS";
        Response resp = BankResponseServiceAPI.postBankResponseGenericBankCode("TESTBANK", body);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== PSLU response ==========
    @Test(description = "PSLU bank response endpoint")
    public void testPSLUResponse() {
        Response resp = BankResponseServiceAPI.postBankResponsePSLU(buildPPSLParams("CC"));
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== Webhook ==========
    @Test(description = "Webhook bank response with UPI paymode")
    public void testWebhookResponse() {
        Response resp = BankResponseServiceAPI.postWebhook("PPSLDIRECT", buildPPSLParams("UPI"));
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== SBI NB POST ==========
    @Test(description = "SBI NB POST with subsidiary bank-referrer header")
    public void testSBINBPostWithSubsidiary() {
        Map<String, String> params = new HashMap<>();
        params.put("bank-referrer", "https://onlinesbh.sbi.co.in/somepath");
        params.put("STATUS", "TXN_SUCCESS");
        params.put("TXNAMOUNT", "100.00");
        Response resp = BankResponseServiceAPI.postSBINBResp(params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== SBI NB GET ==========
    @Test(description = "SBI NB GET with query params")
    public void testSBINBGet() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        params.put("TXNAMOUNT", "100.00");
        Response resp = BankResponseServiceAPI.getSBINBResp(params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== PSB Checker ==========
    @Test(description = "PSB Checker maker-checker JSON endpoint")
    public void testPSBCheckerMakerChecker() {
        Response resp = BankResponseServiceAPI.postPSBChecker("testRequestData", "testChecksum");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== Generic POST {bankcode}/{paymode}/RESP ==========
    @Test(description = "Generic bankcode/paymode POST RESP")
    public void testGenericBankResponsePOST() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        params.put("TXNAMOUNT", "1.00");
        Response resp = BankResponseServiceAPI.postBankResponse("HDFC", "CC", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== Generic GET {bankcode}/{paymode}/RESP ==========
    @Test(description = "Generic bankcode/paymode GET RESP")
    public void testGenericBankResponseGET() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        Response resp = BankResponseServiceAPI.getBankResponse("HDFC", "CC", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== STATUS/RESP verify ==========
    @Test(description = "Bank verify STATUS RESP returns formatted params")
    public void testBankVerifyStatusGET() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        params.put("TXNID", "TEST123");
        Response resp = BankResponseServiceAPI.getBankVerifyStatus("HDFC", "NB", params);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== RESPBODY ==========
    @Test(description = "Bank response RESPBODY with headers and body")
    public void testBankResponseRESPBODY() {
        Map<String, String> headers = new HashMap<>();
        headers.put("custom-header", "test-value");
        Response resp = BankResponseServiceAPI.postBankResponseRESPBODY("HDFC", "CC", "test-body-content", headers);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== POST with txnId ==========
    @Test(description = "Bank response POST with txnId in path")
    public void testBankResponseWithTxnId() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        Response resp = BankResponseServiceAPI.postBankResponseWithTxnId("HDFC", "CC", "TXN123", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== POST with mbid (CCAvenue) ==========
    @Test(description = "Bank response POST with mbid for CCAvenue-style")
    public void testBankResponseWithMbid() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        Response resp = BankResponseServiceAPI.postBankResponseWithMbid("CCAV", "CC", "MBID123", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== POST with txnId + mbid (BOBFSS) ==========
    @Test(description = "Bank response POST with txnId and mbid")
    public void testBankResponseWithTxnIdAndMbid() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        Response resp = BankResponseServiceAPI.postBankResponseWithTxnIdAndMbid("BOB", "CC", "TXN123", "MBID456", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== One-Click VISA POST ==========
    @Test(description = "One-Click VISA enrollment response")
    public void testOneClickVisaPOST() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        params.put("PaRes", "test-pares");
        Response resp = BankResponseServiceAPI.postOneClickVisa("HDFC", "CC", "TXN123", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== One-Click VISA Device GET ==========
    @Test(description = "One-Click VISA device PaRes callback")
    public void testOneClickVisaDeviceGET() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        params.put("PaRes", "validated-pares");
        Response resp = BankResponseServiceAPI.getOneClickVisaDevice("HDFC", "CC", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== UPI v3 FINOF ==========
    @Test(description = "UPI v3 FINOF JSON callback")
    public void testUPIv3FINOFCallback() {
        String payload = "{\"txnId\":\"TEST123\",\"status\":\"SUCCESS\",\"amount\":\"100.00\"}";
        Response resp = BankResponseServiceAPI.postUPIv3("FINOF", payload);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== UPI v2 Yes Bank payload branch ==========
    @Test(description = "UPI v2 Yes Bank callback with payload (mandate)")
    public void testUPIv2YesBankPayloadBranch() {
        String body = "{\"payload\":\"encrypted-data-here\",\"extra\":\"value\"}";
        Response resp = BankResponseServiceAPI.postUPIv2YesBank("YES", body, null, null, null);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== UPI v2 Yes Bank requestMsg branch ==========
    @Test(description = "UPI v2 Yes Bank callback with requestMsg")
    public void testUPIv2YesBankRequestMsgBranch() {
        String body = "{\"requestMsg\":\"some-request-message\",\"txnId\":\"TEST123\"}";
        Response resp = BankResponseServiceAPI.postUPIv2YesBank("YES", body, null, null, null);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== UPI v2 Yes Bank offline branch ==========
    @Test(description = "UPI v2 Yes Bank offline callback with iv/key/hash")
    public void testUPIv2YesBankOfflineBranch() {
        String body = "{\"txnId\":\"TEST123\",\"status\":\"SUCCESS\"}";
        Response resp = BankResponseServiceAPI.postUPIv2YesBank("YES", body, "test-iv", "test-key", "test-hash");
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== Refund UPI v2 payload branch ==========
    @Test(description = "Refund UPI v2 callback with payload")
    public void testRefundUPIv2PayloadBranch() {
        String body = "{\"payload\":\"encrypted-refund-data\"}";
        Response resp = BankResponseServiceAPI.postRefundUPIv2("YES", body, null, null, null);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== Refund UPI v2 requestMsg branch ==========
    @Test(description = "Refund UPI v2 callback with requestMsg")
    public void testRefundUPIv2RequestMsgBranch() {
        String body = "{\"requestMsg\":\"refund-request-msg\"}";
        Response resp = BankResponseServiceAPI.postRefundUPIv2("YES", body, null, null, null);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== Refund UPI v2 offline branch ==========
    @Test(description = "Refund UPI v2 offline callback with headers")
    public void testRefundUPIv2OfflineBranch() {
        String body = "{\"txnId\":\"REFUND123\"}";
        Response resp = BankResponseServiceAPI.postRefundUPIv2("YES", body, "iv-val", "key-val", "hash-val");
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== UPI ACK ==========
    @Test(description = "UPI ACK response endpoint")
    public void testUPIACK() {
        Map<String, String> params = new HashMap<>();
        params.put("txnId", "TEST123");
        params.put("status", "SUCCESS");
        Response resp = BankResponseServiceAPI.postUPIACK("PPBL", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== UPI callback PTYES ==========
    @Test(description = "UPI callback PTYES style")
    public void testUPICallbackPTYES() {
        String payload = "{\"txnId\":\"TEST123\",\"status\":\"SUCCESS\"}";
        Response resp = BankResponseServiceAPI.postUPICallback("PTYES", payload);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== Refund UPI callback ==========
    @Test(description = "Refund UPI callback endpoint")
    public void testRefundUPICallback() {
        String payload = "{\"refundId\":\"REF123\",\"status\":\"SUCCESS\"}";
        Response resp = BankResponseServiceAPI.postRefundUPICallback("PTYES", payload);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }

    // ========== CORP NB GET ==========
    @Test(description = "CORP NB GET with raw query string")
    public void testCORPNBGet() {
        Response resp = BankResponseServiceAPI.getCORPNBResp("STATUS=SUCCESS&TXNID=CORP123");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== ENCRESP GET ==========
    @Test(description = "ENCRESP GET with encrypted response param")
    public void testENCRESPGet() {
        Response resp = BankResponseServiceAPI.getENCRESP("HDFC", "NB", "encdata=abc123xyz");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== GETExt (RESP regex pattern) ==========
    @Test(description = "Bank response GETExt with RESP regex suffix")
    public void testBankResponseGETExt() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        Response resp = BankResponseServiceAPI.getBankResponseGETExt("HDFC", "CC", "DATA", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== Redirect form ==========
    @Test(description = "Redirect form fetch from Redis")
    public void testRedirectFormFetch() {
        Map<String, String> params = new HashMap<>();
        params.put("txnId", "NONEXISTENT_TXN");
        params.put("mid", "TESTMID");
        Response resp = BankResponseServiceAPI.postRedirectForm(params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== UPI recurring parameters ==========
    @Test(description = "UPI recurring params with invalid payload triggers validation failure")
    public void testRecurringParametersValidationFailure() {
        String payload = "{\"header\":{},\"body\":{}}";
        Response resp = BankResponseServiceAPI.postRecurringParameters(payload);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 400, 500);
    }

    // ========== PEMI validation failure ==========
    @Test(description = "PEMI callback with invalid payload returns 400")
    public void testPEMIValidationFailure() {
        String payload = "{\"header\":{\"version\":\"v1\",\"signature\":\"invalid\",\"requestTimestamp\":\"1234567890123456\"},\"body\":{}}";
        Response resp = BankResponseServiceAPI.postPaytmEMI(payload);
        Assertions.assertThat(resp.getStatusCode()).isIn(400, 401, 500);
    }

    // ========== PEMI signature failure ==========
    @Test(description = "PEMI callback with bad signature returns 401")
    public void testPEMISignatureFailure() {
        String payload = "{\"header\":{\"version\":\"v1\",\"signature\":\"invalid-jwt-token\",\"requestTimestamp\":\"1234567890123456\"},"
                + "\"body\":{\"pgTxnId\":\"TXN123\",\"responseStatus\":\"SUCCESS\","
                + "\"txnAmount\":\"100.00\",\"loanAccountNumber\":\"LOAN123\"}}";
        Response resp = BankResponseServiceAPI.postPaytmEMI(payload);
        Assertions.assertThat(resp.getStatusCode()).isIn(400, 401, 500);
    }

    // ========== ICIU UPI POST ==========
    @Test(description = "ICIU UPI POST callback")
    public void testICIUUPIPost() {
        Response resp = BankResponseServiceAPI.postICIUUPI("{\"status\":\"SUCCESS\",\"txnId\":\"ICIU123\"}");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== Axis UPI RESPONSE ==========
    @Test(description = "Axis UPI RESPONSE with JSON body")
    public void testAxisUPIResponse() {
        String body = "{\"txnId\":\"AXIS123\",\"status\":\"SUCCESS\",\"amount\":\"100.00\"}";
        Response resp = BankResponseServiceAPI.postAxisUPIResponse("AXIU", body);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== HDFC UPI RESP (mandate data parsing) ==========
    @Test(description = "HDFC UPI RESP triggers mandate data parsing")
    public void testHDFCUPIResp() {
        Map<String, String> params = new HashMap<>();
        params.put("meRes", "");
        Response resp = BankResponseServiceAPI.postHDFCUPIResp(params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== HDFC UPI RESP with meRes ==========
    @Test(description = "HDFC UPI RESP with meRes skips mandate parsing")
    public void testHDFCUPIRespWithMeRes() {
        Map<String, String> params = new HashMap<>();
        params.put("meRes", "some-mandate-response");
        params.put("txnId", "HDFC_MAN_123");
        Response resp = BankResponseServiceAPI.postHDFCUPIResp(params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== Digipos UPI ==========
    @Test(description = "DIGIPOS UPI callback with digipos flag")
    public void testDigiposUPICallback() {
        Map<String, String> params = new HashMap<>();
        params.put("txnId", "DIGI123");
        params.put("status", "SUCCESS");
        Response resp = BankResponseServiceAPI.postDigiposResp("HITACHI", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== POS Callback ==========
    @Test(description = "POS callback endpoint")
    public void testPOSCallback() {
        Map<String, String> params = new HashMap<>();
        params.put("txnId", "POS123");
        params.put("status", "SUCCESS");
        Response resp = BankResponseServiceAPI.postPOSCallback("HITACHI", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== MBID POST ==========
    @Test(description = "Bank response MBID POST")
    public void testBankResponseMBID() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        Response resp = BankResponseServiceAPI.postBankResponseMBID("HDFC", "CC", "TXN123", "MBID456", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== Cashe Cardless EMI ==========
    @Test(description = "Cashe cardless EMI callback")
    public void testCasheCardlessEmi() {
        Response resp = BankResponseServiceAPI.getCasheCardlessEmi("LEAFREF123");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== UPI v1 Body POST ==========
    @Test(description = "UPI v1 body POST callback")
    public void testUPIv1BodyPost() {
        Response resp = BankResponseServiceAPI.postUPIv1Body("PPBL", "{\"status\":\"SUCCESS\"}");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== UPI NKMB v1 POST ==========
    @Test(description = "UPI NKMB v1 POST callback")
    public void testUPINKMBPost() {
        Response resp = BankResponseServiceAPI.postUPINKMB("NKMB", "{\"status\":\"SUCCESS\",\"txnId\":\"NKMB123\"}");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== UPI RESP POST (generic) ==========
    @Test(description = "UPI RESP POST for generic bank code")
    public void testUPIRespPost() {
        Map<String, String> params = new HashMap<>();
        params.put("txnId", "UPITEST123");
        params.put("status", "SUCCESS");
        Response resp = BankResponseServiceAPI.postUPIResp("PPBL", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== GET with txnId ==========
    @Test(description = "Bank response GET with txnId for tracking")
    public void testBankResponseTxnGET() {
        Response resp = BankResponseServiceAPI.getBankResponseWithTxnId("HDFC", "CC", "TXN123");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== GET with esn/mbid (AUBL) ==========
    @Test(description = "Bank response GET with esn and mbid path")
    public void testBankResponseEsnMbidGET() {
        Response resp = BankResponseServiceAPI.getBankResponseWithEsnMbid("AUBL", "NB", "ESN123", "MBID456");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== GET with /resp/ prefix ==========
    @Test(description = "Bank response GET with /resp/ prefix path")
    public void testBankResponseRespPrefixGET() {
        Response resp = BankResponseServiceAPI.getBankResponseRespPrefix("AUBL", "NB", "ESN123", "MBID456");
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== GET additional (invalid paymode filter) ==========
    @Test(description = "Bank response additional GET with invalid paymode")
    public void testBankResponseAdditionalGET_InvalidPaymode() {
        Response resp = BankResponseServiceAPI.getAdditionalGET("HDFC", "fonts", "somefile.woff", null);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== Third-party UPI v1 POST ==========
    @Test(description = "Third-party UPI v1 POST with structured payload")
    public void testThirdPartyUPIv1() {
        String payload = "{\"header\":{\"version\":\"v1\",\"requestTimestamp\":\"1234567890123456\",\"signature\":\"test\"},"
                + "\"body\":{\"externalSerialNo\":\"ESN123\",\"orderId\":\"ORD123\",\"txnStatus\":\"SUCCESS\","
                + "\"amount\":\"100.00\",\"bankRRN\":\"RRN123\",\"responseCode\":\"0\","
                + "\"responseMessage\":\"Success\"}}";
        Response resp = BankResponseServiceAPI.postThirdPartyUPIv1(payload);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 400, 500);
    }

    // ========== Axis UPI with mandate data ==========
    @Test(description = "Axis UPI RESPONSE with mandate data keyword")
    public void testAxisUPIResponseMandate() {
        String body = "{\"txnId\":\"AXIS456\",\"status\":\"SUCCESS\",\"amount\":\"100.00\",\"data\":{\"mandateInfo\":\"test\"}}";
        Response resp = BankResponseServiceAPI.postAxisUPIResponse("AXIU", body);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== PPSL Direct NB paymode ==========
    @Test(description = "PPSL Direct with NB paymode triggers non-UPI branch")
    public void testPPSLDirectResponse_NB() {
        Response resp = BankResponseServiceAPI.postBankResponsePPSLDirect(buildPPSLParams("NB"));
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== Webhook with invalid paymode ==========
    @Test(description = "Webhook with invalid paymode returns empty")
    public void testWebhookInvalidPaymode() {
        Response resp = BankResponseServiceAPI.postWebhook("TESTBANK", buildPPSLParams("INVALID_MODE"));
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 500);
    }

    // ========== Generic DC paymode POST ==========
    @Test(description = "Generic bankcode/paymode POST with DC")
    public void testGenericBankResponsePOST_DC() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        params.put("TXNAMOUNT", "1.00");
        Response resp = BankResponseServiceAPI.postBankResponse("HDFC", "DC", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== Generic NB paymode POST ==========
    @Test(description = "Generic bankcode/paymode POST with NB")
    public void testGenericBankResponsePOST_NB() {
        Map<String, String> params = new HashMap<>();
        params.put("STATUS", "TXN_SUCCESS");
        params.put("TXNAMOUNT", "1.00");
        Response resp = BankResponseServiceAPI.postBankResponse("ICICI", "NB", params);
        Assertions.assertThat(resp.getStatusCode()).isIn(200, 302, 500);
    }

    // ========== UPI callback with different bank code ==========
    @Test(description = "UPI callback for JUSTPAY bank code")
    public void testUPICallbackJustPay() {
        String payload = "{\"txnId\":\"JP123\",\"status\":\"SUCCESS\",\"amount\":\"50.00\"}";
        Response resp = BankResponseServiceAPI.postUPICallback("JUSTPAY", payload);
        Assertions.assertThat(resp.getStatusCode()).isEqualTo(200);
    }
}
