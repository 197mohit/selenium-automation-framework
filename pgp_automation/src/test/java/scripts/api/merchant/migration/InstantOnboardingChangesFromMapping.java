package scripts.api.merchant.migration;

import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InstantOnboardingChangesFromMapping extends merchantMigrationHelper {
    @Feature("PGP-25479")
    @Owner("Nirottam")
    @Test(description = "create a mid with  SETTLE_FREEZE=Y and check in merchant migration details that we are getting  SETTLE_FREEZE=Y  or not")
    public void CreateMerchantWithSETTLEFREEZE_01() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setStaticPreferences(STATICPREFERENCE.SETTLE_FREEZE.toString())
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        validatePrefStatus(mid,STATICPREFERENCE.SETTLE_FREEZE.toString());
    }
    @Feature("PGP-25479")
    @Owner("Nirottam")
    @Test(description = "create a mid with  SETTLE_FREEZE=Y and Acquring of DC paymode ")
    public void CreateMerchantWithSETTLEFREEZE_02() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setStaticPreferences(STATICPREFERENCE.SETTLE_FREEZE.toString())
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        validatePrefStatus(mid,STATICPREFERENCE.SETTLE_FREEZE.toString());
        Assert.assertEquals(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos[0].payMethod"),"DEBIT_CARD");

    }
    @Feature("PGP-25479")
    @Owner("Nirottam")
    @Test(description = "create a mid with  SETTLE_FREEZE=Y and Acquring of CC paymode ")
    public void CreateMerchantWithSETTLEFREEZE_03() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setStaticPreferences(STATICPREFERENCE.SETTLE_FREEZE.toString())
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        validatePrefStatus(mid,STATICPREFERENCE.SETTLE_FREEZE.toString());
        Assert.assertEquals(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos[0].payMethod"),"CREDIT_CARD");

    }
    @Feature("PGP-25479")
    @Owner("Nirottam")
    @Test(description = "create a mid without  SETTLE_FREEZE=Y ")
    public void CreateMerchantWithSETTLEFREEZE_04() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("MERCHANT-PREFERENCE-INFO.merchantId"),mid);
    }
    @Feature("PGP-25479")
    @Owner("Nirottam")
    @Test(description = "create a mid without  SETTLE_FREEZE=Y  and Acquring of Debit Card")
    public void CreateMerchantWithSETTLEFREEZE_05() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        Assert.assertEquals(withDrawJson.getString("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos[0].payMethod"),"DEBIT_CARD");
    }
}
