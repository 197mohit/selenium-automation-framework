package com.paytm.api.MappingService;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class GetMerchantPreferenceInfoExt extends BaseApi {

    public GetMerchantPreferenceInfoExt(String mid, String preference) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.GET_MERCHANT_PREFRENCE_INFO_EXT.replace("{mid}", mid));
        getRequestSpecBuilder().addQueryParam("prefType", preference);
        getRequestSpecBuilder().addHeader("Authorization", "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }
}
