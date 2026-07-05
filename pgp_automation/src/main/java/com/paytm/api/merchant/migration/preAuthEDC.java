package com.paytm.api.merchant.migration;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;


public class preAuthEDC extends BaseApi {
    String request="";

    public preAuthEDC(String StandardPreAuthEdcEnabled, String walletToken,String mid){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("x-sso-token",walletToken);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.BOSS_PANEL);
        String basePath = Constants.BossAPI.PRE_AUTH;
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("isStandardPreAuthEdcEnabled",StandardPreAuthEdcEnabled);
        getRequestSpecBuilder().setBasePath(basePath);

    }

}
