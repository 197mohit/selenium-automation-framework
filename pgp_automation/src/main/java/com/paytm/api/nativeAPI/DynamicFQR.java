package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.api.PaymentService;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import java.time.Instant;

public class DynamicFQR extends BaseApi {

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
            "   \"qrCodeId\":\"{qrId}\",\n" +
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

    public void setRequest(Constants.MerchantType mid, String sso, String payMethod, String accRefId, String upiLiteBalance) {
        this.request = request
                .replace("{Time}",String.valueOf(Instant.now().toEpochMilli()))
                .replace("{sso}",sso)
                .replace("{MID}",mid.getId())
                .replace("{qrId}",setQrCodeId(mid))
                .replace("{payMethod}",payMethod)
                .replace("{accRefId}",accRefId)
                .replace("{value}",upiLiteBalance);
    }
    public DynamicFQR(Constants.MerchantType mid, String sso, String client, String version, String payMethod, String accRefId, String upiLiteBalance, String uniqueId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        getRequestSpecBuilder().addQueryParam("client", client);
        getRequestSpecBuilder().addQueryParam("version", version);
        getRequestSpecBuilder().addQueryParam("appVersion", version);
        getRequestSpecBuilder().addHeader("X-PGP-Unique-ID", uniqueId);
        setRequest(mid,sso,payMethod,accRefId,upiLiteBalance);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public static String setQrCodeId(Constants.MerchantType mid)
    {
        String Oid = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(mid, "1.00", Oid);
        String qrCodeId = paymentService.execute().then().statusCode(200).extract().jsonPath().getString("body.qrCodeId");
        return qrCodeId;
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
            "   \"qrCodeId\":\"{qrId}\",\n" +
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

    public void setRequest1(Constants.MerchantType mid, String sso, String upiLiteBalance) {
        this.request1 = request1
                .replace("{Time}",String.valueOf(Instant.now().toEpochMilli()))
                .replace("{sso}",sso)
                .replace("{MID}",mid.getId())
                .replace("{qrId}",setQrCodeId(mid))
                .replace("{value}",upiLiteBalance);
    }
    public DynamicFQR(Constants.MerchantType mid, String sso, String client, String version, String upiLiteBalance, String uniqueId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2);
        getRequestSpecBuilder().addQueryParam("client", client);
        getRequestSpecBuilder().addQueryParam("version", version);
        getRequestSpecBuilder().addQueryParam("appVersion", version);
        getRequestSpecBuilder().addHeader("X-PGP-Unique-ID", uniqueId);
        setRequest1(mid,sso,upiLiteBalance);
        getRequestSpecBuilder().setBody(getRequest1());
    }


}
