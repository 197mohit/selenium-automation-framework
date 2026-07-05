package com.paytm.api.Deals;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.io.UnsupportedEncodingException;

public class GetPaymentStatus extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"cart\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"Time\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"JWT\",\n" +
            "        \"signature\": \"\",\n" +
            "        \"token\":\"{{jwt}}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"{{mid}}\",\n" +
            "        \"orderId\": \"{{orderId}}\"\n" +
            "    }\n" +
            "}";

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public GetPaymentStatus() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.PGPAPIResourcePath.GET_PAYMENT_STATUS);
    }

    public GetPaymentStatus buildWithParameters(String mid, String orderId) throws UnsupportedEncodingException {
        String token = PGPHelpers.createTokenForGetPaymentStatusAPI(mid, orderId);
        setContext("head.token",token);
        setContext("body.mid",mid);
        setContext("body.orderId",orderId);
        getRequestSpecBuilder().setBody(getRequest());
        return this;
    }
}
