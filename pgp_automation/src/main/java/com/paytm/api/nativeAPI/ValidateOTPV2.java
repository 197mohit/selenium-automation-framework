package com.paytm.api.nativeAPI;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.validateOtp.ValidateOtp;
import com.paytm.dto.upiIntent.staticQR.Header;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class ValidateOTPV2 extends BaseApi {

    public ValidateOTPV2(String txnToken, String otp, String mid, String orderId ) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.VALIDATE_OTP_V2);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(new ValidateOtp(txnToken,otp));
    }

}

