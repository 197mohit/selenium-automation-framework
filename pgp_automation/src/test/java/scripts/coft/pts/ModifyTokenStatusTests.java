package scripts.coft.pts;

import com.paytm.api.coft.PTS.ModifyTokenStatus;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.appconstants.Constants;
import com.paytm.exceptions.NoSuchKeyException;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author mayankbharshiv
 * @Date 27/05/22 10:00 AM
 * @Version 1.0
 */
public class ModifyTokenStatusTests {

    Constants.MerchantType COFT_MERCHANT_3P = Constants.MerchantType.COFT_MERCHANT_3P;
    public static final String CUST_ID = RandomStringUtils.randomAlphabetic(10);
    public static final String PAYMENT_TYPE = "ECOM";
    public static final String USER_ID = "1002055843";
     public static final String VISA_CARD_DATA = "b9YnuzRm6JXy/wBtF7gAn6VFTNtmdFZfUi4bsiu34R+E8BUCxAdBERA1o8VUDCQL/529yZChV7piWqoGIVHqro2/z+O/tB8XtqFpgeQjoNwCAoydxW26YjJVhqw+UnfHQEfV6+QW0/pXjadeHMKG/NDpuMU9EqNsJtn9ZNkyCZNjEcHd+nYZyCRNaTlCP9QhXULvzVEaiASer0Znc82wkDaT7HEdsjCzQf/2fT+jQBW5vScr1htTHL7ZI6mKyyYiiup3F52kPHjPufUiealBhxsFXCBdiGQCwvd1As99xUlV7BmnlynwOqOZZprUI/sEXeSUf7f6Oqqs+MuyHfj8Ug==";
    public static final String AUTH_REF_ID = "501123338";


    @Test(description = "Verify Modify Tokens Status for a TIN- VISA Scheme")
    public void verifyModifyTokenStatusForVISA() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.DEAD.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, USER_ID).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.tokenStatus")).isEqualTo(Constants.TokenStatus.DEAD.get());
        softly.assertAll();
    }


    @Test(description = "Verify requestedBy field is required")
    public void verifyRequestedByIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.requestedBy")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.requestedBy");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify requestedBy field is invalid")
    public void verifyRequestedByIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest("123", Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("Malformed JSON request");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify reasonCode field is required")
    public void verifyReasonCodeIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.reasonCode")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.reasonCode");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify reasonCode field is invalid")
    public void verifyReasonCodeIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), "123", Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("Malformed JSON request");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify tokenStatus field is required")
    public void verifyTokenStatusIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.tokenStatus")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.tokenStatus");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify tokenStatus field is invalid")
    public void verifyTokenStatusIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), "123", tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("Malformed JSON request");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify tokenIndexNumber field is invalid")
    public void verifyTokenIndexNumberIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), "123", CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("No Token Details found for given tokenIndexNumber: 123");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("405");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify CustId  field is required")
    public void verifyCustIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.userInfo.custId")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("CustId/UserId is mandatory.");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("410");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify UserId field is required")
    public void verifyUserIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), "", "").deleteContext("body.userInfo.userId")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("CustId/UserId is mandatory.");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("410");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify reason field is required")
    public void verifyReasonIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("body.reason")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.reason");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify a dead token cannot be modified")
    public void verifyDeadTokenCannotBeModified() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatusInit = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.DEAD.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        ModifyTokenStatusInit.execute().jsonPath();

        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.FAILED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("State modification restricted from: DEAD to: FAILED");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("450");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

//    @Test(description = "Verify a failed token cannot be modified", enabled = false)
    public void verifyFailedTokenCannotBeModified() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatusInit = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.FAILED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        ModifyTokenStatusInit.execute().jsonPath();

        ModifyTokenStatus ModifyTokenStatus = new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.FAILED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Test(description = "Verify version field is required")
    public void verifyVersionIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("head.version")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.version");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify requestTimestamp field is required")
    public void verifyRequestTimestampIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("head.requestTimestamp")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestTimestamp");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify requestId field is required")
    public void verifyRequestIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "").deleteContext("head.requestId")).generateChecksum(COFT_MERCHANT_3P.getKey());;
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestId");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify signatureType field is required")
    public void verifySignatureTypeIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenStatus ModifyTokenStatus = ((ModifyTokenStatus) new ModifyTokenStatus(COFT_MERCHANT_3P.getId())
                .buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "")
                .generateChecksum(COFT_MERCHANT_3P.getKey())
                .deleteContext("head.signatureType"));
        JsonPath ModifyTokenStatusResponse = ModifyTokenStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Signature type: ");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("1005");
        softly.assertThat(ModifyTokenStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

}
