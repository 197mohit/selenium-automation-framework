package com.paytm.utils.merchant.api.pgp.saved_card;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class DeleteSavedCard extends BaseApi {


    public DeleteSavedCard(String pgpUrl, String custId, String saveCardId) {
        setMethod(BaseApi.MethodType.DELETE);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/savedcardservice/savedCardService/v1/delete/savedcard/userId/cardId/" + custId + "/" + saveCardId);
    }


}
