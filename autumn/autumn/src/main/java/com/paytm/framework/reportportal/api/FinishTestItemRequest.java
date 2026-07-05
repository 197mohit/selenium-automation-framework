package com.paytm.framework.reportportal.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reportportal.ReporterConfig;
import io.restassured.http.ContentType;

public class FinishTestItemRequest extends BaseApi {

    String request = "{\n" +
            "  \"endTime\": null,\n" +
            "  \"launchUuid\": null,\n" +
            "  \"status\": null,\n" +
            "}";

    public FinishTestItemRequest(String testItemId) {
        setMethod(MethodType.PUT);
        getRequestSpecBuilder()
                .setBody(request)
                .setBasePath("/api/v2/" + ReporterConfig.RP_PROJECT + "/item/" + testItemId)
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "bearer " + ReporterConfig.RP_UUID)
                .setBaseUri(ReporterConfig.RP_ENDPOINT);
    }

}
