package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class MccCodeDetail extends BaseApi {
    public MccCodeDetail(String tid, String bank)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingService.MCC_CODE
                .replace("{TID}",tid).replace("{BANK}",bank);
        getRequestSpecBuilder().setBasePath(basePath);
    }
}
