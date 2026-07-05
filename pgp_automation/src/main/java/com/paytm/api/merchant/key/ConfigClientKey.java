package com.paytm.api.merchant.key;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ConfigClientKey extends BaseApi{


    public ConfigClientKey(String mId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/admin/app/v1/pgplus/configClientKey");
        getRequestSpecBuilder().addHeader("X-AUTH-UMP", "zxcs-9098-kls-qw90-xcd");
        getRequestSpecBuilder().addHeader("x-real-ip", "127.0.0.1");
        getRequestSpecBuilder().addQueryParam("mid", mId);
    }

}
