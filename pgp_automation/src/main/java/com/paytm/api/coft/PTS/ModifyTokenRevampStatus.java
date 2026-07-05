package com.paytm.api.coft.PTS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 27/05/22 10:01 AM
 * @Version 1.0
 */
public class ModifyTokenRevampStatus extends BaseApi {
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"1644587731428\",\n" +
            "         \"requestId\": \"merchre000001\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXF1ZXN0VGltZXN0YW1wIjoiMTY0NDU4NzczMTQyOCIsInJlcXVlc3RJZCI6ImZkMTljYmYxLWY1OTQtNGE2ZS1hODNhLWY5NzlkYjYwY2I0MCIsIm1pZCI6IlBBWVRNMTk3IiwidmVyc2lvbiI6InYxIn0.ipa6wAtYS-xdsderoaaDS5rRQZRvEC03l3YDOD-rutk\",\n" +
            "         \"signatureType\": \"CHECKSUM\",\n" +
            "         \"clientId\": \"PAYTM_THEIA\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"requestedBy\": \"MERCHANT\",\n" +
            "        \"reason\": \"Customer wants to delete the token\",\n" +
            "        \"panUniqueReference\": \"\",\n" +
            "        \"isModifyAll\": true,\n" +
            "        \"userInfo\": {\n" +
            "            \"custId\": \"delTknRev93\",\n" +
            "            \"userId\": \"1002293943\"\n" +
            "        },\n" +
            "        \"reasonCode\": \"CUSTOMER_CONFIRMED\",\n" +
            "        \"tokenStatus\": \"DEAD\",\n" +
            "        \"tokenIndexNumber\": \"627d1b5892166d7437f89d2c\"\n" +
            "    }\n" +
            "}";

    public ModifyTokenRevampStatus(String mid) {
        setMethod(MethodType.PUT);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.MODIFY_TOKENS);
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("requestId", UUID.randomUUID().toString());
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public String getRequest() {
        return request;
    }

    public ModifyTokenRevampStatus buildRequest(String requestedBy,String reasonCode,String tokenStatus, String tokenIndexNumber, String custId, String userId, String panUniqueReference, boolean isModifyAll ) {
        setContext("body.requestedBy", requestedBy);
        setContext("body.reasonCode", reasonCode);
        setContext("body.tokenStatus", tokenStatus);
        setContext("body.tokenIndexNumber", tokenIndexNumber);
        setContext("body.userInfo.custId", custId);
        setContext("body.userInfo.userId", userId);
        setContext("body.panUniqueReference", panUniqueReference);
        setContext("body.isModifyAll", isModifyAll);
        return this;
    }

    public ModifyTokenRevampStatus generateChecksum(String key) {
        Gson gson = new Gson();
        String requestBody = gson.toJson(getRequestBody());
        JsonObject tokenizeCardRequest = gson.fromJson(requestBody, JsonObject.class);
        String bodyForChecksum = tokenizeCardRequest.get("body").toString();
        String checksum = PGPUtil.getChecksum(key, bodyForChecksum);
        setContext("head.signature",checksum);
        setContext("head.signatureType","CHECKSUM");
        return this;
    }
}
