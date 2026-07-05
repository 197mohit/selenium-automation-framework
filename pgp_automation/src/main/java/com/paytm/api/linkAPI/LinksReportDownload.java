package com.paytm.api.linkAPI;

import com.paytm.apphelpers.CommonHelpers;
import com.paytm.framework.api.BaseApi;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.Date;
import java.util.List;

public class LinksReportDownload extends BaseApi {
    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhbW91bnQiOiIyMy4wMCIsImlzcyI6IlVNUCIsIm1heFBheW1lbnRzQWxsb3dlZCI6IjEwIiwibWlkIj\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "         \"mid\": \"testli61258254741921\",\n" +
            "    \"linkType\": [\"FIXED\"],\n" +
            "    \"searchStartDate\" : \"14/01/2021\",\n" +
            "    \"searchEndDate\" : \"12/02/2021\"\n" +
            "    }\n" +
            "}";

    public LinksReportDownload() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.LINKS_REPORT_DOWNLOAD);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        System.out.println("inside build request...");
    }
    public String getRequest() {
        return request;
    }
    public  LinksReportDownload buildRequest(String mid, List<String> linkType, String searchStartDate, String searchEndDate) {
        setContext("body.mid",mid);
        setContext("body.linkType",linkType);
        setContext("body.searchStartDate",searchStartDate);
        setContext("body.searchEndDate",searchEndDate);
        return this;
    }
}
