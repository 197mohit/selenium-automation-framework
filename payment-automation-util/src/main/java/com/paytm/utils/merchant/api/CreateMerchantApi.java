package com.paytm.utils.merchant.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.CreateMerchant;
import io.restassured.http.ContentType;

/**
 * Created by deepakkumar on 24/10/17.
 */
public class CreateMerchantApi extends BaseApi {

    private static final String BASE_URI = Constants.ADMIN_SERVER_ADDRESS;
    private static final String BASE_PATH = "/admin/app/api/v1/submitCreateMerchantAPIRequest";

    public CreateMerchantApi(CreateMerchant body) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath(BASE_PATH)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addHeader("x-real-ip", "null")
                .setBody(body);
    }
}
