package scripts.coft.pts;

import com.paytm.api.coft.PTS.FetchTokenDetails;
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
 * @Date 04/01/22 11:52 AM
 * @Version 1.0
 */
public class FetchTokenDetailsTests extends BaseTest {
    Constants.MerchantType COFT_MERCHANT = Constants.MerchantType.COFT_MERCHANT_3P;
    public static final String MASTERCARD_CARD_DATA = "k0U5R535Riq8mURI/nluN72e4NwieEG4P5JJdFXznWKUZCEGPElkz4Ux6llqvC4B+pbVZJ+9oxggwVFHZgVN49yMRzj5n4lDhPf8ItfytgIY+ukQeF8abHxARVI0zVKIWBslT0Od6nKSZC212H9uVMGlcOw610315BSlxFsjyRe324UNwbOPTDXusUMANkdix0rTMpqDn1qqUtfInkLbltun47WXp27TYY4n8rHVjDF+FOQjM7ae9MlktPhwIN8v2tGXXF+lzmE4udNasHxzlG4bVzb6FyXspxrwWKIdkd+m8oqYJP7xtN5Bbn/aSketL8R/3nEy2ZaP5YtgqMNG9w==";
    public static final String VISA_CARD_DATA = "JSvYCkNnkmcDk1dcZwszlFB66qVKqy1CPHfu6HBevaBVNq22WyXI9CAZkeOIeooOdA10IertvKF9Ir3S6XJ3tHGLYkbmb3dCerrOfcizwuOi0tfKzqzhEnnD215ZI5vHla9xSll4qFt2XjOmnRj04LUk6Dbad5lzsMCXAWy9Jb4Qi6giZQG50fyuFuv7R5CUJ0kA+BTum2LPqVVJtXm2gdjc5uk4/cTZxDQgyjSQA5y6mwdurGMSeiFlvo3Rw6fXuzRsU+p62hCY6/3c3WF9cge8TlrcWU7MpiB1zMhB72cHki1jHRrxTD6uo7HvlNUPeC5tUUyyhTgXAsUVE4XRXA==";
    public static final String CUST_ID = RandomStringUtils.randomAlphabetic(10);
    public static final String AUTH_REF_ID = "501123338";
    public static final String USER_ID = "";

//    @Test(description = "Verify Fetch Token Details for a TIN- Mastercard Scheme",enabled = false)
    public void verifyFetchTokenDetailsForMaster() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT.getId())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(COFT_MERCHANT.getId(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), tokenizeCardResponse.getString("head.requestId"));
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT.getId());
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenIndexNumber")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"));
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("ACTIVE");
        softly.assertThat(fetchTokenDetailsResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.cardScheme")).isEqualTo(Constants.CardScheme.MASTERCARD.get());
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenExpiry")).isNotEmpty();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenUniqueReference")).isNotEmpty();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.panUniqueReference")).isNotEmpty();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenBin")).isEqualTo("550690049");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.cardType")).isEqualTo("CC");
        softly.assertAll();
    }

    @Test(description = "Verify Fetch Token Details for a TIN- Visa Scheme")
    public void verifyFetchTokenDetailsForVisa() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();

        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(COFT_MERCHANT.getId(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), tokenizeCardResponse.getString("head.requestId"));
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.mid")).isEqualTo(COFT_MERCHANT.getId());
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenIndexNumber")).isEqualTo(tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"));
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenStatus")).isEqualTo("ACTIVE");
        softly.assertThat(fetchTokenDetailsResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.cardScheme")).isEqualTo(Constants.CardScheme.VISA.get());
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenExpiry")).isNotEmpty();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenUniqueReference")).isNotEmpty();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.panUniqueReference")).isNotEmpty();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.tokenBin")).isEqualTo("476136006");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.tokenInfo.cardType")).isEqualTo("CC");
        softly.assertAll();
    }


    @Test(description = "Verify token index number is conditional in Fetch Token Details")
    public void verifyTINIsConditional() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);

        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(COFT_MERCHANT.getId(), "", tokenizeCardResponse.getString("head.requestId"));
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }


    @Test(description = "Verify request id is conditional in Fetch Token Details")
    public void verifyRequestIdIsConditional() throws IOException, NoSuchKeyException, InterruptedException {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT.getId())
                .buildRequest(VISA_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        Awaitility.await().atMost(3, TimeUnit.SECONDS);
        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(COFT_MERCHANT.getId(), tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber"), "");
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertAll();
    }

    @Test(description = "Verify token index number/request id is mandatory in Fetch Token Details")
    public void verifyTINOrRequestIsMandatory() throws IOException, NoSuchKeyException, InterruptedException {
        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(COFT_MERCHANT.getId(), "", "");
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("requestId/tokenIndexNumber is mandatory.");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultCode")).isEqualTo("410");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify No Token Details is displayed if token does not exist")
    public void verifyNoTokenDetailsFound() throws IOException, NoSuchKeyException, InterruptedException {
        FetchTokenDetails fetchTokenDetails = new FetchTokenDetails(COFT_MERCHANT.getId(), "123456", "");
        JsonPath fetchTokenDetailsResponse = fetchTokenDetails.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultMsg")).isEqualTo("No Token Details found for given tokenIndexNumber: 123456");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultCode")).isEqualTo("405");
        softly.assertThat(fetchTokenDetailsResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

}
