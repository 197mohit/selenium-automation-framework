package scripts.UPI.OnlineUpi;

import com.paytm.api.theia.FetchMerchantUserInfo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Test;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;

/**
 * AI-Generated: 2025-01-04 - Test class for FetchMerchantUserInfo API
 * This class contains test methods for the fetchMerchantUserInfo API endpoint
 */
public class FetchMerchantUserInfoAPITests extends PGPBaseTest {

    /**
     * Validate mcc and upiVerified fields in API response
     */
    @Feature("PGP-60763")
    @Owner("Lokesh_Saxena")
    @Test(description = "Verify mcc and upiVerified fields are present and valid in fetchMerchantUserInfo API response")
    public void validateMccAndUpiVerifiedInResponse() throws Exception {
        
        // Get merchant ID from Constants (no hardcoding)
        String mid = Constants.MerchantType.ALLOWED_TPAP_MERCHANT_PPSL.getId();
        
        // Generate unique order ID using codebase pattern (no hardcoding)
        String orderId = CommonHelpers.generateOrderId();
        
        // Get SSO token from user manager (no hardcoding)
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String ssoToken = user.ssoToken();
        
        // Create API instance and execute the call
        FetchMerchantUserInfo fetchMerchantUserInfoAPI = new FetchMerchantUserInfo(mid, orderId, ssoToken);
        Response response = fetchMerchantUserInfoAPI.execute();
        
        // Validate API response status using soft assertion
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getStatusCode()).as("API call should return status 200").isEqualTo(200);
        softly.assertAll();

        JsonPath jsonPath = response.jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        
        // AI-Generated: 2025-01-02 - Bug fix: Updated JSON path to access mcc and upiVerified from merchantInfoResp
        String mccValue = jsonPath.getString("body.merchantInfoResp.mcc");
        softAssertions.assertThat(mccValue).as("mcc should not be null").isNotNull();
        softAssertions.assertThat(mccValue).as("mcc should not be empty").isNotEmpty();
        
        String upiVerifiedValue = jsonPath.getString("body.merchantInfoResp.upiVerified");
        softAssertions.assertThat(upiVerifiedValue).as("upiVerified should not be null").isNotNull();
        softAssertions.assertThat(upiVerifiedValue).as("upiVerified should be false").isEqualTo("false");
        
        softAssertions.assertAll();
    }
}
