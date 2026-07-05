package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.checkEMIEligibility.CheckEMIEligibilityRequest;
import com.paytm.dto.NativeDTO.getEMIDetails.request.GetEMIDetailsRequest;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CheckEMIEligibility extends BaseApi{


    public CheckEMIEligibility(CheckEMIEligibilityRequest checkEMIEligibilityRequest, String mid) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.CHECK_EMI_ELIGIBILITY);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().setBody(checkEMIEligibilityRequest);
    }
}
