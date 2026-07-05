package com.paytm.utils.merchant.api.wallet;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class BlockDisputeTxn extends BaseApi {


    public BlockDisputeTxn(String baseUri, String ssoToken, String adminGuid, String checksumHash, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(baseUri);
        getRequestSpecBuilder().setBasePath("/wallet-web/blockDisputeTxn");
        getRequestSpecBuilder().addHeader("ssotoken", ssoToken);
        getRequestSpecBuilder().addHeader("mid", adminGuid);
        getRequestSpecBuilder().addHeader("CHECKSUMHASH", checksumHash);
        getRequestSpecBuilder().setBody(body);
    }


}
