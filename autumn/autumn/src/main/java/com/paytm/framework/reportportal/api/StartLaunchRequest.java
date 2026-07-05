package com.paytm.framework.reportportal.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.service.dto.Attribute;
import io.restassured.http.ContentType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StartLaunchRequest extends BaseApi {
    String request = "{\n" +
            "  \"attributes\": [\n" +
            "\n" +
            "  ],\n" +
            "  \"description\": \"\",\n" +
            "  \"mode\": \"\",\n" +
            "  \"name\": \"\",\n" +
            "  \"rerun\": false,\n" +
            "  \"rerunOf\": \"\",\n" +
            "  \"startTime\": \"2020-12-02T06:32:51.848Z\"\n" +
            "}";

    public StartLaunchRequest() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setBasePath("auto_exec_handler/start/launch")
                .setContentType(ContentType.JSON)
                .setBaseUri(ReporterConfig.AUTO_EXEC_HANDLER_URL)
                .setBody(request);
    }

    private String prepareDescription() {
        String description = "";
        description = ReporterConfig.RP_DESCRIPTION + System.lineSeparator();
        return description;
    }

    public StartLaunchRequest createRequest() {
        setContext("rp_url", ReporterConfig.RP_ENDPOINT);
        setContext("rp_uuid", ReporterConfig.RP_UUID);
        setContext("project_name", ReporterConfig.RP_PROJECT);
        setContext("mode", ReporterConfig.RP_MODE);
        setContext("launch_name", ReporterConfig.RP_LAUNCH);
        setContext("rerun", false);
        setContext("rerunOf", null);
        setContext("startTime", Calendar.getInstance().getTime());
        String description = prepareDescription();
        setContext("description", description);
        try {
            String[] attributes = ReporterConfig.RP_ATTRIBUTES.split(";");
            List<Attribute> attributeList = new ArrayList<>();
            if (attributes.length != 0) {

                for (String attribute : attributes) {
                    String key = attribute.split(":")[0];
                    String value = attribute.split(":")[1];
                    attributeList.add(new Attribute().withKey(key.trim())
                            .withValue(value.trim())
                            .withSystem(false));
                    if (key.equalsIgnoreCase("suiteName")) {
                        description = description + System.lineSeparator();
                        description = description + "SUITE_FILE_NAME: " + value + System.lineSeparator();
                    }
                }
            }
            setContext("description", description);
            setContext("attributes", ReporterConfig.RP_ATTRIBUTES);
        } catch (Exception ignore) {
        }
        return this;
    }

}
