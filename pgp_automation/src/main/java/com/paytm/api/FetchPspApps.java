package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchPspApps extends BaseApi {

    String body = "{" +
            "\"head\": " +
            "" +
            "{ \"requestTimestamp\": \"1539601338741\", " +
            "\"version\": \"v1\", " +
            "\"channelId\": \"WAP\", " +
            "\"tokenType\": \"SSO\", " +
            "\"token\": \"{ssoToken}\"}, "+
            "\"body\":{\"mid\":\"{mid}\"," +
            "}"+
            "}";

    public String getRequest()
    {
        return body;
    }

    public  void setRequest(String token, Constants.MerchantType mid) {
        body = body.replace("{ssoToken}", token).replace("{mid}", mid.getId());
    }

    public FetchPspApps(Constants.MerchantType mid, String ssotoken)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PSP_APPS);
        setRequest(ssotoken,mid);
        getRequestSpecBuilder().setBody(getRequest());
    }

}
