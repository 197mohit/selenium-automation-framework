package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class StaticQrUpiPSP extends BaseApi {

    public StaticQrUpiPSP(StaticQrUpiPSPRequest staticQrUpiPSPRequest) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.UPI_PSP_PAYMENT);
        getRequestSpecBuilder().setBody(staticQrUpiPSPRequest);
    }

}
