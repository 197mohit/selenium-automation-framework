package com.paytm.api.linkAPI.templateApis;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchTemplate extends BaseApi {
    String request = "{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"LINK_UI\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"JWT\",\n" +
            "    \"signature\": \"jhddyt87td87vd\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "        \"mid\": \"testli24296717753023\"\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "  \n" +
            "}";
    public FetchTemplate() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FETCHTEMPLATE);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest(){
        return request;
    }
    public FetchTemplate buildRequest(String mid){
        setContext("body.mid",mid);
        return this;
    }
}
