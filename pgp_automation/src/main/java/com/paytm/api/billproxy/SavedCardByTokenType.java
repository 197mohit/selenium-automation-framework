package com.paytm.api.billproxy;

import com.paytm.LocalConfig;
import com.paytm.api.coft.saveCard.SavecardServicegetGcinV1;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

public class SavedCardByTokenType extends BaseApi {

    String request = "{\n" +
            "  \"head\": {\n" +
            "    \"clientId\": \"UBER\",\n" +
            "    \"version\": \"v1\",\n" +
            "    \"requestTimestamp\": \"Time\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"SSO\",\n" +
            "    \"token\": \"992b8cab-df81-45e8-b8a3-15c8c3626600\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"userId\": \"1001873853\"\n" +
            "  }\n" +
            "}";

    public SavedCardByTokenType() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.BillProxy.SAVED_CARD_BY_TOKEN_TYPE);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public SavedCardByTokenType(String xAppId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.BillProxy.SAVED_CARD_BY_TOKEN_TYPE);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
        getRequestSpecBuilder().addHeader("X-APP-RID",xAppId);
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public SavedCardByTokenType buildRequest(String tokenType, User user) {
        if (tokenType.equalsIgnoreCase("SSO")) {
            setContext("head.tokenType", tokenType);
            setContext("head.token",user.ssoToken());
            setContext("body.userId", user.custId());

        } else if (tokenType.equalsIgnoreCase("JWT")) {
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("userId", user.custId());
            tokenMap.put("tokenType",tokenType);
            String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
            setContext("head.tokenType", tokenType);
            setContext("head.token",jwt);
            setContext("body.userId", user.custId());
        }
        return this;
    }

}