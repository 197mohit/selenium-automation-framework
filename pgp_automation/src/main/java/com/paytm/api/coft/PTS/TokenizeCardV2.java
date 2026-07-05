package com.paytm.api.coft.PTS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.coftCenter;
import com.paytm.apphelpers.CoftHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.dto.coft.CardData;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import org.apache.commons.lang.RandomStringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 30/12/21 11:52 AM
 * @Version 1.0
 */
public class TokenizeCardV2 extends BaseApi {

  String requestId= RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
  String request = "{\n"
      + "    \"head\": {\n"
      + "        \"version\": \"v1\",\n"
      + "        \"clientId\": \"PAYTM_THEIA\",\n"
      + "        \"requestTimestamp\": \"{{jwt_timestamp}}\",\n"
      + "        \"requestId\": \"{{orderId}}\",\n"
      + "        \"signatureType\": \"JWT\",\n"
      + "        \"signature\": \"{{jwt_signed}}\"\n"
      + "    },\n"
      + "    \"body\": {\n"
      + "        \"authRefId\": \"123456\",\n"
      + "        \"cardSource\": \"MANUAL_ENTERED\",\n"
      + "        \"encryptedCardData\": \"{{encryptedCardData}}\",\n"
      + "        \"userInfo\": {\n"
      + "            \"custId\": \"{}\",\n"
      + "            \"userId\": \"\",\n"
      + "            \"firstName\": \"Abhishek\",\n"
      + "            \"lastName\": \"Verma\",\n"
      + "            \"mobileNumber\": \"9899096972\"\n"
      + "        },\n"
      + "        \"isSaveAssetForUser\": true,\n"
      + "        \"isSaveAssetForMerchant\": true,\n"
      + "        \"tokenizationConsent\": {\n"
      + "            \"userConsent\": \"1\",\n"
      + "            \"createdAt\": \"Jul 22, 2021 02:46:54 PM\",\n"
      + "            \"userConsentId\": \"1234\"\n"
      + "        },\n"
      + "        \"providerTypes\"  : [\"CARD_NETWORK\", \"CARD_ISSUER\"]\n"
      + "    }\n"
      + "}";
  Gson gson = new Gson();

  public TokenizeCardV2(String mid, String cardScheme) {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
    getRequestSpecBuilder().setBasePath(coftCenter.TOKENIZE_CARD_V2);
    getRequestSpecBuilder().addPathParam("mid", mid);
    getRequestSpecBuilder().addQueryParam("cardScheme", cardScheme);
    getRequestSpecBuilder().addQueryParam("requestId", requestId);
    getRequestSpecBuilder().setBody(getRequest());
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
  }

  public TokenizeCardV2(String mid, String cardScheme, String requestId) {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
    getRequestSpecBuilder().setBasePath(coftCenter.TOKENIZE_CARD_V2);
    getRequestSpecBuilder().addPathParam("mid", mid);
    getRequestSpecBuilder().addQueryParam("cardScheme", cardScheme);
    getRequestSpecBuilder().addQueryParam("requestId", requestId);
    getRequestSpecBuilder().setBody(getRequest());
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
  }

  public String getRequest() {
    return request;
  }


  public TokenizeCardV2 buildRequest(String custId, String userConsent, String authRefId, String cardSource, String userId, boolean isSaveOnUser, boolean isSaveOnMidCustId,ArrayList<String> providerTypes)  {
    setContext("body.userInfo.custId", custId);
    setContext("body.tokenizationConsent.userConsent", userConsent);
    setContext("body.authRefId", authRefId);
    setContext("body.cardSource", cardSource);
    setContext("body.userInfo.userId", userId);
    setContext("body.isSaveAssetForUser", isSaveOnUser);
    setContext("body.isSaveAssetForMerchant", isSaveOnMidCustId);
    setContext("body.providerTypes",providerTypes);
    return this;
  }

  public TokenizeCardV2 buildRequest(String userConsent, String authRefId, String cardSource, String userId, boolean isSaveOnUser, boolean isSaveOnMidCustId,ArrayList<String> providerTypes)  {
    setContext("body.tokenizationConsent.userConsent", userConsent);
    setContext("body.authRefId", authRefId);
    setContext("body.cardSource", cardSource);
    setContext("body.userInfo.userId", userId);
    setContext("body.isSaveAssetForUser", isSaveOnUser);
    setContext("body.isSaveAssetForMerchant", isSaveOnMidCustId);
    setContext("body.providerTypes",providerTypes);
    return this;
  }

  public TokenizeCardV2 buildRequest(String mid,String custId, String userConsent, String authRefId, String cardSource, String userId, boolean isSaveOnUser, boolean isSaveOnMidCustId,ArrayList<String> providerTypes)  {
    setContext("body.userInfo.custId", custId);
    setContext("body.tokenizationConsent.userConsent", userConsent);
    setContext("body.authRefId", authRefId);
    setContext("body.cardSource", cardSource);
    setContext("body.userInfo.userId", userId);
    setContext("body.isSaveAssetForUser", isSaveOnUser);
    setContext("body.isSaveAssetForMerchant", isSaveOnMidCustId);
    setContext("body.providerTypes",providerTypes);
    return this;
  }

  public TokenizeCardV2 generateJwt(String mid)
  {
    String requestTimeStamp = String.valueOf(Instant.now().toEpochMilli());
    String jwtToken = SavedCardHelpersNew.jwtSignatureTokenizeMidCustId(requestId,mid,requestTimeStamp);
    setContext("head.requestId",requestId);
    setContext("head.signature", jwtToken);
    setContext("head.signatureType", "JWT");
    setContext("head.requestTimestamp",requestTimeStamp);
    return this;
  }
  public TokenizeCardV2 encryptCardData(String cardNumber, String expiryMonth, String expiryYear, String cvv)
       {
    CardData cardDataObject = new CardData();
    cardDataObject.setCardData(cardNumber, expiryMonth, expiryYear, cvv);
    String encryptedCardData = CoftHelper.encryptCardData(cardDataObject);
    setContext("body.encryptedCardData", encryptedCardData);
    return this;
  }
}
