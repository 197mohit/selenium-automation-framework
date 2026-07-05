package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class MerchantProfile extends BaseApi {

    private String getMerchantProfileurl = Constants.MappingService.MERCHANT_PROFILE;

    public MerchantProfile(String mid,String id){
        getMerchantProfileurl = getMerchantProfileurl.replace("{mid}",mid);
        getMerchantProfileurl = getMerchantProfileurl.replace("{id}",id);
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(getMerchantProfileurl);

    }
}