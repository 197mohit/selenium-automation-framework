package scripts.api.merchant.migration;

import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.api.MappingService.ClearCache.RemoveMerchantAttributePreferenceInfoCache;
import com.paytm.api.MappingService.GetMerchantAttributePreferenceInfo;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.CreateMerchantEDC;
import com.paytm.api.merchant.migration.EditMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import java.util.ArrayList;
import java.util.HashMap;

public class preAuthEDCMigration extends merchantMigrationHelper {

    @Feature("PGP-30274")
    @Owner("Nirottam")
    @Test(description = "Checking Migration of preAuthEDC ProductCode 51051000100000000054")
    public void preAuthEDC_01() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMerchantIndustryType("SMALL")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        enablePreAuthEDCOnMerchant(mid);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000054");
        validateProductCodes(productsCodes,pcode);

    }
    @Feature("PGP-30274")
    @Owner("Nirottam")
    @Test(description = "Checking Migration of 51051000100000000054 product code with BW Merchant")
    public void preAuthEDC_02() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMerchantIndustryType("SMALL")
                .setBWEnabled("TRUE")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"BW_ENABLED");
        enablePreAuthEDCOnMerchant(mid);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000054");
        validateProductCodes(productsCodes,pcode);

    }


}
