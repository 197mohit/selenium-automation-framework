package com.paytm.api.qr;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchQR extends BaseApi {

    String request = "{\n" +
            "\"request\": {\n" +
            "  \"qrCodeId\":\"{qrCodeId}\"\n" +
            "},\n" +
            "\"ipAddress\": \"127.0.0.1\",\n" +
            "\"platformName\": \"PayTM\",\n" +
            "\"operationType\": \"QR_CODE\"\n" +
            "}";

    public String getRequest()
    {
        return  request;
    }

    public FetchQR setRequest(String qrCodeId)
    {
        request = request.replace("{qrCodeId}",qrCodeId);
        return this;
    }

    public FetchQR(String qrCodeId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.QR_HOST);
        getRequestSpecBuilder().setBasePath(Constants.QR.FETCH_QR);
        setRequest(qrCodeId);
        getRequestSpecBuilder().setBody(getRequest());

    }

}
