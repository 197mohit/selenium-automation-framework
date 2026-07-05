package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.NativeAPIResourcePath;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import java.nio.charset.StandardCharsets;

public class V4ValidateVpa extends BaseApi {

  String request = "{\"head\":{\"channelId\":\"WAP\",\"requestTimestamp\":\"1712316800821\",\"clientId\":\"supergw\"},\"body\":{\"mid\":\"qa8abh33054038637431\",\"vpa\":\"paytm.uat@axis\",\"requestType\":\"NATIVE\"}}";


  public V4ValidateVpa() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(NativeAPIResourcePath.V4_VALIDATE_VPA);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {return request;}


  public V4ValidateVpa buildRequest(String mid, String referenceId, Long jwtTimeStamp,
      String vpa,String jwtSecret, String iss) {
    getRequestSpecBuilder().addHeader("JWT_TOKEN",createJwt(mid,vpa,jwtTimeStamp, iss,jwtSecret));
    getRequestSpecBuilder().addQueryParam("mid",mid);
    getRequestSpecBuilder().addQueryParam("referenceId",referenceId);
    setContext("head.requestTimestamp", jwtTimeStamp);
    setContext("body.mid", mid);
    setContext("body.vpa", vpa);
    return this;
  }

  // Create JWT
  public String createJwt(String mid,String vpa,Long currentTimeStamp,String iss,String jwtSecret)
  {
    return Jwts.builder()
        .setHeaderParam("typ", "JWT")
        .setHeaderParam("alg", "HS256")
        .setIssuer(iss)
        .claim("mid", mid)
        .claim("iat", currentTimeStamp)
        .claim("vpa", vpa)
        .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes(StandardCharsets.UTF_8))
        .compact();
  }
}
