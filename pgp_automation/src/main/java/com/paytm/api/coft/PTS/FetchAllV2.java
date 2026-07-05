package com.paytm.api.coft.PTS;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.coftCenter;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FetchAllV2 extends BaseApi {

  String request = "{\n" +
      "    \"targetType\": \"TARGET_TYPE\",\n" +
      "    \"userId\": \"userId\",\n" +
      "    \"merchantId\": \"merchantId\",\n" +
      "    \"externalUserId\": \"externalUserId\",\n" +
      "    \"includeExpiredTokens\": false,\n" +
      "    \"includeExpiredCards\": false\n" +
      "}";

  public FetchAllV2() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.CARD_CENTER);
    getRequestSpecBuilder().setBasePath(coftCenter.FETCH_ALL_V2);
    getRequestSpecBuilder().setBody(getRequest());
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().addHeader("requestId", "1234");
    getRequestSpecBuilder().addHeader("client-id", LocalConfig.CARD_SERVICE_THEIA_CLIENT_ID);
    getRequestSpecBuilder().addHeader("client-id", LocalConfig.CARD_SERVICE_THEIA_CLIENT_ID);
    getRequestSpecBuilder().addHeader("client-token",
        getCardServiceJwtToken(LocalConfig.CARD_SERVICE_THEIA_CLIENT_ID,
            LocalConfig.CARD_SERVICE_THEIA_CLIENT_SECRET));
  }

  public String getRequest() {
    return request;
  }

  public FetchAllV2 buildRequest(String targetType, String merchantId, String externalUserId) {
    setContext("targetType", targetType);
    setContext("merchantId", merchantId);
    setContext("externalUserId", externalUserId);
    deleteContext("userId");
    return this;
  }

  public FetchAllV2 buildRequest(String targetType, String userId,String paytmUserId,String merchantId, String paytmMid) {
    setContext("targetType", targetType);
    setContext("userId", userId);
    setContext("paytmUserId",paytmUserId);
    setContext("merchantId",merchantId);
    setContext("paytmMid",paytmMid);
    deleteContext("externalUserId");
    return this;
  }

  public String getCardServiceJwtToken(String clientId, String clientKey) {
    byte[] decodedKey = Base64.getDecoder().decode(clientKey);
    System.out.println("decodedKey is: " + decodedKey);
    Map<String, Object> claims = new HashMap<String, Object>();
    claims.put("client-id", clientId);

    return Jwts.builder().setHeader(claims).setIssuer("CardService").addClaims(claims)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .signWith(SignatureAlgorithm.HS512, decodedKey)
        .compact();
  }
}
