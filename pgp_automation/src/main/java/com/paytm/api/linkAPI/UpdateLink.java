package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class UpdateLink extends BaseApi {

    String request = "{\n" +
            "\"head\": {\n" +
            " \"timestamp\": \"1539601338741\",\n" +
            " \"clientId\": \"78yds87ty7ds\",\n" +
            " \"version\": \"v2\",\n" +
            " \"channelId\": \"WEB\",\n" +
            " \"tokenType\": \"AES\",\n" +
            " \"signature\": \"buS7+ck4UxqkMOt1R65T7S6ymAlWYKNtFNPODcUSGMmyJeS/rwoL2tJsNQ49UQzEzOh0BbXJYsBmn4x9R069a4/31o453RJC/pDrjB2X4Qs=\"\n" +
            "},\n" +
            "\"body\": {\n" +
            "\"mid\": \"JyFuti78224712775813\",\n" +
            "\"linkId\":\"3875\",\n" +
            "\"linkDescription\": \"link paymen123t\"\n" +
            "}\n" +
            "}";

    public String getRequest() {return request;}

    public UpdateLink(String mid, String linkId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.UPDATE_LINK);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setContext("body.mid", mid);
        setContext("body.linkId", linkId);
    }

    public UpdateLink buildRequest(String mid, String linkId){
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        return this;
    }
}
