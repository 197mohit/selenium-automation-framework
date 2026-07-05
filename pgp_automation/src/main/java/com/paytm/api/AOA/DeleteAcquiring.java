package com.paytm.api.AOA;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class DeleteAcquiring extends BaseApi {
    String request ="{\n" +
            "    \"acquiringConfigInfo\": {\n" +
            "        \"merchantId\": \"216820000007629216189\",\n" +
            "        \"recordId\": \"120100000008820530185\"\n" +
            "    }\n" +
            "}";
    public DeleteAcquiring() {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.DELETE_ACQUIRING);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }


    public DeleteAcquiring buildRequest(String mid, String recordId) {
        setContext("acquiringConfigInfo.merchantId", mid);
        setContext("acquiringConfigInfo.recordId",recordId);
        return this;
    }




}
