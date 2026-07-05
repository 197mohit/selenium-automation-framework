package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ExtendedInfoDetail extends BaseApi {
        public ExtendedInfoDetail(String mid)
        {
                setMethod(BaseApi.MethodType.GET);
                getRequestSpecBuilder().setContentType(ContentType.JSON);
                getRequestSpecBuilder().setAccept(ContentType.JSON);
                getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
                getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
                String basePath = Constants.MappingService.MERCHANT_EXTENDED_INFO
                        .replace("{mid}",mid);
                getRequestSpecBuilder().setBasePath(basePath);
        }
}
