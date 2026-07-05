package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.sendOtp.SendOtp;
import com.paytm.dto.NativeDTO.validateOtp.ValidateOtp;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * Created by anjukumari on 22/10/18
 */
public class ValidateOTP extends BaseApi{
    public ValidateOTP(String txnToken, String otp, String mid, String orderId ) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.VALIDATE_OTP);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        getRequestSpecBuilder().setBody(new ValidateOtp(txnToken,otp));
    }
}
