package com.paytm.utils.merchant.api.wallet;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class Withdraw extends BaseApi {


    public Withdraw(String baseUri, String ssoToken, String merchantGuid, String checksumHash, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(baseUri);
        getRequestSpecBuilder().setBasePath("/wallet-web/withdraw");
        getRequestSpecBuilder().addHeader("ssotoken", ssoToken);
        getRequestSpecBuilder().addHeader("is_admin", "false");
        getRequestSpecBuilder().addHeader("tokentype", "OAUTH");
        getRequestSpecBuilder().addHeader("mid", merchantGuid);
        getRequestSpecBuilder().addHeader("CHECKSUMHASH", checksumHash);
        getRequestSpecBuilder().setBody(body);
    }


}
