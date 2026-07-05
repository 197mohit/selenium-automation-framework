package scripts.coft.pts;

import com.paytm.api.coft.PTS.ExtendGenerateTokenData;
import com.paytm.api.coft.PTS.ModifyTokenRevampStatus;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.appconstants.Constants;
import com.paytm.exceptions.NoSuchKeyException;
import com.paytm.framework.ui.base.test.BaseTest;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author mayankbharshiv
 * @Date 06/01/22 06:52 PM
 * @Version 1.0
 */
public class ExtendGenerateTokenDataTests extends BaseTest {
    Constants.MerchantType COFT_MERCHANT = Constants.MerchantType.COFT_MERCHANT;
    Constants.MerchantType COFT_MERCHANT_3P = Constants.MerchantType.COFT_MERCHANT_3P;
    public static final String CUST_ID = RandomStringUtils.randomAlphabetic(10);
    public static final String PAYMENT_TYPE = "ECOM";
    public static final String USER_ID = "1002055843";
    public static final String MASTERCARD_CARD_DATA = "b2dWv0hyUh1Q2tyRvKfm3d3NvPr9LcXHa63u9nLcGEFCBbhsxfQiZEbp8UKnNQJPV9D6F5QRa81EuD7WfJRvf0zN3/aC1ZmRmIHcUQ9l1vJjuOVpzTpROyOgsCr2A7xCF0RRUlcFy7Q98slfwi0kvLJaDASlrA+zMdufpnYXR8DMb/0UgWYGZGqyjfAWiN80M48B8BYiqGBwwtKJNz4KsjltBqk6V59yXWuhRdAnG2N+rF9mYEsPJ6lBZZeX1fXdS3e9ssNVojduq25ejN/EeSL/DdAPHDmjtd9WUK5DZplgorhn+88y2mst77jvJ8xV5ELwfm2M2Mh53KapOWZR9A==";
    public static final String VISA_CARD_DATA = "b9YnuzRm6JXy/wBtF7gAn6VFTNtmdFZfUi4bsiu34R+E8BUCxAdBERA1o8VUDCQL/529yZChV7piWqoGIVHqro2/z+O/tB8XtqFpgeQjoNwCAoydxW26YjJVhqw+UnfHQEfV6+QW0/pXjadeHMKG/NDpuMU9EqNsJtn9ZNkyCZNjEcHd+nYZyCRNaTlCP9QhXULvzVEaiASer0Znc82wkDaT7HEdsjCzQf/2fT+jQBW5vScr1htTHL7ZI6mKyyYiiup3F52kPHjPufUiealBhxsFXCBdiGQCwvd1As99xUlV7BmnlynwOqOZZprUI/sEXeSUf7f6Oqqs+MuyHfj8Ug==";
    public static final String AUTH_REF_ID = "501123338";

//    @Test(description = "Verify Generate Token Data for Active TIN- Mastercard Scheme",enabled = false)
    public void verifyExtendGenerateTokenDataForMaster() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);


        ExtendGenerateTokenData ExtendGenerateTokenData = new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT_3P.getId());
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.paymentType")).isEqualTo(PAYMENT_TYPE);
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tavv")).isNotEmpty();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.cardToken")).isNotEmpty();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenIndexNumber")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"));
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.cardScheme")).isEqualTo(Constants.CardScheme.MASTERCARD.get());
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.tokenExpiry")).isNotEmpty();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.tokenUniqueReference")).isNotEmpty();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.panUniqueReference")).isNotEmpty();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.tokenBin")).isEqualTo("550690049");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.cardType")).isEqualTo("CC");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.issuingBankName")).isNotEmpty();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.displayName")).isNotEmpty();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.tokenBin")).isEqualTo("550690049");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.tokenIndexNumber")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"));
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("ACTIVE");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.cardSuffix")).isEqualTo("0040");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.tokenInfo.tokenSuffix")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify Token Data cannot be generated when cust id is mismatched")
    public void verifyExtendGenerateTokenDataForCustidMisMatch() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), "123", "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("CustId mismatch");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Token Data cannot be generated for a DEAD Token")
    public void verifyExtendGenerateTokenDataForDeadToken() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.DEAD.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.panUniqueReference")).generateChecksum(COFT_MERCHANT_3P.getKey());
        modifyTokenRevampStatus.execute().jsonPath();
        Awaitility.await().atMost(5, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("Your digital card token has expired. Kindly use another payment option for this payment");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("427");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

//    @Test(description = "Verify Token Data cannot be generated for a SUSPENDED Token", enabled = false)
    public void verifyExtendGenerateTokenDataForSuspendedToken() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.panUniqueReference")).generateChecksum(COFT_MERCHANT_3P.getKey());
        modifyTokenRevampStatus.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("Tokenization Failed, further operation not allowed");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("451");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify version field is required")
    public void verifyVersionIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("head.version")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.version");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify requestTimestamp field is required")
    public void verifyRequestTimestampIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("head.requestTimestamp")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestTimestamp");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify requestId field is required")
    public void verifyRequestIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("head.requestId")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestId");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify signatureType field is required")
    public void verifySignatureTypeIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey()).deleteContext("head.signatureType"));
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Signature type: ");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("1005");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify tokenIndexNumber field is required")
    public void verifyTokenIndexNumberIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.tokenIndexNumber")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.tokenIndexNumber");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify tokenIndexNumber field is invalid")
    public void verifyTokenIndexNumberIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        ExtendGenerateTokenData ExtendGenerateTokenData = new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, "123", CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("No Token Details found for given tokenIndexNumber");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("405");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify CustId  field is required")
    public void verifyCustInfoIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.userInfo.custId")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("CustId/UserId is mandatory.");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("410");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify paymentType field is required")
    public void verifyPaymentTypeIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest(PAYMENT_TYPE, tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.paymentType")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.paymentType");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify paymentType field is invalid")
    public void verifyPaymentTypeIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).
                buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ExtendGenerateTokenData ExtendGenerateTokenData = ((ExtendGenerateTokenData) new ExtendGenerateTokenData(COFT_MERCHANT_3P.getId()).buildRequest("123", tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ExtendGenerateTokenDataResponse = ExtendGenerateTokenData.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Malformed JSON request");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ExtendGenerateTokenDataResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

}
