package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.PreAuthCapture.PreAuthCaptureDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class PreAuthCaptureAPI extends BaseApi {

    private final String endPoint = "/paymentservices/HANDLER_FF/withdrawScw";

    public PreAuthCaptureAPI(PreAuthCaptureDTO preAuthCaptureDTO) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBody(preAuthCaptureDTO);
    }

}
