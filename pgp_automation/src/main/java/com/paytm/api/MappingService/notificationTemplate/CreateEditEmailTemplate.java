package com.paytm.api.MappingService.notificationTemplate;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CreateEditEmailTemplate extends BaseApi {

    String id;
    String mid;
    String templateId;
    String enabled;

    String request = "{\n" +
            "    \"id\": \"\",\n" +
            "    \"serviceType\": \"PAYMENT\",\n" +
            "    \"category\": \"OFFLINE\",\n" +
            "    \"recipient\": \"USER\",\n" +
            "    \"status\": \"SUCCESS\",\n" +
            "    \"bankCode\": \"ALL\",\n" +
            "    \"errorCode\": \"ALL\",\n" +
            "    \"mid\": \"{mid}\",\n" +
            "    \"payMode\": \"ALL\",\n" +
            "    \"enabled\": \"Y\",\n" +
            "    \"params\": \"params\",\n" +
            "    \"senderEmail\": \"test@yopmail.com\",\n" +
            "    \"senderName\": \"Test\",\n" +
            "    \"replyTo\": \"Customer\",\n" +
            "    \"templateAttachment\": \"templateAttachment\",\n" +
            "    \"templateId\": \"\",\n" +
            "    \"templateHeader\": \"paytm\",\n" +
            "    \"templateBody\": \"paytm\",\n" +
            "    \"templateDescription\": \"paytmDescription\",\n" +
            "    \"templateName\": \"paytm\",\n" +
            "    \"templateParams\": \"\"\n" +
            "}";

    public CreateEditEmailTemplate(String mid) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.CREATE_EDIT_EMAIL_TEMPLATE);
        request = request.replace("{mid}",mid);
        getRequestSpecBuilder().setBody(request);
    }

    public CreateEditEmailTemplate editRequest(String id, String templateId, String enabled) {
        setContext("id", id);
        setContext("templateId", templateId);
        setContext("enabled", enabled);
        return this;
    }
}
