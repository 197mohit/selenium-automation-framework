package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.validateOtp.ValidateOtp;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

public class LogoutUserV2 extends BaseApi {

    public LogoutUserV2(String txnToken, String mid, String orderId ) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderId",orderId);
        map.put("mid",mid);
        map.put("txnToken",txnToken);
        this.setMethod(BaseApi.MethodType.POST);
        this.getRequestSpecBuilder().setContentType(ContentType.JSON);
        this.getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        this.getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.LOGOUT_USER_V2);
        this.getRequestSpecBuilder().addQueryParam("mid", mid);
        this.getRequestSpecBuilder().addQueryParam("orderId", orderId);
        this.getRequestSpecBuilder().setBody(new ValidateOtp(map));
    }






}
