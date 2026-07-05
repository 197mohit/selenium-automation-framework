package scripts.api.merchant.migration;


import com.paytm.ServerConfigProvider;
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
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import java.util.ArrayList;
import java.util.HashMap;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
@Feature("PGP-40118")
public class BWMerchantMigration extends merchantMigrationHelper{
    @Feature("PGP-29961")
    @Owner("Nirottam Singh")
    @Test(description = "Creating BW merchant and Checking Migration ")
    public void CreatingBW_Merchant() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());

    }
    @Feature("PGP-29961")
    @Owner("Nirottam Singh")
    @Test(description = "Creating BW merchant With paymodes and Checking Migration")
    public void CreatingBW_MerchantWithPaymodes() throws InterruptedException {
        ArrayList<String>ExpectedPaymodes=new ArrayList<>();
        ExpectedPaymodes.add("CREDIT_CARD");
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        validatePaymodesInMigrationDetail(ExpectedPaymodes);

    }
    @Feature("PGP-29961")
    @Owner("Nirottam Singh")
    @Test(description = "Creating BW merchant and Checking AssetInfo Parameters in QUERY_ASSETS Call In Mapping Service Facade Logs")
    public void CreatingBW_MerchantANDCheckingQUERYASSETSAPI() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);
       String alipayId = getAlipayId(mid);
       String clientId=getReqIdForQuerAssetLogs(alipayId);

       Thread.sleep(200000);
       String grepcmd2 = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
              "grep \"" + clientId+ "\" | grep \"QUERY_ASSETS\" | grep \"RESPONSE\" | grep \"assetType\"";

        String mappingServiceLogs2 = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd2);

        Assertions.assertThat(mappingServiceLogs2).contains("\"assetType\":\"SAVINGS_ACCT\"");
        Assertions.assertThat(mappingServiceLogs2).contains("\"firstName\":\"TOUCH WOOD LIMITED\"");
        Assertions.assertThat(mappingServiceLogs2).contains("cardIndexNo");

    }
    @Feature("PGP-29961")
    @Owner("Nirottam Singh")
    @Test(description = "Creating BW merchant and Checking AssetInfo Parameters in QUERY_ASSETS Call In Mapping Service Facade Logs With EDC Request Type")
    public void CreatingBW_MerchantANDCheckingQUERYASSETSAPIWithPaymodes() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        ArrayList<String>ExpectedPaymodes=new ArrayList<>();
        ExpectedPaymodes.add("CREDIT_CARD");
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);
        String alipayId = getAlipayId(mid);
        String clientId=getReqIdForQuerAssetLogs(alipayId);

        Thread.sleep(200000);
        String grepcmd2 = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + clientId+ "\" | grep \"QUERY_ASSETS\" | grep \"RESPONSE\" | grep \"assetType\"";

        String mappingServiceLogs2 = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd2);

        Assertions.assertThat(mappingServiceLogs2).contains("\"assetType\":\"SAVINGS_ACCT\"");
        Assertions.assertThat(mappingServiceLogs2).contains("\"firstName\":\"TOUCH WOOD LIMITED\"");
        Assertions.assertThat(mappingServiceLogs2).contains("cardIndexNo");


    }
}
