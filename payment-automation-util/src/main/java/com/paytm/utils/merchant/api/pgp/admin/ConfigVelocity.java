package com.paytm.utils.merchant.api.pgp.admin;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.Velocities;
import io.restassured.http.ContentType;

public class ConfigVelocity extends BaseApi {


    private static final String BASE_URI = Constants.ADMIN_SERVER_ADDRESS;

    public ConfigVelocity(Velocities body) {
        this(body.toString());
    }

    public ConfigVelocity(String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath("/admin/app/api/configVelocity")
                .setAccept(ContentType.JSON)
                .setBody(body);
    }


}
