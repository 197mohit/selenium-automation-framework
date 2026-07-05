package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * API client for GET /merchant-status/healthcheck
 *
 * Exercises HealthCheckService.healthCheck():
 *   → file exists → 200 SUCCESS
 *   → file absent → 404 FAILED
 */
public class MerchantStatusHealthCheckApi extends BaseApi {

    public MerchantStatusHealthCheckApi() {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.TEXT);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/merchant-status/healthcheck");
    }
}
