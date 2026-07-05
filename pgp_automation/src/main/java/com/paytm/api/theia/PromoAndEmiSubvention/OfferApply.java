package com.paytm.api.theia.PromoAndEmiSubvention;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
public class OfferApply extends BaseApi {
    public OfferApply(String body){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.OFFER_APPLY);
        getRequestSpecBuilder().setBody(body);
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public OfferApply(String body,String referenceId ){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.OFFER_APPLY);
        getRequestSpecBuilder().setBody(body);
        getRequestSpecBuilder().addQueryParam("referenceId", referenceId);
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
}
