package com.paytm.api.notification;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetNotificationTemplateMaster extends BaseApi {

    public GetNotificationTemplateMaster(String category, String notificationType, String errorCode, String txnStatus)
    {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String getTemplateBody = Constants.NotificationService.GET_TEMPLATE_BODY
                .replace("{category}",category)
                .replace("{notifType}",notificationType)
                .replace("{txnStatus}",txnStatus);
        getRequestSpecBuilder().setBasePath(getTemplateBody);
        getRequestSpecBuilder().addQueryParam("INST_ERROR_CODE",errorCode);
    }




    public GetNotificationTemplateMaster(String category, String notificationType, String txnStatus)
    {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String getTemplateBody = Constants.NotificationService.GET_TEMPLATE_BODY
                .replace("{category}",category)
                .replace("{notifType}",notificationType)
                .replace("{txnStatus}",txnStatus);
        getRequestSpecBuilder().setBasePath(getTemplateBody);
    }




}
