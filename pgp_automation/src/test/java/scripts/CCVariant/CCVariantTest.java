package scripts.CCVariant;

import com.paytm.api.billproxy.CardTokenizeViaCin;
import com.paytm.api.billproxy.FetchBinRequest;
import com.paytm.api.billproxy.FetchCinRequest;
import com.paytm.api.billproxy.SavedCardByTokenType;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;


@Owner(Constants.Owner.PRIYANSHI)
@Feature("PGP-32476")
public class CCVariantTest extends PGPBaseTest {


    @Test(description = "To verify Parameters of head and body and Verify success response for Diners Privilege variant")
    public void verifyingDinersPrivilegeHDFCBin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        FetchBinRequest fetchBinRequest = new FetchBinRequest(user.ssoToken());
        fetchBinRequest.setContext("body.binNumber", PaymentDTO.DINERS_PRIVILEGE_CONSUMER_HDFC_BIN);
        JsonPath withDrawJson = fetchBinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("head.clientId")).isEqualTo("ANDROID");
        softly.assertThat(withDrawJson.getString("head.version")).isEqualTo("v5");
        softly.assertThat(withDrawJson.getString("head.responseTimestamp")).isNotNull();
        softly.assertThat(withDrawJson.getString("head.signature")).isEmpty();
        softly.assertThat(withDrawJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(withDrawJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("body.responseStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("body.issuingBank")).isEqualTo("HDFC");
        softly.assertThat(withDrawJson.getString("body.cardScheme")).isEqualTo("MASTER");
        softly.assertThat(withDrawJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertThat(withDrawJson.getString("body.isIndian")).isEqualTo("TRUE");
        softly.assertThat(withDrawJson.getString("body.displayName")).isEqualTo("HDFC Bank");
        softly.assertThat(withDrawJson.getString("body.cardVariant")).isEqualTo("Diners Privilege");
        softly.assertAll();
    }



    @Test(description = "Verify success response for Diners Black variant")
    public void verifyingDinersBlackHDFCBin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        FetchBinRequest fetchBinRequest = new FetchBinRequest(user.ssoToken());
        fetchBinRequest.setContext("body.binNumber", PaymentDTO.DINERS_BLACK_PREMIUM_HDFC_BIN);
        JsonPath withDrawJson = fetchBinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(withDrawJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("body.responseStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("body.issuingBank")).isEqualTo("HDFC");
        softly.assertThat(withDrawJson.getString("body.cardScheme")).isEqualTo("MASTER");
        softly.assertThat(withDrawJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertThat(withDrawJson.getString("body.isIndian")).isEqualTo("TRUE");
        softly.assertThat(withDrawJson.getString("body.displayName")).isEqualTo("HDFC Bank");
        softly.assertThat(withDrawJson.getString("body.cardVariant")).isEqualTo("Diners Black");
        softly.assertAll();
    }

    @Test(description = "Verify success response for Platinum Times variant")
    public void verifyingPlatinumTimesHDFCBin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        FetchBinRequest fetchBinRequest = new FetchBinRequest(user.ssoToken());
        fetchBinRequest.setContext("body.binNumber", PaymentDTO.PLATINUM_TIMES_SIGNATURE_HDFC_BIN);
        JsonPath withDrawJson = fetchBinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(withDrawJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("body.responseStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("body.issuingBank")).isEqualTo("HDFC");
        softly.assertThat(withDrawJson.getString("body.cardScheme")).isEqualTo("MASTER");
        softly.assertThat(withDrawJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertThat(withDrawJson.getString("body.isIndian")).isEqualTo("TRUE");
        softly.assertThat(withDrawJson.getString("body.displayName")).isEqualTo("HDFC Bank");
        softly.assertThat(withDrawJson.getString("body.cardVariant")).isEqualTo("Platinum Times");
        softly.assertAll();
    }

    @Test(description = "Verify success response for Regalia ICICI variant")
    public void verifyingRegaliaICICIBin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        FetchBinRequest fetchBinRequest = new FetchBinRequest(user.ssoToken());
        fetchBinRequest.setContext("body.binNumber", PaymentDTO.REGALIA_SIGNATURE_ICICI_BIN);
        JsonPath withDrawJson = fetchBinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(withDrawJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("body.responseStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("body.issuingBank")).isEqualTo("ICICI");
        softly.assertThat(withDrawJson.getString("body.cardScheme")).isEqualTo("MASTER");
        softly.assertThat(withDrawJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertThat(withDrawJson.getString("body.isIndian")).isEqualTo("TRUE");
        softly.assertThat(withDrawJson.getString("body.displayName")).isEqualTo("ICICI Bank");
        softly.assertThat(withDrawJson.getString("body.cardVariant")).isEqualTo("Regalia");
        softly.assertAll();
    }

    @Test(description = "Verify success response for Eden ICICI variant")
    public void verifyingEdenICICIBin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        FetchBinRequest fetchBinRequest = new FetchBinRequest(user.ssoToken());
        fetchBinRequest.setContext("body.binNumber", PaymentDTO.EDEN_PREMIUM_ICICI_BIN);
        JsonPath withDrawJson = fetchBinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(withDrawJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("body.responseStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("body.issuingBank")).isEqualTo("ICICI");
        softly.assertThat(withDrawJson.getString("body.cardScheme")).isEqualTo("MASTER");
        softly.assertThat(withDrawJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertThat(withDrawJson.getString("body.isIndian")).isEqualTo("TRUE");
        softly.assertThat(withDrawJson.getString("body.displayName")).isEqualTo("ICICI Bank");
        softly.assertThat(withDrawJson.getString("body.cardVariant")).isEqualTo("Eden");
        softly.assertAll();
    }


    @Test(description = "Verify that application return proper error messages when bin below 6 digits are sent in request")
    public void verifyingResponseViaPassingIncorrectBINBelow6Digits() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        FetchBinRequest fetchBinRequest = new FetchBinRequest(user.ssoToken());
        fetchBinRequest.setContext("body.binNumber", "");
        JsonPath withDrawJson = fetchBinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("body.responseCode")).isEqualTo("101");
        softly.assertThat(withDrawJson.getString("body.message")).isEqualTo("Illegal parameters");
        softly.assertThat(withDrawJson.getString("body.responseMessage")).isEqualTo("PARAM_ILLEGAL");
        softly.assertThat(withDrawJson.getString("body.responseStatus")).isEqualTo("U");
        softly.assertAll();
    }

    @Test(description = "Verify that application return proper error messages when bin below 9 digits are sent in request")
    public void verifyingResponseViaPassingIncorrectBINBelow9Digit() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        FetchBinRequest fetchBinRequest = new FetchBinRequest(user.ssoToken());
        fetchBinRequest.setContext("body.binNumber", "23456737");
        JsonPath withDrawJson = fetchBinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("body.responseCode")).isEqualTo("411");
        softly.assertThat(withDrawJson.getString("body.responseMessage")).isEqualTo("FETCH_BIN_FAILURE");
        softly.assertThat(withDrawJson.getString("body.message")).isEqualTo("Fetch bin failed");
        softly.assertThat(withDrawJson.getString("body.responseStatus")).isEqualTo("F");
        softly.assertThat(withDrawJson.getString("body.issuingBank")).isEqualTo(null);
        softly.assertThat(withDrawJson.getString("body.cardScheme")).isEqualTo(null);
        softly.assertThat(withDrawJson.getString("body.cardType")).isEqualTo(null);
        softly.assertThat(withDrawJson.getString("body.isIndian")).isEqualTo(null);
        softly.assertThat(withDrawJson.getString("body.displayName")).isEqualTo(null);
        softly.assertThat(withDrawJson.getString("body.cardVariant")).isEqualTo(null);
        softly.assertAll();
    }

    @Test(description = "Verifying success response for Diners Privilege variant Via FetchCin API")
    public void verifyingResponseOfDinersPrivilegeViaFetchCinAPI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.DINERS_PRIVILEGE_CONSUMER_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(jsonPath.getString("body.message")).isEqualTo("Success");
        softly.assertThat(jsonPath.getString("body.cardToken")).isNotNull();
        softly.assertThat(jsonPath.getString("body.issuingBank")).isEqualTo("HDFC");
        softly.assertThat(jsonPath.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertThat(jsonPath.getString("body.firstSixDigits")).isEqualTo(PaymentDTO.DINERS_PRIVILEGE_CONSUMER_HDFC_BIN);
        String cardToken = jsonPath.getString("body.cardToken");
        FetchCinRequest fetchCinRequest = new FetchCinRequest();
        fetchCinRequest.setContext("body.cardToken",cardToken);
        JsonPath resultJson = fetchCinRequest.execute().jsonPath();
        softly.assertThat(resultJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(resultJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(resultJson.getString("body.cardVariant")).isEqualTo("Diners Privilege");
        softly.assertThat(resultJson.getString("body.issuingBank")).isEqualTo("HDFC");
        softly.assertThat(resultJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertAll();
    }

    @Test(description = "Verifying success response for Diners Black variant Via FetchCin API")
    public void verifyingResponseOfDinersBlackViaFetchCinAPI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.DINERS_BLACK_PREMIUM_HDFC_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        String cardToken = jsonPath.getString("body.cardToken");
        FetchCinRequest fetchCinRequest = new FetchCinRequest();
        fetchCinRequest.setContext("body.cardToken",cardToken);
        JsonPath resultJson = fetchCinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(resultJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(resultJson.getString("body.cardVariant")).isEqualTo("Diners Black");
        softly.assertThat(resultJson.getString("body.issuingBank")).isEqualTo("HDFC");
        softly.assertThat(resultJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertAll();
    }

    @Test(description = "Verifying success response for Platinum Times variant Via FetchCin API")
    public void verifyingResponseOfPlatinumTimesViaFetchCinAPI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.PLATINUM_TIMES_SIGNATURE_HDFC_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        String cardToken = jsonPath.getString("body.cardToken");
        FetchCinRequest fetchCinRequest = new FetchCinRequest();
        fetchCinRequest.setContext("body.cardToken",cardToken);
        JsonPath resultJson = fetchCinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(resultJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(resultJson.getString("body.cardVariant")).isEqualTo("Platinum Times");
        softly.assertThat(resultJson.getString("body.issuingBank")).isEqualTo("HDFC");
        softly.assertThat(resultJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertAll();
    }

    @Test(description = "Verifying success response for Regalia Signature variant Via FetchCin API")
    public void verifyingResponseOfRegaliaSignatureViaFetchCinAPI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.REGALIA_SIGNATURE_ICICI_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        String cardToken = jsonPath.getString("body.cardToken");
        FetchCinRequest fetchCinRequest = new FetchCinRequest();
        fetchCinRequest.setContext("body.cardToken",cardToken);
        JsonPath resultJson = fetchCinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(resultJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(resultJson.getString("body.cardVariant")).isEqualTo("Regalia");
        softly.assertThat(resultJson.getString("body.issuingBank")).isEqualTo("ICICI");
        softly.assertThat(resultJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertAll();
    }

    @Test(description = "Verifying success response for Eden Premium variant Via FetchCin API")
    public void verifyingResponseOfEdenPremiumViaFetchCinAPI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.EDEN_PREMIUM_ICICI_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        String cardToken = jsonPath.getString("body.cardToken");
        FetchCinRequest fetchCinRequest = new FetchCinRequest();
        fetchCinRequest.setContext("body.cardToken",cardToken);
        JsonPath resultJson = fetchCinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultJson.getString("body.responseCode")).isEqualTo("200");
        softly.assertThat(resultJson.getString("body.responseMessage")).isEqualTo("SUCCESS");
        softly.assertThat(resultJson.getString("body.cardVariant")).isEqualTo("Eden");
        softly.assertThat(resultJson.getString("body.issuingBank")).isEqualTo("ICICI");
        softly.assertThat(resultJson.getString("body.cardType")).isEqualTo("CREDIT_CARD");
        softly.assertAll();
    }

    @Test(description = "Verify that proper error message is displayed if incorrect cin is sent in request")
    public void verifyingErrorResponseViaSendingInvalidCin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.EDEN_PREMIUM_ICICI_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin","1234");
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.responseCode")).isEqualTo("410");
        softly.assertThat(jsonPath.getString("body.responseMessage")).isEqualTo("TOKEN_FAILURE");
        softly.assertThat(jsonPath.getString("body.message")).isEqualTo("Token generation failed");
        softly.assertThat(jsonPath.getString("body.responseStatus")).isEqualTo("F");
        softly.assertAll();
    }



    @Test(description = "Verify that proper error message is displayed if incorrect cardToken is sent in request")
    public void verifyingErrorMessageViaSendingIncorrectCardToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.EDEN_PREMIUM_ICICI_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        FetchCinRequest fetchCinRequest = new FetchCinRequest();
        fetchCinRequest.setContext("body.cardToken","2");
        JsonPath resultJson = fetchCinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultJson.getString("body.responseCode")).isEqualTo("419");
        softly.assertThat(resultJson.getString("body.responseMessage")).isEqualTo("INVALID_TOKEN");
        softly.assertThat(resultJson.getString("body.message")).isEqualTo("Invalid token");
        softly.assertThat(resultJson.getString("body.responseStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify that proper error message is displayed if incorrect signature is sent in request")
    public void verifyingErrorResponseViaSendingIncorrectSignature() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.EDEN_PREMIUM_ICICI_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        String cardToken = jsonPath.getString("body.cardToken");
        FetchCinRequest fetchCinRequest = new FetchCinRequest();
        fetchCinRequest.setContext("body.cardToken",cardToken);
        fetchCinRequest.setContext("body.signature","2");
        JsonPath resultJson = fetchCinRequest.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resultJson.getString("body.responseCode")).isEqualTo("103");
        softly.assertThat(resultJson.getString("body.message")).isEqualTo("Invalid JSON");
        softly.assertThat(resultJson.getString("body.responseMessage")).isEqualTo("PARSE_ERROR");
        softly.assertThat(resultJson.getString("body.responseStatus")).isEqualTo("U");
        softly.assertAll();
    }

    @Test(description = "Verify that proper error message is displayed if incorrect SSO Token is sent in request")
    public void verifyingErrorResponseViaSendingIncorrectSSOToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.EDEN_PREMIUM_ICICI_CARD);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token","12345");
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(withDrawJson.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid token");
        softly.assertAll();
    }

    @Test(description = "Verify 'issuingBankCardVariant' is returned as null if card variant is not set")
    public void verifyingIssuingBankCardVariantIfCardIsCardVariantNotSet() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER);
        SavedCardByTokenType savedCardByTokenType = new SavedCardByTokenType();
        savedCardByTokenType.setContext("head.token",user.ssoToken());
        savedCardByTokenType.setContext("body.userId",user.custId());
        JsonPath withDrawJson = savedCardByTokenType.execute().jsonPath();
        String cinNumberPath = withDrawJson.getString("body.savedCardDetails.cardIndexNumber");
        String cinNumber = cinNumberPath.substring(1,cinNumberPath.length()-1);
        CardTokenizeViaCin cardTokenizeViaCin = new CardTokenizeViaCin(user.ssoToken());
        cardTokenizeViaCin.setContext("body.cin",cinNumber);
        JsonPath jsonPath = cardTokenizeViaCin.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jsonPath.getString("body.cardVariant")).isNull();
        softly.assertAll();
    }



}
