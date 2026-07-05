package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;

public class GetMerchantPreferenceInfo extends BaseApi {

    public GetMerchantPreferenceInfo(String mid){
        setMethod(MethodType.GET);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.GET_MERCH_PREFERENCE_INFO
                .replace("{mid}", mid));
    }
}