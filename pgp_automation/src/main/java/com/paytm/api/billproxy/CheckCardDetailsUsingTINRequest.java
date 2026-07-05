package com.paytm.api.billproxy;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.BillProxy.CARD_DETAILS_USING_TIN;
import static com.paytm.appconstants.Constants.BillProxy.SSO_REQUEST_USING_TIN;


public class CheckCardDetailsUsingTINRequest extends BaseApi{

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"IN\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"tin\": \"61a10a955dc2d36cb0eb0e3c\",\n" +
            "        \"userId\": \"1000711378\"\n" +
            "    }\n" +
            "}";


    public  CheckCardDetailsUsingTINRequest(){

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CARD_DETAILS_USING_TIN);
        getRequestSpecBuilder().setBody(getRequest());
    }


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    String requestWithTIN = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"IN\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"{signature}\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"tin\": \"{tin}\",\n" +
            "        \"userId\": \"{user}\"\n" +
            "    }\n" +
            "}";

    public  CheckCardDetailsUsingTINRequest(String ssoToken, String tin, String user, String signature) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CARD_DETAILS_USING_TIN);
        getRequestSpecBuilder().addHeader("ssoToken", ssoToken);
        System.out.println(tin + user + signature);
        requestWithTIN = requestWithTIN.replace("{tin}",tin).replace("{user}",user).replace("{signature}",signature);
        System.out.println(requestWithTIN);
        getRequestSpecBuilder().setBody(getRequestWithTIN());
    }
    public String getRequestWithTIN() {
        return requestWithTIN;
    }
    public void setRequestWithTIN(String requestWithTIN) {
        this.requestWithTIN = requestWithTIN;
    }

    public CheckCardDetailsUsingTINRequest(String tin){
    }
}