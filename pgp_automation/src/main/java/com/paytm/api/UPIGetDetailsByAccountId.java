package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class UPIGetDetailsByAccountId extends BaseApi {

    Response response;

    public UPIGetDetailsByAccountId(String condition, String response) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath(Constants.Mockbank.UPI_PREDICATE_URL);
        getRequestSpecBuilder().setBody("{\"condition\":" + condition +",\"response\":{"+ response +"}}");
    }
}
