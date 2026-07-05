package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;

public class User extends BaseApi {


    public User(String authBaseUri, String ssoToken) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setBasePath("/user");
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().addHeader("session_token", ssoToken);
    }


}
