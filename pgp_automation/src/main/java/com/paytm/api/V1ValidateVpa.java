package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.NativeAPIResourcePath;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApi.MethodType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import static com.paytm.LocalConfig.JWT_KEY;

public class V1ValidateVpa extends BaseApi {



  String request = "{\"head\":{\"version\":\"v1\",\"requestTimestamp\":\"1713951144651\",\"requestId\":\"81700a8b-c6c7-4ba3-94e3-3a8799ef7840\",\"channelId\":\"WEB\",\"clientId\":\"theiavalidatevpa\",\"tokenType\":\"SSO\",\"token\":\"0b1ky7ft4u9irzuwb8bx82ttwjscfx620140\"},\"body\":{\"vpa\":\"9999661503@paytm\",\"mid\":\"qa8NIR86671481652340\"}}";



  public V1ValidateVpa() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(NativeAPIResourcePath.VPA_VALIDATE);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {return request;}

  public V1ValidateVpa buildRequest(String tokenType, String token,String vpa,String orderId,String numericId,String mid,String phoneNo) {
    getRequestSpecBuilder().addQueryParam("mid",mid);
    getRequestSpecBuilder().addQueryParam("orderId",orderId);
    setContext("head.tokenType", tokenType);
    setContext("head.token", token);
    setContext("body.numericId", numericId);
    setContext("body.mid", mid);
    setContext("body.vpa", vpa);
    setContext("body.phoneNo", phoneNo);
    return this;
  }

  public V1ValidateVpa buildRequestwithJWT( String vpa,String orderId,String mid,String txnToken)
  {
    getRequestSpecBuilder().addQueryParam("mid",mid);
    getRequestSpecBuilder().addQueryParam("orderId",orderId);
    setContext("head.tokenType", "JWT");
    setContext("head.token", createJWTValidateVpa(mid,vpa));
    setContext("body.mid", mid);
    setContext("body.vpa", vpa);
    setContext("body.orderId",orderId);
    setContext("head.txnToken",txnToken);
    return this;
  }

  public String createJWTValidateVpa(String mid, String vpa)
  {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("mid", mid);
    tokenMap.put("vpa",vpa);
    String jwtToken = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.theiavalidatevpa,"mX0V6lyzZLJMdqZK1DPv94FN68Tm+F0MqO+8KYMcG+I=");
    return jwtToken;

  }
}
