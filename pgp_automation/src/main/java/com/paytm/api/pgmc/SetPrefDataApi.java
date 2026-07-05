package com.paytm.api.pgmc;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.Map;

public class SetPrefDataApi extends BaseApi {

    public SetPrefDataApi(String mid, Map<String, Object> prefData) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGMC_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.SET_PREF_DATA + mid);
        getRequestSpecBuilder().setBody(prefData);
    }
}
