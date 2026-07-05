package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.http.ContentType;

public class Authorize extends BaseApi {


    public Authorize(String authBaseUri, String authorization, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/oauth2/authorize");
        getRequestSpecBuilder().addHeader("authorization", authorization);
        getRequestSpecBuilder().setBody(body);
    }

    public Authorize(String authBaseUri, String clientKey, String secretkey, String body) {
        setMethod(BaseApi.MethodType.POST);
        PreemptiveBasicAuthScheme basicAuthScheme = new PreemptiveBasicAuthScheme();
        basicAuthScheme.setUserName(clientKey);
        basicAuthScheme.setPassword(secretkey);
        getRequestSpecBuilder().setAuth(basicAuthScheme);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/oauth2/authorize");
        getRequestSpecBuilder().setBody(body);
    }

}
