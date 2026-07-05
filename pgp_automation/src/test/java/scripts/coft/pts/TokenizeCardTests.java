package scripts.coft.pts;

import com.paytm.api.coft.PTS.TokenizeCard;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.exceptions.NoSuchKeyException;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TokenizeCardTests extends PGPBaseTest {
    Constants.MerchantType COFT_MERCHANT_3P = Constants.MerchantType.COFT_MERCHANT_3P;
    Constants.MerchantType COFT_MERCHANT = Constants.MerchantType.COFT_MERCHANT;
    public static final String MASTERCARD_CARD_DATA = "b2dWv0hyUh1Q2tyRvKfm3d3NvPr9LcXHa63u9nLcGEFCBbhsxfQiZEbp8UKnNQJPV9D6F5QRa81EuD7WfJRvf0zN3/aC1ZmRmIHcUQ9l1vJjuOVpzTpROyOgsCr2A7xCF0RRUlcFy7Q98slfwi0kvLJaDASlrA+zMdufpnYXR8DMb/0UgWYGZGqyjfAWiN80M48B8BYiqGBwwtKJNz4KsjltBqk6V59yXWuhRdAnG2N+rF9mYEsPJ6lBZZeX1fXdS3e9ssNVojduq25ejN/EeSL/DdAPHDmjtd9WUK5DZplgorhn+88y2mst77jvJ8xV5ELwfm2M2Mh53KapOWZR9A==";
    public static final String VISA_CARD_DATA = "C8+AlytR+9kV5l44BFJZBdX308YZjlChBx6b2GepjslQnwrX+cDC7ie1Jc5zgB1pDi/oR8HxJTCpZB+QnkkVK3WDF3b2ucPixvHxt+Lz1SVsBLFVW23F7omGrFEtV3jzQalZL3fZum9mYytCK+TNZB/wTvhYUMNfpvc1IInoy/wDbvdv1+hW32LTLYyd7qIIaL1Lizhz19bKKal+H+R20RCGM26ZxHBWTsQfKu3ArqdrENQAvu9PACl04Dfnu3+k72wfLqZ/DFOHWjmPdv9RKEgQFvxMwKcez8hbXdAjxVLo/6fegbYczQLDoNg4OuOjxsnf/gwjdI9Rj9j0YlfU/w==";
    public static final String CUST_ID = RandomStringUtils.randomAlphabetic(10);
    public static final String AUTH_REF_ID = "501123338";
    public static final String USER_ID = "1001839527";

    @Test(description = "Verify Tokenization of card for custId - Mastercard Scheme")
    public void verifyTokenizationForMastercardScheme() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.MASTERCARD.get())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenStatus")).isEqualTo("INIT");
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertAll();
    }

//    @Test(description = "Verify Tokenization of card for User Id- Mastercard Scheme",enabled = false)
    public void verifyTokenizationForMastercardSchemeWithUserID() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpersNew.deleteAllCardsUserId(user.ssoToken());
        Awaitility.await().atMost(3, TimeUnit.SECONDS);
        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT.getId(), Constants.CardScheme.MASTERCARD.get())
                .buildRequest(MASTERCARD_CARD_DATA, user.custId(), Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), user.custId())
                .generateChecksum(COFT_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenStatus")).isEqualTo("INIT");
        softly.assertThat(tokenizeCardResponse.getString("head.version")).isEqualTo("v1");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization of card - VISA Scheme")
    public void verifyTokenizationForVisaScheme() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(tokenizeCardResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertThat(tokenizeCardResponse.getString("body.tokenIndexNumber")).isNotEmpty();
        softly.assertThat(tokenizeCardResponse.getString("body.tokenStatus")).isEqualTo("INIT");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization fails when user consent is not provided")
    public void verifyTokenizationFailsForNoUserConsent() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).
                buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.NO.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("UserConsent not provided");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization fails when card source is invalid")
    public void verifyCardSourceIsValid() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get()).
                buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, "", "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Malformed JSON request");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization fails when encrypted card data is invalid")
    public void verifyEncryptedCardDataIsValid() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get())
                .buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("requestBody.encryptedCardData : encryptedCardData should not be empty : rejected value []");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization fails when encrypted card data is not sent")
    public void verifyEncryptedCardDataIsNotSent() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = ((TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get())
                .buildRequest("", CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "")
                .deleteContext("body.encryptedCardData"))
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("requestBody.encryptedCardData : encryptedCardData should not be empty : rejected value [null]");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization fails when card source is not sent")
    public void verifyCardSourceIsNotSent() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = ((TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "")
                .deleteContext("body.cardSource"))
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("requestBody.cardSource : must not be null : rejected value [null]");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization fails when tokenization consent is not sent")
    public void verifyTokenizationConsentIsNotSent() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = ((TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "")
                .deleteContext("body.tokenizationConsent.userConsent"))
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("requestBody.tokenizationConsent.userConsent : must not be null : rejected value [null]");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify user consent accepts 0/1")
    public void verifyTokenizationConsentIsInvalid() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get())
                .buildRequest(VISA_CARD_DATA, CUST_ID, "2", AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).contains("equestBody.tokenizationConsent.userConsent : Allowed Value for user Consent is 0/1 : rejected value [2]");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Tokenization fails when mid is invalid")
    public void verifyMidIsValid() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = new TokenizeCard("123", Constants.CardScheme.VISA.get())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).contains("No ACTIVE TRID found for merchant");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("442");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

//    @Test(description = "Verify Tokenization fails when cust id is not sent", enabled = false)
    public void verifyCustIdIsNotSent() throws IOException, NoSuchKeyException {
        TokenizeCard tokenizeCard = ((TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.VISA.get())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "")
                .deleteContext("body.userInfo.custId"))
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("requestBody.tokenizationConsent.userConsent : must not be null : rejected value [null]");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify version field is required")
    public void verifyVersionIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeCard tokenizeCard = ((TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.MASTERCARD.get())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), USER_ID)
                .deleteContext("head.version"))
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.version");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify requestTimestamp field is required")
    public void verifyRequestTimestampIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeCard tokenizeCard = ((TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.MASTERCARD.get())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), USER_ID)
                .deleteContext("head.requestTimestamp"))
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestTimestamp");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify requestId field is required")
    public void verifyRequestIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeCard tokenizeCard = ((TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.MASTERCARD.get())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), USER_ID)
                .deleteContext("head.requestId"))
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestId");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify signature field is required")
    public void verifySignatureIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeCard tokenizeCard = (TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.MASTERCARD.get())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), USER_ID)
                .generateChecksum(COFT_MERCHANT_3P.getKey())
                .deleteContext("head.signature");
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.signature");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify signatureType field is required")
    public void verifySignatureTypeIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeCard tokenizeCard = (TokenizeCard) new TokenizeCard(COFT_MERCHANT_3P.getId(), Constants.CardScheme.MASTERCARD.get())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), USER_ID)
                .generateChecksum(COFT_MERCHANT_3P.getKey())
                .deleteContext("head.signatureType");
        JsonPath tokenizeCardResponse = tokenizeCard.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Signature type: ");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultCode")).isEqualTo("1005");
        softly.assertThat(tokenizeCardResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }
}
