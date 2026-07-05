package com.paytm.api.coft.saveCard;

import static com.paytm.apphelpers.PGPHelpers.getFromALIPAY_USER;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.savedCard;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FetchCardsFromCardCenter extends BaseApi {

  String request = "{\n"
      + "    \"targetType\": \"MERCHANT_USER_CARD\",\n"
      + "    \"merchantId\": \"qa8abh33054038637431\",\n"
      + "    \"externalUserId\": \"Test101\",\n"
      + "    \"paytmMid\": \"qa8abh33054038637431\",\n"
      + "    \"includeExpiredTokens\": false,\n"
      + "    \"includeTokenShortcuts\": false,\n"
      + "    \"includeExpiredCards\": false,\n"
      + "    \"querySubAssetTypes\": [\n"
      + "        \"CARD_NETWORK\",\n"
      + "        \"CARD_ISSUER\"\n"
      + "    ],\n"
      + "    \"querySubAssetTypePriority\": \"CARD_NETWORK_AND_CARD_ISSUER\"\n"
      + "}";

  public FetchCardsFromCardCenter() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().addHeader("requestId", "eee");
    getRequestSpecBuilder().addHeader("client-token", getJwtToken());
    getRequestSpecBuilder().addHeader("client-id", "PAYTM_THEIA");
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.CARD_CENTER);
    getRequestSpecBuilder().setBasePath(savedCard.CARD_CENTER_FETCH_ALL);
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {
    return request;
  }

  public FetchCardsFromCardCenter buildRequest(String targetType, String mid, String custId,
      String paytmMid,
      ArrayList<String> querySubAssetTypes, String querySubAssetTypePriority) {
    setContext("targetType", targetType);
    setContext("merchantId", mid);
    setContext("externalUserId", custId);
    setContext("paytmMid", paytmMid);
    setContext("querySubAssetTypes", querySubAssetTypes);
    setContext("querySubAssetTypePriority", querySubAssetTypePriority);
    return this;
  }

  public FetchCardsFromCardCenter buildRequest(
      String targetType, User user,
      String vaultIdentifier, String querySubAssetTypePriority,
      ArrayList<String> querySubAssetTypes) {
    setContext("targetType", targetType);
    String alipayUserId = getFromALIPAY_USER(user.custId()).get("oldpg_id").toString();
    setContext("userId", alipayUserId);
    setContext("paytmUserId", user.custId());
    setContext("vaultIdentifier", vaultIdentifier);
    setContext("querySubAssetTypes", querySubAssetTypes);
    setContext("querySubAssetTypePriority", querySubAssetTypePriority);
    deleteContext("merchantId");
    deleteContext("externalUserId");
    deleteContext("paytmMid");
    return this;
  }

  private String getJwtToken() {
    HashMap<String, Object> claims = new HashMap<>();
    claims.put("client-id", "PAYTM_THEIA");
    return generateJwtToken("7cdea2c5575d073ebc2b801dc867cee9c34a75c9e38fbc63754a6e1509892b4240621ffb46cddedad6489e9f5327dfebe05d3501956194608133f7ca3c5bd7a6",
        "CardService", SignatureAlgorithm.HS512, new HashMap<>(claims), claims);
  }

  public String generateJwtToken(final String key, final String issuer,
      final SignatureAlgorithm signatureAlgorithm, final Map<String, Object> headers,
      final Map<String, Object> claims) {
    return Jwts.builder()
        .setHeaderParams(headers)
        .setClaims(claims)
        .setIssuer(issuer)
        .setIssuedAt(new Date())
        .signWith(signatureAlgorithm, Base64.getDecoder().decode(key))
        .compact();
  }

}
