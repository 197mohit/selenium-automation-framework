package com.paytm.api.coft.PTS;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.coftCenter;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchAll extends BaseApi {
    String request = "{\n" +
            "    \"targetType\": \"TARGET_TYPE\",\n" +
            "    \"userId\": \"userId\",\n" +
            "    \"merchantId\": \"merchantId\",\n" +
            "    \"externalUserId\": \"externalUserId\",\n" +
            "    \"includeExpiredTokens\": false,\n" +
            "    \"includeExpiredCards\": false\n" +
            "}";

    public FetchAll() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.CARD_CENTER);
        getRequestSpecBuilder().setBasePath(coftCenter.FETCH_ALL_V2);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
        getRequestSpecBuilder().addHeader("requestId", "1234");
        getRequestSpecBuilder().addHeader("client-id",LocalConfig.CARD_SERVICE_THEIA_CLIENT_ID);
        getRequestSpecBuilder().addHeader("client-id",LocalConfig.CARD_SERVICE_THEIA_CLIENT_ID);
        getRequestSpecBuilder().addHeader("client-token",LocalConfig.CARD_SERVICE_THEIA_CLIENT_ID);}

    public String getRequest() {
        return request;
    }

    public FetchAll buildRequest(String targetType,String merchantId, String externalUserId) {
        setContext("targetType", targetType);
        setContext("merchantId", merchantId);
        setContext("externalUserId",externalUserId);
        deleteContext("userId");
        return this;
    }

    public FetchAll buildRequest(String targetType,String userId) {
        setContext("targetType", targetType);
        setContext("userId", userId);
        deleteContext("merchantId");
        deleteContext("externalUserId");
        return this;
    }

}
