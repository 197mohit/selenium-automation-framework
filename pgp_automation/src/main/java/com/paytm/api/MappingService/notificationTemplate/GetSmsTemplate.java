package com.paytm.api.MappingService.notificationTemplate;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetSmsTemplate extends BaseApi {
    private String basePath = Constants.MappingService.GET_SMS_TEMPLATE;

     public GetSmsTemplate(String mid) {
         basePath = basePath.replace("{mid}", mid);
         setMethod(BaseApi.MethodType.GET);
         getRequestSpecBuilder().setContentType(ContentType.JSON);
         getRequestSpecBuilder().setAccept(ContentType.JSON);
         getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
         getRequestSpecBuilder().setBasePath(basePath);
     }
}
