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

public class ExpireLink  extends BaseApi {
    String request="{\n" +
            "\"head\": {\n" +
            " \"timestamp\": \"1539601338741\",\n" +
            " \"clientId\": \"LINK_UI\",\n" +
            " \"version\": \"v2\",\n" +
            " \"channelId\": \"WEB\",\n" +
            " \"tokenType\": \"JWT\",\n" +
            " \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsaW5rSWQiOiI1MjE4NDkzMDgiLCJpc3MiOiJMSU5LX1VJIiwibWlkIjoiVEVTVE1ENDEzNDAwNTk0MTU3OTIifQ.bVGz932QPYVFu2qQnVZv2FkBP3GlV_UW40R0nFonmcE\"\n" +
            "},\n" +
            "\"body\": {\"mid\": \"FfpEkO71115958595487\",\"linkId\": \"24521\"}\n" +
            "}";
    public ExpireLink() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.EXPIRE_LINK);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest(){
        return request;
    }
    public  ExpireLink buildRequest(String mid, String linkId) {
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        return this;
    }
}
