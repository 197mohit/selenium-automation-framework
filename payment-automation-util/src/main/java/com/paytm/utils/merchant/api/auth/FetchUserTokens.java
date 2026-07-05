package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;

public class FetchUserTokens extends BaseApi {


    public FetchUserTokens(String authBaseUri, String authorization, String ssoToken) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/oauth2/usertokens");
        getRequestSpecBuilder().addHeader("authorization", authorization);
        getRequestSpecBuilder().addHeader("Access_Token",ssoToken);
    }


}
