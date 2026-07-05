package scripts.coft.pts;

import com.paytm.api.coft.PTS.ModifyTokenRevampStatus;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.base.test.UserManager;
import com.paytm.dto.PaymentDTO;
import com.paytm.exceptions.NoSuchKeyException;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author mayankbharshiv
 * @Date 27/05/22 10:00 AM
 * @Version 1.0
 */
public class ModifyTokenRevampStatusTests extends PGPBaseTest {

    Constants.MerchantType COFT_MERCHANT = Constants.MerchantType.COFT_MERCHANT;
    Constants.MerchantType COFT_MERCHANT_3P = Constants.MerchantType.COFT_MERCHANT_3P;
    public static final String CUST_ID = RandomStringUtils.randomAlphanumeric(70);
    public static final String PAYMENT_TYPE = "ECOM";

    public static final String VISA_CARD_DATA = "b9YnuzRm6JXy/wBtF7gAn6VFTNtmdFZfUi4bsiu34R+E8BUCxAdBERA1o8VUDCQL/529yZChV7piWqoGIVHqro2/z+O/tB8XtqFpgeQjoNwCAoydxW26YjJVhqw+UnfHQEfV6+QW0/pXjadeHMKG/NDpuMU9EqNsJtn9ZNkyCZNjEcHd+nYZyCRNaTlCP9QhXULvzVEaiASer0Znc82wkDaT7HEdsjCzQf/2fT+jQBW5vScr1htTHL7ZI6mKyyYiiup3F52kPHjPufUiealBhxsFXCBdiGQCwvd1As99xUlV7BmnlynwOqOZZprUI/sEXeSUf7f6Oqqs+MuyHfj8Ug==";
    public static final String AUTH_REF_ID = "501123338";



    @Test(description = "Verify Modify Bulk Tokens Status for a PAR- VISA Scheme",priority = 0)
    public void verifyModifyTokenStatusForVISA() throws Exception {
        User userId = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpersNew.deleteAllCardsUserId(userId.ssoToken());

        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT.getId()).buildRequest(paymentDTO.VISA_COFT_CARD,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCvvNumber(),userId.custId(),Constants.TokenizationConsent.YES.get(),null,Constants.CardSource.MANUAL_ENTERED.get(),userId.custId(),true,true);

        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.DEAD.get(), "", userId.custId(), userId.custId(), tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).generateChecksum(COFT_MERCHANT.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        List <String> list = modifyTokenRevampStatusResponse.getList("body.tokenInfos");
        System.out.println("TIN is: "+ modifyTokenRevampStatusResponse.getString("body.tokenInfos["+1+"].tokenIndexNumber"));;
        System.out.println("Size of card array is: "+ list.size());

        for (int i =0;i<list.size();i++)
        {
            String actualTin = modifyTokenRevampStatusResponse.getString("body.tokenInfos["+i+"].tokenIndexNumber");
            String expectedTin = tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference");

            if (actualTin.equalsIgnoreCase(expectedTin))
            {
                SoftAssertions softly = new SoftAssertions();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("ACCEPTED");
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("100");
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("A");
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenStatus")).isEqualTo(Constants.TokenStatus.ACTIVE.get());
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenIndexNumber")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"));
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].cardScheme")).isEqualTo(Constants.CardScheme.VISA.get());
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenExpiry")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].issuingBankName")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].cardSuffix")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenUniqueReference")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].panUniqueReference")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"));
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenBin")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].displayName")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenSuffix")).isNotEmpty();
                softly.assertAll();
            }
        }


    }

    @Test(description = "Verify Modify Single Token Status for a TIN- VISA Scheme",priority = 0)
    public void verifyModifyTokenStatusForVISASingleTin() throws Exception {
        User userId = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpersNew.deleteAllCardsUserId(userId.ssoToken());

        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT.getId()).buildRequest(paymentDTO.VISA_COFT_CARD,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCvvNumber(),userId.custId(),Constants.TokenizationConsent.YES.get(),null,Constants.CardSource.MANUAL_ENTERED.get(),userId.custId(),true,true);

        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.DEAD.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), userId.custId(), userId.custId(), tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), false).deleteContext("body.panUniqueReference")).generateChecksum(COFT_MERCHANT.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        List <String> list = modifyTokenRevampStatusResponse.getList("body.tokenInfos");
        System.out.println("TIN is: "+ modifyTokenRevampStatusResponse.getString("body.tokenInfos["+1+"].tokenIndexNumber"));;
        System.out.println("Size of card array is: "+ list.size());
        for (int i =0;i<list.size();i++) {
            String actualTin = modifyTokenRevampStatusResponse.getString("body.tokenInfos[" + i + "].tokenIndexNumber");
            String expectedTin = tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference");
            if (actualTin.equalsIgnoreCase(expectedTin)) {
                SoftAssertions softly = new SoftAssertions();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("ACCEPTED");
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("100");
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("A");
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenStatus")).isEqualTo(Constants.TokenStatus.ACTIVE.get());
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenIndexNumber")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"));
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].cardScheme")).isEqualTo(Constants.CardScheme.VISA.get());
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenExpiry")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].issuingBankName")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].cardSuffix")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenUniqueReference")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].panUniqueReference")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"));
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenBin")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].displayName")).isNotEmpty();
                softly.assertThat(modifyTokenRevampStatusResponse.getString("body.tokenInfos[0].tokenSuffix")).isNotEmpty();
                softly.assertAll();
            }
        }
    }

    @Test(description = "Verify requestedBy field is required")
    public void verifyRequestedByIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        ;
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), "", CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.requestedBy")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.requestedBy");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify requestedBy field is invalid")
    public void verifyRequestedByIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest("123", Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("Malformed JSON request");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify reasonCode field is required")
    public void verifyReasonCodeIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT.getKey());
        ;
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.reasonCode")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.reasonCode");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify reasonCode field is invalid")
    public void verifyReasonCodeIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        ;
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), "123", Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("Malformed JSON request");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify tokenStatus field is required")
    public void verifyTokenStatusIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.tokenStatus")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.tokenStatus");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify tokenStatus field is invalid")
    public void verifyTokenStatusIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), "123", tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("Malformed JSON request");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify PAR/tokenIndexNumber field is required")
    public void verifyTokenIndexNumberIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        ;
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", "", false).deleteContext("body.tokenIndexNumber")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("tokenIndexNumber/panUniqueReference is mandatory.");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("410");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify tokenIndexNumber field is invalid")
    public void verifyTokenIndexNumberIsInvalid() throws IOException, NoSuchKeyException, InterruptedException {
        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), "123", CUST_ID, "", "", true).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("No Token Details found for given tokenIndexNumber: 123");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("405");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify CustId  field is required")
    public void verifyCustIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.userInfo.custId")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("CustId/UserId is mandatory.");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("410");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify UserId field is required")
    public void verifyUserIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), "", "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.userInfo.userId")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("CustId/UserId is mandatory.");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("410");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify reason field is required")
    public void verifyReasonIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("body.reason")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestBody.reason");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify a dead token cannot be modified")
    public void verifyDeadTokenCannotBeModified() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatusInit = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.DEAD.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", "", false).generateChecksum(COFT_MERCHANT_3P.getKey());
        modifyTokenRevampStatusInit.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.FAILED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", "", false).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("State modification restricted from: DEAD to: FAILED");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("450");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

//    @Test(description = "Verify a failed token cannot be modified", enabled = false)
    public void verifyFailedTokenCannotBeModified() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        ;
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatusInit = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.FAILED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", "", false).generateChecksum(COFT_MERCHANT_3P.getKey());
        ;
        modifyTokenRevampStatusInit.execute().jsonPath();

        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.FAILED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", "", false).generateChecksum(COFT_MERCHANT_3P.getKey());
        ;
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Test(description = "isModifyAll cannot be false for panUniqueReference")
    public void verifyIsModifyAllIsNotFalseForPARRequest() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);
        ModifyTokenRevampStatus modifyTokenRevampStatus = new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.FAILED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), false).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("isModifyAll cannot be false for panUniqueReference");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("409");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify version field is required")
    public void verifyVersionIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("head.version")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.version");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify requestTimestamp field is required")
    public void verifyRequestTimestampIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("head.requestTimestamp")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestTimestamp");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify requestId field is required")
    public void verifyRequestIdIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).deleteContext("head.requestId")).generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).contains("requestHeader.requestId");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify signatureType field is required")
    public void verifySignatureTypeIsRequired() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard tokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId()).buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.MANUAL_ENTERED.get(), "").generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = tokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        ModifyTokenRevampStatus modifyTokenRevampStatus = ((ModifyTokenRevampStatus) new ModifyTokenRevampStatus(COFT_MERCHANT_3P.getId()).buildRequest(Constants.RequestedBy.TOKEN_REQUESTOR.get(), Constants.ReasonCode.CUSTOMER_CONFIRMED.get(), Constants.TokenStatus.SUSPENDED.get(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), CUST_ID, "", tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference"), true).generateChecksum(COFT_MERCHANT_3P.getKey()).deleteContext("head.signatureType"));
        JsonPath modifyTokenRevampStatusResponse = modifyTokenRevampStatus.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Signature type: ");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultCode")).isEqualTo("1005");
        softly.assertThat(modifyTokenRevampStatusResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }
}
