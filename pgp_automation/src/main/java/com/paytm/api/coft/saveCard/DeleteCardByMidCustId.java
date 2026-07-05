package com.paytm.api.coft.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class DeleteCardByMidCustId extends BaseApi {
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJtaWQiOiJlMWoyaGwzODc1MTY4MTg4NjEyOSIsImN1c3RJZCI6ImN1c3Q1IiwiY2FyZElkIjoiMjAyMDEwMTEwMTQ1MGRiZWMzYmE5ZDc4YmNjMjIyMzRlMzIxMGExNTdhNjE3IiwicmVxdWVzdGVkQnkiOiJDQVJESE9MREVSIiwicmVhc29uIjoiQ3VzdG9tZXIgd2FudHMgdG8gZGVsZXRlIHRoZSB0b2tlbiIsInJlYXNvbkNvZGUiOiJDVVNUT01FUl9DT05GSVJNRUQiLCJpc3MiOiJ0cyJ9.aZVOVeJZe2bIbt2eeI0mgN7qCPcCITJ0cvZ5tVl_Czo\",\n" +
            "        \"tokenType\": \"JWT\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"e1j2hl38751681886129\",\n" +
            "        \"custId\": \"cust5\",\n" +
            "        \"cardId\": \"2020101101450dbec3ba9d78bcc22234e3210a157a617\",\n" +
            "        \"requestedBy\": \"CARDHOLDER\",\n" +
            "        \"reason\": \"Customer wants to delete the token\",\n" +
            "        \"reasonCode\": \"CUSTOMER_CONFIRMED\"\n" +
            "    }\n" +
            "}";

    public DeleteCardByMidCustId (String mid,String custId)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_CARD_DELETE);
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
        getRequestSpecBuilder().addQueryParam("mid",mid);
        getRequestSpecBuilder().addQueryParam("custId",custId);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public String getRequest() {
        return request;
    }

    public DeleteCardByMidCustId buildRequest(String mid, String custId,String cardId,String tokenType,String JWT) {
        setContext("head.tokenType", tokenType);
        setContext("head.token", JWT);
        setContext("body.mid",mid);
        setContext("body.custId",custId);
        setContext("body.cardId",cardId);
        setContext("body.requestedBy","CARDHOLDER");
        setContext("body.reason","Customer wants to delete the token");
        setContext("body.reasonCode","CUSTOMER_CONFIRMED");
        return this;
    }
}
