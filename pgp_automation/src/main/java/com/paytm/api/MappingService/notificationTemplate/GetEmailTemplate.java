package com.paytm.api.MappingService.notificationTemplate;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetEmailTemplate extends BaseApi {
    private String basePath = Constants.MappingService.GET_EMAIL_TEMPLATE;

    public GetEmailTemplate(String mid) {
        basePath = basePath.replace("{mid}", mid);
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(basePath);
    }
}
