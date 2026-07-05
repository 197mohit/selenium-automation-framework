package scripts.api.theia.fetchPayOptions;

import com.paytm.api.nativeAPI.FetchPaymentOptionV4;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.AJEESH;
import static com.paytm.appconstants.Constants.Owner.PAYAL;

public class FetchPayOptionV4Test extends PGPBaseTest {

    @Owner(PAYAL)
    @Feature("PGP-35329")
    @Test(description = "To verify Parameters of head and body and Verify success response for Diners Privilege variant")
    public void verifyingDinersPrivilegeHDFCBin() throws Exception {
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";
        Constants.MerchantType mid = Constants.MerchantType.UPI;
        FetchPaymentOptionV4 fetchPaymentOption = new FetchPaymentOptionV4(mid.getId(),referenceId);
        JsonPath withDrawJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getBoolean("body.merchantPayOption.upiProfile.upiOnboarding")).isFalse();
        softly.assertThat(withDrawJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.bankAccounts[1].mpinSet")).isEqualToIgnoringCase("Y");
        softly.assertAll();
    }
}
