package com.paytm.framework.reportportal.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.service.dto.LaunchInfo;
import io.restassured.http.ContentType;

public class LogItemRequest extends BaseApi {

    String request = "{\n" +
            "  \"file\": null,\n" +
            "  \"itemUuid\": null,\n" +
            "  \"launchUuid\": null,\n" +
            "  \"level\": null,\n" +
            "  \"message\": null,\n" +
            "  \"time\": null,\n" +
            "  \"uuid\": null\n" +
            "}";

    public LogItemRequest() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setBasePath("/api/v2/" + ReporterConfig.RP_PROJECT + "/log")
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "bearer " + ReporterConfig.RP_UUID)
                .setBaseUri(ReporterConfig.RP_ENDPOINT)
                .setBody(request);
        setContext("launchUuid", LaunchInfo.getInstance().getLaunchId());
    }

}
