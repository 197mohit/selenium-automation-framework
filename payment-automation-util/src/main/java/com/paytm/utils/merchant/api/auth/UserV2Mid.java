package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;

import java.util.HashMap;
import java.util.Map;

public class UserV2Mid extends BaseApi {


    public UserV2Mid(String authBaseUri, String authorization, String ssoToken, String pgmid) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/v2/user/mid");
        getRequestSpecBuilder().addHeader("data", ssoToken);
        getRequestSpecBuilder().addHeader("Authorization", authorization);
        getRequestSpecBuilder().addHeader("pgmid", pgmid);
        getRequestSpecBuilder().addHeader("verification_type", "oauth_token");
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("fetch_strategy", "BASIC");
        getRequestSpecBuilder().addQueryParams(queryParam);
    }


}