package com.paytm.api.subscription;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SubscriptionCancel extends BaseApi {
    String cancelRequest="{\n" +
            "    \"head\":{\n" +
            "            \"signature\" : \"LKGluwgzijYxwP4wPTTO8qz7xRtwL+rlyfmqNIy4xLDtngW2V1U8mIy143kiaaZQuR9CSV4yVHi04aJxLFz9Q+AhW9ZEvrEt+0zGjuOWXg=\",\n" +
            "            \"tokenType\" : \"AES\",\n" +
            "            \"clientId\"  : \"C11\"\n" +
            "        },\n" +
            "         \"body\": {\n" +
            "           \"mid\":\"{MID}\" , " +
            "           \"subsId\" : \"{SUBS_ID}\" }\n" +
            "}";

    public SubscriptionCancel setMID(String mid){
        setContext("body.mid",mid);
        return this;
    }

    public SubscriptionCancel setSubsId(String subsId) {
        setContext("body.subsId",subsId);
        return this;
    }

    public SubscriptionCancel() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.SubscriptionService.SUBSCRIPTION_CANCEL);
    }

    public String getRequest() {return cancelRequest;}
}
