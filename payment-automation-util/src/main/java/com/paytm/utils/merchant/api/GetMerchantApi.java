package com.paytm.utils.merchant.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

/**
 * Created by deepakkumar on 25/10/17.
 */
public class GetMerchantApi extends BaseApi {

    private static final String BASE_URI = Constants.PGP_HOST;
    private static final String BASE_PATH = "/mapping-service/query/merchant/migration/details/{mid}/true";

    public GetMerchantApi(String mid) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setBaseUri(BASE_URI);
        getRequestSpecBuilder().setBasePath(BASE_PATH.replace("{mid}", mid));
        getRequestSpecBuilder().setAccept(ContentType.JSON);
    }
}
