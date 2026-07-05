package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.TreeMap;

/**
 * API client for POST /merchant-status/v1/recovery/status with head.version=v2.
 *
 * Exercises RecoveryStatusController → RecoveryStatusServiceBizImpl.getRecoveryStatusV2()
 * branch that is enabled when head.version == "v2" AND FF4J flag
 * {@code enable.merchant.status.v2.recovery.status} is enabled for the MID.
 */
public class RecoveryStatusV2Api extends BaseApi {

    private static final String ENDPOINT = "/merchant-status/v1/recovery/status";

    /**
     * V2 recovery status query by orderId + deductionType.
     */
    public RecoveryStatusV2Api(String mid, String merchantKey, String orderId, String deductionType) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        treemap.put("DEDUCTIONTYPE", deductionType);
        String checksum = PGPUtil.getChecksum(merchantKey, treemap);

        String body = "{"
                + "\"head\":{\"clientId\":\"LMS\",\"version\":\"v2\"},"
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
                        + "\"CHECKSUMHASH\":\"" + checksum + "\"}");
        getRequestSpecBuilder().setBody(body);
        updateDocumentContext(null);
    }

    /**
     * V2 recovery status with deliberately wrong checksum.
     */
    public RecoveryStatusV2Api(String mid, String orderId, String deductionType, boolean badChecksum) {
        String body = "{"
                + "\"head\":{\"clientId\":\"LMS\",\"version\":\"v2\"},"
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
