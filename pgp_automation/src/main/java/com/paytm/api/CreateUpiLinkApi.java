package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.createUPILink.CreateUpiLinkRequest;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CreateUpiLinkApi extends BaseApi {

    private final String endPoint = "/theia/v1/createUPILink";

    public CreateUpiLinkApi(CreateUpiLinkRequest createUpiLinkRequest) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().addQueryParam("mid", createUpiLinkRequest.getBody().getMid());
        getRequestSpecBuilder().addQueryParam("orderId", createUpiLinkRequest.getBody().getOrderId());
        getRequestSpecBuilder().setBody(createUpiLinkRequest);
    }



}
