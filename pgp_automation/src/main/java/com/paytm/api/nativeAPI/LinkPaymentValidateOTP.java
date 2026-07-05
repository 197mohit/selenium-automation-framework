package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class LinkPaymentValidateOTP extends BaseApi {

    public LinkPaymentValidateOTP(String otp, String state, String uniqueId){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.Link_Payment_Validate_OTP);
        getRequestSpecBuilder().setBody("{\n" +
                "    \"otp\": \"" + otp + "\",\n" +
                "    \"state\": \"" + state + "\",\n" +
                "    \"uniqueId\": \"" + uniqueId + "\"\n" +
                "}");
    }
}
