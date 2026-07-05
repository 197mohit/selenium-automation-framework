package com.paytm.utils.merchant.api.pgp.admin;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.ConfigureMBID;
import io.restassured.http.ContentType;

public class ConfigureMBId extends BaseApi {

    private static final String BASE_URI = Constants.ADMIN_SERVER_ADDRESS;

    public ConfigureMBId(ConfigureMBID body) {
        this(body.toString());
    }

    public ConfigureMBId(String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath("/admin/app/api/configureMBID")
                .setAccept(ContentType.JSON)
                .setBody(body);
    }

}
