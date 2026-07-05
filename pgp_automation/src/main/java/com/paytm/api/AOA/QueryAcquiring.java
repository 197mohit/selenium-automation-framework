package com.paytm.api.AOA;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class QueryAcquiring extends BaseApi {
    String request ="{\n" +
            "    \"acquiringConfigInfo\":{\n" +
            "        \"merchantId\":\"{mid}\"\n" +
            "    }      \n" +
            "}";
    public QueryAcquiring() {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.QUERY_ACQUIRING);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }


    public QueryAcquiring buildRequest(String mid) {
        setContext("acquiringConfigInfo.merchantId", mid);
        return this;
    }




}
