package com.paytm.api.notification;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class LoyalityPointsNotify extends BaseApi {

    String request = "{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"version\": \"1.1.1\",\n" +
            "            \"function\": \"alipayplus.promotion.loyalitypoints.expiryNotify\",\n" +
            "            \"clientId\": \"PAYTW3IN51\",\n" +
            "            \"reqTime\": \"2020-09-10T14:06:38+05:30\",\n" +
            "            \"reqMsgId\": \"296e621b-95f9-45f2-bb19-67ae7a3f8900\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"notificationDataList\": [\n" +
            "                {\n" +
            "                    \"category\": \"PRE_EXPIRY\",\n" +
            "                    \"extendInfo\": \"\",\n" +
            "                    \"notificationPlaceholders\": {\n" +
            "                        \"expiryPoints\": \"30\",\n" +
            "                        \"expiryDate\": \"Sun May 30 23:59:59 CST 2021\",\n" +
            "                        \"availableBalancePoints\":\"1200\"\n" +
            "                    },\n" +
            "                    \"userId\": \"1000703698\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"3e3cc008683405bc0863289eac2bee52e3f835600dc95fd5732c1916b0d4e4d9\"\n" +
            "}";

    public LoyalityPointsNotify() {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.LOYALITY_POINTS_EXPIRY);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");

    }

    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }

}




