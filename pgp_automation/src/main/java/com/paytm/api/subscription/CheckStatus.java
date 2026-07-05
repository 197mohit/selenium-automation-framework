package com.paytm.api.subscription;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CheckStatus extends BaseApi {
    String checkStatusRequest="{\n" +
            "    \"head\":{\n" +
            "            \"version\" : \"v1\",\n" +
            "            \"timestamp\" : \"123456789345\",\n" +
            "            \"signature\" : \"NrF6w+PHS5hG8bqdJCekG31kKZVPuUcFxPbcPgir5pfyNm6dDkfVf42wfRJu+y6doH4bKJnDDMFBNYX0vTyriGLkOJ0/4kCRD3/MTdtz5So=\",\n" +
            "            \"tokenType\" : \"AES\",\n" +
            "            \"clientId\"  : \"1234\"\n" +
            "        },\n" +
            "         \"body\": {\n" +
            "           \"orderId\":\"{Order_ID}\"," +
            "           \"mid\":\"{MID}\" , " +
            "           \"subsId\" : \"{SUBS_ID}\" }\n" +
            "}";

    public CheckStatus setMID(String mid){
        setContext("body.mid",mid);
        return this;
    }

    public CheckStatus setOrderID(String orderID) {
        setContext("body.orderId",orderID);
        return this;
    }

    public CheckStatus setSubsId(String subsId) {
        setContext("body.subsId",subsId);
        return this;
    }
    public CheckStatus() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.SubscriptionService.SUBSCRIPTION_CHECK_STATUS);
    }
    public String getRequest() {return checkStatusRequest;}
}