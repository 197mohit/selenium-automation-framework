package scripts.api.theia.FetchPublicKey;

import com.paytm.CreateToken;
import com.paytm.api.theia.FetchPublicKey;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 * Java-only coverage for {@link FetchPublicKey}; access token from {@link com.paytm.CreateToken}.
 */

public class FetchPublicKeyACCESSTest extends PGPBaseTest {

    private static String newReferenceId() {
        return String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
    }

    private static String accessTokenFromCreateToken(Constants.MerchantType merchant, String referenceId) {
        CreateToken createToken = new CreateToken(merchant, "", referenceId);
        JsonPath tokenJson = createToken.execute().jsonPath();
        Assertions.assertThat(tokenJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String accessToken = tokenJson.getString("body.accessToken");
        Assertions.assertThat(accessToken).isNotNull().isNotEmpty();
        return accessToken;
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1717")
    @Test(description = "Verify fetchPublicKey API response when tokenType is ACCESS and token is valid")
    public void fetchPublicKeySuccess_algoKeyIdAndKeyPresent() {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String referenceId = newReferenceId();
        String accessToken = accessTokenFromCreateToken(merchant, referenceId);

        Response response = new FetchPublicKey(merchant.getId(), referenceId, accessToken).execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jp = response.jsonPath();
        Assertions.assertThat(jp.getString("body.algo")).isNotNull().isNotBlank();
        Assertions.assertThat(jp.getString("body.keyId")).isNotNull().isNotBlank();
        Assertions.assertThat(jp.getString("body.key")).isNotNull().isNotBlank();
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1717")
    @Test(description = "Verify fetchPublicKey API responsewhen tokenType is ACCESS and referenceId is not passed in request body")
    public void fetchPublicKey_missingReferenceId() {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String referenceId = newReferenceId();
        String accessToken = accessTokenFromCreateToken(merchant, referenceId);

        Response response = FetchPublicKey.builder()
                .mid(merchant.getId())
                .referenceId(referenceId)
                .accessToken(accessToken)
                .omitQueryReferenceId()
                .omitBodyReferenceId()
                .build()
                .execute();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        JsonPath jpMissingRef = response.jsonPath();
        Assertions.assertThat(jpMissingRef.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jpMissingRef.getString("body.resultInfo.resultCode")).isEqualTo("1007");
        Assertions.assertThat(jpMissingRef.getString("body.resultInfo.resultMsg")).isEqualTo("Missing mandatory element");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1717")
    @Test(description = "Verify fetchPublicKey API response when tokenType is ACCESS and mid is not passed in request body")
    public void fetchPublicKey_nullMid() {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String referenceId = newReferenceId();
        String accessToken = accessTokenFromCreateToken(merchant, referenceId);

        Response response = FetchPublicKey.builder()
                .referenceId(referenceId)
                .accessToken(accessToken)
                .omitQueryMid()
                .bodyMidJsonNull()
                .build()
                .execute();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        JsonPath jpNullMid = response.jsonPath();
        Assertions.assertThat(jpNullMid.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jpNullMid.getString("body.resultInfo.resultCode")).isEqualTo("1007");
        Assertions.assertThat(jpNullMid.getString("body.resultInfo.resultMsg")).isEqualTo("Missing mandatory element");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1717")
    @Test(description = "Verify fetchPublicKey API response when tokenType is ACCESS and token is invalid")
    public void fetchPublicKey_invalidAccessToken() {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        String referenceId = newReferenceId();
        accessTokenFromCreateToken(merchant, referenceId);

        Response response = new FetchPublicKey(merchant.getId(), referenceId,
                "invalid-access-token-deadbeefdeadbeefdeadbeefdeadbeef").execute();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        JsonPath jpInvalidToken = response.jsonPath();
        Assertions.assertThat(jpInvalidToken.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jpInvalidToken.getString("body.resultInfo.resultCode")).isEqualTo("1006");
        Assertions.assertThat(jpInvalidToken.getString("body.resultInfo.resultMsg")).isEqualTo("Your Session has expired.");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PG-1717")
    @Test(description = "Verify fetchPublicKey API response when body mid does not match mid used in CreateToken")
    public void fetchPublicKey_midMismatchWithCreateToken() {
        Constants.MerchantType tokenMerchant = Constants.MerchantType.PGOnly;
        Constants.MerchantType otherMerchant = Constants.MerchantType.EMI_DC_MERCHANT;
        String referenceId = newReferenceId();
        String accessToken = accessTokenFromCreateToken(tokenMerchant, referenceId);

        Response response = new FetchPublicKey(otherMerchant.getId(), referenceId, accessToken).execute();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        JsonPath jpMidMismatch = response.jsonPath();
        Assertions.assertThat(jpMidMismatch.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jpMidMismatch.getString("body.resultInfo.resultCode")).isEqualTo("1002");
        Assertions.assertThat(jpMidMismatch.getString("body.resultInfo.resultMsg")).isEqualTo("TOKEN_VALIDATION_FAILED");
    }
}
