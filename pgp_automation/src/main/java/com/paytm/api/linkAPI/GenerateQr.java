package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GenerateQr extends BaseApi {

    String request="{\n" +
            "\"head\":\n" +
            "\n" +
            "{ \"timestamp\": \"1539601338741\", \"clientId\": \"UMP\", \"version\": \"v2\", \"channelId\": \"WEB\", \"tokenType\": \"JWT\", \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsaW5rSWQiOiIyODA5OSIsImlzcyI6IlVNUCIsIm1pZCI6IiJ9.Zez4ila_SKbTe2rDiekokB3kHkyOe21KpPo03qVi3Ug\"}\n" +
            ",\n" +
            "\"body\":{\"mid\":\"qa8PG294377944191275\",\"linkId\":\"30124\"}\n" +
            "}";

    public GenerateQr() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.GENERATE_QR);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest(){
        return request;
    }

    public  GenerateQr buildRequest(String mid, String linkId) {
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        return this;
    }
}
