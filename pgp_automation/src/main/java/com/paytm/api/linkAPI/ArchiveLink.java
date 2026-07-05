package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ArchiveLink extends BaseApi {
    String request="{\n" +
            "\"head\": {\n" +
            " \"timestamp\": \"1539601338741\",\n" +
            " \"clientId\": \"78yds87ty7ds\",\n" +
            " \"version\": \"v2\",\n" +
            " \"channelId\": \"WEB\",\n" +
            " \"tokenType\": \"AES\",\n" +
            " \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsaW5rSWQiOiIxNTIyIiwiaXNzIjoidHMiLCJtaWQiOiJhbmtvZmY4ODM3MjM0MjQzMTUyNyJ9.HJGCZKpdfG9NxlCM9BZTcG1vIYYo3-6S1HQ_x7wpqto\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"mid\": \"linksO38457083060859\",\n" +
            "\"linkId\": \"19409\"\n" +
            "}\n" +
            "}";
    public ArchiveLink() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.ARCHIVE_LINK);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest(){
        return request;
    }
    public ArchiveLink buildRequest(String mid, String linkId){
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        return this;
    }
}
