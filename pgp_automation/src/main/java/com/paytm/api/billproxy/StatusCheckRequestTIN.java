package com.paytm.api.billproxy;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.BillProxy.STATUS_QUERY_USING_TIN;


public class StatusCheckRequestTIN extends BaseApi{

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"IN\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"uniqueId\": \"7984549753\",\n" +
            "        \"statusRequestId\": \"7984549753\",\n" +
            "        \"orderId\": \"2653111503\",\n" +
            "        \"tin\": \"61a10a955dc2d36cb0eb0e3c\",\n" +
            "        \"entityName\": \"MOCK\",\n" +
            "        \"amount\": \"1\",\n" +
            "        \"cardHolderName\": \"abc\",\n" +
            "        \"userId\": \"1000711378\"\n" +
            "    }\n" +
            "}";


    public StatusCheckRequestTIN(){

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(STATUS_QUERY_USING_TIN);
        getRequestSpecBuilder().setBody(getRequest());
    }


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

}