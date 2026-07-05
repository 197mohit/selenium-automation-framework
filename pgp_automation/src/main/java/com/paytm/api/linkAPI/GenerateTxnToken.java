package com.paytm.api.linkAPI;
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


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class GenerateTxnToken extends BaseApi {
    String request= "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"78yds87ty7ds\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"jhddyt87td87vd\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"FfpEkO71115958595487\",\n" +
            "        \"linkId\": \"19143\",\n" +
            "        \"amount\": \"2\",\n" +
            "        \"ssoToken\": \"8ca51731-dbf0-4b08-a77f-7ecca9746600\",\n" +
            "        \"custId\": \"123234\"\n" +
            "    }\n" +
            "}";
    public GenerateTxnToken() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.GENERATE_TXN_TOKEN);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest() {
        return request;
    }
    public  GenerateTxnToken  buildRequest(String mid, String linkId, String SSOToken, String type, String amount, String custId) {
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        setContext("body.ssoToken",SSOToken);
        setContext("body.custId",custId);
        if(type=="GENERIC"){
            setContext("body.amount", amount);
        }
        return this;
    }
}
