package com.paytm.api;
import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;


public class UpiPspProcessor extends BaseApi {

    private final String endPoint = "upi-psp-processor/v1/order/pay/upipsp";

    public UpiPspProcessor(StaticQrUpiPSPRequest staticQrUpiPSPRequest) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setBody(staticQrUpiPSPRequest);
    }
}
