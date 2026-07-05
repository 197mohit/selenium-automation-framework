package com.paytm.api.MappingService.ClearCache;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;

public class RemoveMerchantAttributePreferenceInfoCache extends BaseApi {

    public RemoveMerchantAttributePreferenceInfoCache(String id){
        setMethod(MethodType.GET);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.REMOVE_CACHE_ATTRIBUTE_PREFERENCE
                .replace("{p+mid}", id));
    }
}