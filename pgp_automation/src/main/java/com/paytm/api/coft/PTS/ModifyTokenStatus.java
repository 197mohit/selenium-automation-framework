package com.paytm.api.coft.PTS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.time.Instant;
import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 07/01/22 06:02 PM
 * @Version 1.0
 */
public class ModifyTokenStatus extends BaseApi {
    String requestId = UUID.randomUUID().toString();

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"1638349002239\",\n" +
            "        \"requestId\": \"fd19cbf1-f594-4a6e-a83a-f979db60cb40\",\n" +
            "        \"signature\": \"80g8OXf4oTfC8F+nEaWmiGY/SYIcFrTeLfklNdpMZ/wV/mUobUGBSnqOSVfcHIyfYTlD13FJ0wIi/jHts3JuqjrfOQM4LNWNhyPh8PWAItQ=\",\n" +
            "        \"signatureType\": \"CHECKSUM\",\n" +
            "        \"clientId\": \"PAYTM_THEIA\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"requestedBy\": \"TOKEN_REQUESTOR\",\n" +
            "        \"reason\": \"Customerwantstodeletethetoken\",\n" +
            "        \"userInfo\": {\n" +
            "            \"custId\": \"VmwGpbmFhe\",\n" +
            "            \"userId\": \"1002055843\"\n" +
            "        },\n" +
            "        \"reasonCode\": \"CUSTOMER_CONFIRMED\",\n" +
            "        \"tokenStatus\": \"DEAD\",\n" +
            "        \"tokenIndexNumber\": \"6373a30edc40bb6ad8769094\"\n" +
            "    }\n" +
            "}";

    public ModifyTokenStatus(String mid) {
        setMethod(MethodType.PUT);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.MODIFY_TOKEN_STATUS);
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("requestId", UUID.randomUUID().toString());
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }

    public String getRequest() {
        return request;
    }

    public ModifyTokenStatus buildRequest(String requestedBy,String reasonCode,String tokenStatus, String tokenIndexNumber, String custId, String userId) {
        setContext("body.requestedBy", requestedBy);
        setContext("body.reasonCode", reasonCode);
        setContext("body.tokenStatus", tokenStatus);
        setContext("body.tokenIndexNumber", tokenIndexNumber);
        setContext("body.userInfo.custId", custId);
        setContext("body.userInfo.userId", userId);
        return this;
    }

    public ModifyTokenStatus generateChecksum(String key) {
        Gson gson = new Gson();
        String requestBody = gson.toJson(getRequestBody());
        JsonObject tokenizeCardRequest = gson.fromJson(requestBody, JsonObject.class);
        String bodyForChecksum = tokenizeCardRequest.get("body").toString();
        String checksum = PGPUtil.getChecksum(key, bodyForChecksum);
        setContext("head.signature",checksum);
        setContext("head.signatureType","CHECKSUM");
        return this;
    }
    public ModifyTokenStatus createJwt(String mid)
    {
        String requestTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        String jwtToken = SavedCardHelpersNew.jwtSignatureTokenizeMidCustId(requestId,mid,requestTimeStamp);
        setContext("head.requestId",requestId);
        setContext("head.signature", jwtToken);
        setContext("head.signatureType", "JWT");
        setContext("head.requestTimestamp",requestTimeStamp);
        return this;
    }

}
