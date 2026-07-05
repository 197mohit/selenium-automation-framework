package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.TreeMap;

/**
 * API class for POST /merchant-status/v1/recovery/status
 *
 * Checksum is sent as JsonData query param (same pattern as getTxnStatus).
 * Request body carries the full nested head+body JSON for the controller.
 *
 * Two query flows:
 *   - By orderId:    body contains mid + orderId + deductionType
 *   - By deductionId: body contains mid + deductionId
 */
public class RecoveryStatusApi extends BaseApi {

    private static final String ENDPOINT = "/merchant-status/v1/recovery/status";

    /**
     * Query recovery status by mid + orderId + deductionType.
     */
    public RecoveryStatusApi(String mid, String merchantKey, String orderId, String deductionType) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        treemap.put("DEDUCTIONTYPE", deductionType);
        String checksum = PGPUtil.getChecksum(merchantKey, treemap);

        String body = "{"
                + "\"head\":{\"clientId\":\"LMS\",\"version\":\"v1\"},"
                + "\"body\":{"
                + "\"mid\":\"" + mid + "\","
                + "\"orderId\":\"" + orderId + "\","
                + "\"deductionType\":\"" + deductionType + "\""
                + "}}";

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        // addQueryParam → URL query string; must NOT use addParam (form body) alongside setBody()
        getRequestSpecBuilder().addQueryParam("JsonData",
                "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId + "\","
                        + "\"DEDUCTIONTYPE\":\"" + deductionType + "\","
                        + "\"CHECKSUMHASH\":\"" + checksum + "\"}");
        getRequestSpecBuilder().setBody(body);
        // Clear requestContext so execute() does not re-set the body as an Object,
        // which causes RestAssured 3.x to throw IllegalStateException when a POST
        // spec has both a body and addQueryParam params.
        updateDocumentContext(null);
    }

    /**
     * Query recovery status by mid + deductionId.
     */
    public RecoveryStatusApi(String mid, String merchantKey, String deductionId) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("DEDUCTIONID", deductionId);
        String checksum = PGPUtil.getChecksum(merchantKey, treemap);

        String body = "{"
                + "\"head\":{\"clientId\":\"LMS\",\"version\":\"v1\"},"
                + "\"body\":{"
                + "\"mid\":\"" + mid + "\","
                + "\"deductionId\":\"" + deductionId + "\""
                + "}}";

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        getRequestSpecBuilder().addQueryParam("JsonData",
                "{\"MID\":\"" + mid + "\",\"DEDUCTIONID\":\"" + deductionId + "\","
                        + "\"CHECKSUMHASH\":\"" + checksum + "\"}");
        getRequestSpecBuilder().setBody(body);
        updateDocumentContext(null);
    }

    /**
     * Mismatch constructor: valid checksum computed on jsonData* values, but body carries
     * override values — triggers RecoveryStatusUtil.validateRequest() mismatch which maps
     * to INVALID_INPUT_DATA (400).
     *
     * @param merchantKey         key for the merchant identified by jsonDataMid
     * @param jsonDataMid         MID used in JsonData param (checksum computed on this)
     * @param jsonDataOrderId     orderId in JsonData param
     * @param jsonDataDeductionType deductionType in JsonData param
     * @param bodyMid             MID placed in request body (differs from jsonDataMid to trigger mismatch)
     * @param bodyOrderId         orderId placed in request body
     * @param bodyDeductionType   deductionType placed in request body
     */
    public RecoveryStatusApi(String merchantKey,
                             String jsonDataMid, String jsonDataOrderId, String jsonDataDeductionType,
                             String bodyMid, String bodyOrderId, String bodyDeductionType) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", jsonDataMid);
        treemap.put("ORDERID", jsonDataOrderId);
        treemap.put("DEDUCTIONTYPE", jsonDataDeductionType);
        String checksum = PGPUtil.getChecksum(merchantKey, treemap);

        String body = "{"
                + "\"head\":{\"clientId\":\"LMS\",\"version\":\"v1\"},"
                + "\"body\":{"
                + "\"mid\":\"" + bodyMid + "\","
                + "\"orderId\":\"" + bodyOrderId + "\","
                + "\"deductionType\":\"" + bodyDeductionType + "\""
                + "}}";

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        getRequestSpecBuilder().addQueryParam("JsonData",
                "{\"MID\":\"" + jsonDataMid + "\",\"ORDERID\":\"" + jsonDataOrderId + "\","
                        + "\"DEDUCTIONTYPE\":\"" + jsonDataDeductionType + "\","
                        + "\"CHECKSUMHASH\":\"" + checksum + "\"}");
        getRequestSpecBuilder().setBody(body);
        updateDocumentContext(null);
    }

    /**
     * Query with deliberately wrong checksum — for negative / checksum-mismatch tests.
     */
    public RecoveryStatusApi(String mid, String orderId, String deductionType, boolean badChecksum) {
        String body = "{"
                + "\"head\":{\"clientId\":\"LMS\",\"version\":\"v1\"},"
                + "\"body\":{"
                + "\"mid\":\"" + mid + "\","
                + "\"orderId\":\"" + orderId + "\","
                + "\"deductionType\":\"" + deductionType + "\""
                + "}}";

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        getRequestSpecBuilder().addQueryParam("JsonData",
                "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId + "\","
                        + "\"DEDUCTIONTYPE\":\"" + deductionType + "\","
                        + "\"CHECKSUMHASH\":\"INVALID_CHECKSUM_VALUE\"}");
        getRequestSpecBuilder().setBody(body);
        updateDocumentContext(null);
    }
}