package scripts.coft.pts;

import com.paytm.api.coft.PTS.FetchPendingTokens;
import com.paytm.apphelpers.SavedCardHelpersNew;
import com.paytm.exceptions.NoSuchKeyException;
import com.paytm.framework.ui.base.test.BaseTest;
import io.restassured.path.json.JsonPath;
import io.swagger.models.auth.In;
import java.time.Instant;
import java.util.Date;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 17/03/22 12:14 PM
 * @Version 1.0
 */
public class FetchPendingTokensTests extends BaseTest {
    public static final String USER_ID = "1002055843";
    public static final String NO_TOKENS_USER_ID="1001839527";
    public static String timeStamp= String.valueOf(new Date().getTime());

    @Test(description = "Verify Pending Tokens are returned")
    public void verifyPendingTokensAreReturned() throws UnsupportedEncodingException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = new FetchPendingTokens().buildRequest(requestId, signature, USER_ID,timeStamp );
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        List<HashMap> tokenInfos = fetchPendingTokensResponse.get("body.tokenInfos");
        for (HashMap responseAttributes : tokenInfos) {
            softly.assertThat(responseAttributes.get("cardPrefixHash").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("tokenIndexNumber").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("cardScheme").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("issuingBankName").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("cardSuffix").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("cardType").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("displayName").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("instId").toString()).isNotEmpty();
            softly.assertThat(responseAttributes.get("tokenStatus").toString()).isEqualTo("INIT");
        }
        softly.assertThat(fetchPendingTokensResponse.getString("head.version")).isEqualTo("v1");
        softly.assertThat(fetchPendingTokensResponse.getString("head.responseTimestamp")).isNotEmpty();
        softly.assertThat(fetchPendingTokensResponse.getString("head.requestId")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify version field is required")
    public void verifyVersionIsRequired() throws IOException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = (FetchPendingTokens) new FetchPendingTokens().buildRequest(requestId, signature, USER_ID, timeStamp).deleteContext("head.version");
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).contains("Invalid JWT");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify requestTimestamp field is required")
    public void verifyRequestTimestampIsRequired() throws IOException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = (FetchPendingTokens) new FetchPendingTokens().buildRequest(requestId, signature, USER_ID,timeStamp ).deleteContext("head.requestTimestamp");
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).contains("Invalid JWT");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify requestId field is required")
    public void verifyRequestIdIsRequired() throws IOException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = (FetchPendingTokens) new FetchPendingTokens().buildRequest(requestId, signature, USER_ID, timeStamp).deleteContext("head.requestId");
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).contains("Invalid JWT");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify signature field is required")
    public void verifySignatureIsRequired() throws IOException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = (FetchPendingTokens) new FetchPendingTokens().buildRequest(requestId, signature, USER_ID, timeStamp).deleteContext("head.signature");
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).contains("Invalid JWT");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }


    @Test(description = "Verify signatureType field is required")
    public void verifySignatureTypeIsRequired() throws IOException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = (FetchPendingTokens) new FetchPendingTokens().buildRequest(requestId, signature, USER_ID, timeStamp).deleteContext("head.signatureType");
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).isEqualTo("INTERNAL_SYSTEM_ERROR");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("500");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify userId is required")
    public void verifyUserIdIsNotSent() throws IOException, NoSuchKeyException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId,timeStamp );

        FetchPendingTokens fetchPendingTokens = (FetchPendingTokens) new FetchPendingTokens().buildRequest(requestId, signature, USER_ID, timeStamp).deleteContext("body.userId");
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).isEqualTo("requestBody.userId : userId should not be empty : rejected value [null]");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify response for invalid userID")
    public void verifyUserdIsInvalid() throws IOException, NoSuchKeyException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = (FetchPendingTokens) new FetchPendingTokens().buildRequest(requestId, signature, "1234567890", timeStamp);
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).contains("No Token Details found for given userId");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("408");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify Pending Tokens are not returned if there are no failed/inti on USER")
    public void verifyPendingTokensAreNotReturned() throws UnsupportedEncodingException {
        String requestId = UUID.randomUUID().toString();
        String signature = SavedCardHelpersNew.jwtSignature(requestId, timeStamp);

        FetchPendingTokens fetchPendingTokens = new FetchPendingTokens().buildRequest(requestId, signature, NO_TOKENS_USER_ID,timeStamp );
        JsonPath fetchPendingTokensResponse = fetchPendingTokens.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultMsg")).contains("No Token Details found for given userId");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultCode")).isEqualTo("408");
        softly.assertThat(fetchPendingTokensResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    

}
