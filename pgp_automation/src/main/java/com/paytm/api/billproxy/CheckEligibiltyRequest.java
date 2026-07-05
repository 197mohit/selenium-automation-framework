package com.paytm.api.billproxy;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.BillProxy.ELIGIBILITY_USING_CIN;


public class CheckEligibiltyRequest extends BaseApi{

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"IN\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"eligibilityRequestId\": \"61a10a955dc2d36cb0eb0e3c\",\n" +
            "        \"cin\": \"2020072717530a2ebbb09d36bac1c63ba066e376f1d12\",\n" +
            "        \"entityName\": \"MOCK\",\n" +
            "        \"userId\": \"1000036031\"\n" +
            "    }\n" +
            "}";


    public CheckEligibiltyRequest(){

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ELIGIBILITY_USING_CIN);
        getRequestSpecBuilder().setBody(getRequest());
    }


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

}