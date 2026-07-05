package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * Created by sureshgupta on 15/09/17.
 */
public class ProcessTransaction extends BaseApi {

    public ProcessTransaction() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/processTransaction");
    }
}
