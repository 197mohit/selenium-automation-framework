package com.paytm.api.wallet;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetQRCodeInfoApi extends BaseApi {

    private final String GET_QRCODE_URL = "/wallet-web/getQRCodeInfo";

    public GetQRCodeInfoApi(String qrCodeId) {
        String body = "{\"request\":{\"qrCodeId\":\""+qrCodeId+"\"},\"ipAddress\":\"127.0.0.1\",\"platformName\":\"PayTM\",\"operationType\":\"QR_CODE\"}";
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        getRequestSpecBuilder().setBasePath(GET_QRCODE_URL);
        getRequestSpecBuilder().setBody(body);
    }

}
