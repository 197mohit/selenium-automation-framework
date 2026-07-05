package scripts.api.mappingService;

import com.paytm.api.MappingService.MerchantProfile;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class MerchantProfilePostpaidOnAddNPay extends PGPBaseTest {

    public boolean validate_MerchantPreference(String mid,String prefName,String prefValue) {
        return PGPHelpers.validate_MerchantPreference(mid, prefName,prefValue);
    }

    @Owner("Mayuri")
    @Feature("PGP-33334")
    @Test(description = "default value true for postPaidOnAddNPay")
    public void validatePostPaidOnAddNPayDefault() {
        Constants.MerchantType merchant = Constants.MerchantType.AddMoney;
        MerchantProfile merchantProfilePaytm = new MerchantProfile(merchant.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfilePaytm.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("postPaidOnAddNPay")).as("postPaidOnAddNPay should be true by default").isEqualTo("true");
    }

    @Owner("Mayuri")
    @Feature("PGP-33334")
    @Test(description = "validate postPaidOnAddNPay when mcc value matched with property file")
    public void validatePostPaidOnAddNPayWhenMCCCodeMatchedWithPropertyFile() {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        MerchantProfile merchantProfile = new MerchantProfile(merchant.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("postPaidOnAddNPay")).as("postPaidOnAddNPay should be false when mcc code matched with proprty file").isEqualTo("false");
    }

    @Owner("Mayuri")
    @Feature("PGP-33334")
    @Test(description = "validate postPaidOnAddNPay when ALLOW_POSTPAID_ON_ADDNPAY pref enabled,mcc code doesn't lie in blacklisted property list")
    public void validatePostPaidOnAddNPayWhenALLOW_POSTPAID_ON_ADDNPAYPrefEnabled() {
        Constants.MerchantType merchant = Constants.MerchantType.NATIVE_ADDNPAY;
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.NATIVE_ADDNPAY.getId(),"ALLOW_POSTPAID_ON_ADDNPAY", "Y");
        }
        MerchantProfile merchantProfile = new MerchantProfile(merchant.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("postPaidOnAddNPay")).as("postPaidOnAddNPay should be true when ALLOW_POSTPAID_ON_ADDNPAY enabled on mid").isEqualTo("true");
    }

    @Owner("Mayuri")
    @Feature("PGP-33334")
    @Test(description = "validate postPaidOnAddNPay when ALLOW_POSTPAID_ON_ADDNPAY,BLOCK_POSTPAID_ON_ADDNPAY pref N,mcc code doesn't lie in blacklisted property list")
    public void validatePostPaidOnAddNPayWhenALLOW_POSTPAID_ON_ADDNPAYAndBLOCK_POSTPAID_ON_ADDNPAYPrefDisabled() {
        Constants.MerchantType merchant = Constants.MerchantType.POSTPAID_BLOCK_ALLOW_PREF_N;
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_BLOCK_ALLOW_PREF_N.getId(),"ALLOW_POSTPAID_ON_ADDNPAY", "N");
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_BLOCK_ALLOW_PREF_N.getId(),"BLOCK_POSTPAID_ON_ADDNPAY", "N");
        }
        MerchantProfile merchantProfile = new MerchantProfile(merchant.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("postPaidOnAddNPay")).as("postPaidOnAddNPay should be true when BOTH pref disabled on mid").isEqualTo("true");
    }

    @Owner("Mayuri")
    @Feature("PGP-33334")
    @Test(description = "validate postPaidOnAddNPay when BLOCK_POSTPAID_ON_ADDNPAY pref N,mcc code doesn't lie in blacklisted property list")
    public void validatePostPaidOnAddNPayWhenBLOCK_POSTPAID_ON_ADDNPAYPrefEnabled() {
        Constants.MerchantType merchant = Constants.MerchantType.POSTPAID_BLOCK_PREF_Y;
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_BLOCK_PREF_Y.getId(),"ALLOW_POSTPAID_ON_ADDNPAY", "N");
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_BLOCK_PREF_Y.getId(),"BLOCK_POSTPAID_ON_ADDNPAY", "Y");
        }
        MerchantProfile merchantProfile = new MerchantProfile(merchant.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("postPaidOnAddNPay")).as("postPaidOnAddNPay should be false when BLOCK_POSTPAID_ON_ADDNPAY pref enabled on mid").isEqualTo("false");
    }

    @Owner("Mayuri")
    @Feature("PGP-33334")
    @Test(description = "validate postPaidOnAddNPay when ALLOW_POSTPAID_ON_ADDNPAY,BLOCK_POSTPAID_ON_ADDNPAY pref Y,mcc code doesn't lie in blacklisted property list")
    public void validatePostPaidOnAddNPayWhenBLOCK_POSTPAID_ON_ADDNPAYAndALLOW_POSTPAID_ON_ADDNPAYPrefEnabled() {
        Constants.MerchantType merchant = Constants.MerchantType.POSTPAID_BLOCK_ALLOW_PREF_Y;
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_BLOCK_ALLOW_PREF_Y.getId(),"ALLOW_POSTPAID_ON_ADDNPAY", "Y");
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_BLOCK_ALLOW_PREF_Y.getId(),"BLOCK_POSTPAID_ON_ADDNPAY", "Y");
        }
        MerchantProfile merchantProfile = new MerchantProfile(merchant.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("postPaidOnAddNPay")).as("postPaidOnAddNPay should be false when both pref enabled on mid").isEqualTo("false");
    }

    @Owner("Mayuri")
    @Feature("PGP-33334")
    @Test(description = "validate postPaidOnAddNPay when ALLOW_POSTPAID_ON_ADDNPAY pref Y,mcc code lies in blacklisted property list")
    public void validatePostPaidOnAddNPayWhenALLOW_POSTPAID_ON_ADDNPAYPrefEnabledMCCBlacklisted() {
        Constants.MerchantType merchant = Constants.MerchantType.POSTPAID_ALLOW_ALLOW_PREF_Y;
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_ALLOW_ALLOW_PREF_Y.getId(),"ALLOW_POSTPAID_ON_ADDNPAY", "Y");
            validate_MerchantPreference(Constants.MerchantType.POSTPAID_ALLOW_ALLOW_PREF_Y.getId(),"BLOCK_POSTPAID_ON_ADDNPAY", "N");
        }
        MerchantProfile merchantProfile = new MerchantProfile(merchant.getId(),"paytm");
        JsonPath merchantProfileResponse = merchantProfile.execute().jsonPath();
        Assertions.assertThat(merchantProfileResponse.getString("postPaidOnAddNPay")).as("postPaidOnAddNPay should be true when ALLOW_POSTPAID_ON_ADDNPAY pref enabled on mid and mcc lies in blacklisted list").isEqualTo("true");
    }
}
