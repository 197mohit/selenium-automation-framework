package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;


public class CloseOrder extends BaseApi {

    public CloseOrder(String orderId , String mid , String ssoToken) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/closeOrder");

        //to remove charset from content type
        EncoderConfig encoderConfig = new EncoderConfig();
        getRequestSpecBuilder().setConfig(RestAssuredConfig.config()
                .encoderConfig(encoderConfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .setContentType(ContentType.JSON);
        Map<String,String> request = new HashMap<>();
        request.put("ORDER_ID",orderId);
        request.put("MID",mid);
        request.put("USER_TOKEN",ssoToken);
        getRequestSpecBuilder().setBody(request);
    }

    /**
     * Constructor with extra IS_FORCE_CLOSE param to close the order
     * @param orderId
     * @param mid
     * @param IS_FORCE_CLOSE
     */
    public CloseOrder(String orderId , String mid , boolean IS_FORCE_CLOSE) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath("/theia/closeOrder");

        //to remove charset from content type
        EncoderConfig encoderConfig = new EncoderConfig();
        getRequestSpecBuilder().setConfig(RestAssuredConfig.config()
                        .encoderConfig(encoderConfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .setContentType(ContentType.JSON);
        Map<String,Object> request = new HashMap<>();
        request.put("ORDER_ID",orderId);
        request.put("MID",mid);
        request.put("IS_FORCE_CLOSE",IS_FORCE_CLOSE);
        getRequestSpecBuilder().setBody(request);
    }

}
