package com.paytm.api.AOA;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetByAoaMidGatewayName extends BaseApi {
    private String getURL= Constants.MappingService.AOA_GET_GATEWAY;
    public GetByAoaMidGatewayName(String mid)
    {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getURL=getURL.replace("{mid}", mid);
        getRequestSpecBuilder().setBasePath(getURL);
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
}
