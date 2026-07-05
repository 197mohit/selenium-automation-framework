package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.TreeMap;

/**
 * API client for:
 *   POST /merchant-status/api/v1/getPaymentStatus   (NativePaymentStatusController.processSecuredPaymentStatusPOST)
 *   POST /merchant-status/api/v1/v2/getPaymentStatus (NativePaymentStatusController.getPaymentStatus)
 *
 * Auth: CHECKSUM tokenType — checksum computed over {mid, orderId} body fields.
 */
public class NativePaymentStatusApi extends BaseApi {

    private static final String ENDPOINT_V1    = Constants.PGPAPIResourcePath.GET_PAYMENT_STATUS;
    private static final String ENDPOINT_V2    = "/merchant-status/api/v1/v2/getPaymentStatus";

    /**
     * Build a valid checksum-authenticated request.
     *
     * @param merchantType merchant whose key is used to compute the checksum
     * @param orderId      order to query; pass "" to test blank-orderId validation
     * @param v2           true → v2/getPaymentStatus endpoint; false → v1/getPaymentStatus
     */
    public NativePaymentStatusApi(com.paytm.appconstants.Constants.MerchantType merchantType,
                                  String orderId,
                                  boolean v2) {
        String mid = merchantType.getId();
        String checksumInput = "{\"mid\":\"" + mid + "\",\"orderId\":\"" + orderId + "\"}";
        String token = PGPUtil.getChecksum(merchantType.getKey(), checksumInput);

        String body = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\","
                + "\"requestTimestamp\":\"" + System.currentTimeMillis() + "\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"CHECKSUM\",\"signature\":\"\","
                + "\"token\":\"" + token + "\"},"
                + "\"body\":{\"mid\":\"" + mid + "\",\"orderId\":\"" + orderId + "\"}"
                + "}";

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(v2 ? ENDPOINT_V2 : ENDPOINT_V1);
        getRequestSpecBuilder().setBody(body);
    }

    /**
     * Build a request with a raw JSON body — for negative/tampered-checksum tests.
     *
     * @param rawBody complete JSON string to POST as-is
     * @param v2      true → v2 endpoint
     */
    public NativePaymentStatusApi(String rawBody, boolean v2) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(v2 ? ENDPOINT_V2 : ENDPOINT_V1);
        getRequestSpecBuilder().setBody(rawBody);
    }
}
