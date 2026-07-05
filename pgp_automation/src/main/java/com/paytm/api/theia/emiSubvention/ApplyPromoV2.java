package com.paytm.api.theia.emiSubvention;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ApplyPromoV2 extends BaseApi {



    public ApplyPromoV2(String body , String mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.NativeAPIResourcePath.APPLY_PROMO_V2);
        getRequestSpecBuilder().setBody(body);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
}
