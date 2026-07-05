package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class PMandateUpload extends BaseApi {

    private final String UPLOAD_URL = "/subscription/paper/mandate/uploadUrl";

    public PMandateUpload(String subsId, String mid) {
        String body = "{\"head\":{\"version\":\"v1\",\"timestamp\":\"1556618314529\",\"tokenType\":\"AES\"},\"body\":{\"subscriptionId\":\"{subsId}\",\"mid\":\"{mid}\"}}";
        body = body.replace("{subsId}", subsId);
        body = body.replace("{mid}", mid);
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(UPLOAD_URL);
        getRequestSpecBuilder().setBody(body);
    }


}
