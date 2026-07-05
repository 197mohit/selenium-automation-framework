package com.paytm.api.MappingService.notificationTemplate;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetTemplateGlobalConfiguration extends BaseApi {

    public GetTemplateGlobalConfiguration () {
        String basePath = Constants.MappingService.GET_TEMPLATE_GLOBAL_CONFIG;
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(basePath);
    }
}
