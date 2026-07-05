package com.paytm.api.billproxy;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import static com.paytm.appconstants.Constants.BillProxy.CARD_TOKENIZE_CIN;
import static com.paytm.appconstants.Constants.BillProxy.FETCH_CIN_REQUEST;

public class FetchCinRequest extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ANDROID\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1519736590323\",\n" +
            "        \"channelId\": \"S2S\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"cardToken\": \"202106280501189fda57242b285322a4340ed1bd468aa\"\n" +
            "    }\n" +
            "}";
    public FetchCinRequest() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(FETCH_CIN_REQUEST);
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