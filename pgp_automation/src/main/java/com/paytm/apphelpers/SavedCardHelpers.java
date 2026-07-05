package com.paytm.apphelpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.paytm.LocalConfig;
import com.paytm.api.coft.PTS.TokenizeCardV2;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.api.coft.saveCard.FetchCardsFromCardCenter;
import com.paytm.api.coft.saveCard.HandlerInternalDeleteBinUser;
import com.paytm.api.coft.saveCard.HanlerInternalBinInfo;
import com.paytm.api.coft.saveCard.SavedCardByMidCustId;
import com.paytm.api.saveCard.SavedCardApi;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.TokenFields;
import com.paytm.appconstants.Constants.TokenStatus;
import com.paytm.appconstants.Constants.VAULTIDENTIFIER;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.saveCard.SaveCardRequestGeneric;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.dto.saveCard.saveTrustedCardRequest;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.user.alipay.AlipayUser;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.exception.authException.AuthException;
import com.paytm.utils.merchant.util.exception.pgpException.NoResultFoundException;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.bson.Document;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.SkipException;
import org.testng.asserts.SoftAssert;


import java.io.IOException;
import java.util.concurrent.Callable;


public class SavedCardHelpers {

  private Response response;
  private JsonPath jsonPath;

  private static String tin;
  private static String gcin;
  private static String bin;
  private static String lastFourDigit;

  public static String getIssuerTin() {
    return issuerTin;
  }

  public static void setIssuerTin(String issuerTin) {
    SavedCardHelpers.issuerTin = issuerTin;
  }

  public static String getBin() {
    return bin;
  }

  public static String getLastFourDigit() {
    return lastFourDigit;
  }

  private static String issuerTin;

  public Response getResponse() {
    return response;
  }

  public SavedCardHelpers setResponse(Response response) {
    this.response = response;
    return this;
  }

  public JsonPath getJsonPath() {
    return jsonPath;
  }

  public SavedCardHelpers setJsonPath(JsonPath jsonPath) {
    this.jsonPath = jsonPath;
    return this;
  }

  public static String getTin() {
    return tin;
  }

  public static String getGcin() {
    return gcin;
  }

  public static void setTin(String tokenIndexNumber) {
    tin = tokenIndexNumber;
  }

  public static void setGcin(String globalPanIndex) {
    gcin = globalPanIndex;
  }

  public static void setTokenBin(String tokenBin) {
    bin = tokenBin;
  }

  public static void setcardSuffix(String cardSuffix) {
    lastFourDigit = cardSuffix;
  }

   /*
    @Step("Add Card on user")
    public synchronized static void addCard(User user, String expiryMonth, String expiryYear, String cardNumber) throws AuthException {
        String custId = user.custId();
        PGPUtil.addCard(LocalConfig.PGP_HOST, custId, expiryMonth, expiryYear, cardNumber);
        //SavedCardHelpers.validateSavedCardPresence(user);
    }

    */

  @Step("Add Card on user")
  public synchronized static void addCard(User user, String expiryMonth, String expiryYear,
      String cardNumber) throws Exception {
    System.out.println("Adding card on user");
    TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard("PAYTM197").buildRequest(null,
            Constants.TokenizationConsent.YES.get(), "BwACAkYlhgICEwADMTE2EAAAAAA=", Constants.CardSource.MANUAL_ENTERED.get(),
            user.custId(), true, true)
        .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt("PAYTM197");
    JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    String actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
    String tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
    setTin(tin);
    String cardSuffix = tokenizeCardResponse.getString("body.tokenInfo.cardSuffix");
    setcardSuffix(cardSuffix);
    int count = 5;
    while (actual.equalsIgnoreCase("Request timeout at card network")
        || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
      SavedCardHelpersNew.modifyTokenUserId(user, tin, Constants.TokenStatus.DEAD.get());
      Thread.sleep(5000);
      TokenizeDirectCard tokenizeDirectCard1 = new TokenizeDirectCard("PAYTM197").buildRequest(null,
              Constants.TokenizationConsent.YES.get(), null, Constants.CardSource.MANUAL_ENTERED.get(),
              user.custId(), true, true)
          .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt("PAYTM197");
      tokenizeCardResponse = tokenizeDirectCard1.execute().jsonPath();
      actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
      tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
      setTin(tin);
      count--;
    }
    String[] expected = {
        "SUCCESS",
        "Token has already been created"};
    softly.assertThat(Arrays.asList(expected)).contains(actual);
    softly.assertAll();

    HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(user.ssoToken(), true,
        VAULTIDENTIFIER.OCL.get());
    JsonPath binInfoResponse = binInfo.execute().jsonPath();
    int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
    count = 0;
    while (count < 5 && size == 0) {
      Thread.sleep(5000);
      binInfoResponse = binInfo.execute().jsonPath();
      size = Integer.parseInt(binInfoResponse.getString("SIZE"));
      count++;
    }

    if (size == 0) {
      SavedCardHelpersNew.modifyTokenUserId(user, tin, Constants.TokenStatus.DEAD.get());
      TokenizeDirectCard tokenizeDirectCard2 = new TokenizeDirectCard("PAYTM197").buildRequest(null,
              Constants.TokenizationConsent.YES.get(), null, Constants.CardSource.MANUAL_ENTERED.get(),
              user.custId(), true, true)
          .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt("PAYTM197");
      tokenizeCardResponse = tokenizeDirectCard2.execute().jsonPath();
      actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
      tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
      setTin(tin);
      count = 5;
      while (actual.equalsIgnoreCase("Request timeout at card network")
          || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
        SavedCardHelpersNew.modifyTokenUserId(user, tin, Constants.TokenStatus.DEAD.get());
        Thread.sleep(5000);
        TokenizeDirectCard tokenizeDirectCard3 = new TokenizeDirectCard("PAYTM197").buildRequest(null,
                Constants.TokenizationConsent.YES.get(), null, Constants.CardSource.MANUAL_ENTERED.get(),
                user.custId(), true, true)
            .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt("PAYTM197");
        tokenizeCardResponse = tokenizeDirectCard3.execute().jsonPath();
        actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
        tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
        setTin(tin);
        count--;
      }
      softly.assertThat(Arrays.asList(expected)).contains(actual);
      softly.assertAll();
      binInfoResponse = binInfo.execute().jsonPath();
      size = Integer.parseInt(binInfoResponse.getString("SIZE"));
      count = 0;
      while (count < 5 && size == 0) {
        Thread.sleep(5000);
        binInfoResponse = binInfo.execute().jsonPath();
        size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        count++;
      }
    }
  }

  @Step("Add Card on user in other Vaults")
  public synchronized static void addCardInOtherVaults(User user, String expiryMonth,
      String expiryYear, String cardNumber,
      String vaultType) throws Exception {
    TokenizeDirectCard tokenizeDirectCard;
    MerchantType merchantType;
    if (vaultType.equalsIgnoreCase("PPBL")) {
      merchantType = MerchantType.PPBL_VAULT_MID;
      tokenizeDirectCard = new TokenizeDirectCard(
          merchantType.getId()).buildRequest(merchantType.getId(), null,
              Constants.TokenizationConsent.YES.get(), null,
              Constants.CardSource.MANUAL_ENTERED.get(), user.custId(), true, true)
          .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(
              merchantType.getId());
    } else {

      merchantType = MerchantType.PMALL_VAUTL_MID;
      tokenizeDirectCard = new TokenizeDirectCard(
          merchantType.getId()).buildRequest(merchantType.getId(), null,
              Constants.TokenizationConsent.YES.get(), null,
              Constants.CardSource.MANUAL_ENTERED.get(), user.custId(), true, true)
          .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(
              merchantType.getId());
    }
    JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    String actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
    String tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
    setTin(tin);
    String cardSuffix = tokenizeCardResponse.getString("body.tokenInfo.cardSuffix");
    setcardSuffix(cardSuffix);
    int count = 5;
    while (actual.equalsIgnoreCase("Request timeout at card network")
        || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
      SavedCardHelpersNew.modifyTokenUserIdInOtherVault(user, tin, Constants.TokenStatus.DEAD.get(),
          merchantType.getId());
      Thread.sleep(5000);
      tokenizeDirectCard = new TokenizeDirectCard(
        merchantType.getId()).buildRequest(merchantType.getId(), null,
            Constants.TokenizationConsent.YES.get(), null,
            Constants.CardSource.MANUAL_ENTERED.get(), user.custId(), true, true)
        .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(
            merchantType.getId());
      tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
      actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
      tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
      setTin(tin);
      count--;
    }
    String[] expected = {
        "SUCCESS",
        "Token has already been created"};
    softly.assertThat(Arrays.asList(expected)).contains(actual);
    softly.assertAll();
    HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(user.ssoToken(), true, vaultType);
    JsonPath binInfoResponse = binInfo.execute().jsonPath();
    int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
    count = 0;
    while (count < 3 && size == 0) {
      Thread.sleep(5000);
      binInfoResponse = binInfo.execute().jsonPath();
      size = Integer.parseInt(binInfoResponse.getString("SIZE"));
      count++;
    }

    if (size == 0) {
      SavedCardHelpersNew.modifyTokenUserIdInOtherVault(user, tin, Constants.TokenStatus.DEAD.get(),
          merchantType.getId());
          tokenizeDirectCard = new TokenizeDirectCard(
            merchantType.getId()).buildRequest(merchantType.getId(), null,
                Constants.TokenizationConsent.YES.get(), null,
                Constants.CardSource.MANUAL_ENTERED.get(), user.custId(), true, true)
            .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(
                merchantType.getId());
      tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
      actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
      tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
      setTin(tin);
      count = 5;
      while (actual.equalsIgnoreCase("Request timeout at card network")
          || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
        SavedCardHelpersNew.modifyTokenUserIdInOtherVault(user, tin,
            Constants.TokenStatus.DEAD.get(),
            merchantType.getId());
        Thread.sleep(5000);
        tokenizeDirectCard = new TokenizeDirectCard(
        merchantType.getId()).buildRequest(merchantType.getId(), null,
            Constants.TokenizationConsent.YES.get(), null,
            Constants.CardSource.MANUAL_ENTERED.get(), user.custId(), true, true)
        .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(
            merchantType.getId());
        tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
        tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
        setTin(tin);
        count--;
      }
      softly.assertThat(Arrays.asList(expected)).contains(actual);
      softly.assertAll();
      binInfoResponse = binInfo.execute().jsonPath();
      size = Integer.parseInt(binInfoResponse.getString("SIZE"));
      count = 0;
      while (count < 3 && size == 0) {
        Thread.sleep(5000);
        binInfoResponse = binInfo.execute().jsonPath();
        size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        count++;
      }
    }
  }

  @Step("Add Card on midCustID")
  public synchronized static void addCardOnMidCustId(Constants.MerchantType merchant, String custId,
      String expiryMonth, String expiryYear, String cardNumber) throws Exception {
    TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(merchant.getId()).buildRequest(
            custId, Constants.TokenizationConsent.YES.get(), null,
            Constants.CardSource.MANUAL_ENTERED.get(), null, true, true)
        .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(merchant.getId());

    JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    String actual = tokenizeCardResponse.getString("body.resultInfo.resultMsg");
    String tin = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
    String gcin = tokenizeCardResponse.getString("body.tokenInfo.globalPanIndex");
    setGcin(gcin);
    setTin(tin);
    int count = 5;
    while (actual.equalsIgnoreCase("Request timeout at card network")
        || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
      SavedCardHelpersNew.modifyTokenMidCustId(merchant.getId(), tin, custId,
          Constants.TokenStatus.DEAD.get());
      Thread.sleep(5000);
      TokenizeDirectCard tokenizeDirectCard2 = new TokenizeDirectCard(merchant.getId()).buildRequest(
                      custId, Constants.TokenizationConsent.YES.get(), null,
                      Constants.CardSource.MANUAL_ENTERED.get(), null, true, true)
              .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(merchant.getId());
      JsonPath tokenizeCardResponse2 = tokenizeDirectCard2.execute().jsonPath();
      actual = tokenizeCardResponse2.getString("body.resultInfo.resultMsg");
      tin = tokenizeCardResponse2.getString("body.tokenInfo.tokenIndexNumber");
      setTin(tin);
      count--;
    }
    String[] expected = {
        "SUCCESS",
        "Token has already been created"};
    softly.assertThat(Arrays.asList(expected)).contains(actual);
    softly.assertAll();

    SavedCardByMidCustId savedCardByMidCustId = new SavedCardByMidCustId().buildRequest(merchant,
        custId, "CHECKSUM");
    JsonPath saveCardByMidCustIdResponse = savedCardByMidCustId.execute().jsonPath();
    if (saveCardByMidCustIdResponse.getList("response").size() == 0) {
      Thread.sleep((long) (10 * 1000));
      saveCardByMidCustIdResponse = savedCardByMidCustId.execute().jsonPath();
    }

    List saveCards = saveCardByMidCustIdResponse.getList("response");
    if (saveCards.size() == 0) {
      SavedCardHelpersNew.modifyTokenMidCustId(merchant.getId(), tin, custId,
          Constants.TokenStatus.DEAD.get());
      TokenizeDirectCard tokenizeDirectCard3 = new TokenizeDirectCard(merchant.getId()).buildRequest(
                      custId, Constants.TokenizationConsent.YES.get(), null,
                      Constants.CardSource.MANUAL_ENTERED.get(), null, true, true)
              .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(merchant.getId());
      JsonPath tokenizeCardResponse3 = tokenizeDirectCard3.execute().jsonPath();
      actual = tokenizeCardResponse3.getString("body.resultInfo.resultMsg");
      tin = tokenizeCardResponse3.getString("body.tokenInfo.tokenIndexNumber");
      setTin(tin);
      count = 5;
      while (actual.equalsIgnoreCase("Request timeout at card network")
          || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
        SavedCardHelpersNew.modifyTokenMidCustId(merchant.getId(), tin, custId,
            Constants.TokenStatus.DEAD.get());
        Thread.sleep(5000);
        TokenizeDirectCard tokenizeDirectCard4 = new TokenizeDirectCard(merchant.getId()).buildRequest(
                        custId, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.MANUAL_ENTERED.get(), null, true, true)
                .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(merchant.getId());
        JsonPath tokenizeCardResponse4 = tokenizeDirectCard4.execute().jsonPath();
        actual = tokenizeCardResponse4.getString("body.resultInfo.resultMsg");
        tin = tokenizeCardResponse4.getString("body.tokenInfo.tokenIndexNumber");
        setTin(tin);
        count--;
      }
      softly.assertThat(Arrays.asList(expected)).contains(actual);
      softly.assertAll();
    }

  }

  @Step("Add Issuer token on user")
  public synchronized static void addIssuerTokenOnUser(User user, String expiryMonth,
      String expiryYear,
      String cardNumber, ArrayList<String> providerTypes, String mid) throws Exception {

    TokenizeCardV2 tokenizeCardV2 = new TokenizeCardV2(mid, "VISA", "1234").buildRequest(
            user.custId(),
            Constants.TokenizationConsent.YES.get(), null,
            Constants.CardSource.MANUAL_ENTERED.get(), user.custId(), true, true, providerTypes)
        .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(mid);

    JsonPath tokenizeCardV2Response = tokenizeCardV2.execute().jsonPath();
    String actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");

    int count = 5;
    while (actual.equalsIgnoreCase("Request timeout at card network")
        || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
      String token = tokenizeCardV2Response.getString("body.tokenInfos[0].tokenIndexNumber");
      SavedCardHelpersNew.modifyTokenUserId(user, token, Constants.TokenStatus.DEAD.get());
      Thread.sleep(5000);
      tokenizeCardV2Response = tokenizeCardV2.execute().jsonPath();
      actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");
      count--;
    }
    String[] expected = {
        "ACCEPTED",
        "Token has already been created"};
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(Arrays.asList(expected)).contains(actual);
    softly.assertAll();

    String jsonString = tokenizeCardV2Response.prettyPrint();
    JsonParser parser = new JsonParser();
    JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject().getAsJsonObject("body");
    JsonArray tokenInfos = jsonObject.getAsJsonArray("tokenInfos");
    for (int i = 0; i < tokenInfos.size(); i++) {
      JsonObject tokenInfo = tokenInfos.get(i).getAsJsonObject();
      String providerType = tokenInfo.get("providerType").getAsString();

      if (providerType.equals("CARD_ISSUER")) {
        String issuerToken = tokenInfo.get("tokenIndexNumber").getAsString();
        setIssuerTin(issuerToken);
        String gcin = tokenInfo.get("globalPanIndex").getAsString();
        setGcin(gcin);
      }
      if (providerType.equals("CARD_NETWORK")) {
        String networkToken = tokenInfo.get("tokenIndexNumber").getAsString();
        setTin(networkToken);
        String gcin = tokenInfo.get("globalPanIndex").getAsString();
        setGcin(gcin);
      }
    }

    FetchCardsFromCardCenter fetchCardsFromCardCenter = new FetchCardsFromCardCenter().buildRequest(
        "PAYTM_USER_CARD", user, "OCL", "CARD_NETWORK_AND_CARD_ISSUER", providerTypes);

    JsonPath fetchCardsResponse = fetchCardsFromCardCenter.execute().jsonPath();

    if (fetchCardsResponse.getList("cardInfos").size() == 0) {
      Thread.sleep((long) (10 * 1000));
      fetchCardsResponse = fetchCardsFromCardCenter.execute().jsonPath();
    }
    List saveCards = fetchCardsResponse.getList("cardInfos");

    if (saveCards.size() == 0) {
      SavedCardHelpersNew.modifyTokenUserId(user, tin, Constants.TokenStatus.DEAD.get());

      tokenizeCardV2Response = tokenizeCardV2.execute().jsonPath();
      actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");

      count = 5;
      while (actual.equalsIgnoreCase("Request timeout at card network")
          || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
        String token = tokenizeCardV2Response.getString("body.tokenInfos[0].tokenIndexNumber");
        SavedCardHelpersNew.modifyTokenUserId(user, token, Constants.TokenStatus.DEAD.get());
        Thread.sleep(5000);
        tokenizeCardV2Response = tokenizeCardV2.execute().jsonPath();
        actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");
        count--;
      }
      softly.assertThat(Arrays.asList(expected)).contains(actual);
      softly.assertAll();

      jsonString = tokenizeCardV2Response.prettyPrint();
      parser = new JsonParser();
      jsonObject = parser.parse(jsonString).getAsJsonObject().getAsJsonObject("body");
      tokenInfos = jsonObject.getAsJsonArray("tokenInfos");
      for (int i = 0; i < tokenInfos.size(); i++) {
        JsonObject tokenInfo = tokenInfos.get(i).getAsJsonObject();
        String providerType = tokenInfo.get("providerType").getAsString();

        if (providerType.equals("CARD_ISSUER")) {
          String issuerToken = tokenInfo.get("tokenIndexNumber").getAsString();
          setIssuerTin(issuerToken);
          String gcin = tokenInfo.get("globalPanIndex").getAsString();
          setGcin(gcin);
        }
        if (providerType.equals("CARD_NETWORK")) {
          String networkToken = tokenInfo.get("tokenIndexNumber").getAsString();
          setTin(networkToken);
          String gcin = tokenInfo.get("globalPanIndex").getAsString();
          setGcin(gcin);
        }
      }
    } else {
      softly.assertThat(saveCards.contains(tin));
      softly.assertThat(saveCards.contains(issuerTin));
      softly.assertAll();
    }
  }


  @Step("Add issuer Token on mid+CustId")
  public synchronized static void addIssuerTokenOnMidCustId(Constants.MerchantType mid,
      String custId,
      String expiryMonth, String expiryYear, String cardNumber, ArrayList<String> providerTypes) {

    TokenizeCardV2 tokenizeCardV2 = new TokenizeCardV2(mid.getId(), "VISA", CommonHelpers.generateOrderId()).buildRequest(
            custId,
            Constants.TokenizationConsent.YES.get(), null,
            Constants.CardSource.MANUAL_ENTERED.get(), null, true, true, providerTypes)
        .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(mid.getId());

    JsonPath tokenizeCardV2Response = tokenizeCardV2.execute().jsonPath();
    String actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");
    String token = tokenizeCardV2Response.getString("body.tokenInfos[0].tokenIndexNumber");
    int count = 5;
    while (actual.equalsIgnoreCase("Request timeout at card network")
        || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
      SavedCardHelpersNew.modifyTokenMidCustId(mid.getId(), token, custId,
          Constants.TokenStatus.DEAD.get());
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      TokenizeCardV2 tokenizeCardV2_1 = new TokenizeCardV2(mid.getId(), "VISA", CommonHelpers.generateOrderId()).buildRequest(
                      custId,
                      Constants.TokenizationConsent.YES.get(), null,
                      Constants.CardSource.MANUAL_ENTERED.get(), null, true, true, providerTypes)
              .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(mid.getId());

      tokenizeCardV2Response = tokenizeCardV2_1.execute().jsonPath();
      actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");
      count--;
    }
    String[] expected = {
        "ACCEPTED",
        "Token has already been created"};
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(Arrays.asList(expected)).contains(actual);
    softly.assertAll();

    String jsonString = tokenizeCardV2Response.prettyPrint();
    JsonParser parser = new JsonParser();
    JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject().getAsJsonObject("body");
    JsonArray tokenInfos = jsonObject.getAsJsonArray("tokenInfos");
    for (int i = 0; i < tokenInfos.size(); i++) {
      JsonObject tokenInfo = tokenInfos.get(i).getAsJsonObject();
      String providerType = tokenInfo.get("providerType").getAsString();

      if (providerType.equals("CARD_ISSUER")) {
        String issuerToken = tokenInfo.get("tokenIndexNumber").getAsString();
        setIssuerTin(issuerToken);
        String gcin = tokenInfo.get("globalPanIndex").getAsString();
        setGcin(gcin);
      }
      if (providerType.equals("CARD_NETWORK")) {
        String networkToken = tokenInfo.get("tokenIndexNumber").getAsString();
        setTin(networkToken);
        String gcin = tokenInfo.get("globalPanIndex").getAsString();
        setGcin(gcin);
      }
    }

    SavedCardByMidCustId savedCardByMidCustId = new SavedCardByMidCustId().buildRequest(mid,
        custId, "CHECKSUM", providerTypes);
    JsonPath saveCardByMidCustIdResponse = savedCardByMidCustId.execute().jsonPath();
    if (saveCardByMidCustIdResponse.getList("response").size() == 0) {
      try {
        Thread.sleep((long) (10 * 1000));
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      saveCardByMidCustIdResponse = savedCardByMidCustId.execute().jsonPath();
    }

    List saveCards = saveCardByMidCustIdResponse.getList("response");
    token = tokenizeCardV2Response.getString("body.tokenInfos[0].tokenIndexNumber");

    if (saveCards.size() == 0) {
      SavedCardHelpersNew.modifyTokenMidCustId(mid.getId(), token, custId,
          Constants.TokenStatus.DEAD.get());
      TokenizeCardV2 tokenizeCardV2_2 = new TokenizeCardV2(mid.getId(), "VISA", CommonHelpers.generateOrderId()).buildRequest(
                      custId,
                      Constants.TokenizationConsent.YES.get(), null,
                      Constants.CardSource.MANUAL_ENTERED.get(), null, true, true, providerTypes)
              .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(mid.getId());

      tokenizeCardV2Response = tokenizeCardV2_2.execute().jsonPath();
      actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");
      token = tokenizeCardV2Response.getString("body.tokenInfos[0].tokenIndexNumber");
      count = 5;
      while (actual.equalsIgnoreCase("Request timeout at card network")
          || actual.equalsIgnoreCase("Error connecting with card network") && count > 0) {
        SavedCardHelpersNew.modifyTokenMidCustId(mid.getId(), token, custId,
            Constants.TokenStatus.DEAD.get());
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        TokenizeCardV2 tokenizeCardV2_3 = new TokenizeCardV2(mid.getId(), "VISA", CommonHelpers.generateOrderId()).buildRequest(
                        custId,
                        Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.MANUAL_ENTERED.get(), null, true, true, providerTypes)
                .encryptCardData(cardNumber, expiryMonth, expiryYear, "802").generateJwt(mid.getId());

        tokenizeCardV2Response = tokenizeCardV2_3.execute().jsonPath();
        actual = tokenizeCardV2Response.getString("body.resultInfo.resultMsg");
        count--;
      }
      softly.assertThat(Arrays.asList(expected)).contains(actual);
      softly.assertAll();

      jsonString = tokenizeCardV2Response.prettyPrint();
      parser = new JsonParser();
      jsonObject = parser.parse(jsonString).getAsJsonObject().getAsJsonObject("body");
      tokenInfos = jsonObject.getAsJsonArray("tokenInfos");
      for (int i = 0; i < tokenInfos.size(); i++) {
        JsonObject tokenInfo = tokenInfos.get(i).getAsJsonObject();
        String providerType = tokenInfo.get("providerType").getAsString();

        if (providerType.equals("CARD_ISSUER")) {
          String issuerToken = tokenInfo.get("tokenIndexNumber").getAsString();
          setIssuerTin(issuerToken);
          String gcin = tokenInfo.get("globalPanIndex").getAsString();
          setGcin(gcin);
        }
        if (providerType.equals("CARD_NETWORK")) {
          String networkToken = tokenInfo.get("tokenIndexNumber").getAsString();
          setTin(networkToken);
          String gcin = tokenInfo.get("globalPanIndex").getAsString();
          setGcin(gcin);
        }
      }
    } else {
      softly.assertThat(saveCards.contains(tin));
      softly.assertThat(saveCards.contains(issuerTin));
      softly.assertAll();
    }
  }
    /*
    @Step("Delete all saved cards on: {0}")
    public static void deleteSavedCard(User user) throws AuthException {
        Awaitility.with().pollInSameThread().await()
                .pollDelay(Duration.FIVE_SECONDS)
                .pollInterval(Duration.TWO_SECONDS)
                .atMost(Duration.ONE_MINUTE)
                .until(clearCards(user));
    }


     */

  @Step("Delete all saved cards on: {0}")
  public static void deleteSavedCard(User user) throws AuthException {
    MongoCollection<Document> collection = MongoDBHelper.getCollection("TOKEN_DATA");
    MongoDBHelper.deleteDeadTokens(collection);
    HandlerInternalDeleteBinUser.deleteAllCards(user);
  }

  public static void deleteCardOnUser(User user, String token, String vaultIdentifier)
      throws InterruptedException {
    HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(
        user.ssoToken(), token);
    JsonPath delBinResp = deleteBinUser.execute().jsonPath();
    int noOfCardsDeleted = Integer.parseInt(delBinResp.getString("NUMBER_OF_RECORDS"));
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(noOfCardsDeleted).isEqualTo(1);
    softly.assertAll();

    if (vaultIdentifier.equalsIgnoreCase("PPBL")) {
      HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(user.ssoToken(), true,
          VAULTIDENTIFIER.PPBL.get());
      JsonPath binInfoResponse = binInfo.execute().jsonPath();
      int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
      int count = 0;
      while (count < 5 && size != 0) {
        Thread.sleep(5000);
        binInfoResponse = binInfo.execute().jsonPath();
        size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        count++;
      }
      softly.assertThat(binInfoResponse.getList("BIN_DETAILS")).doesNotContain(tin);
      softly.assertAll();
      System.out.println("Successfully deleted PPBL card");
    }

    if (vaultIdentifier.equalsIgnoreCase("OCL")) {
      HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(user.ssoToken(), true,
          VAULTIDENTIFIER.OCL.get());
      JsonPath binInfoResponse = binInfo.execute().jsonPath();
      int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
      int count = 0;
      while (count < 5 && size != 0) {
        Thread.sleep(5000);
        binInfoResponse = binInfo.execute().jsonPath();
        size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        count++;
      }
      softly.assertThat(binInfoResponse.getList("BIN_DETAILS")).doesNotContain(tin);
      softly.assertAll();
      System.out.println("Successfully deleted OCL card");
    }
  }

  @Step("Update token status")
  public static void updateTokenStatus(String token, TokenStatus tokenStatus) {
    MongoCollection<Document> collection = MongoDBHelper.getCollection("TOKEN_DATA");
    Document dbObject = MongoDBHelper.findDocumentById(collection, token, TokenFields.ID.get());
    String currentTokenState = dbObject.getString("tokenState");
    System.out.println(
        "Current token state is " + currentTokenState + " and expected token status is "
            + tokenStatus.toString());
    int count = 0;
    while (count < 5) {
      if (!currentTokenState.equalsIgnoreCase(tokenStatus.toString())) {
        MongoDBHelper.updateDocumentByObjectId(collection, token, "tokenState",
            tokenStatus.toString());
        dbObject = MongoDBHelper.findDocumentById(collection, token, TokenFields.ID.get());
        currentTokenState = dbObject.getString("tokenState");
        System.out.println("Current token state is " + currentTokenState);

        if (currentTokenState.equalsIgnoreCase(tokenStatus.toString())) {
          System.out.println("Token " + token + " moved to expected state");
          break;
        } else {
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          count++;
        }
      } else {
        System.out.println("Token is already in desired state");
        count++;
      }
    }
  }

  @Step("Deleting all tokens with same PAR")
  public static void updateTokenWithPar(String token, TokenStatus tokenStatus) {
    MongoCollection<Document> collection = MongoDBHelper.getCollection("TOKEN_DATA");
    if (collection == null) {
        System.out.println("Collection TOKEN_DATA not found.");
        return;
    }
    Document dbObject = MongoDBHelper.findDocumentById(collection, token, TokenFields.PAN.get());
    if (dbObject == null) {
        System.out.println("Document with provided token and PAN field not found.");
        return;
    }
    String par = dbObject.getString("primaryAccountRefNumber");
    if (par == null) {
        System.out.println("primaryAccountRefNumber field not found in the document.");
        return;
    }
    System.out.println("PAR is " + par);
    MongoDBHelper.updateDocumentByAnyField(collection, "primaryAccountRefNumber", par, "tokenState", tokenStatus.toString(), true);
}

  private static Callable<Boolean> clearCards(User user) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        final com.paytm.utils.merchant.user.User user1 = new com.paytm.utils.merchant.user.User(
            user.mobNo(), user.password(), true);
        ((AlipayUser) user1.asType(AlipayUser.class)).getSavedCards().clear();
        user1.getSavedCards().clear();
        return user1.getSavedCards().size() == 0;
      }
    };
  }

  public static void deleteSavedCard(String custId) {
    PGPUtil.deleteSavedCard(LocalConfig.PGP_HOST, custId);
  }


  public static void validateSavedCardPresence(User user) throws AuthException {
    String custId = user.custId();
    PGPUtil.validateSavedCardPresence(LocalConfig.PGP_HOST, custId);
  }
    /*
    @Step("Get savedCardId from user {0} at index {1}")
    public static String getSavedCardId(User user, int index) throws AuthException {
        String custId = user.custId();
        DriverManager.setCaptureScreenShot(false);
        String savedCardId = PGPUtil.getSavedCardId(LocalConfig.PGP_HOST, custId, index);
        DriverManager.setCaptureScreenShot(true);
        return savedCardId;
    }
    */

  @Step("Get savedCardId from user {0} at index {1}")
  public static String getSavedCardId(User user, int index) throws AuthException {
    HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(user.ssoToken(), true);
    JsonPath binInfoResponse = binInfo.execute().jsonPath();
    return binInfoResponse.getString("BIN_DETAILS[" + index + "].SAVE_CARD_ID");
  }

  @Step("Validate saved card's absence for user {0}")
  public static void validateSavedCardAbsence(User user) throws AuthException {
    String custId = user.custId();
    DriverManager.setCaptureScreenShot(false);
    PGPUtil.validateSavedCardAbsence(LocalConfig.PGP_HOST, custId);
    DriverManager.setCaptureScreenShot(true);
  }

  public static String getSavedCardId(String subsId) {
    String savedCardId = null;
    try {
      savedCardId = PGPUtil.getSavedCardId(LocalConfig.PGP_DB_CONNECTION_URL, subsId);
    } catch (NoResultFoundException e) {
      throw new SkipException(e.getMessage(), e);
    }
    return savedCardId;
  }

  public static void validateSaveCardDB_ByCardID(String cardId) {
    SoftAssert softAssert = new SoftAssert();
    String query1 = "select * from SAVED_CARD_INFO  where card_id='" + cardId + "';";
    String query2 = "SELECT * FROM SAVED_MID_CARD_INFO where card_id='" + cardId + "';";
    String query3 = "SELECT * FROM SAVED_BIN_INFO where card_id='" + cardId + "';";
    String card_number_SAVED_CARD_INFO = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query1).get(0).get("card_number")
        .toString();
    softAssert.assertNotNull(card_number_SAVED_CARD_INFO);
    String user_id_SAVED_MID_CARD_INFO = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query2).get(0).get("user_id")
        .toString();
    softAssert.assertNotNull(user_id_SAVED_MID_CARD_INFO);
    String first_six_digits_SAVED_BIN_INFO = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query3).get(0)
        .get("first_six_digits").toString();
    softAssert.assertNotNull(first_six_digits_SAVED_BIN_INFO);
  }

  public static void updateCardExpiry_withExpiredExpiry(String cardId, User user) throws Exception {
    String expiredEncryptedExpiry = " MsPV/oXL0oIeQ/l1GhwaZw==";
    String expiredAesEncryptedExpiry = "yXWreBNpThbSNDyehYQFkQ==";
    String queryForExp =
        "update SAVED_CARD_INFO set expiry_date='" + expiredEncryptedExpiry + "'where card_id={?}";
    String queryForExpAes =
        "update SAVED_CARD_INFO set expiry_date_AES='" + expiredAesEncryptedExpiry
            + "'where card_id={?}";
    DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL,
        queryForExp.replace("{?}", cardId.toString()));
    DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL,
        queryForExpAes.replace("{?}", cardId.toString()));
    PGPBaseTest.TRANSACTIONAL_REDIS_CLUSTER()
        .del("PG_USER_CARDS_V4_" + user.custId(), "USER_CARDS_V4_" + user.custId());
//        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("PG_USER_CARDS_V4_"+user.custId(),"USER_CARDS_V4_"+user.custId());
  }

  public static void updateCardExpiry_withExpiredExpiry(String cardId, String merchant,
      String custId) throws Exception {
    String expiredEncryptedExpiry = " MsPV/oXL0oIeQ/l1GhwaZw==";
    String expiredAesEncryptedExpiry = "yXWreBNpThbSNDyehYQFkQ==";
    String queryForExp =
        "update SAVED_CARD_INFO set expiry_date='" + expiredEncryptedExpiry + "'where card_id={?}";
    String queryForExpAes =
        "update SAVED_CARD_INFO set expiry_date_AES='" + expiredAesEncryptedExpiry
            + "'where card_id={?}";
    DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL,
        queryForExp.replace("{?}", cardId.toString()));
    DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL,
        queryForExpAes.replace("{?}", cardId.toString()));
    PGPBaseTest.TRANSACTIONAL_REDIS_CLUSTER().del("PG_USER_CARDS_V4_" + merchant + "_" + custId,
        "USER_CARDS_V4_" + merchant + "_" + custId);
//        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("PG_USER_CARDS_V4_"+merchant+"_"+custId,"USER_CARDS_V4_"+merchant+"_"+custId);
  }


  public SaveCardResponseBase saveTrustedCard(String cardNumber, String userId, String expiry)
      throws Exception {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      saveTrustedCardRequest request = new saveTrustedCardRequest();
      request.setCardNumber(cardNumber);
      request.setUserId(userId);
      request.setExpiryDate(expiry);
      request.setTokenType("JWT");
      request.setToken(
          JWT.create()
              .withIssuer("ts")
              .withClaim("userId", request.getUserId())
              .withClaim("cardNumber", request.getCardNumber())
              .withClaim("expiryDate", request.getExpiryDate())
              .withClaim("tokenType", request.getToken())
              .sign(Algorithm.HMAC256("hUOB1Us6TIv5zAgpDwIcs4QBvGxh0VeR"))
      );
      this.response = SavedCardApi.saveTrustedCard(request);
      Assertions.assertThat(this.response.getStatusCode()).as("status code").isEqualTo(200);
      this.jsonPath = this.response.jsonPath();
      //String response = new JSONObject(this.jsonPath.get()).toJSONString();
      SaveCardResponseBase saveCardResponseBase = objectMapper.readValue(getJsonString(response),
          SaveCardResponseBase.class);
      return saveCardResponseBase;
    } catch (IOException e) {
      e.printStackTrace();
      throw new NullPointerException("Response not found");
    }
  }

  public void validateSavedCardResponse_Success(SaveCardResponseBase saveCardResponseBase) {
    SoftAssert softAssert = new SoftAssert();
    softAssert.assertEquals(this.response.getStatusCode(), 200);
    softAssert.assertEquals(saveCardResponseBase.getResponseStatus(), "SUCCESS");
    softAssert.assertEquals(saveCardResponseBase.getCodeDetail(), "Success");
    softAssert.assertEquals(saveCardResponseBase.getHttpSubCode(), "200");
    softAssert.assertEquals(saveCardResponseBase.getHttpCode(), "200");
    softAssert.assertAll();
  }

  public void validateSavedCardResponse_Failure(SaveCardResponseBase saveCardResponseBase) {
    SoftAssert softAssert = new SoftAssert();
    softAssert.assertEquals(this.response.getStatusCode(), 200);
    softAssert.assertEquals(saveCardResponseBase.getResponseStatus(), "FAILURE");
    softAssert.assertEquals(saveCardResponseBase.getCodeDetail(), "Invalid data entered by user");
    softAssert.assertEquals(saveCardResponseBase.getHttpSubCode(), "406");
    softAssert.assertEquals(saveCardResponseBase.getHttpCode(), "400");
    softAssert.assertAll();
  }

  // validate savecard entry in SAVED_MID_CARD_INFO and SAVED_BIN_INFO
  public void ValidateSaveCardDbEntry(String cardNum, String userId, String apiCardId) {
    String dbCardId = getSavedSaveCardId_FromDB_ByUserAndCardNum(userId, cardNum);
    Reporter.report.info("Validating DB entry for save card API");
    Assertions.assertThat(dbCardId).isEqualToIgnoringCase(apiCardId);
  }

  public void ValidateEncrSaveCardExpiry_FromDB(String cardId, String aesExp) {
    String query = "select expiry_date_AES from SAVED_CARD_INFO where card_id='" + cardId + "'";
    Map<String, Object> map = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0);
    String Db_exp_aes = map.get("expiry_date_AES").toString();
    Reporter.report.info("Validating Encrypted Exp in DB ");
    Assertions.assertThat(Db_exp_aes).isEqualTo(aesExp)
        .withFailMessage("Invalid aes encrypted expiry");
  }

  public void ValidateEncrSaveCardNumber_FromDB(String cardId, String aesNum) {
    String query = "select card_number_AES from SAVED_CARD_INFO where card_id='" + cardId + "'";
    Map<String, Object> map = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0);
    String Db_num_aes = map.get("card_number_AES").toString();
    Reporter.report.info("Validating Encrypted card number in DB ");
    Assertions.assertThat(Db_num_aes).isEqualTo(aesNum)
        .withFailMessage("Invalid aes encrypted card number");
  }

  public void Validate_CardStatus_FromDB(String cardId, Object status) {
    String query = "select status from SAVED_CARD_INFO where card_id='" + cardId + "'";
    Map<String, Object> map = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0);
    Reporter.report.info("Validating saved card status in DB with status= " + status);
    Assertions.assertThat(map.get("status")).isEqualTo(status)
        .withFailMessage("Invalid saved card status");
  }

  public static String getCIN(String cardId) {
    String query = "select card_index_number from SAVED_CARD_INFO where card_id='" + cardId + "'";
    Map<String, Object> map = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0);
    System.out.println("Map " + map);
    Reporter.report.info("CIN for savedCardId: " + cardId + " is " + map.get("card_index_number"));
    return map.get("card_index_number").toString();

  }


  public String getSavedSaveCardId_FromDB_ByUserAndCardNum(String userId, String cardNum) {
    int cardLength = cardNum.length();
    String dbCardId = null;
    String cardFirstSix = cardNum.substring(0, 6);
    String cardLastFour = cardNum.substring(cardLength - 4, cardLength);
    String query = "select * from SAVED_MID_CARD_INFO a, SAVED_BIN_INFO b, SAVED_CARD_INFO c where a.user_id='{?}' and b.first_six_digits = '{?}' and b.last_four_digits='{?}' and a.card_id=b.card_id and c.card_id=b.card_id and c.status=1;\n";

    String updatedQuery = CommonHelpers.getUpdatedString(query, userId, cardFirstSix, cardLastFour);
    Reporter.report.info("Query for getting saved card details:" + updatedQuery);
    try {
      dbCardId = DatabaseUtil.getInstance()
          .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, updatedQuery).get(0)
          .get("card_index_number").toString();
      Reporter.report.info("DB entry validation for saved card");
      return dbCardId;
    } catch (Exception e) {
      e.printStackTrace();
      throw new NullPointerException("Card Id not saved in DB");
    }
  }

  public void ValidateCardStatus(String cardId, String val) {
    String query = "select status from SAVED_CARD_INFO where card_id='" + cardId + "'";
    String status = DatabaseUtil.getInstance()
        .executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0).get("status")
        .toString();
    Assertions.assertThat(status).isEqualTo(val);
    Reporter.report.info("Saved card status validation done");
  }

  public SaveCardResponseBase saveCardCache(String userId, String encCardNum, String encExp,
      String cardNum, String txnId) throws Exception {
    int cardLength = cardNum.length();
    String cardFirstSix = cardNum.substring(0, 6);
    String cardLastFour = cardNum.substring(cardLength - 4, cardLength);
    SaveCardRequestGeneric saveCardRequestGeneric = new SaveCardRequestGeneric();
    saveCardRequestGeneric.setCardNumber(encCardNum);
    saveCardRequestGeneric.setUserId(userId);
    saveCardRequestGeneric.setStatus(1);
    saveCardRequestGeneric.setCardType("0");
    saveCardRequestGeneric.setExpiryDate(encExp);
    saveCardRequestGeneric.setFirstSixDigit(cardFirstSix);
    saveCardRequestGeneric.setLastFourDigit(cardLastFour);
    saveCardRequestGeneric.setTransactionId(txnId);
    this.response = SavedCardApi.saveCardDetailCache(saveCardRequestGeneric);
    this.jsonPath = this.response.jsonPath();
    ObjectMapper mapper = new ObjectMapper();
    try {
      SaveCardResponseBase responseBase = mapper.readValue(getJsonString(response),
          SaveCardResponseBase.class);
      return responseBase;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Cannot parse response to SaveCardResponseBase");
    }
  }

  public SaveCardResponseBase saveCardUserId(String userId, String encCardNum, String encExp,
      String cardNum) throws Exception {
    int cardLength = cardNum.length();
    String cardLastFour = cardNum.substring(cardLength - 4, cardLength);
    return saveCardUserId(userId, encCardNum, encExp, cardNum, cardLastFour, "0");
  }


  public SaveCardResponseBase saveCardUserId(String userId, String encCardNum, String encExp,
      String cardNum, String lastFourDigits, String cardType) throws Exception {
    String cardFirstSix = cardNum.substring(0, 6);
    SaveCardRequestGeneric saveCardRequestGeneric = new SaveCardRequestGeneric();
    saveCardRequestGeneric.setCardNumber(encCardNum);
    saveCardRequestGeneric.setUserId(userId);
    saveCardRequestGeneric.setStatus(1);
    saveCardRequestGeneric.setExpiryDate(encExp);
    saveCardRequestGeneric.setCardType(cardType);
    if (cardType.equalsIgnoreCase("9")) {
      saveCardRequestGeneric.setFirstSixDigit("null");
      saveCardRequestGeneric.setLastFourDigit("null");
    } else {
      saveCardRequestGeneric.setFirstSixDigit(cardFirstSix);
      saveCardRequestGeneric.setLastFourDigit(lastFourDigits);
    }
    this.response = SavedCardApi.saveCardDetailUserId(saveCardRequestGeneric);
    this.jsonPath = this.response.jsonPath();
    ObjectMapper mapper = new ObjectMapper();
    try {
      SaveCardResponseBase responseBase = mapper.readValue(getJsonString(response),
          SaveCardResponseBase.class);
      return responseBase;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Cannot parse response to SaveCardResponseBase");
    }
  }

  public SaveCardResponseBase saveCard_custId_mId(String cardNum, String custId, String mId,
      String expDate) throws Exception {
    int cardLength = cardNum.length();
    String cardFirstSix = cardNum.substring(0, 6);
    String cardLastFour = cardNum.substring(cardLength - 4, cardLength);
    SaveCardRequestGeneric saveCardRequestGeneric = new SaveCardRequestGeneric();
    saveCardRequestGeneric.setPaymentTypeId("0");
    saveCardRequestGeneric.setCardNumber(cardNum);
    saveCardRequestGeneric.setExpiryDate(expDate);
    saveCardRequestGeneric.setCustId(custId);
    saveCardRequestGeneric.setmId(mId);
    saveCardRequestGeneric.setFirstSixDigit(cardFirstSix);
    saveCardRequestGeneric.setLastFourDigit(cardLastFour);
    this.response = SavedCardApi.saveCardDetailCustIdMid(saveCardRequestGeneric);
    this.jsonPath = this.response.jsonPath();
    ObjectMapper mapper = new ObjectMapper();
    try {
      SaveCardResponseBase responseBase = mapper.readValue(getJsonString(response),
          SaveCardResponseBase.class);
      return responseBase;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Cannot parse response to SaveCardResponseBase");
    }
  }

  public void validateCacheForDelete(String key) throws Exception {
//        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI);
    try {
      String val = PGPBaseTest.TRANSACTIONAL_REDIS_CLUSTER().hkeys(key).toArray()[0].toString();
//            String val = jedis.keys(key).toArray()[0].toString();
      throw new Exception("Value found in Redis");
    } catch (ArrayIndexOutOfBoundsException e) {

    }
  }

  public SaveCardResponseBase saveCardDetail_ToDB_FromCache(String txnId) throws Exception {
    SaveCardRequestGeneric request = new SaveCardRequestGeneric();
    request = request.setTransactionId(txnId);
    response = SavedCardApi.saveCardDetail_ToDB_FromCache(request);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public String getJsonString(Response object) throws Exception {
    JSONParser parser = new JSONParser();
    return parser.parse(object.asString()).toString();
  }

  public SaveCardResponseBase getSaveCardDetails_userId(User user) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    response = savedCardApi.getSaveCardByUserId(user.custId());
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;

  }


  public SaveCardResponseBase getSaveCardDetails_cardId(User user, String cardId) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    this.response = savedCardApi.getSaveCardByCardId(user.custId(), cardId);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase getSaveCardDetails_byUser_andStatus(User user, String status)
      throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    this.response = savedCardApi.getSaveCardBy_User_Status(user.custId(), status);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase getSaveCardDetails_byMid_custId_userId(User user, String custId,
      String mid) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    this.response = savedCardApi.getSaveCardBy_mId_custId_userId(user.custId(), custId, mid);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase getSaveCardDetails_byMid_custId_userId_cardId(User user,
      String custId, String mid, String cardId) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    this.response = savedCardApi.getSaveCardBy_mId_custId_userId_cardId(user.custId(), custId, mid,
        cardId);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase deleteSave_byCardId_mId_custId_userId(String cardId, String mid,
      String custId, User user) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    this.response = savedCardApi.deleteSave_cardId_mId_custId_userId(user.custId(), cardId, custId,
        mid);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase deleteSave_byUserId_cardId(User user, String cardId)
      throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    this.response = savedCardApi.deleteSaveCard_userId_cardId(user.custId(), cardId);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase deleteSaveCard_fromCache(String txnId) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    this.response = savedCardApi.deleteSaveCard_fromCache(txnId);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  /**
   * open APIs
   */


  public SaveCardResponseBase getSavedCard_BySsoToken(String token) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    response = savedCardApi.getSavedCard_bySsotoken(token);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase getSavedCard_BySsoToken(User user) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    response = savedCardApi.getSavedCard_bySsotoken(user.ssoToken());
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase getSaveCard_ByMid_custId(String mid, String merchantKey,
      String custId) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    response = savedCardApi.getSaveCard_byMid_custId(mid, merchantKey, custId);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase getSaveCard_ByMid_custId_token(String mid, String merchantKey,
      String custId, String token) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    response = savedCardApi.getSaveCard_byMid_custId_token(mid, merchantKey, custId, token);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase deleteSaveCard_On_custId_mid_cardId(String mid, String merchantKey,
      String custId, String cardId) throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    response = savedCardApi.deleteSaveCard_On_custId_mid_cardId(mid, merchantKey, custId, cardId);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  public SaveCardResponseBase deleteSaveCard_On_ssotoken_cardId(String cardId, String ssotoken)
      throws Exception {
    SavedCardApi savedCardApi = new SavedCardApi();
    response = savedCardApi.deleteSaveCard_On_ssoToken_cardId(cardId, ssotoken);
    ObjectMapper mapper = new ObjectMapper();
    SaveCardResponseBase saveCardResponseBase = mapper.readValue(getJsonString(response),
        SaveCardResponseBase.class);
    return saveCardResponseBase;
  }

  // /savedcardservice/savedcardOpenAPIService/deleteSavedCardByTokenType
  @Step("Delete saved cards on User on the basis of SavedCardID from Paytm and P+")
  public static void deleteSavedCardByTokenType(User user, String saveCardId) throws AuthException {
    PGPHelpers.deleteSavedCardByTokenType(user, saveCardId);
  }

  public static void assertStoreCardPrefEnabled(Constants.MerchantType merchantType) {
    String preference = "STORE CARD DETAILS";
    PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference, "YES");
  }

  public static void assertStoreCardPrefDisabled(Constants.MerchantType merchantType) {
    String preference = "STORE CARD DETAILS";
    PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference, "NO");
  }

  public static void enableAllSavedCardFlags() {
    prerequisite:
    {

      //MID/CustId
      FF4JFlags.enable("shortCircuitSavedCardServiceReadForMidCustId");
      FF4JFlags.enable("fetchSavedcardFromPlatformForMidCustId");
      FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");

      //UserId
      FF4JFlags.enable("shortCircuitSavedCardServiceReadForUserId");
      FF4JFlags.enable("fetchSavedcardFromPlatformForUserId");
      FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
    }

  }

  public static void disableAllSavedCardFlags() {
    prerequisite:
    {

      //MID/CustId
      FF4JFlags.disable("shortCircuitSavedCardServiceReadForMidCustId");
      FF4JFlags.disable("fetchSavedcardFromPlatformForMidCustId");
      FF4JFlags.disable("returnSavedCardsFromPlatformForMidCustId");

      //UserId
      FF4JFlags.disable("shortCircuitSavedCardServiceReadForUserId");
      FF4JFlags.disable("fetchSavedcardFromPlatformForUserId");
      FF4JFlags.disable("returnSavedCardsFromPlatformForUserId");
    }

  }

  ///////////////////////////P Plus API///////////////////

    /*
    // alipayplus/user/asset/bindAsset.htm
    @Step("Add Card on user P+")
    public synchronized static String addCardAlipay(User user, String expiryMonth, String expiryYear, String cardNumber) throws AuthException {
        String userId = user.custId();
        return PGPHelpers.saveCardAtAlipayUserBind(userId,expiryMonth,expiryYear,cardNumber);
    }

     */

  @Step("Add Card on user P+")
  public synchronized static String addCardAlipay(User user, String expiryMonth, String expiryYear,
      String cardNumber) throws Exception {
    addCard(user, expiryMonth, expiryYear, cardNumber);
    return getSavedCardId(user, 0);
  }

  // alipayplus/alipayplus/merchant/asset/bindAsset.htm
  @Step("Add Card on MID CustId P+")
  public synchronized static String addCardAlipay(String mid, String custId, String expiryMonth,
      String expiryYear, String cardNumber) throws AuthException {
    return PGPHelpers.saveCardAtAlipayMerchantBind(mid, custId, expiryMonth, expiryYear,
        cardNumber);
  }

  // merchant/asset/query/customerAssets
  @Step("Fetch Cards on MID CustId P+")
  public synchronized static Response fetchCardsAlipay(String mid, String custId)
      throws AuthException {
    return PGPHelpers.getCardsAlipayMerchantAsset(mid, custId);
  }

  // /user/assets/queryByFilter
  @Step("Fetch Cards on UserID P+")
  public synchronized static Response fetchCardsAlipay(User user) throws AuthException {
    String userId = user.custId();
    return PGPHelpers.getCardsAlipayUserAsset(userId);
  }

  // /merchant/asset/delete/customerAsset
  @Step("Delete Cards on MID CustId P+")
  public synchronized static void deleteSavedCardsAlipay(String mid, String custId)
      throws AuthException {
    PGPHelpers.deleteAlipayAssetMerchant(mid, custId);
  }

    /*
    // /user/asset/delete
    @Step("Delete Cards on UserID P+")
    public synchronized static void deleteSavedCardsAlipay(User user) throws AuthException {
        String userId = user.custId();
        PGPHelpers.deleteAlipayAssetUser(userId);
    }

     */

  @Step("Delete Cards on UserID P+")
  public synchronized static void deleteSavedCardsAlipay(User user) throws AuthException {
    deleteSavedCard(user);
  }

  public synchronized static boolean verifyParamPresentInDirectPassthrough(String log,
      String parameterName) {
    List<String> requiredParam = new ArrayList<>();
    String key = "directPassThroughInfo\":\"";
    int index = 0;
    while (index != -1) {
      index = log.indexOf(key, index);
      if (index != -1) {
        index += key.length();
        int endIndex = log.indexOf("\"", index);
        if (endIndex != -1) {
          String directPassThroughInfo = log.substring(index, endIndex);

          byte[] decodedBytes = Base64.getDecoder().decode(directPassThroughInfo);
          String decodedString = new String(decodedBytes);

          // Parse the decoded string to a JSON object
          JSONObject jsonObject = new JSONObject(decodedString);

          // Get the 'tavv' parameter from the JSON object
          String paramValue = jsonObject.getString(parameterName);

          if (paramValue != null) {
            return true;
          } else {
            return false;
          }

        }
      }
    }
    return false;
  }

  public synchronized static boolean verifyParamsPresentInDirectPassthrough(String log,
      List<String> parameterNames) {
    final String key = "directPassThroughInfo\":\"";
    int index = 0;

    while (index != -1) {
      index = log.indexOf(key, index);
      if (index != -1) {
        index += key.length();
        int endIndex = log.indexOf("\"", index);
        if (endIndex != -1) {
          String directPassThroughInfo = log.substring(index, endIndex);
          byte[] decodedBytes = Base64.getDecoder().decode(directPassThroughInfo);
          String decodedString = new String(decodedBytes);
          JSONObject jsonObject = new JSONObject(decodedString);

          boolean allParamsPresent = true;
          for (String paramName : parameterNames) {
            if (!jsonObject.has(paramName)) {
              allParamsPresent = false;
              break;
            }
          }

          if (allParamsPresent) {
            return true;
          }
        }
      }
    }
    return false;
  }
}