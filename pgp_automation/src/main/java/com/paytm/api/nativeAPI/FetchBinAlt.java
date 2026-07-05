package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchBinAlt extends BaseApi {

    public FetchBinAlt(String bin, String version) {

        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);

        if(version==null) {
            String basePath = Constants.MappingService.Alt_BIN_DETAILS_API
                    .replace("{bin}", bin);
            getRequestSpecBuilder().setBasePath(basePath);
        }
        else{
            String basePath = Constants.MappingService.Alt_BIN_DETAILS
                    .replace("{bin}",bin);
            getRequestSpecBuilder().setBasePath(basePath);
        }

    }
}
