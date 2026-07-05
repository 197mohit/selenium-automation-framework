package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;

public class GetMerchantAttributePreferenceInfo extends BaseApi {

    public GetMerchantAttributePreferenceInfo(String mid, String alipayId){
        setMethod(MethodType.GET);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.MERCHANT_ATTRIBUTE_PREFERENCE
                .replace("{mid}", mid)
                .replace("{id}", alipayId));
    }
}