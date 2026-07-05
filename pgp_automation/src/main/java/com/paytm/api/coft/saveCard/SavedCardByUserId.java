package com.paytm.api.coft.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.TreeMap;

public class SavedCardByUserId extends BaseApi {
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"CC_BILL_PAYMENT\",\n" +
            "        \"tokenType\": \"JWT\",\n" +
            "        \"token\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cyIsInVzZXJJZCI6IjE3MDAwMzU2MzcifQ.QmmzEKiLoNqRUjOlp4SHuYyCwME9BDqtphwnuB7-Kx0\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"userId\": \"1700035637\",\n" +
            "        \"filterTokenCards\":false\n" +
            "    }\n" +
            "}";

public SavedCardByUserId()
{
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_USER_COFT_CARDS);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
}

    public SavedCardByUserId(String xAppId)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_USER_COFT_CARDS);
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
        getRequestSpecBuilder().addHeader("X-APP-RID",xAppId);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public String getRequest() {
        return request;
    }

    public SavedCardByUserId buildRequest(String clientId, String tokenType, String token, String userId) {
        setContext("head.clientId",clientId);
        setContext("head.tokenType", tokenType);
        setContext("head.token", token);
        setContext("body.userId",userId);
        return this;
    }
}
