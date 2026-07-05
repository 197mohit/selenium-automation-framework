package com.paytm.api.MappingService;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class MerchantAddPreferenceInfoExt extends BaseApi {


    public MerchantAddPreferenceInfoExt(MerchantAddPreferenceInfoReq o, String mid) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.MappingService.ADD_MERCHANT_PREFRENCE_INFO_EXT
        .replace("{mid}", mid));
        getRequestSpecBuilder().setBody(o);
    }

}
