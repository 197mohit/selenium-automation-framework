package com.paytm.utils.merchant.api.pgp.refund;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class Refund extends BaseApi {

    public Refund(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/refund/HANDLER_INTERNAL/REFUND")
        .setBody(body);
    }

}
