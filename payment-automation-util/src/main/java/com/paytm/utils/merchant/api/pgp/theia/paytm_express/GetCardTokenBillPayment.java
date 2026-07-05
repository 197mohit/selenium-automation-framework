package com.paytm.utils.merchant.api.pgp.theia.paytm_express;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetCardTokenBillPayment extends BaseApi {


    public GetCardTokenBillPayment(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/theia/PAYTM_EXPRESS/getCardToken/billPayment");
        getRequestSpecBuilder().setBody(body);
    }

    public GetCardTokenBillPayment(String pgpUrl, String mid, String custId, String cardNumber) {
        this(pgpUrl, "CARD_NUMBER=" + cardNumber + "&CUST_ID=" + custId + "&MID=" + mid);
    }


}
