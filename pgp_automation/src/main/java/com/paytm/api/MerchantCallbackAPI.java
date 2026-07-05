package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;

import static com.paytm.LocalConfig.MOCK_HOST;
import static com.paytm.framework.api.BaseApi.MethodType.GET;

public class MerchantCallbackAPI extends BaseApi {

    public MerchantCallbackAPI(String orderId) {
        this.setMethod(GET);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        this.getRequestSpecBuilder().setBaseUri(MOCK_HOST);
        this.getRequestSpecBuilder().setBasePath("/mockbank/merchant-callback");
        this.getRequestSpecBuilder().addQueryParam("orderId", orderId);
    }
}