package scripts.coft.savedCardService;

import com.paytm.LocalConfig;
import com.paytm.api.coft.saveCard.SavedCardByUserId;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.TokenStatus;
import com.paytm.appconstants.Constants.VAULTIDENTIFIER;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.ff4j.FF4JFeatures;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;

public class SavedCardByUserIdTests extends PGPBaseTest {
    Random random = new Random();
    RedisHelper redisHelper= new RedisHelper(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);
    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-42626")
    @Test(description = "Validate the successful transaction of fetch tokenise cards by userid with jwt token where the key has created in vault")
    public void VerifySuccessFullTxnWithUserIdWhereJwtFromVault() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.CLIENTID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("RISK_VERIFIER_PG","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("response.savedCardId")).containsPattern(tin);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error when clientId is missing in request")
    public void VerifyErrorWhenClientIdMissing() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId()).deleteContext("head.clientId");
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("F");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("400");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("410");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Mandatory fields missing");
        softly.assertThat(savedCardByUserIdResponse.getList("response").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error when tokenType is missing in request")
    public void VerifyErrorWhenTokenTypeMissing() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId()).deleteContext("head.tokenType");
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("F");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("1400");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Token Type is invalid");
        softly.assertThat(savedCardByUserIdResponse.getList("response").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error when token is missing in request")
    public void VerifyErrorWhentokenMissing() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId()).deleteContext("head.token");
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("F");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("400");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("410");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Mandatory fields missing");
        softly.assertThat(savedCardByUserIdResponse.getList("response").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error when userId is missing in request")
    public void VerifyErrorWhenuserIdMissing() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId()).deleteContext("body.userId");
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("F");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("400");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("410");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Mandatory fields missing");
        softly.assertThat(savedCardByUserIdResponse.getList("response").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify no error is thrown error when filterTokenCards is missing in request")
    public void VerifyNoErrorWhenfilterTokenCardsMissing() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId()).deleteContext("body.filterTokenCards");
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Success");
        softly.assertThat(savedCardByUserIdResponse.getList("response").size()).isNotEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify token cards are not returned in response if filter token card is true")
    public void VerifyNoTokenCardReturnedIfFilterTokenCardIsTrue() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId()).setContext("body.filterTokenCards",true);
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("204");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByUserIdResponse.getList("response").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify Success Response")
    public void VerifySuccessResponse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Success");
        softly.assertThat(savedCardByUserIdResponse.getString("response")).contains(tin);
        softly.assertAll();
    }


    @Owner(Constants.Owner.ABHISHEK_KULKARNI)
    @Feature("PGP-42626")
    @Test(description = "Validate the transaction of fetch tokenise cards by UserID with invalid jwt token")
    public void VerifySavedCardDetailsWithUserIdAndInvalidJwt() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.INVALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("F");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Jwt Validation Failure");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify pending tokens are not returned in Success Response")
    public void VerifyPendingTokensAreNotReturnedInSuccessResponse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.Tokenization_Year,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,tin, VAULTIDENTIFIER.OCL.get());
        SavedCardHelpers.updateTokenStatus(tin, TokenStatus.INIT);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("204");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByUserIdResponse.getString("response")).doesNotContain(tin);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify pending PPBL tokens are not returned in Success Response")
    public void VerifyPendingPPBLTokensAreNotReturnedInSuccessResponse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user,PaymentDTO.EXP_MONTH,PaymentDTO.Tokenization_Year,PaymentDTO.VISA_COFT_CARD_NUMBER,VAULTIDENTIFIER.PPBL.get());
        String tin = SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,tin, VAULTIDENTIFIER.PPBL.get());
        SavedCardHelpers.updateTokenStatus(tin,TokenStatus.INIT);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("204");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByUserIdResponse.getString("response")).doesNotContain(tin);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify active PPBL tokens are not returned in Success Response")
    public void VerifyActivePPBLTokensAreNotReturnedInSuccessResponse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER,VAULTIDENTIFIER.PPBL.get());
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("204");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByUserIdResponse.getString("response")).doesNotContain(tin);
        softly.assertAll();
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50508")
    @Test(description = "Verify Success Response")
    public void requestToV2_fetchall_SavedCardByUser() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String xAppId= "Test"+ random.nextInt(10000);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(xAppId);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.Tokenization_Year,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,xAppId);
        String pgpId = logs.substring(logs.indexOf("PGP-ID")+7, logs.indexOf("X-APP-RID="+xAppId)-2);

        String fetchAllRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpId, "REQUEST");
        Assertions.assertThat(fetchAllRequestLogs).contains("\"TYPE\" : \"REQUEST\"");
        Assertions.assertThat(fetchAllRequestLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchAllRequestLogs).contains("ASSET_CENTER_SERVICE");
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50508")
    @Test(description = "Verify Success Response")
    public void requestToV2_fetchall_SavedCardByUserID_SuccessResponse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String xAppId= "Test"+ random.nextInt(10000);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(xAppId);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.Tokenization_Year,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,xAppId);
        String pgpId = logs.substring(logs.indexOf("PGP-ID")+7, logs.indexOf("X-APP-RID="+xAppId)-2);

        String fetchAllRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,pgpId);
        Assertions.assertThat(fetchAllRequestLogs).contains("user/cards/v2/fetch-all");

        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("httpSubCode")).isEqualTo("200");
        softly.assertThat(savedCardByUserIdResponse.getString("codeDetail")).isEqualTo("Success");
        softly.assertThat(savedCardByUserIdResponse.getString("response")).contains(tin);
        softly.assertAll();
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50508")
    @Test(description = "Verify Success Response")
    public void requestToV2_fetchall_SavedCardByUserID_Response() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String xAppId= "Test"+ random.nextInt(10000);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(xAppId);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.Tokenization_Year,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,xAppId);
        String pgpId = logs.substring(logs.indexOf("PGP-ID")+7, logs.indexOf("X-APP-RID="+xAppId)-2);

        String fetchAllRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpId, "REQUEST");
        Assertions.assertThat(fetchAllRequestLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchAllRequestLogs).contains("ASSET_CENTER_SERVICE");

        String fetchAllResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpId, "RESPONSE");
        Assertions.assertThat(fetchAllResponseLogs).contains("TYPE\" : \"RESPONSE");
        Assertions.assertThat(fetchAllResponseLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchAllResponseLogs).contains("ASSET_CENTER_SERVICE");
        Assertions.assertThat(fetchAllResponseLogs).contains("status\":\"SUCCESS");
        Assertions.assertThat(fetchAllResponseLogs).contains("cardInfos");
        Assertions.assertThat(fetchAllResponseLogs).contains("binInfo");
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50508")
    @Test(description = "Verify Success Response")
    public void SavedCardByUserID_Response_Validate() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        FF4JFlags.enable("sc_returnSavedCardFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_DEFAULT.defaultEnableJwtAuthenticationForCardService");
        redisHelper.delete("FF4J_FEATURE_DEFAULT.returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_DEFAULT.sc_returnSavedCardFromPlatformForUserId");
        String xAppId= "Test"+ random.nextInt(10000);
        SavedCardByUserId savedCardByUserId = new SavedCardByUserId(xAppId);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,PaymentDTO.EXP_MONTH,PaymentDTO.Tokenization_Year,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("userId", user.custId());
        String jwt = PGPHelpers.createJsonWebToken(tokenMap, PGPHelpers.ISSUER.ts, LocalConfig.VALID_VAULT_JWT_KEY);
        savedCardByUserId.buildRequest("CC_BILL_PAYMENT","JWT",jwt,user.custId());
        SoftAssertions softly = new SoftAssertions();
        JsonPath savedCardByUserIdResponse = savedCardByUserId.execute().jsonPath();
        softly.assertThat(savedCardByUserIdResponse.getString("responseStatus")).isEqualTo("SUCCESS");
        softly.assertThat(savedCardByUserIdResponse.getString("httpCode")).isEqualTo("200");
        softly.assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,xAppId);
        String pgpId = logs.substring(logs.indexOf("PGP-ID")+7, logs.indexOf("X-APP-RID="+xAppId)-2);

        String fetchAllRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpId, "REQUEST");
        Assertions.assertThat(fetchAllRequestLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchAllRequestLogs).contains("ASSET_CENTER_SERVICE");

        String fetchAllResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpId, "RESPONSE");
        Assertions.assertThat(fetchAllResponseLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchAllResponseLogs).contains("ASSET_CENTER_SERVICE");
        Assertions.assertThat(fetchAllResponseLogs).contains("status\":\"SUCCESS");

        String cardId = fetchAllResponseLogs.substring(fetchAllResponseLogs.indexOf("cardId")+9, fetchAllResponseLogs.indexOf("cardExpiryMonth")-3);
        String par = fetchAllResponseLogs.substring(fetchAllResponseLogs.indexOf("par")+6, fetchAllResponseLogs.indexOf("tokenStatus")-3);
        String tokenBin = fetchAllResponseLogs.substring(fetchAllResponseLogs.indexOf("tokenBin")+11, fetchAllResponseLogs.indexOf("cardDisplayName")-3);
        String lastFourDigit = fetchAllResponseLogs.substring(fetchAllResponseLogs.indexOf("lastFourDigit")+16, fetchAllResponseLogs.indexOf("tokenBin")-3);
        String globalPanIndex = fetchAllResponseLogs.substring(fetchAllResponseLogs.indexOf("globalPanIndex")+17, fetchAllResponseLogs.indexOf("binInfo")-3);

        System.out.println("Details from logs are"+ cardId+" "+par+" "+tokenBin+" "+lastFourDigit+" "+globalPanIndex);
        softly.assertThat(savedCardByUserIdResponse.getString("response[0].savedCardId")).isEqualTo(cardId);
        softly.assertThat(savedCardByUserIdResponse.getString("response[0].panUniqueReference")).isEqualTo(par);
        softly.assertThat(savedCardByUserIdResponse.getString("response[0].tokenBin")).isEqualTo(tokenBin);
        softly.assertThat(savedCardByUserIdResponse.getString("response[0].cardSuffix")).isEqualTo(lastFourDigit);
        softly.assertThat(savedCardByUserIdResponse.getString("response[0].globalPanIndex")).isEqualTo(globalPanIndex);
        softly.assertAll();
    }
}