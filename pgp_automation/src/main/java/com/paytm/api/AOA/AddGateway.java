package com.paytm.api.AOA;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class AddGateway extends BaseApi {
    String request ="{\n" +
            "  \"aoaMerchantId\": \"{AOAMID}\",\n" +
            "  \"gatewayMerchantId\": \"{MERCHANT}\",\n" +
            "  \"gatewayName\": \"PAYTMPG\",\n" +
            "  \"configDetails\": {\n" +
            "    \"entityKey\": \"{KEY}\"\n" +
            "  }\n" +
            "}";
    public AddGateway() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.AOA_ADD_GATEWAY);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String aoaMid, String mid, String key) {
        this.request = request.replace("{AOAMID}",aoaMid).replace("{MERCHANT}",mid).replace("{KEY}",key);

    }
    public AddGateway buildRequest(String aoamid,String mid,String gatewayName, String key) {
        setContext("aoaMerchantId", aoamid);
        setContext("gatewayMerchantId",mid);
        setContext("gatewayName",gatewayName);
        setContext("configDetails.entityKey",key);
        return this;
    }
}
