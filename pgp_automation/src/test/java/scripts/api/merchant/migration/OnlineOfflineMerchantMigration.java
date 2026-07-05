package scripts.api.merchant.migration;

import com.paytm.ServerConfigProvider;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.api.MappingService.ClearCache.RemoveMerchantAttributePreferenceInfoCache;
import com.paytm.api.MappingService.GetMerchantAttributePreferenceInfo;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.CreateMerchantEDC;
import com.paytm.api.merchant.migration.EditMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Feature("PGP-30644")
public class OnlineOfflineMerchantMigration extends merchantMigrationHelper {
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType should be online for new mid migrated as online")
    public void SolutionTypeForOnlinemerchant() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setSolutionType("ONLINE")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, "paytm");
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        String merchantSolutionType=res1.getString("merchantSolutionType");
        Assert.assertEquals(merchantSolutionType,"ONLINE");

    }
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType  should be offline for new mid migrated as offline")
    public void SolutionTypeForOfflinemerchant() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setSolutionType("OFFLINE")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        System.out.println("mid is...."+mid);
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, "paytm");
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        String merchantSolutionType=res1.getString("merchantSolutionType");
        Assert.assertEquals(merchantSolutionType,"OFFLINE");

    }
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType  should be null for new mid migrated as corporate")
    public void SolutionTypeForCorporatemerchant() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setSolutionType("CORPORATE")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, "paytm");
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        String merchantSolutionType=res1.getString("merchantSolutionType");
        Assert.assertEquals(merchantSolutionType,null);

    }
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType should be null for  for existing merchant")
    public void SolutionTypeForExistingmerchant() throws Exception {
        mid = "goldii83121310353320";
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, "paytm");
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        Assert.assertNull(res1.getString("merchantSolutionType"));

    }
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType should be null  for alipay combination for corporate")
    public void SolutionTypeForAlipayId() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setSolutionType("CORPORATE")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        System.out.println("alipayId is----"+alipayId);
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, alipayId);
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        String merchantSolutionType=res1.getString("merchantSolutionType");
        Assert.assertEquals(merchantSolutionType,null);

    }
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType should be online  for alipay combination for online")
    public void SolutionTypeForOnlineAlipayId() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setSolutionType("ONLINE")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, alipayId);
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        String merchantSolutionType=res1.getString("merchantSolutionType");
        Assert.assertEquals(merchantSolutionType,"ONLINE");

    }
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType should be offline  for alipay combination for offline")
    public void SolutionTypeForOfflineAlipayId() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setSolutionType("OFFLINE")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, alipayId);
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        String merchantSolutionType=res1.getString("merchantSolutionType");
        Assert.assertEquals(merchantSolutionType,"OFFLINE");

    }
    @Feature("PGP-40118")
    @Owner("Nirottam")
    @Test(description = "Verify value of merchantSolutionType should be null for  for existing merchant for alipay combination")
    public void SolutionTypeForOildAlipayId() throws Exception {
        mid = "goldii83121310353320";
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(mid, alipayId);
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath();
        String merchantSolutionType=res1.getString("merchantSolutionType");
        Assert.assertEquals(merchantSolutionType,null);

    }


}
