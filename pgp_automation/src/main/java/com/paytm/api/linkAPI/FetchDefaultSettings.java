package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchDefaultSettings extends BaseApi {

    String request="{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"78yds87ty7ds\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"JWT\",\n" +
            "    \"signature\": \"jhddyt87td87vd\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"mid\": \"Online97162038260814\"\n" +
            "  }\n" +
            "}";

    public FetchDefaultSettings() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FETCH_DEFAULT_SETTINGS);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest(){
        return request;
    }

    public FetchDefaultSettings buildRequest(String mid){
        setContext("body.mid",mid);
        return this;
    }
}
