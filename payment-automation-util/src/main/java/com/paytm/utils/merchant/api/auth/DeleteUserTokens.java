package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class DeleteUserTokens extends BaseApi {


    public DeleteUserTokens(String authBaseUri, String authorization, String ssoToken) {
        setMethod(BaseApi.MethodType.DELETE);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/oauth2/usertokens");
        getRequestSpecBuilder().addParam("accessTokenId", ssoToken);
        getRequestSpecBuilder().addHeader("authorization", authorization);
    }


}
