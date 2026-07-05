package scripts.coft.savedCardService;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paytm.LocalConfig;
import com.paytm.api.coft.saveCard.SavedCardByMidCustId;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.utils.RedisClusterUtil;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.apphelpers.SavedCardHelpersNew.modifyTokenMidCustId;

public class SavedCardByMidCustIdTests extends PGPBaseTest {

  Constants.MerchantType coftMerchant = Constants.MerchantType.COFT_MERCHANT_3P;
  public static String custId =
      RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
  RedisHelper redisHelper = new RedisHelper(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,
      LocalConfig.PG_REDIS_CLUSTER_PASS);

  public void validateSubAssetTypeAndSubAssetCode(JsonPath jsonPath, String tin,
      String subAssetType, String subAssetCode) {
    List<Map<String, Object>> tokenInfos = jsonPath.getList("response");

    for (Map<String, Object> tokenInfo : tokenInfos) {
      String providerType = (String) tokenInfo.get("subAssetType");
      String token = (String) tokenInfo.get("savedCardId");
      SoftAssertions softAssertions = new SoftAssertions();

      if (providerType.equals("CARD_ISSUER") && token.equals(tin)) {
        softAssertions.assertThat(subAssetType).isEqualTo(tokenInfo.get("subAssetType"));
        softAssertions.assertThat(subAssetCode).isEqualTo(tokenInfo.get("subAssetCode"));
        softAssertions.assertAll();
      }
      if (providerType.equals("CARD_NETWORK") && token.equals(tin)) {
        softAssertions.assertThat(subAssetType).isEqualTo(tokenInfo.get("subAssetType"));
        softAssertions.assertThat(subAssetCode).isEqualTo(tokenInfo.get("subAssetCode"));
        softAssertions.assertAll();
      }
    }
  }

  @Test
  public void test() {
    RedisHelper redisHelper = new RedisHelper(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,
        LocalConfig.PG_REDIS_CLUSTER_PASS);
    System.out.println("Redis key value is: " + redisHelper.getKeyValue(
        "FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId"));
    redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
    System.out.println("Redis key value is: " + redisHelper.getKeyValue(
        "FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId"));
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify only JWT is supported in tokenType", groups = "Security")
  public void VerifyOnlyJWTSupported() throws Exception {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "test", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("1400");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Token Type is invalid");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify error message when tokenType param is not sent in request", groups = "Security")
  public void VerifyTokenTypeNotSent() throws Exception {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("400");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("410");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Mandatory fields missing");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify error message when token param is not sent in req", groups = "Security")
  public void VerifyTokenNotSent() throws Exception {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", "");
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("400");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("410");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Mandatory fields missing");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify error message when incorrect token is sent in req", groups = "Security")
  public void VerifyIncorrectTokenSent() throws Exception {
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", "test");
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("400");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("1003");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Jwt Validation Failure");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify error message when incorrect mid is sent in req")
  public void VerifyIncorrectMidSent() throws Exception {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", "e1j2hl3875168188612");
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.SAVED_CARD_PG_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest("e1j2hl3875168188612",
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("400");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("407");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Incorrect mid");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify error message when custId is not sent in req")
  public void VerifyCustIdNotSent() throws Exception {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        "", false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("400");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("410");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Mandatory fields missing");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify error message when mid is not sent in req")
  public void VerifyMidNotSent() throws Exception {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest("", custId, false,
        "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("400");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("410");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Mandatory fields missing");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify proper response is returned if correct params are sent in request")
  public void VerifyCorrectResponse() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Success");
    softly.assertThat(savedCardResponse.getList("response.savedCardId[]").contains(tin));
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify tokenized cards are not returned in response if filterTokenCards=true is sent in request")
  public void VerifyFilterTokenTrue() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "true");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, true, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("204");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Card does not exist for given parameters");
    softly.assertThat(savedCardResponse.getList("response").size()).isEqualTo(0);
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify tokenized cards are returned if  filterToken is not sent in request")
  public void VerifyFilterTokenNotSent() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Success");
    softly.assertThat(savedCardResponse.getList("response.savedCardId[]").contains(tin));
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify all parameters in success response")
  public void VerifyAllParams() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Success");
    softly.assertThat(savedCardResponse.getString("response.savedCardId[0]")).isEqualTo(tin);
    softly.assertThat(savedCardResponse.getString("response.cardType[0]")).isEqualTo("CC");
    softly.assertThat(savedCardResponse.getString("response.cardScheme[0]")).isEqualTo("VISA");
    softly.assertThat(savedCardResponse.getString("response.expiryDate[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.cardFirstSixDigits[0]").length())
        .isEqualTo(6);
    softly.assertThat(savedCardResponse.getString("response.cardSuffix[0]").length()).isEqualTo(4);
    softly.assertThat(savedCardResponse.getString("response.issuingBankName[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.tokenBin[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.panUniqueReference[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.tokenStatus[0]")).isEqualTo("ACTIVE");
    softly.assertThat(savedCardResponse.getBoolean("response.isEligibleForCoft[0]"))
        .isEqualTo(false);
    softly.assertThat(savedCardResponse.getBoolean("response.isCardCoft[0]")).isEqualTo(true);
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify Dead tokens are not returned in response")
  public void VerifyDeadTokenNotReturned() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();

    modifyTokenMidCustId(coftMerchant.getId(), tin, custId, "DEAD");
    savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("204");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Card does not exist for given parameters");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Test(description = "Verify SUSPENDED token is not returned in response")
  public void VerifyAllParamsSuspendedToken() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    modifyTokenMidCustId(coftMerchant.getId(), tin, custId, "SUSPENDED");
    savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("204");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Card does not exist for given parameters");
    softly.assertAll();
  }


  @Owner(Constants.Owner.ABHISHEK_KULKARNI)
  @Feature("PGP-42626")
  @Test(description = "Validate the successful transaction of fetch tokenise cards by mid and custid with jwt token where the key has created in vault", groups = "Security")
  public void VerifySuccessFullTxnWhereJwtFromVault() throws Exception {
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("response")).contains(tin);
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_KULKARNI)
  @Feature("PGP-42626")
  @Test(description = "Validate the transaction of fetch tokenise cards by mid and custid with invalid jwt token", groups = "Security")
  public void VerifySavedCardDetailsWithInvalidJWTFromVault() throws Exception {
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.INVALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("F");
    softly.assertThat(savedCardResponse.getString("codeDetail"))
        .isEqualTo("Jwt Validation Failure");
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_KULKARNI)
  @Feature("PGP-42626")
  @Test(description = "Validate the successful transaction of fetch tokenise cards by mid and custid with checksum for Tokenise cards", groups = "Security")
  public void VerifySuccessFullTxnWithCheksum() throws Exception {
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(
        Constants.MerchantType.COFT_MERCHANT_3P, custId, "CHECKSUM");
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("response")).contains(tin);
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-53209")
  @Test(description = "Issuer both issuer token and network tokens are returned in response when tokenType: [CARD_NETWORK, CARD_ISSUER] is sent in request", groups = "Security")
  public void VerifyBothTokens() throws Exception {
    custId =
        RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    ArrayList<String> list = new ArrayList<>();
    list.add("CARD_NETWORK");
    list.add("CARD_ISSUER");
    SavedCardHelpers.addIssuerTokenOnMidCustId(MerchantType.ISSUER_TOKEN_3P, custId,
        PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.ISSUER_TOKENIZATION_VISA_CARD, list);

    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", MerchantType.ISSUER_TOKEN_3P.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    tokenMap.put("tokenType", list.toString());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);

    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(
        MerchantType.ISSUER_TOKEN_3P.getId(), custId, "JWT", jwt, list);
    JsonPath saveCardByMidCustIdResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(saveCardByMidCustIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains(SavedCardHelpers.getTin());
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains(SavedCardHelpers.getIssuerTin());
    softly.assertAll();
    validateSubAssetTypeAndSubAssetCode(saveCardByMidCustIdResponse, SavedCardHelpers.getTin(),
        "CARD_NETWORK", "VISA");
    validateSubAssetTypeAndSubAssetCode(saveCardByMidCustIdResponse,
        SavedCardHelpers.getIssuerTin(), "CARD_ISSUER", "HDFC");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-53209")
  @Test(description = "Only issuer token is returned in response when tokenType: [CARD_ISSUER] is sent in request", groups = "Security")
  public void VerifyOnlyIssuerTokens() throws Exception {
    custId =
        RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    ArrayList<String> list = new ArrayList<>();
    list.add("CARD_NETWORK");
    list.add("CARD_ISSUER");
    SavedCardHelpers.addIssuerTokenOnMidCustId(MerchantType.ISSUER_TOKEN_3P, custId,
        PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.ISSUER_TOKENIZATION_VISA_CARD, list);

    list.remove("CARD_NETWORK");
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", MerchantType.ISSUER_TOKEN_3P.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    tokenMap.put("tokenType", list.toString());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);

    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(
        MerchantType.ISSUER_TOKEN_3P.getId(), custId, "JWT", jwt, list);
    JsonPath saveCardByMidCustIdResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(saveCardByMidCustIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains(SavedCardHelpers.getIssuerTin());
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .doesNotContain(SavedCardHelpers.getTin());
    softly.assertAll();
    validateSubAssetTypeAndSubAssetCode(saveCardByMidCustIdResponse,
        SavedCardHelpers.getIssuerTin(), "CARD_ISSUER", "HDFC");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-53209")
  @Test(description = "Only network token is returned in response when tokenType: [CARD_NETWORK] is sent in request")
  public void VerifyOnlyNetworkTokens() throws Exception {
    custId =
        RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    ArrayList<String> list = new ArrayList<>();
    list.add("CARD_NETWORK");
    list.add("CARD_ISSUER");
    SavedCardHelpers.addIssuerTokenOnMidCustId(MerchantType.ISSUER_TOKEN_3P, custId,
        PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.ISSUER_TOKENIZATION_VISA_CARD, list);

    list.remove("CARD_ISSUER");
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", MerchantType.ISSUER_TOKEN_3P.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    tokenMap.put("tokenType", list.toString());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);

    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(
        MerchantType.ISSUER_TOKEN_3P.getId(), custId, "JWT", jwt, list);
    JsonPath saveCardByMidCustIdResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(saveCardByMidCustIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains(SavedCardHelpers.getTin());
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .doesNotContain(SavedCardHelpers.getIssuerTin());
    softly.assertAll();
    validateSubAssetTypeAndSubAssetCode(saveCardByMidCustIdResponse, SavedCardHelpers.getTin(),
        "CARD_NETWORK", "VISA");
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-53209")
  @Test(description = "Only network token is returned in response when tokenType is not sent in request also subAssetCode and subAssetType is not returned")
  public void VerifyOnlyNetworkTokenWhenTokenTypeNotSent() throws Exception {
    custId =
        RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    ArrayList<String> list = new ArrayList<>();
    list.add("CARD_NETWORK");
    list.add("CARD_ISSUER");
    SavedCardHelpers.addIssuerTokenOnMidCustId(MerchantType.ISSUER_TOKEN_3P, custId,
        PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.ISSUER_TOKENIZATION_VISA_CARD, list);

    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", MerchantType.ISSUER_TOKEN_3P.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);

    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(
        MerchantType.ISSUER_TOKEN_3P.getId(), custId, "JWT", jwt, list);
    savedCards.deleteContext("body.tokenType");
    JsonPath saveCardByMidCustIdResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(saveCardByMidCustIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains(SavedCardHelpers.getTin());
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .doesNotContain(SavedCardHelpers.getIssuerTin());
    softly.assertAll();
  }

  @Owner(Constants.Owner.ABHISHEK_VERMA)
  @Feature("PGP-53209")
  @Test(description = "Verify only network token is returned in response if only network token in present even if tokenType: [CARD_NETWORK, CARD_ISSUER] is sent in request")
  public void VerifyNetworkIsReturnedIfNetworkIsSaved() throws Exception {
    custId =
        RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    ArrayList<String> list = new ArrayList<>();
    list.add("CARD_NETWORK");
    SavedCardHelpers.addIssuerTokenOnMidCustId(MerchantType.ISSUER_TOKEN_3P, custId,
        PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.ISSUER_TOKENIZATION_VISA_CARD, list);
    list.add("CARD_ISSUER");
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", MerchantType.ISSUER_TOKEN_3P.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    tokenMap.put("tokenType", list.toString());
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);

    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(
        MerchantType.ISSUER_TOKEN_3P.getId(), custId, "JWT", jwt, list);
    JsonPath saveCardByMidCustIdResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(saveCardByMidCustIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains(SavedCardHelpers.getTin());
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .doesNotContain("CARD_ISSUER");
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains("subAssetCode");
    softly.assertThat(saveCardByMidCustIdResponse.getString("response"))
        .contains("subAssetType");
    softly.assertAll();
    validateSubAssetTypeAndSubAssetCode(saveCardByMidCustIdResponse, SavedCardHelpers.getTin(),
        "CARD_NETWORK", "VISA");
  }

  @Owner(Constants.Owner.POOJA)
  @Feature("PGP-50508")
  @Test(description = "verify request is sent to user/cards/v2/fetch-all when theia.migrateFetchAllToV2 ff4j flag is ON")
  public void requestToV2_fetchall_merchant_coft_card() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    FF4JFlags.enable("DEFAULT.defaultEnableJwtAuthenticationForCardService");
    FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
    redisHelper.delete("FF4J_FEATURE_DEFAULT.defaultEnableJwtAuthenticationForCardService");
    redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Success");
    softly.assertAll();
    String userId = custId;
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service, userId);
    String pgpid = logs.substring(logs.indexOf("PGP-ID") + 7, logs.indexOf("X-APP-RID") - 2);

    String fetchallRequestLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service_facade, pgpid, "REQUEST");
    Assertions.assertThat(fetchallRequestLogs).contains("\"TYPE\" : \"REQUEST\"");
    Assertions.assertThat(fetchallRequestLogs).contains("user/cards/v2/fetch-all");
    Assertions.assertThat(fetchallRequestLogs).contains("ASSET_CENTER_SERVICE");
    Assertions.assertThat(fetchallRequestLogs).contains(coftMerchant.getId());
  }

  @Owner(Constants.Owner.POOJA)
  @Feature("PGP-50508")
  @Test(description = "Verify all parameters in success response when request sent to v2/fetchall")
  public void requestToV2_fetchall_merchant_coft_card_SuccessResponse() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    String userId = custId;
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service, userId);
    String pgpid = logs.substring(logs.indexOf("PGP-ID") + 7, logs.indexOf("X-APP-RID") - 2);

    String fetchallRequestLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service, pgpid);
    Assertions.assertThat(fetchallRequestLogs).contains("user/cards/v2/fetch-all");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Success");
    softly.assertThat(savedCardResponse.getString("response.savedCardId[0]")).isEqualTo(tin);
    softly.assertThat(savedCardResponse.getString("response.cardType[0]")).isEqualTo("CC");
    softly.assertThat(savedCardResponse.getString("response.cardScheme[0]")).isEqualTo("VISA");
    softly.assertThat(savedCardResponse.getString("response.expiryDate[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.cardFirstSixDigits[0]").length())
        .isEqualTo(6);
    softly.assertThat(savedCardResponse.getString("response.cardSuffix[0]").length()).isEqualTo(4);
    softly.assertThat(savedCardResponse.getString("response.issuingBankName[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.tokenBin[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.panUniqueReference[0]")).isNotNull();
    softly.assertThat(savedCardResponse.getString("response.tokenStatus[0]")).isEqualTo("ACTIVE");
    softly.assertThat(savedCardResponse.getBoolean("response.isEligibleForCoft[0]"))
        .isEqualTo(false);
    softly.assertThat(savedCardResponse.getBoolean("response.isCardCoft[0]")).isEqualTo(true);
    softly.assertAll();
  }

  @Owner(Constants.Owner.POOJA)
  @Feature("PGP-50508")
  @Test(description = "verify request is sent to user/cards/v2/fetch-all when theia.migrateFetchAllToV2 ff4j flag is ON")
  public void V2fetchall_merchant_coft_card_Response() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Success");
    softly.assertAll();
    String userId = custId;
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service, userId);
    String pgpid = logs.substring(logs.indexOf("PGP-ID") + 7, logs.indexOf("X-APP-RID") - 2);

    String fetchallRequestLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service_facade, pgpid, "REQUEST");
    Assertions.assertThat(fetchallRequestLogs).contains("\"TYPE\" : \"REQUEST\"");
    Assertions.assertThat(fetchallRequestLogs).contains("user/cards/v2/fetch-all");
    Assertions.assertThat(fetchallRequestLogs).contains("ASSET_CENTER_SERVICE");
    Assertions.assertThat(fetchallRequestLogs).contains(coftMerchant.getId());

    String fetchallResponseLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service_facade, pgpid, "RESPONSE");
    Assertions.assertThat(fetchallResponseLogs).contains("TYPE\" : \"RESPONSE");
    Assertions.assertThat(fetchallResponseLogs).contains("user/cards/v2/fetch-all");
    Assertions.assertThat(fetchallResponseLogs).contains("ASSET_CENTER_SERVICE");
    Assertions.assertThat(fetchallResponseLogs).contains("status\":\"SUCCESS");
    Assertions.assertThat(fetchallResponseLogs).contains("cardInfos");
    Assertions.assertThat(fetchallResponseLogs).contains("binInfo");
  }

  @Owner(Constants.Owner.POOJA)
  @Feature("PGP-50508")
  @Test(description = "verify request is sent to user/cards/v2/fetch-all when theia.migrateFetchAllToV2 ff4j flag is ON")
  public void merchantCoftCard_Response_validate() throws Exception {
    custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();
    SavedCardHelpers.addCardOnMidCustId(coftMerchant, custId, PaymentDTO.EXP_MONTH,
        PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin = SavedCardHelpers.getTin();
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("tokenType", "JWT");
    tokenMap.put("mid", coftMerchant.getId());
    tokenMap.put("custId", custId);
    tokenMap.put("filterTokenCards", "false");
    String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts,
        LocalConfig.VALID_VAULT_JWT_KEY);
    SavedCardByMidCustId savedCards = new SavedCardByMidCustId().buildRequest(coftMerchant.getId(),
        custId, false, "JWT", jwt);
    JsonPath savedCardResponse = savedCards.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(savedCardResponse.getString("responseStatus")).isEqualTo("SUCCESS");
    softly.assertThat(savedCardResponse.getString("httpCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("httpSubCode")).isEqualTo("200");
    softly.assertThat(savedCardResponse.getString("codeDetail")).isEqualTo("Success");
    softly.assertAll();
    String userId = custId;
    String logs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service, userId);
    String pgpid = logs.substring(logs.indexOf("PGP-ID") + 7, logs.indexOf("X-APP-RID") - 2);

    String fetchallResponseLogs = LogsValidationHelper.verifyLogsOnPod(
        PG2LogsValidationHelper.setEnvService.savedcard_service_facade, pgpid, "RESPONSE");
    Assertions.assertThat(fetchallResponseLogs).contains("TYPE\" : \"RESPONSE");
    Assertions.assertThat(fetchallResponseLogs).contains("user/cards/v2/fetch-all");
    Assertions.assertThat(fetchallResponseLogs).contains("ASSET_CENTER_SERVICE");
    Assertions.assertThat(fetchallResponseLogs).contains("status\":\"SUCCESS");

    String cardId = fetchallResponseLogs.substring(fetchallResponseLogs.indexOf("cardId") + 9,
        fetchallResponseLogs.indexOf("cardExpiryMonth") - 3);
    String par = fetchallResponseLogs.substring(fetchallResponseLogs.indexOf("par") + 6,
        fetchallResponseLogs.indexOf("tokenStatus") - 3);
    String cardBin = fetchallResponseLogs.substring(fetchallResponseLogs.indexOf("cardBin") + 10,
        fetchallResponseLogs.indexOf("globalPanIndex") - 3);
    String lastFourDigit = fetchallResponseLogs.substring(
        fetchallResponseLogs.indexOf("lastFourDigit") + 16,
        fetchallResponseLogs.indexOf("tokenBin") - 3);
    String globalPanIndex = fetchallResponseLogs.substring(
        fetchallResponseLogs.indexOf("globalPanIndex") + 17,
        fetchallResponseLogs.indexOf("binInfo") - 3);

    softly.assertThat(savedCardResponse.getString("response.savedCardId[0]")).isEqualTo(cardId);
    softly.assertThat(savedCardResponse.getString("response.panUniqueReference[0]")).isEqualTo(par);
    softly.assertThat(savedCardResponse.getString("response.cardFirstSixDigits[0]"))
        .isEqualTo(cardBin);
    softly.assertThat(savedCardResponse.getString("response.cardSuffix[0]"))
        .isEqualTo(lastFourDigit);
    softly.assertThat(savedCardResponse.getString("response.globalPanIndex[0]"))
        .isEqualTo(globalPanIndex);
    softly.assertAll();

  }
}
