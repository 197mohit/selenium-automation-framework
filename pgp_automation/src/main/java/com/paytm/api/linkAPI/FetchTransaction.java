package com.paytm.api.linkAPI;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.api.AOA.AddGateway;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.FetchBinDetailResponse;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.luaj.vm2.ast.Str;

public class FetchTransaction extends BaseApi {
    String request= "{\n" +
            "\"head\": {\n" +
            " \"timestamp\": \"1539601338741\",\n" +
            " \"clientId\": \"78yds87ty7ds\",\n" +
            " \"version\": \"v2\",\n" +
            " \"channelId\": \"WEB\",\n" +
            " \"tokenType\": \"AES\",\n" +
            " \"signature\": \"buS7+ck4UxqkMOt1R65T7S6ymAlWYKNtFNPODcUSGMmyJeS/rwoL2tJsNQ49UQzEzOh0BbXJYsBmn4x9R069a4/31o453RJC/pDrjB2X4Qs=\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "  \"mid\": \"testli24296717753023\",\n" +
            "  \"linkId\": 1004,\n" +
            " \"pageNo\": 1,\n" +
            " \"pageSize\": 10,\n" +
            " \"searchStartDate\": \"01/01/2022\",\n" +
            " \"searchEndDate\": \"12/01/2022\",\n" +
            " \"fetchAllTxns\" : true\n" +
            " \n" +
            "}\n" +
            "}\n";
    public FetchTransaction() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FETCH_TRANSACTION);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest() {
        return request;
    }
    public   FetchTransaction buildRequest(String mid, String linkId, String todate, String fromdate, boolean fetchAllTxns) {
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        setContext("body.searchFilterRequestBody.toDate",todate);
        setContext("body.searchFilterRequestBody.fromDate",fromdate);
        setContext("body.pageNo","1");
        setContext("body.pageSize","10");
        setContext("body.fetchAllTxns",fetchAllTxns);
        return this;
    }
}
