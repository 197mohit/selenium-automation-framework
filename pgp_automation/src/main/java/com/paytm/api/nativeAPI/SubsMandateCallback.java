package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SubsMandateCallback extends BaseApi {

    private final String SUBS_MANDATE_CALLBACK = "/subscription/mandate/callback";

    public SubsMandateCallback(String mandateReqId, String status) {
        String body = "{\"mandateReqId\": \""+mandateReqId+"PG1\",\"status\": \""+status+"\",\"umrn\": \"PYTM"+mandateReqId+"PG1\"}";
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(SUBS_MANDATE_CALLBACK);
        getRequestSpecBuilder().addHeader("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXF1ZXN0UmVmZXJlbmNlSWQiOiIxMjMiLCJpc3MiOiJQQVlUTSIsInBhcnRuZXJJZCI6IlNQT05TT1JfTkFDSF9QR1RfMDAxNTAzIiwidGltZXN0YW1wIjoxNTU0ODExMzQ3NzMxfQ.2eJ-SyQnkoIY9GO8_wzPHBg5__BUSRymP7Sj5cpQ2i8");
        getRequestSpecBuilder().setBody(body);
    }


}
