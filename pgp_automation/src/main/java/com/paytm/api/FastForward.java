package com.paytm.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FastForward extends BaseApi {

    public FastForward(Object FastForwardRequestDTO) {
        ObjectMapper mapper = new ObjectMapper();
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.IVR_FAST_FWD);
        getRequestSpecBuilder().addParam("JsonData", FastForwardRequestDTO);
    }

    public FastForward(FastForwardAppRequest fastForwardAppRequest) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.FAST_FWD);
        getRequestSpecBuilder().setBody(fastForwardAppRequest);
    }
}
