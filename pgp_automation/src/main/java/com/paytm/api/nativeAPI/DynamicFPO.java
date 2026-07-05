package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import java.time.Instant;

public class DynamicFPO extends BaseApi {

    String request = "{\n" +
            "\"head\":{\n" +
            "   \"version\":\"v2\",\n" +
            "   \"channelId\":\"APP\",\n" +
            "   \"tokenType\":\"SSO\",\n" +
            "   \"requestTimestamp\":\"{Time}\",\n" +
            "   \"token\":\"{sso}\",\n" +
            "   \"requestId\":\"Test1128\"\n" +
            "},\n" +
            "\"body\":\n" +
            "{\n" +
            "   \"twoFADetails\": {\n" +
            "            \"twoFARequired\": false,\n" +
            "            \"txnType\": \"P2M\"\n" +
            "        },\n" +
            "   \"mid\":\"{MID}\",\n" +
            "   \"generateOrderId\": true,\n" +
            "   \"isLiteEligible\": true,\n" +
            "   \"isOffline\":true,\n" +
            "   \"recentPayMode\": {\n" +
            "            \"payMethod\": \"{payMethod}\",\n" +
            "            \"accRefId\": \"{accRefId}\",\n" +
            "            \"isAddNdPayRecentPayMode\":false\n" +
            "        },\n" +
            "   \"upiLiteBalance\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": \"{value}\"\n" +
            "        },\n" +
            "   \"isPayInstrumentPriorityRequired\": true\n" +
            "}\n" +
            "}";

    public String getRequest()
    {
        return request;
    }

    public void setRequest(String mid, String sso, String payMethod, String accRefId, String upiLiteBalance) {
        this.request = request
                .replace("{Time}",String.valueOf(Instant.now().toEpochMilli()))
                .replace("{sso}",sso)
                .replace("{MID}",mid)
                .replace("{payMethod}",payMethod)
                .replace("{accRefId}",accRefId)
                .replace("{value}",upiLiteBalance);
    }
    public DynamicFPO(Constants.MerchantType mid, String sso, String client, String version, String payMethod, String accRefId, String upiLiteBalance, String uniqueId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2);
        getRequestSpecBuilder().addQueryParam("mid",mid.getId());
        getRequestSpecBuilder().addQueryParam("client", client);
        getRequestSpecBuilder().addQueryParam("version", version);
        getRequestSpecBuilder().addHeader("X-PGP-Unique-ID", uniqueId);
        setRequest(mid.getId(),sso,payMethod,accRefId,upiLiteBalance);
        getRequestSpecBuilder().setBody(getRequest());
    }

    String request1 = "{\n" +
            "\"head\":{\n" +
            "   \"version\":\"v2\",\n" +
            "   \"channelId\":\"APP\",\n" +
            "   \"tokenType\":\"SSO\",\n" +
            "   \"requestTimestamp\":\"{Time}\",\n" +
            "   \"token\":\"{sso}\",\n" +
            "   \"requestId\":\"Test1128\"\n" +
            "},\n" +
            "\"body\":\n" +
            "{\n" +
            "   \"twoFADetails\": {\n" +
            "            \"twoFARequired\": false,\n" +
            "            \"txnType\": \"P2M\"\n" +
            "        },\n" +
            "   \"mid\":\"{MID}\",\n" +
            "   \"generateOrderId\": true,\n" +
            "   \"isLiteEligible\": true,\n" +
            "   \"isOffline\":true,\n" +
            "   \"upiLiteBalance\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": \"{value}\"\n" +
            "        },\n" +
            "   \"isPayInstrumentPriorityRequired\": true\n" +
            "}\n" +
            "}";

    public String getRequest1()
    {
        return request1;
    }

    public void setRequest1(String mid, String sso,  String upiLiteBalance) {
        this.request1 = request1
                .replace("{Time}",String.valueOf(Instant.now().toEpochMilli()))
                .replace("{sso}",sso)
                .replace("{MID}",mid)
                .replace("{value}",upiLiteBalance);
    }
    public DynamicFPO(Constants.MerchantType mid, String sso, String client, String version, String upiLiteBalance, String uniqueId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2);
        getRequestSpecBuilder().addQueryParam("mid",mid.getId());
        getRequestSpecBuilder().addQueryParam("client", client);
        getRequestSpecBuilder().addQueryParam("version", version);
        getRequestSpecBuilder().addHeader("X-PGP-Unique-ID", uniqueId);
        setRequest1(mid.getId(),sso,upiLiteBalance);
        getRequestSpecBuilder().setBody(getRequest1());
    }
}
