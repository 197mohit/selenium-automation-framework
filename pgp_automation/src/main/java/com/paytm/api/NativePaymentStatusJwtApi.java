package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

/**
 * API client for native payment status endpoints using JWT authentication.
 *
 * Exercises the JWT branch in NativePaymentStatusRequestFilter:
 *   tokenType == "JWT" → JWTUtil.verifyJWTToken()
 *
 * Endpoints:
 *   POST /merchant-status/api/v1/getPaymentStatus
 *   POST /merchant-status/api/v1/v2/getPaymentStatus
 */
public class NativePaymentStatusJwtApi extends BaseApi {

    private static final String ENDPOINT_V1 = Constants.PGPAPIResourcePath.GET_PAYMENT_STATUS;
    private static final String ENDPOINT_V2 = "/merchant-status/api/v1/v2/getPaymentStatus";

    /**
     * Build a JWT-authenticated native payment status request.
     *
     * @param mid     merchant ID
     * @param orderId order to query
     * @param jwtKey  secret key for HMAC-SHA256 signing
     * @param v2      true → v2/getPaymentStatus endpoint
     */
    public NativePaymentStatusJwtApi(String mid, String orderId, String jwtKey, boolean v2) {
        Map<String, String> claims = new HashMap<>();
        claims.put("iss", "cart");
        claims.put("BODY", "{\"mid\":\"" + mid + "\",\"orderId\":\"" + orderId + "\"}");

        String jwtToken = PGPHelpers.createJsonWebToken(claims, PGPHelpers.ISSUER.ts, jwtKey);

        String body = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\","
                + "\"requestTimestamp\":\"" + System.currentTimeMillis() + "\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"JWT\","
                + "\"token\":\"" + jwtToken + "\"},"
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
     * Build a request with an invalid JWT token string (for negative test).
     */
    public NativePaymentStatusJwtApi(String mid, String orderId, boolean v2, boolean invalidToken) {
        String body = "{"
                + "\"head\":{\"clientId\":\"cart\",\"version\":\"v1\","
                + "\"requestTimestamp\":\"" + System.currentTimeMillis() + "\","
                + "\"channelId\":\"WEB\",\"tokenType\":\"JWT\","
                + "\"token\":\"INVALID_JWT_TOKEN_STRING\"},"
                + "\"body\":{\"mid\":\"" + mid + "\",\"orderId\":\"" + orderId + "\"}"
                + "}";

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(v2 ? ENDPOINT_V2 : ENDPOINT_V1);
        getRequestSpecBuilder().setBody(body);
    }
}
