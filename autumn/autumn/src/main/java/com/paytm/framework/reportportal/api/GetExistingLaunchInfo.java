package com.paytm.framework.reportportal.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reportportal.ReporterConfig;
import io.restassured.http.ContentType;

public class GetExistingLaunchInfo extends BaseApi {

    private String body = "{\n" +
            "  \"project_name\": \"\",\n" +
            "  \"launch_name\": \"\"\n" +
            "}";

    public GetExistingLaunchInfo(){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setBaseUri(ReporterConfig.AUTO_EXEC_HANDLER_URL)
                .setBasePath("auto_exec_handler/get/latest/launch")
                .setContentType(ContentType.JSON)
                .setBody(body);
        setContext("project_name", ReporterConfig.RP_PROJECT)
                .setContext("launch_name", ReporterConfig.RP_LAUNCH);
    }

}
