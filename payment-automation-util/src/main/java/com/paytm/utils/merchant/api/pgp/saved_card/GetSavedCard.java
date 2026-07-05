package com.paytm.utils.merchant.api.pgp.saved_card;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetSavedCard extends BaseApi {


    public GetSavedCard(String pgpUrl, String custId) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/savedcardservice/savedCardService/v1/get/savedcard/userId/" + custId);
    }


}
