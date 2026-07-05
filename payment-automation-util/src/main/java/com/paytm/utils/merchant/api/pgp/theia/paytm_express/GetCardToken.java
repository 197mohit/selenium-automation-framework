package com.paytm.utils.merchant.api.pgp.theia.paytm_express;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetCardToken extends BaseApi {


    public GetCardToken(String pgpUrl, String mId, String custId, String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        this(pgpUrl, "CARD_NUMBER=" + cardNumber + "&CARD_EXPIRY_MONTH=" + expiryMonth + "&CARD_EXPIRY_YEAR=" + expiryYear + "&CVV=" + cvv + "&CUST_ID=" + custId + "&MID=" + mId);
    }

    public GetCardToken(String pgpUrl, String mId, String custId, String savedCardId, String cvv) {
        this(pgpUrl, "SAVED_CARD_ID=" + savedCardId + "&CVV=" + cvv + "&CUST_ID=" + custId + "&MID=" + mId);
    }

    public GetCardToken(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/theia/PAYTM_EXPRESS/getCardToken");
        getRequestSpecBuilder().setBody(body);
    }


}
