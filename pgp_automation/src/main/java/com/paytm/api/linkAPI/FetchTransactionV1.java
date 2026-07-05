package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchTransactionV1 extends BaseApi {

    String request="{\n" +
            "\"head\": {\n" +
            " \"timestamp\": \"1539601338741\",\n" +
            " \"clientId\": \"78yds87ty7ds\",\n" +
            " \"version\": \"v1\",\n" +
            " \"channelId\": \"WEB\",\n" +
            " \"tokenType\": \"AES\",\n" +
            " \"signature\": \"buS7+ck4UxqkMOt1R65T7S6ymAlWYKNtFNPODcUSGMmyJeS/rwoL2tJsNQ49UQzEzOh0BbXJYsBmn4x9R069a4/31o453RJC/pDrjB2X4Qs=\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "  \"mid\": \"linksO38457083060859\",\n" +
            "  \"linkId\": 21341,\n" +
            " \"pageNo\": 1,\n" +
            " \"pageSize\": 10,\n" +
            " \"searchStartDate\": \"01/07/2022\",\n" +
            " \"searchEndDate\": \"22/07/2022\",\n" +
            " \"fetchAllTxns\" : true\n" +
            " \n" +
            "}\n" +
            "}\n";

    public FetchTransactionV1() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FETCH_TRANSACTION_V1);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest(){
        return request;
    }
    public FetchTransactionV1 buildRequest(String mid, Integer linkId,String searchStartDate,String searchEndDate){
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        setContext("body.searchStartDate",searchStartDate);
        setContext("body.searchEndDate",searchEndDate);
        return this;
    }
}
