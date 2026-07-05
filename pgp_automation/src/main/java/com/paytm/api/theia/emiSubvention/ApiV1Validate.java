package com.paytm.api.theia.emiSubvention;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class ApiV1Validate extends BaseApi {


    public ApiV1Validate(String mid, ApiV1ValidateRequest request) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.NativeAPIResourcePath.EMI_SUBVENTION_VALIDATE);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(request);
    }

    public ApiV1Validate(String mid, String orderId, ApiV1ValidateRequest request) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.NativeAPIResourcePath.EMI_SUBVENTION_VALIDATE);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(request);
    }


}