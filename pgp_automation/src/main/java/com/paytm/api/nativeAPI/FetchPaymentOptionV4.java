package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchPaymentOptionV4 extends BaseApi {

    String body= "{\n" +
            "\"head\":{\n" +
            "   \"version\":\"v4\",\n" +
            "   \"channelId\":\"WEB\",\n" +
            "   \"requestTimestamp\":\"1525344042691\",\n" +
            "   \"clientId\":\"supergw\"\n" +
            "},\n" +
            "\"body\":\n" +
            "{\n" +
            "   \"mid\":\"{MID}\",\n" +
            "   \"deviceId\":\"\",\n" +
            "   \"enablePaymentMode\":null,\n" +
            "   \"disablePaymentMode\":null,\n" +
            "   \"paytmSsoToken\":\"\",\n" +
            "   \"requestype\":\"DEFAULT\",\n" +
            "   \"txnAmount\":\"100\",\n" +
            "   \"subscriptionInfo\":\"\",\n" +
            "   \"productCode\":\"\",\n" +
            "   \"userDetails\": {\n" +
            "            \"id\": \"1000647342\",\n" +
            "            \"mobile\": \"7007101778\"\n" +
            "        },\n" +
            "   \"extendIndo\":\"\"\n" +
            "}\n" +
            "}";

    public String getRequest()
    {
        return body;
    }

    public  FetchPaymentOptionV4 setRequest(String mid) {
        this.body = body.replace("{MID}",mid);
        return this;
    }
    public FetchPaymentOptionV4(String mid, String referenceId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V4);
        getRequestSpecBuilder().addQueryParam("mid",mid);
        getRequestSpecBuilder().addQueryParam("referenceId", referenceId);
        getRequestSpecBuilder().addHeader("JWT_TOKEN","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJtaWQiOiJlcURqSVA0ODQ1NzA4MDYwMjI4NCIsImRldmljZUlkIjoiIiwiZW5hYmxlUGF5bWVudE1vZGUiOm51bGwsImRpc2FibGVQYXltZW50TW9kZSI6bnVsbCwicGF5dG1Tc29Ub2tlbiI6IiIsInJlcXVlc3R5cGUiOiJERUZBVUxUIiwidHhuQW1vdW50IjoiMTAwIiwic3Vic2NyaXB0aW9uSW5mbyI6IiIsInByb2R1Y3RDb2RlIjoiIiwidXNlckRldGFpbHMiOnsiaWQiOiIxMDAwNjQ3MzQyIiwibW9iaWxlIjoiNzAwNzEwMTc3OCJ9LCJleHRlbmRJbmRvIjoiIiwiaXNzIjoic3VwZXJndyJ9.F0M1XFY5OrCbKudIB6Zu2g1PKmfcroEvGcGE0U6qqO4");
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setRequest(mid);
        getRequestSpecBuilder().setBody(getRequest());
    }
}
