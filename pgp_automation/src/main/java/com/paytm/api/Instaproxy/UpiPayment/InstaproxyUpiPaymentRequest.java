package com.paytm.api.Instaproxy.UpiPayment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.InstaproxyUpiPaymentPayload;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.instaproxy.upipayment.UpiPaymentRequestDTO;
import com.paytm.dto.instaproxy.upipayment.UpiPaymentResponseDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

import java.io.IOException;

// API integration: Instaproxy PG2 UPI payment/request.htm
public class InstaproxyUpiPaymentRequest extends BaseApi {

    public InstaproxyUpiPaymentRequest(String reqMsgId, String signature, String cookieHeaderValue) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.InstaProxyService.INSTAPROXY_UPI_PAYMENT_REQUEST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("reqMsgId", reqMsgId);
        getRequestSpecBuilder().addHeader("signature", signature);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        if (cookieHeaderValue != null && !cookieHeaderValue.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", cookieHeaderValue);
        }
    }

    /**
     * Same pattern as {@link com.paytm.api.nativeAPI.InitTxn#InitTxn(com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO)} —
     * configures the call and attaches the request body in one step.
     */
    // AI-Generated: 2026-04-10 - API integration: constructor with request DTO (InitTxn-style)
    public InstaproxyUpiPaymentRequest(String reqMsgId, String signature, String cookieHeaderValue, UpiPaymentRequestDTO dto) {
        this(reqMsgId, signature, cookieHeaderValue);
        setBody(dto);
    }

    // API integration: attach serialized PG2 UPI payment request DTO
    public void setBody(UpiPaymentRequestDTO dto) {
        try {
            getRequestSpecBuilder().setBody(dto.toJson());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize PG2 UPI payment request DTO", e);
        }
    }

    /**
     * Same pattern as {@link com.paytm.api.nativeAPI.InitTxn#executeInitTxn(com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO)} —
     * executes the request, asserts HTTP 200, and deserializes the response body.
     * Uses default {@code reqMsgId}, {@code signature}, and cookie from {@link InstaproxyUpiPaymentPayload#cookieFromConfig()}.
     */
    // AI-Generated: 2026-04-10 - API integration: execute + parse response (InitTxn.executeInitTxn style)
    public static UpiPaymentResponseDTO executeUpiPaymentRequest(UpiPaymentRequestDTO dto) {
        return executeUpiPaymentRequest(dto, "12345", "werwaeq", InstaproxyUpiPaymentPayload.cookieFromConfig());
    }

    /**
     * @param reqMsgId            header {@code reqMsgId}
     * @param signature           header {@code signature}
     * @param cookieHeaderValue   optional {@code Cookie} header (may be empty)
     */
    public static UpiPaymentResponseDTO executeUpiPaymentRequest(UpiPaymentRequestDTO dto, String reqMsgId,
            String signature, String cookieHeaderValue) {
        Response response = new InstaproxyUpiPaymentRequest(reqMsgId, signature, cookieHeaderValue, dto).execute();
        int statusCode = response.statusCode();
        Assertions.assertThat(statusCode).as("Instaproxy UPI payment HTTP status: " + statusCode).isEqualTo(200);
        try {
            return UpiPaymentResponseDTO.fromJson(response.asString());
        } catch (IOException e) {
            Assertions.fail("Change in Instaproxy UPI payment response JSON", e);
            return null;
        }
    }
}
