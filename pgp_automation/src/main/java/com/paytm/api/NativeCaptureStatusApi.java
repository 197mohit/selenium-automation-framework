package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

/**
 * API client for:
 *   POST /merchant-status/api/v1/v5/order/status   (NativePaymentStatusController.OrderStatusV5)
 *
 * Used for PreAuth capture/void/release status queries.
 * Auth: CHECKSUM tokenType — same pattern as NativePaymentStatusApi.
 */
public class NativeCaptureStatusApi extends BaseApi {

    private static final String ENDPOINT = "/merchant-status/api/v1/v5/order/status";

    /**
     * Build a checksum-authenticated capture-status request.
     *
     * @param merchantType merchant whose key is used for checksum
     * @param orderId      PreAuth order to query
     * @param txnType      "CAPTURE", "VOID", or "RELEASE" — drives which branch is taken
     * @param captureId    captureId, only used when txnType=CAPTURE; pass null otherwise
     */
    public NativeCaptureStatusApi(Constants.MerchantType merchantType,
                                  String orderId,
                                  String txnType,
                                  String captureId) {
        String mid = merchantType.getId();
        String checksumInput = "{\"mid\":\"" + mid + "\",\"orderId\":\"" + orderId + "\"}";
        String token = PGPUtil.getChecksum(merchantType.getKey(), checksumInput);

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("{")
                .append("\"head\":{\"clientId\":\"cart\",\"version\":\"v1\",")
                .append("\"requestTimestamp\":\"").append(System.currentTimeMillis()).append("\",")
                .append("\"channelId\":\"WEB\",\"tokenType\":\"CHECKSUM\",\"signature\":\"\",")
                .append("\"token\":\"").append(token).append("\"},")
                .append("\"body\":{")
                .append("\"mid\":\"").append(mid).append("\",")
                .append("\"orderId\":\"").append(orderId).append("\",")
                .append("\"txnType\":\"").append(txnType).append("\"");
        if (captureId != null) {
            bodyBuilder.append(",\"captureId\":\"").append(captureId).append("\"");
        }
        bodyBuilder.append("}}");

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        getRequestSpecBuilder().setBody(bodyBuilder.toString());
    }

    /**
     * Build a raw-body request for negative tests (invalid checksum, blank fields, etc.).
     */
    public NativeCaptureStatusApi(String rawBody) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        getRequestSpecBuilder().setBody(rawBody);
    }
}