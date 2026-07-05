package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class PMandateDownload extends BaseApi {

    private final String DOWNLOAD_URL = "/subscription/paper/mandate/downloadUrl";

    public PMandateDownload(String subsId, String mid) {
        String body = "{\"head\":{\"version\":\"v1\",\"timestamp\":\"1556618314529\",\"tokenType\":\"AES\"},\"body\":{\"subscriptionId\":\"{subsId}\",\"mid\":\"{mid}\"}}";
        body = body.replace("{subsId}", subsId);
        body = body.replace("{mid}", mid);
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(DOWNLOAD_URL);
        getRequestSpecBuilder().setBody(body);
    }

}
