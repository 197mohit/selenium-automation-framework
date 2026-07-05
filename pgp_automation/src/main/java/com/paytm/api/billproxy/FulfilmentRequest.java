package com.paytm.api.billproxy;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.BillProxy.FULFILMENT_USING_CIN;


public class FulfilmentRequest extends BaseApi{

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"IN\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"uniqueId\": \"136417001649\",\n" +
            "        \"fulfilmentRequestId\": \"136417001649\",\n" +
            "        \"orderId\": \"2653111477\",\n" +
            "        \"cin\": \"2020072717530a2ebbb09d36bac1c63ba066e376f1d12\",\n" +
            "        \"entityName\": \"MOCK\",\n" +
            "        \"amount\": \"1\",\n" +
            "        \"cardHolderName\": \"abc\",\n" +
            "        \"userId\": \"10000360\"\n" +
            "    }\n" +
            "}";


    public  FulfilmentRequest(){

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(FULFILMENT_USING_CIN);
        getRequestSpecBuilder().setBody(getRequest());
    }


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

}