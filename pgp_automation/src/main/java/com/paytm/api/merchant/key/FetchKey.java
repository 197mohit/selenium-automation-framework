package com.paytm.api.merchant.key;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchKey extends BaseApi{

    private String PATH = "/api/v1/merchant/{mid}/key";

    public FetchKey(String mId, String token) {
        PATH = PATH.replace("{mid}", mId);
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.BOSS_PANEL);
        getRequestSpecBuilder().setBasePath(PATH);
        getRequestSpecBuilder().addHeader("x-sso-token", token);
    }

}
