package com.paytm.api.billproxy;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.BillProxy.BILL_PROXY_URI_PREFIX;
import static com.paytm.appconstants.Constants.BillProxy.CARD_DETAILS_USING_SSO;
import static com.paytm.appconstants.Constants.BillProxy.SSO_REQUEST_USING_TIN;


public class CheckCardDetailsUsingSSORequest extends BaseApi{

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"IN\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"cardNumber\": \"5506900480000016\"\n" +
            "    }\n" +
            "}";

    String requestWithTIN = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"IN\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1637579151424\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"tin\": \"{tin}\",\n" +
            "        \"userId\": \"{user}\"\n" +
            "    }\n" +
            "}";

    public  CheckCardDetailsUsingSSORequest(String ssoToken, String tin, String user) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(SSO_REQUEST_USING_TIN);
        getRequestSpecBuilder().addHeader("ssoToken", ssoToken);
        requestWithTIN = requestWithTIN.replace("{tin}",tin).replace("{user}",user);
        System.out.println(requestWithTIN);
        getRequestSpecBuilder().setBody(getRequestWithTIN());
    }

    public String getRequestWithTIN() {
        return requestWithTIN;
    }
    public void setRequestWithTIN(String requestWithTIN) {
        this.requestWithTIN = requestWithTIN;
    }

    public  CheckCardDetailsUsingSSORequest(String ssoToken){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CARD_DETAILS_USING_SSO);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("ssoToken", ssoToken);
    }


    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

}