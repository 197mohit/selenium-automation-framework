package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;

import java.util.HashMap;
import java.util.Map;

public class UserV2 extends BaseApi {


    public UserV2(String authBaseUri, String authorization, String ssoToken) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/v2/user");
        getRequestSpecBuilder().addHeader("data", ssoToken);
        getRequestSpecBuilder().addHeader("Authorization", authorization);
        getRequestSpecBuilder().addHeader("verification_type", "oauth_token");
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("fetch_strategy", "user_attribute,user_type");
        getRequestSpecBuilder().addQueryParams(queryParam);
    }


}
