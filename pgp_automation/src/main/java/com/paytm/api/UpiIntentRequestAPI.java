package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class UpiIntentRequestAPI extends BaseApi {

    private final String endPoint = "/mockbank/generate/upi/intent/pay";

    public UpiIntentRequestAPI(UPIIntentRequestDTO upiIntentRequestDTO) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setBody(upiIntentRequestDTO);
    }

}
