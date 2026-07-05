package com.paytm.utils.merchant.api.wallet;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CheckBalance extends BaseApi {


    public CheckBalance(String baseUri, String ssoToken) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(baseUri);
        getRequestSpecBuilder().setBasePath("/wallet-web/checkBalance");
        getRequestSpecBuilder().addHeader("ssotoken", ssoToken);
        getRequestSpecBuilder().addHeader("is_admin", "false");
        getRequestSpecBuilder().addHeader("tokentype", "OAUTH");
    }


}
