package com.paytm.utils.merchant.api.auth;

import com.paytm.framework.api.BaseApi;

public class ResourceUser extends BaseApi {


    public ResourceUser(String authBaseUri, String authorization, String mobileNo) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setBaseUri(authBaseUri);
        getRequestSpecBuilder().setBasePath("/v2/user");
        getRequestSpecBuilder().addHeader("authorization", authorization);
        getRequestSpecBuilder().addHeader("verification_type", "service_token");
        getRequestSpecBuilder().addQueryParam("phone",mobileNo);
        getRequestSpecBuilder().addQueryParam("fetch_strategy","userId");
    }


}
