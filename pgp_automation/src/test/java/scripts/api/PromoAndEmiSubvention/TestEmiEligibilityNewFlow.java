package scripts.api.PromoAndEmiSubvention;

import com.paytm.api.nativeAPI.CheckEMIEligibility;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.checkEMIEligibility.CheckEMIEligibilityRequest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.paytm.appconstants.Constants.Owner.SATWIK_SHARMA;

/**
 * This is the new class for Emi Eligibility Flow as per the new flow on PROD
 * all the elibility calls going to affordability
 * as theia.emi.dc.eligibility.migrate.to.affordability : ALL on PROD
 * <p>
 * This new flow only supports DC so we have no explicit case of paymode
 */
public class TestEmiEligibilityNewFlow extends PGPBaseTest {


    @Test(description = "Check EMI Eligibility with Valid SSO Token")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-52610")
    public void checkEMIEligiBilityWithValidSSOToken() throws Exception {

        String ssoToken = AuthHelpers.getSSOToken("5050442210", "paytm@123");
        
        String mid = Constants.MerchantType.EMI_DISCOVERY.getId();

        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC_ONLY.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
    }


    @Test(description = "Check EMI Eligibility with InValid SSO Token")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-52610")
    public void checkEMIEligiBilityWithInValidSSOToken() throws Exception {

        String ssoToken = AuthHelpers.getSSOToken("5050442210", "paytm@123");

        ssoToken = ssoToken + "sds";
        
                String mid = Constants.MerchantType.EMI_DISCOVERY.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("HTTP Response Code other than 200 received in Fetching User Details");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("3001");
    }


    @Test(description = "Check EMI Eligibility with channel code not passed")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-52610")
    public void checkEMIEligiBilityWithChannelCodeNotPassed() throws Exception {

        String ssoToken = AuthHelpers.getSSOToken("5050442210", "paytm@123");
        
                String mid = Constants.MerchantType.EMI_DISCOVERY.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid,"", emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].channelCode").toString()).as("channelCode mismatch").isEqualToIgnoringCase(Constants.Bank.HDFC_ONLY.toString());
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].emiType").toString()).as("EmiType mismatch").isEqualToIgnoringCase("DEBIT_CARD");
        Assertions.assertThat(response.jsonPath().get("body.emiEligibility[0].eligible").toString()).as("Eligibility mismatch").isEqualToIgnoringCase("true");
    }


    @Test(description = "Check EMI Eligibility with mid  not passed")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-52610")
    public void checkEMIEligiBilityWhenMidIsNotPassed() throws Exception {

        String ssoToken = AuthHelpers.getSSOToken("5050442210", "paytm@123");

        String mid = Constants.MerchantType.EMI_DISCOVERY.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, "", Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1001");
    }


    @Test(description = "Check EMI Eligibility with SSO  not passed")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-52610")
    public void checkEMIEligiBilityWhenSSOIsNotPassed() throws Exception {

        String ssoToken = AuthHelpers.getSSOToken("5050442210", "paytm@123");
        
                String mid = Constants.MerchantType.EMI_DISCOVERY.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("DEBIT_CARD");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest("", mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1001");

    }


    @Test(description = "Check EMI Eligibility with Emi Type  not passed")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-52610")
    public void checkEMIEligiBilityWhenEmiTypeIsNotPassed() throws Exception {

        String ssoToken = AuthHelpers.getSSOToken("5050442210", "paytm@123");

        String mid = Constants.MerchantType.EMI_DISCOVERY.getId();
        ArrayList<String> emiTypes = new ArrayList<>();
        emiTypes.add("");
        CheckEMIEligibilityRequest checkEMIEligibilityRequest = new CheckEMIEligibilityRequest(ssoToken, mid, Constants.Bank.HDFC_ONLY.toString(), emiTypes);
        Response response = new CheckEMIEligibility(checkEMIEligibilityRequest, mid).execute();

        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("STATUS mismatch").isEqualToIgnoringCase("F");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultMsg").toString()).as("Result Message mismatch").isEqualToIgnoringCase("Request parameters are not valid");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("Result Code mismatch").isEqualToIgnoringCase("1001");

    }


}
