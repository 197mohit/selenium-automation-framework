package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import java.io.UnsupportedEncodingException;

public class AxisBankMockApi extends BaseApi {

    static String request="{\n" +
            "    \"callbackUrl\": \"{callbackUrl}\",\n" +
            "    \"merchChanId\": \"NEHAMADANMADANPRDAPP0068131\",\n" +
            "  \"creditVpa\":\"{merchantVpa}\",\n" +
            "    \"esn\": \"{esn}\",\n" +
            "    \"transactionAmount\": \"{transAmount}\"\n" +
            "}";

    public static String setRequest(String callbackUrl,String merchantVpa,String esn,String transAmount){
        request= request.replace("{callbackUrl}",callbackUrl).replace("{merchantVpa}",merchantVpa).replace("{esn}",esn).replace("{transAmount}",transAmount);
        return request;

    }
    public static String getRequest(){
        return request;
    }

    public AxisBankMockApi(String callbackUrl,String merchantVpa,String esn,String transAmount){
        setRequest(callbackUrl,merchantVpa,esn,transAmount);
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri("https://automation-pg-ext.paytm.in");
        getRequestSpecBuilder().setBasePath("/mockbank/axis/upiIntentTxn");
        getRequestSpecBuilder().setBody(getRequest());

    }
    public static JsonPath axisBankMockApi(String callbackUrl,String merchantVpa,String esn,String transAmount) throws UnsupportedEncodingException {
        return new AxisBankMockApi(callbackUrl,merchantVpa,esn,transAmount).execute().jsonPath();
    }

}
