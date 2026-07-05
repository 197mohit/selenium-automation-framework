package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class RefundStatusApi extends BaseApi {
    public RefundStatusApi(String mid, String refId) {
        APIBuilder:
        {
            setMethod(MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.REFUND_STATUS);
            getRequestSpecBuilder().addParam("JsonData", "{\"MID\": \"" + mid + "\", \"REFID\": \"" + refId + "\"}");
        }
    }
}
