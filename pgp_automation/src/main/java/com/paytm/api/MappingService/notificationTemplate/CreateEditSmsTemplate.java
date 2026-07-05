package com.paytm.api.MappingService.notificationTemplate;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CreateEditSmsTemplate extends BaseApi {
    String id;
    String mid;
    String enabled;
    String templateId;

    String request = "{\n" +
            "    \"id\": \"\",\n" +
            "    \"notificationType\": \"SMS\",\n" +
            "    \"serviceType\": \"PAYMENT\",\n" +
            "    \"category\": \"ONLINE\",\n" +
            "    \"recipient\": \"USER\",\n" +
            "    \"txnStatus\": \"SUCCESS\",\n" +
            "    \"bankCode\": \"ALL\",\n" +
            "    \"instErrorCode\": \"ALL\",\n" +
            "    \"mid\": \"{mid}\",\n" +
            "    \"payMode\": \"ALL\",\n" +
            "    \"params\": \"params\",\n" +
            "    \"enabled\": \"Y\",\n" +
            "    \"sender\": \"Test\",\n" +
            "    \"dltTemplateId\": \"\",\n" +
            "    \"dltEntityId\": \"\",\n" +
            "    \"templateId\": \"\",\n" +
            "    \"templateName\": \"NotfSuccess1\",\n" +
            "    \"templateHeader\": \"SuccessHeader1\",\n" +
            "    \"templateBody\": \"SuccessBody1\",\n" +
            "    \"extendedTemplateBody\": \"\",\n" +
            "    \"templateDescription\": \"DEFAULT\"\n" +
            "}";
    public CreateEditSmsTemplate(String mid) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.CREATE_EDIT_SMS_TEMPLATE);
        request = request.replace("{mid}",mid);
        getRequestSpecBuilder().setBody(request);
    }

    public CreateEditSmsTemplate editRequest(String id, String templateId, String enabled) {
        setContext("id", id);
        setContext("templateId", templateId);
        setContext("enabled", enabled);
        return this;
    }

}
