package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetTokenRangeAPI extends BaseApi {

    public GetTokenRangeAPI() {

        //P+ API "/user/token/bin/query" is deprecated now
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.SUPERGW_LITE);       //Alipay URL is deprecated
        getRequestSpecBuilder().setBasePath(Constants.Alipay.USER_TOKEN_BIN_QUERY);  //No longer in use - /user/token/bin/query
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("Authorization", "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
        getRequestSpecBuilder().setBody("{\"bin\":\"465010000\"}");
    }
}