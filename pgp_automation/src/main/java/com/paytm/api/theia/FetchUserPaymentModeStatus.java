package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.List;

public class FetchUserPaymentModeStatus extends BaseApi {
    String request= "{\n" +
            "    \"head\": {\n" +
            "        \"tokenType\": \"{TokenType}\",\n" +
            "        \"token\": \"{Token}\"\n" +
            "    },\n" +
            "    \"body\":{\"mid\":\"{MID}\",\"mobileNo\":\"{MobNo}\",\"paymentMode\":[\"{PayModes}\"]}\n" +
            "}";


    public FetchUserPaymentModeStatus() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.FETCH_USER_PAYMENT_MODES);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }

    public FetchUserPaymentModeStatus buildRequest(String TokenType, String Token, String MID, String MobNo, List PayModes){
        setContext("head.tokenType", TokenType);
        setContext("head.token", Token);
        setContext("body.mid", MID);
        setContext("body.mobileNo", MobNo);
        setContext("body.paymentMode", PayModes);
        return this;
    }
}
