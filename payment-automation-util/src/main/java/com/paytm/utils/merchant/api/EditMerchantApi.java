package com.paytm.utils.merchant.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.EditMerchant;
import io.restassured.http.ContentType;

/**
 * Created by deepakkumar on 22/10/17.
 */

public class EditMerchantApi extends BaseApi {

    private static final String BASE_URI = Constants.ADMIN_SERVER_ADDRESS;
    private static final String BASE_PATH = "/admin/app/api/editMerchantAPI";

    public EditMerchantApi(EditMerchant body) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setBaseUri(BASE_URI);
        getRequestSpecBuilder().setBasePath(BASE_PATH);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBody(body);
        getRequestSpecBuilder().addHeader("x-real-ip", "null");
    }
}
