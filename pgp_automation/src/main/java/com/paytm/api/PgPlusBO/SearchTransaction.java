package com.paytm.api.PgPlusBO;


import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SearchTransaction extends BaseApi {


    String request = "{\n" +
            "    \"searchConditions\": [\n" +
            "        {\n" +
            "            \"searchBy\": \"txnId\",\n" +
            "            \"searchValue\": \"{TRANSACTION_ID}\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"searchBy\": \"showRetry\",\n" +
            "            \"searchValue\": \"true\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"orderStatus\": \"ALL\"\n" +
            "}";


    public SearchTransaction(String txnId) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);  //pgplus-bo issue fixed moving it to PGP_HOST
        getRequestSpecBuilder().setBasePath(Constants.PgPlusBo.SEARCH_TRANSACTION);
        setRequest(txnId);
        getRequestSpecBuilder().setBody(request);


    }

    public void setRequest(String txnId) {
        this.request = request
                .replace("{TRANSACTION_ID}", txnId);
    }
}
