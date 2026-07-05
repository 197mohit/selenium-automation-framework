package com.paytm.api.Instaproxy;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class BankResponseServiceAPI {

    private static final String BASE_URI = LocalConfig.PGP_HOST;
    private static final String INSTAPROXY_BASE = "/instaproxy";

    private static RequestSpecification baseSpec() {
        return RestAssured.given()
                .header("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .baseUri(BASE_URI);
    }

    // --- PPSL endpoints ---

    public static Response postBankResponsePPSLDirect(Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/PPSLDIRECT/RESP");
    }

    public static Response postBankResponsePPSLPG(String urlEncodedBody) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .body(urlEncodedBody)
                .post(INSTAPROXY_BASE + "/bankresponse/PPSLPG/RESP");
    }

    public static Response postBankResponseGenericBankCode(String bankCode, String urlEncodedBody) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .body(urlEncodedBody)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/RESP");
    }

    public static Response postBankResponsePSLU(Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/PSLU/RESP");
    }

    public static Response postWebhook(String bankCode, Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/webhook");
    }

    // --- SBI endpoints ---

    public static Response postSBINBResp(Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/SBI/NB/RESP");
    }

    public static Response getSBINBResp(Map<String, String> queryParams) {
        return baseSpec()
                .queryParams(queryParams)
                .get(INSTAPROXY_BASE + "/bankresponse/SBI/NB/RESP");
    }

    // --- PSB Checker ---

    public static Response postPSBChecker(String requestData, String checksum) {
        String body = "{\"RequestData\":\"" + requestData + "\",\"Checksum\":\"" + checksum + "\"}";
        return baseSpec()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post(INSTAPROXY_BASE + "/bankresponse/PSBChecker/NB/RESP");
    }

    // --- Generic bankcode/paymode endpoints ---

    public static Response postBankResponse(String bankCode, String payMode, Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESP");
    }

    public static Response getBankResponse(String bankCode, String payMode, Map<String, String> queryParams) {
        return baseSpec()
                .queryParams(queryParams)
                .get(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESP");
    }

    public static Response getBankVerifyStatus(String bankCode, String payMode, Map<String, String> queryParams) {
        return baseSpec()
                .queryParams(queryParams)
                .get(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/STATUS/RESP");
    }

    public static Response postBankResponseRESPBODY(String bankCode, String payMode, String body,
                                                     Map<String, String> headers) {
        RequestSpecification spec = baseSpec().contentType(ContentType.TEXT);
        headers.forEach(spec::header);
        return spec.body(body)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESPBODY");
    }

    public static Response postBankResponseWithTxnId(String bankCode, String payMode, String txnId,
                                                      Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/" + txnId);
    }

    public static Response postBankResponseWithMbid(String bankCode, String payMode, String mbid,
                                                     Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESP/" + mbid);
    }

    public static Response postBankResponseWithTxnIdAndMbid(String bankCode, String payMode, String txnId,
                                                             String mbid, Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESP/" + txnId + "/" + mbid);
    }

    // --- One-Click VISA ---

    public static Response postOneClickVisa(String bankCode, String payMode, String txnId,
                                             Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/ONE_CLICK/VISA/" + bankCode + "/" + payMode + "/RESP/" + txnId);
    }

    public static Response getOneClickVisaDevice(String bankCode, String payMode, Map<String, String> queryParams) {
        return baseSpec()
                .queryParams(queryParams)
                .get(INSTAPROXY_BASE + "/bankresponse/ONE_CLICK/VISA/" + bankCode + "/" + payMode + "/RESP");
    }

    // --- UPI endpoints ---

    public static Response postUPINKMB(String bankCode, String body) {
        return baseSpec()
                .contentType(ContentType.TEXT)
                .body(body != null ? body : "")
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/RESP/v1");
    }

    public static Response postUPIResp(String bankCode, Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/RESP");
    }

    public static Response postHDFCUPIResp(Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/HDFC/UPI/RESP");
    }

    public static Response postDigiposResp(String bankCode, Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/digipos/RESP");
    }

    public static Response postPOSCallback(String bankCode, Map<String, String> queryParams) {
        return baseSpec()
                .queryParams(queryParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/POS/RESP");
    }

    public static Response postUPIv3(String bankCode, String jsonPayload) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/v3/RESP");
    }

    public static Response postICIUUPI(String body) {
        return baseSpec()
                .contentType(ContentType.TEXT)
                .body(body != null ? body : "")
                .post(INSTAPROXY_BASE + "/bankresponse/ICIU/UPI/RESP");
    }

    public static Response postAxisUPIResponse(String bankCode, String jsonBody) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/RESPONSE");
    }

    public static Response postUPIv1Body(String bankCode, String body) {
        return baseSpec()
                .contentType(ContentType.TEXT)
                .body(body != null ? body : "")
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/v1/RESP");
    }

    public static Response postUPIv2YesBank(String bankCode, String jsonBody, String iv, String key, String hash) {
        RequestSpecification spec = baseSpec();
        if (iv != null) spec.header("iv", iv);
        if (key != null) spec.header("key", key);
        if (hash != null) spec.header("hash", hash);
        return spec.contentType(ContentType.TEXT)
                .body(jsonBody)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/v2/RESP");
    }

    public static Response postRefundUPIv2(String bankCode, String jsonBody, String iv, String key, String hash) {
        RequestSpecification spec = baseSpec();
        if (iv != null) spec.header("iv", iv);
        if (key != null) spec.header("key", key);
        if (hash != null) spec.header("hash", hash);
        return spec.contentType(ContentType.TEXT)
                .body(jsonBody)
                .post(INSTAPROXY_BASE + "/refundresponse/" + bankCode + "/UPI/v2/RESP");
    }

    public static Response postUPIACK(String bankCode, Map<String, String> jsonBody) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/ACK/RESP");
    }

    public static Response postThirdPartyUPIv1(String jsonPayload) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(jsonPayload)
                .post(INSTAPROXY_BASE + "/bankresponse/v1/UPI/RESP");
    }

    public static Response postUPICallback(String bankCode, String jsonPayload) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/UPI/callback");
    }

    public static Response postRefundUPICallback(String bankCode, String jsonPayload) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .post(INSTAPROXY_BASE + "/refundresponse/" + bankCode + "/UPI/callback");
    }

    // --- Special GET endpoints ---

    public static Response getCORPNBResp(String queryString) {
        return baseSpec()
                .get(INSTAPROXY_BASE + "/bankresponse/CORP/NB/RESP?" + queryString);
    }

    public static Response getENCRESP(String bankCode, String payMode, String queryString) {
        return baseSpec()
                .get(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/ENCRESP?" + queryString);
    }

    public static Response getBankResponseGETExt(String bankCode, String payMode, String respSuffix,
                                                  Map<String, String> queryParams) {
        RequestSpecification spec = baseSpec();
        if (queryParams != null && !queryParams.isEmpty()) {
            spec.queryParams(queryParams);
        }
        return spec.get(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESP" + respSuffix);
    }

    // --- Redirect form, recurring, PEMI ---

    public static Response postRedirectForm(Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/redirectForm");
    }

    public static Response postRecurringParameters(String jsonPayload) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(jsonPayload)
                .post(INSTAPROXY_BASE + "/bankresponse/upi/recurring/renewalParameters");
    }

    public static Response postPaytmEMI(String jsonPayload) {
        return baseSpec()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(jsonPayload)
                .post(INSTAPROXY_BASE + "/bankresponse/pg2/PEMI/RESP");
    }

    // --- MBID, Cashe, tracking GETs ---

    public static Response postBankResponseMBID(String bankCode, String payMode, String txnId, String mbid,
                                                 Map<String, String> queryParams) {
        return baseSpec()
                .queryParams(queryParams)
                .post(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/" + txnId + "/mbid/" + mbid);
    }

    public static Response getCasheCardlessEmi(String leafRefNo) {
        return baseSpec()
                .queryParam("leafRefNo", leafRefNo)
                .get(INSTAPROXY_BASE + "/bankresponse/cashe/cardlessemi/resp");
    }

    public static Response postMerchantMock(Map<String, String> formParams) {
        return baseSpec()
                .contentType(ContentType.URLENC)
                .formParams(formParams)
                .post(INSTAPROXY_BASE + "/bankresponse/merchant/mock/RESP");
    }

    public static Response getBankResponseWithTxnId(String bankCode, String payMode, String txnId) {
        return baseSpec()
                .get(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESP/" + txnId);
    }

    public static Response getBankResponseWithEsnMbid(String bankCode, String payMode, String esn, String mbid) {
        return baseSpec()
                .get(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/RESP/" + esn + "/" + mbid);
    }

    public static Response getBankResponseRespPrefix(String bankCode, String payMode, String esn, String mbid) {
        return baseSpec()
                .get(INSTAPROXY_BASE + "/bankresponse/resp/" + bankCode + "/" + payMode + "/" + esn + "/" + mbid);
    }

    public static Response getAdditionalGET(String bankCode, String payMode, String pathParam,
                                             Map<String, String> queryParams) {
        RequestSpecification spec = baseSpec();
        if (queryParams != null && !queryParams.isEmpty()) {
            spec.queryParams(queryParams);
        }
        return spec.get(INSTAPROXY_BASE + "/bankresponse/" + bankCode + "/" + payMode + "/" + pathParam);
    }
}
