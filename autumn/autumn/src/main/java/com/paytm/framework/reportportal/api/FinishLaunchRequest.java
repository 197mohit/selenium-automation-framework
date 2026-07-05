package com.paytm.framework.reportportal.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.service.dto.Attribute;
import io.restassured.http.ContentType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FinishLaunchRequest extends BaseApi {

    String request = "{\n" +
            "  \"endTime\": \"\",\n" +
            "  \"status\": \"\"\n" +
            "}";

    public FinishLaunchRequest(String launchId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
//                .addHeader("Authorization", "bearer " + ReporterConfig.RP_UUID)
                .setBasePath("/auto_exec_handler/stop/launch")
                .setContentType(ContentType.JSON)
                .setBaseUri(ReporterConfig.AUTO_EXEC_HANDLER_URL)
                .setBody(request);
    }

    public FinishLaunchRequest createRequest(String status) {
//        setContext("description", RPConfigLoader.RP_DESCRIPTION);
        setContext("endTime", Calendar.getInstance().getTime());
        setContext("status", status);
        setContext("rp_url", ReporterConfig.RP_ENDPOINT)
                .setContext("rp_uuid", ReporterConfig.RP_UUID)
                .setContext("project_name", ReporterConfig.RP_PROJECT)
                .setContext("launch_name", ReporterConfig.RP_LAUNCH);

//        try {
//            String[] attributes = ReporterConfig.RP_ATTRIBUTES.split(";");
//            if (attributes.length != 0) {
//                List<Attribute> attributeList = new ArrayList<>();
//                for (String attribute : attributes) {
//                    String key = attribute.split(":")[0];
//                    String value = attribute.split(":")[1];
//                    attributeList.add(new Attribute().withKey(key)
//                            .withValue(value)
//                            .withSystem(false));
//                }
//                setContext("attributes", attributeList);
//            }
//        } catch (Exception e) {
//        }

        return this;
    }
}
