package com.paytm.api.coft.PTS;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 06/01/22 05:02 PM
 * @Version 1.0
 */
public class FetchPendingTokens extends BaseApi {

    String request = "{\n" +
            "  \"head\": {\n" +
            "    \"version\": \"v1\",\n" +
            "    \"requestTimestamp\": \"1544614590000\",\n" +
            "    \"requestId\": \"merchreq000001\",\n" +
            "    \"signature\": \"{signature}\",\n" +
            "    \"signatureType\": \"JWT\",\n" +
            "    \"clientId\" : \"PAYTM_THEIA\"\n" +
            "  },\n" +
            "\"body\": {\n" +
            "     \"userId\" : \"1002055843\"\n" +
            "   }\n" +
            "}";

    public FetchPendingTokens() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.FETCH_PENDING_TOKENS);
        getRequestSpecBuilder().addQueryParam("requestId", UUID.randomUUID().toString());
        getRequestSpecBuilder().setBody(getRequest());
    }

    public String getRequest() {
        return request;
    }

    public FetchPendingTokens buildRequest(String requestId,String signature, String userId,
        String timeStamp) {
        setContext("body.userId", userId);
        setContext("head.requestId", requestId);
        setContext("head.signature",signature);
        setContext("head.requestTimestamp",timeStamp);
        return this;
    }
}
