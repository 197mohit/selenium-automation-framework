package com.paytm.utils.merchant.api.pgp.theia;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ProcessTransaction extends BaseApi {


    public ProcessTransaction(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/theia/processTransaction");
        getRequestSpecBuilder().setBody(body);
    }


}
