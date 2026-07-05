package scripts.coft.savedCardService;

import com.paytm.api.billproxy.SavedCardByTokenType;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.TokenStatus;
import com.paytm.appconstants.Constants.VAULTIDENTIFIER;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.util.Random;

import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class SaveCardByTokenTypeTest extends PGPBaseTest {
    Random random = new Random();

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify version is not mandatory param")
    public void versionNotMandatory() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.version");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify requestTimestamp is not mandatory param")
    public void verifyRequestTimestampNotMandatory() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.requestTimestamp");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify requestId is not mandatory param")
    public void verifyRequestIdNotMandatory() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.requestId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify channelId is not mandatory param")
    public void verifyChannelIdNotMandatory() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.channelId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }


    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify tokenType is mandatory param")
    public void verifyTokenTypeIsMandatory() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.tokenType");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("One or more mandatory parameters is/are missing.");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("00000002");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails")).isNull();
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify token is mandatory param")
    public void verifyTokenIsMandatory() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.token");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("One or more mandatory parameters is/are missing.");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("00000002");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails")).isNull();
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify userid is not mandatory if tokenType is SSO")
    public void verifyUserIdNotMandatoryForSSOTokenType() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("body.userId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify userid is mandatory if tokenType is JWT",groups = "Security")
    public void verifyTokenIsMandatoryForJWTTokenType() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("JWT",user).deleteContext("body.userId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid data entered by user");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("9163");
        softly.assertThat(savedCardByTokenTypeResponse.getList("body.savedCardDetails").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify error message in case of wrong JWT",groups = "Security")
    public void verifyErrorForWrongJWT() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("JWT",user).setContext("head.token","test");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid data entered by user");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("9163");
        softly.assertThat(savedCardByTokenTypeResponse.getList("body.savedCardDetails").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify no card is returned in response if not saved on user")
    public void verifyResponseWhenCardNotSaved() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user);
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("2789");
        softly.assertThat(savedCardByTokenTypeResponse.getList("body.savedCardDetails").size()).isEqualTo(0);
        softly.assertAll();
    }
//Card will not be returned in case of JWT as this was used earlier now only UserId is supported
    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify success response in case of correct JWT",groups = "Security")
    public void verifySuccessResponseJWT() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("JWT",user);
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify success response in case of correct SSO",groups = "Security")
    public void verifySuccessResponseSSO() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        System.out.println("Tin is:"+tin);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.channelId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("00000000");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardId")).containsPattern(tin);
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.maskedCardNumber")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardType")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardExpiry")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardScheme")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.bankName")).isNotNull();
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify Pending OCL token is not returned")
    public void verifySuccessResponseSSOInCaseOfPending() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,tin, VAULTIDENTIFIER.OCL.get());
        SavedCardHelpers.updateTokenStatus(tin,TokenStatus.INIT);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.channelId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("2789");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardId")).doesNotContainPattern(tin);
        softly.assertThat(savedCardByTokenTypeResponse.getList("body.savedCardDetails").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify Pending PPBL token is not returned")
    public void verifySuccessResponseSSOInCaseOfPendingPPBL() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER,VAULTIDENTIFIER.PPBL.get());
        String tin = SavedCardHelpers.getTin();
        SavedCardHelpers.deleteCardOnUser(user,tin, VAULTIDENTIFIER.PPBL.get());
        SavedCardHelpers.updateTokenStatus(tin,TokenStatus.INIT);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.channelId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("2789");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardId")).doesNotContainPattern(tin);
        softly.assertThat(savedCardByTokenTypeResponse.getList("body.savedCardDetails").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.ABHISHEK_VERMA)
    @Test(description = "Verify active PPBL token is not returned")
    public void verifySuccessResponseSSOInCaseOfActivePPBL() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER,VAULTIDENTIFIER.PPBL.get());
        String tin = SavedCardHelpers.getTin();
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType();
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.channelId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("2789");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Card does not exist for given parameters");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardId")).doesNotContainPattern(tin);
        softly.assertThat(savedCardByTokenTypeResponse.getList("body.savedCardDetails").size()).isEqualTo(0);
        softly.assertAll();
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50508")
    @Test(description = "verify request is sent to user/cards/v2/fetch-all when theia.migrateFetchAllToV2 ff4j flag is ON")
    public void requestToV2_fetchall_SavedCardByTokenType() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String xAppId= "Test"+ random.nextInt(10000);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType(xAppId);
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.version");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
        String userId = user.custId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,xAppId);
        String pgpid = logs.substring(logs.indexOf("PGP-ID")+7, logs.indexOf("X-APP-RID")-2);

        String fetchallRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpid);
        Assertions.assertThat(fetchallRequestLogs).contains("\"TYPE\" : \"REQUEST\"");
        Assertions.assertThat(fetchallRequestLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchallRequestLogs).contains("ASSET_CENTER_SERVICE");
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50508")
    @Test(description = "Verify all parameters in success response when request sent to v2/fetchall")
    public void requestToV2_fetchall_SavedCardByTokenType_SuccessResponse() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        System.out.println("Tin is:"+tin);
        String xAppId= "Test"+ random.nextInt(10000);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType(xAppId);
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.channelId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        String userId = user.custId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,xAppId);
        String pgpid = logs.substring(logs.indexOf("PGP-ID")+7, logs.indexOf("X-APP-RID")-2);

        String fetchallRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,pgpid);
        Assertions.assertThat(fetchallRequestLogs).contains("user/cards/v2/fetch-all");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("00000000");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardId")).containsPattern(tin);
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.maskedCardNumber")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardType")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardExpiry")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.cardScheme")).isNotNull();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.savedCardDetails.bankName")).isNotNull();
        softly.assertAll();
    }

    @Owner(Constants.Owner.POOJA)
    @Feature("PGP-50508")
    @Test(description = "Verify all parameters in success response when request sent to v2/fetchall")
    public void requestToV2_fetchall_SavedCardByTokenType_response() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin = SavedCardHelpers.getTin();
        System.out.println("Tin is:"+tin);
        String xAppId= "Test"+ random.nextInt(10000);
        SavedCardByTokenType savedCardByTokenType= new SavedCardByTokenType(xAppId);
        savedCardByTokenType.buildRequest("SSO",user).deleteContext("head.channelId");
        JsonPath savedCardByTokenTypeResponse = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(savedCardByTokenTypeResponse.getString("body.resultInfo.resultCode")).isEqualTo("00000000");

        String userId = user.custId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service,xAppId);
        String pgpid = logs.substring(logs.indexOf("PGP-ID")+7, logs.indexOf("X-APP-RID")-2);

        String fetchallRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpid, "REQUEST");
        Assertions.assertThat(fetchallRequestLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchallRequestLogs).contains("ASSET_CENTER_SERVICE");

        String fetchallResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.savedcard_service_facade,pgpid, "RESPONSE");
        Assertions.assertThat(fetchallResponseLogs).contains("TYPE\" : \"RESPONSE");
        Assertions.assertThat(fetchallResponseLogs).contains("user/cards/v2/fetch-all");
        Assertions.assertThat(fetchallResponseLogs).contains("ASSET_CENTER_SERVICE");
        Assertions.assertThat(fetchallResponseLogs).contains("status\":\"SUCCESS");
        Assertions.assertThat(fetchallResponseLogs).contains("cardInfos");
        Assertions.assertThat(fetchallResponseLogs).contains("binInfo");
    }


    @Owner(Constants.Owner.AJEESH)
    @Feature("VULN-24740")
    @Test(description = "Verfiy that Generic Failure is shown while hiding important data.")
    public void testDeleteSavedCardByTokenTypeGET() {
        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.GET);
        api.getRequestSpecBuilder().setContentType(ContentType.JSON);
        api.getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        api.getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_DELETE_SAVEDCARDBYTOKENTYPE);

        Response response = api.execute();
        JsonPath jsonResponse = response.jsonPath();

        assertThat(jsonResponse.getString("responseStatus")).isEqualTo("FAILURE");
        assertThat(jsonResponse.getInt("httpCode")).isEqualTo(500);
        assertThat(jsonResponse.getInt("httpSubCode")).isEqualTo(500);
        assertThat(jsonResponse.getString("codeDetail")).isEqualTo("System Error");
        assertThat(jsonResponse.getString("response")).isEqualTo("System Error");
    }

}
