package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.List;

public class MerchantV1MerchantIdList extends BaseApi {

    String request = "{\n" +
            "    \"merchantIdList\": [\n" +
            "        \"AUTOQ847569449405149\"\n" +
            "    ],\n" +
            "    \"identifier\": null\n" +
            "}";
    public MerchantV1MerchantIdList(List<String> Mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.MERCHANT_V3_MERCHANTIDLIST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest() {return request;}

}
