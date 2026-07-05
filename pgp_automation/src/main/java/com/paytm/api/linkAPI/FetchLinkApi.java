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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class FetchLinkApi extends BaseApi {
    String request = "{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"78yds87ty7ds\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"AES\",\n" +
            "    \"signature\": \"jhddyt87td87vd\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "       \n" +
            "       \"mid\": \"k1G7l346739004133410\",\n" +
            "       \"linkId\":\"2618\",\n" +
            "        \"pageNo\": \"1\",\n" +
            "        \"pageSize\": 20,\n" +
            "        \"searchFilterRequestBody\": {\n" +
            "            \"toDate\": \"18/02/2022\",\n" +
            "            \"fromDate\": \"04/02/2022\",\n" +
            "            \"isActive\": \"true\"\n" +
            "        }\n" +
            "    }\n" +
            "  }\n";

    public FetchLinkApi() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.LINK_FETCH);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }


    public  FetchLinkApi buildRequest(String mid, String linkId, String todate, String fromdate) {
        setContext("body.mid",mid);
        setContext("body.linkId",linkId);
        setContext("body.searchFilterRequestBody.toDate",todate);
        setContext("body.searchFilterRequestBody.fromDate",fromdate);
        setContext("body.pageNo","1");
        setContext("body.pageSize","10");
        return this;
    }
}
