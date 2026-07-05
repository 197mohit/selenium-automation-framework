package com.paytm.api.qr;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class EditQR extends BaseApi {

    String request = "{\"request\": {\n" +
            "\n" +
            "        \"qrCodeId\":\"{qrCodeId}\",\n" +
            "        \"secondaryPhoneNumber\":\"{mobileNumber}\"\n" +
            "\n" +
            "  },\n" +
            "\n" +
            "  \"ipAddress\": \"127.0.0.1\",\n" +
            "\n" +
            "  \"platformName\": \"PayTM\",\n" +
            "\n" +
            "  \"operationType\": \"QR_CODE\"}";

    public String getRequest()
    {
        return  request;
    }

    public EditQR setRequest(String qrCodeId,String mobileNumber)
    {
        request = request.replace("{qrCodeId}",qrCodeId).replace("{mobileNumber}",mobileNumber);
        return this;
    }

    public EditQR(String qrCodeId,String mobileNumber) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("clientid","a5516f104428408fb6051f833c9bb9e0");
        getRequestSpecBuilder().addHeader("hash","d67d25073a05b3b47cfdc5e16f78dea39cee9d57c6a7a523321b3dd6dc975f94");
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.QR_HOST);
        getRequestSpecBuilder().setBasePath(Constants.QR.EDIT_QR);
        setRequest(qrCodeId,mobileNumber);
        getRequestSpecBuilder().setBody(getRequest());

    }


}
