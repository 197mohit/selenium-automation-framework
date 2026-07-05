package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;

public class GetBrandEmiDetails extends BaseApi{


    private String getbrandEmiSetailUrl= Constants.MappingService.GET_BRAND_EMI;

    public GetBrandEmiDetails(String mid,String brands) {

        getbrandEmiSetailUrl = getbrandEmiSetailUrl.replace("{mid}", mid);
//        getbrandEmiSetailUrl= getbrandEmiSetailUrl.replace("{brands}",brands);
        setMethod(BaseApi.MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST);
        requestSpecBuilder.setBasePath(getbrandEmiSetailUrl);
        requestSpecBuilder.addQueryParam("brandCodes",brands);

    }
}
