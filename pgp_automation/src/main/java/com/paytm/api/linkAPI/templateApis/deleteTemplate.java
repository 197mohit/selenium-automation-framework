package com.paytm.api.linkAPI.templateApis;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class deleteTemplate extends BaseApi {
    String request="{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v2\",\n" +
            "        \"timestamp\": \"1573635710587\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"signature\": \"xyBhvTOzwLwlT5rIjFLgvMk+2VIYX0fcRCN/juLLqBudHVwLR87Judg0nigyr54EDFimhXnJafAHTTumkEPMUXqLwkj2KSFd7mZIjChlv4U=\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"clientId\": \"UMP\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "\t\t\"templateId\":\"23\",\n" +
            "\t\t\"mid\" : \"JyFuti78224712775813\"\n" +
            "   }\n" +
            "}";
    public deleteTemplate() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.DELETEEMPLATE);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest(){
        return request;
    }
    public deleteTemplate buildRequest(String mid, String templateId){
        setContext("body.mid",mid);
        setContext("body.templateId",templateId);
        return this;
    }
}
