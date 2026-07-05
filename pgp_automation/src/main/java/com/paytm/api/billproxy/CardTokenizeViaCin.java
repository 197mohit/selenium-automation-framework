package com.paytm.api.billproxy;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.appconstants.Constants.BillProxy.CARD_TOKENIZE_CARD_NO_V1;
import static com.paytm.appconstants.Constants.BillProxy.CARD_TOKENIZE_CIN;
import static com.paytm.framework.api.BaseApi.MethodType.POST;

public class CardTokenizeViaCin extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ANDROID\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimeStamp\": \"1519736590323\",\n" +
            "        \"channelId\": \"APP\",\n" +
            "        \"signature\": \"\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"cin\": \"2021062401480aaffa725a691093a641d268b86ffdf01\"\n" +
            "    }\n" +
            "}";
    public CardTokenizeViaCin(String ssoToken) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(CARD_TOKENIZE_CIN);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        getRequestSpecBuilder().addHeader("ssotoken", ssoToken);
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }


}