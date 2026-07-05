package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SummaryLink extends BaseApi {

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
            "    \"mid\": \"Online97162038260814\",\n" +
            "    \"fromDate\" : \"14/07/2022\",\n" +
            "    \"toDate\" : \"18/07/2022\",\n" +
            "    \"allowArchived\" : 0\n" +
            "  }\n" +
            "}";

    public SummaryLink() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.SUMMARY_LINK);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest(){
        return request;
    }

    public SummaryLink buildRequest(String mid, String fromDate,String toDate){
        setContext("body.mid",mid);
        setContext("body.fromDate",fromDate);
        setContext("body.toDate",toDate);
        return this;
    }
}
