package com.paytm.api.coft.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;

import static com.paytm.apphelpers.PGPHelpers.getFromALIPAY_USER;

public class DeleteCardInCardCenter extends BaseApi {

  public DeleteCardInCardCenter(User user, String cardId) {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().addHeader("requestId", "eee");
    getRequestSpecBuilder().addHeader("client-token", getJwtToken());
    getRequestSpecBuilder().addHeader("client-id", "PAYTM_COFT_CENTER");
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.CARD_CENTER);
    getRequestSpecBuilder().setBasePath(Constants.savedCard.DELETE_USER_CARD);
    String alipayUserId = getFromALIPAY_USER(user.custId()).get("oldpg_id").toString();
    getRequestSpecBuilder().setBody(getRequest(alipayUserId, cardId, user.custId()));

  }

  public DeleteCardInCardCenter(User user, String cardId, String ppblVaultmid) {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().addHeader("requestId", "eee");
    getRequestSpecBuilder().addHeader("client-token", getJwtToken());
    getRequestSpecBuilder().addHeader("client-id", "PAYTM_COFT_CENTER");
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setBaseUri(LocalConfig.CARD_CENTER);
    getRequestSpecBuilder().setBasePath(Constants.savedCard.DELETE_USER_CARD);
    String alipayUserId = getFromALIPAY_USER(user.custId()).get("oldpg_id").toString();
    getRequestSpecBuilder().setBody(getRequest(alipayUserId, cardId, user.custId(), ppblVaultmid));

  }

  String request = "{\n" +
      "    \"targetType\": \"PAYTM_USER_CARD\",\n" +
      "   \"userId\":\"216810000002758122014\",\n" +
      "    \"cardId\": \"63b2be3ec044290d546dc7b6\"\n" +
      "}";

  public String getRequest(String alipayUserId, String cardId, String paytmUserId) {
    setContext("targetType", "PAYTM_USER_CARD");
    setContext("userId", alipayUserId);
    setContext("cardId", cardId);
    setContext("paytmUserId", paytmUserId);
    return request;
  }

  public String getRequest(String alipayUserId, String cardId, String paytmUserId,
      String ppblVaultmid) {
    setContext("targetType", "PAYTM_USER_CARD");
    setContext("userId", alipayUserId);
    setContext("cardId", cardId);
    setContext("paytmUserId", paytmUserId);
    setContext("paytmMid", ppblVaultmid);
    setContext("vaultIdentifier", ppblVaultmid);
    return request;
  }

  private String getJwtToken() {
    HashMap<String, Object> claims = new HashMap<>();
    claims.put("client-id", "PAYTM_COFT_CENTER");
    return generateJwtToken("oKiJJ7Q/xlDW7fzR8D3oRA==",
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
