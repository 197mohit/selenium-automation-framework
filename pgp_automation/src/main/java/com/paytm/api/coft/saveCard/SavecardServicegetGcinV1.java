package com.paytm.api.coft.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SavecardServicegetGcinV1 extends BaseApi {

    String request = "{\n" +
            "  \"head\": {\n" +
            "    \"clientId\": \"C11\",\n" +
            "    \"version\": \"v1\",\n" +
            "    \"requestTimestamp\": \"Time\",\n" +
            "    \"channelId\": \"WAP\",\n" +
            "    \"tokenType\": \"ACCESS_TOKEN\",\n" +
            "    \"token\": \"68cd1a8654dc4bc49e901d548ee8dbb41681115109226\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"userId\": \"1001873853\",\n" +
            "    \"cardNo\":\"5123450000000008\"\n" +
            "\n" +
            "  }\n" +
            "}";

    public SavecardServicegetGcinV1 ()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVECARD_SERVICE_GET_GCIN);
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
        getRequestSpecBuilder().setBody(getRequest());
    }

    public String getRequest() {
        return request;
    }

    public SavecardServicegetGcinV1 buildRequest(String token, String userId,String cardNumber) {
        setContext("head.token", token);
        setContext("body.userId",userId);
        setContext("body.cardNo",cardNumber);
        return this;
    }
}
