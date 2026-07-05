package scripts.coft.pts;

import com.paytm.api.coft.PTS.TokenizeCard;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

@Owner(Constants.Owner.PRAGYA_KURELE)
@Feature("PGP-41977")

public class ErrorMessagingInTokenisationFailure extends PGPBaseTest {

    public static final String CUST_ID = RandomStringUtils.randomAlphabetic(10);
    Constants.MerchantType COFT_ERROR_MERCHANT = Constants.MerchantType.COFT_ERROR_MERCHANT;
    Constants.MerchantType COFT_MERCHANT_3P = Constants.MerchantType.COFT_MERCHANT_3P;

    @Test(description = "Verify the error message as \"Failure from card network\" and response code as 701\n" +
            "when tokenisation failed at card network due to decline response- VISA Cardscheme")
    public void TC_001_verifyTokenizationForVisaDecline() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_ERROR_MERCHANT.getId())
                .buildRequest(PaymentDTO.COFT_DECLINE_VISA_CARD, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV, CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_DECLINE_VISA_CARD, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_ERROR_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Failure from card network");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("701");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_ERROR_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("FAILED");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardScheme")).isEqualTo("VISA");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardType")).isNotEmpty();
        softly.assertAll();
    }


    @Test(description = "Verify the error mesage \"TRID not configured at card network\" and response code 717\n" +
            "when tokenisation failed at network due to trConfigIssue")
    public void TC_002_verifyTokenizationForVisatrConfigIssue() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_ERROR_MERCHANT.getId())
                .buildRequest(PaymentDTO.COFT_TRCONFIG_ISSUE_VISA_CARD, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV, CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_TRCONFIG_ISSUE_VISA_CARD, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_ERROR_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("TRID not configured at card network");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("717");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_ERROR_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("FAILED");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardScheme")).isEqualTo("VISA");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardType")).isNotEmpty();
        softly.assertAll();

    }

    @Test(description = "Verify the error mesage \"Failure from card network\" and response code 701\n" +
            "when tokenisation failed at network due to notAllowed")
    public void TC_003_verifyTokenizationForVisaNotAllwed() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_ERROR_MERCHANT.getId())
                .buildRequest(PaymentDTO.COFT_NOTALLOWED_VISA_CARD, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV, CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_NOTALLOWED_VISA_CARD, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_ERROR_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Failure from card network");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("701");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_ERROR_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("FAILED");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardScheme")).isEqualTo("VISA");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardType")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Bad Request from card Network\" and response code 701\n" +
            "when tokenisation failed at network due to invalidRequest")
    public void TC_004_verifyTokenizationForVisaInvalidRequest() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_ERROR_MERCHANT.getId())
                .buildRequest(PaymentDTO.COFT_INVALIDREQUEST_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV, CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_INVALIDREQUEST_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_ERROR_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Bad Request from card Network");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("700");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_ERROR_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("FAILED");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardScheme")).isEqualTo("VISA");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardType")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Card not allowed by network\" and response code 713\n" +
            "when tokenisation failed at network due to cardNotAllowed")
    public void TC_005_verifyTokenizationForCardNotAllowed() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_ERROR_MERCHANT.getId())
                .buildRequest(PaymentDTO.COFT_CARD_NOT_ALLOWED_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV, CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_CARD_NOT_ALLOWED_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_ERROR_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Card not allowed by card network");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("713");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_ERROR_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("FAILED");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardScheme")).isEqualTo("VISA");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardType")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Error connecting with card network\" and response code 708\n" +
            "when tokenisation failed at network due to WebClientRequestException")
    public void TC_006_verifyTokenizationForFailedNetwork() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_ERROR_MERCHANT.getId())
                .buildRequest(PaymentDTO.COFT_FAILEDNETWORK_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV, CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_FAILEDNETWORK_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_ERROR_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Error connecting with card network");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("708");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_ERROR_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("FAILED");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardScheme")).isEqualTo("VISA");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardType")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Bad Request from card Network\" and response code 700\n" +
            "when tokenisation failed at network due to invalidParameter")
    public void TC_007_verifyTokenizationForInvalidParameter() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_ERROR_MERCHANT.getId())
                .buildRequest(PaymentDTO.COFT_INVALIDPARAMETER_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV, CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_INVALIDPARAMETER_VISA, PaymentDTO.COFT_DECLINE_VISA_EXP_MONTH, PaymentDTO.COFT_DECLINE_VISA_EXP_YEAR, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_ERROR_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Bad Request from card Network");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("700");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_ERROR_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("FAILED");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardScheme")).isEqualTo("VISA");
        softly.assertThat(tokenizeCardResponse.getString("body.tokenInfo.cardType")).isNotEmpty();
        softly.assertAll();
    }

    /*------------------ASYNC FLOW-------------------*/


    @Test(description = "Verify the error message as \"Failure from card network\" and response code as 701\n" +
            "when tokenisation failed at card network due to decline response- VISA Cardscheme")
    public void TC_008_verifyTokenizationForVisaDecline() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_DECLINE_VISA_CARD, PaymentDTO.COFT_VISA_MONTH_EXPIRY, PaymentDTO.COFT_VISA_YEAR_EXPIRY, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_MERCHANT_3P.getKey());

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"TRID not configured at card network\" and response code 717\n" +
            "when tokenisation failed at network due to trConfigIssue")
    public void TC_009_verifyTokenizationForVisatrConfigIssue() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_TRCONFIG_ISSUE_VISA_CARD, PaymentDTO.COFT_VISA_MONTH_EXPIRY, PaymentDTO.COFT_VISA_YEAR_EXPIRY, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_MERCHANT_3P.getKey());

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertAll();
    }


    @Test(description = "Verify the error mesage \"Failure from card network\" and response code 701\n" +
            "when tokenisation failed at network due to notAllowed")
    public void TC_010_verifyTokenizationForVisaNotAllwed() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_NOTALLOWED_VISA_CARD, PaymentDTO.COFT_VISA_MONTH_EXPIRY, PaymentDTO.COFT_VISA_YEAR_EXPIRY, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_MERCHANT_3P.getKey());

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Bad Request from card Network\" and response code 701\n" +
            "when tokenisation failed at network due to invalidRequest")
    public void TC_011_verifyTokenizationForVisaInvalidRequest() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_INVALIDREQUEST_VISA, PaymentDTO.COFT_VISA_MONTH_EXPIRY, PaymentDTO.COFT_VISA_YEAR_EXPIRY, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_MERCHANT_3P.getKey());

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Card not allowed by network\" and response code 713\n" +
            "when tokenisation failed at network due to cardNotAllowed")
    public void TC_012_verifyTokenizationForCardNotAllowed() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_CARD_NOT_ALLOWED_VISA, PaymentDTO.COFT_VISA_MONTH_EXPIRY, PaymentDTO.COFT_VISA_YEAR_EXPIRY, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_MERCHANT_3P.getKey());

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Error connecting with card network\" and response code 708\n" +
            "when tokenisation failed at network due to WebClientRequestException")
    public void TC_013_verifyTokenizationForFailedNetwork() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), false, true)
                .encryptCardData(PaymentDTO.COFT_FAILEDNETWORK_VISA, PaymentDTO.COFT_VISA_MONTH_EXPIRY, PaymentDTO.COFT_VISA_YEAR_EXPIRY, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_MERCHANT_3P.getKey());

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertAll();
    }

    @Test(description = "Verify the error mesage \"Bad Request from card Network\" and response code 700\n" +
            "when tokenisation failed at network due to invalidParameter")
    public void TC_014_verifyTokenizationForInvalidParameter() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), null,
                        Constants.CardSource.CARD_ON_FILE.get(), user.custId(), true, true)
                .encryptCardData(PaymentDTO.COFT_INVALIDPARAMETER_VISA, PaymentDTO.COFT_VISA_MONTH_EXPIRY, PaymentDTO.COFT_VISA_YEAR_EXPIRY, PaymentDTO.COFT_VISA_CVV)
                .generateChecksum(COFT_MERCHANT_3P.getKey());

        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertAll();
    }


}
