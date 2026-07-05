package com.paytm.framework.reportportal.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reportportal.ReporterConfig;
import io.restassured.http.ContentType;

public class StartChildTestItemRequest extends BaseApi {

    private String request = "{\n" +
            "  \"attributes\": [\n" +
            "\n" +
            "  ],\n" +
            "  \"codeRef\": null,\n" +
            "  \"description\": null,\n" +
            "  \"hasStats\": true,\n" +
            "  \"launchUuid\": null,\n" +
            "  \"name\": null,\n" +
            "  \"parameters\": [\n" +
            "\n" +
            "  ],\n" +
            "  \"retry\": false,\n" +
            "  \"retryOf\": null,\n" +
            "  \"startTime\": null,\n" +
            "  \"testCaseId\": null,\n" +
            "  \"type\": null,\n" +
            "  \"uniqueId\": null\n" +
            "}";

    public StartChildTestItemRequest(String parentId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setBasePath("/api/v2/" + ReporterConfig.RP_PROJECT + "/item/" + parentId)
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "bearer " + ReporterConfig.RP_UUID)
                .setBaseUri(ReporterConfig.RP_ENDPOINT)
                .setBody(request);
        setContext("retry", true);
    }

}
