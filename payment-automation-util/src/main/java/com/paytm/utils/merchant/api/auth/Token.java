package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;
import io.restassured.authentication.BasicAuthScheme;
import io.restassured.http.ContentType;

public class Token extends BaseApi {


    public Token(String authBaseUri, String authorization, String code) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/oauth2/token");
        getRequestSpecBuilder().addHeader("authorization", authorization);
        getRequestSpecBuilder().addFormParam("grant_type", "authorization_code");
        getRequestSpecBuilder().addFormParam("code", code);
        getRequestSpecBuilder().addFormParam("scope", "paytm");
    }



    public Token(String authBaseUri,String clientKey,String secretkey, String code) {
        setMethod(BaseApi.MethodType.POST);
        BasicAuthScheme basicAuthScheme = new BasicAuthScheme();
        basicAuthScheme.setUserName(clientKey);
        basicAuthScheme.setPassword(secretkey);
        getRequestSpecBuilder().setAuth(basicAuthScheme);
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/oauth2/token");
        getRequestSpecBuilder().addFormParam("grant_type", "authorization_code");
        getRequestSpecBuilder().addFormParam("code", code);
        getRequestSpecBuilder().addFormParam("scope", "paytm");
    }




}
