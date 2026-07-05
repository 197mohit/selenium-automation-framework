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


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class GetLinkDetail extends BaseApi {
    String request="{\n" +
            "   \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"78yds87ty7ds\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES \",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJVTVAiLCJtaWQiOiJGZnBFa083MTExNTk1ODU5NTQ4NyJ9.nbxEqwv2rzGQz7OF8bpwgW0CYzNA0j7OfFk66tB7LSI\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"FfpEkO71115958595487\",\n" +
            "        \"linkId\": \"18920\",\n" +
            "        \"paymentFormId\":\"LINK_PAYMENT_FORM_1892081269\",\n" +
            "        \"amount\": \"1.0\",\n" +
            "        \"orderId\": \"202201251643040034544\"\n" +
            "    }\n" +
            "}";
    public GetLinkDetail() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.GET_LINK_DETAIL);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest() {
        return request;
    }
    public  GetLinkDetail buildRequest(String mid, String linkId, String amount, String orderId) {
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        setContext("body.amount",amount);
        setContext("body.orderId",orderId);
        return this;
    }
}
