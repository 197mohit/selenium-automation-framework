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

import com.paytm.api.merchant.migration.merchantMigrationHelper;
@Feature("PGP-40118")
public class paymentsBusinessWalletReconciliationView extends merchantMigrationHelper {
    @Feature("PGP-30274")
    @Owner("Nirottam")
    @Test(description = "Create a merchant with BW_RECON_FLAG=True, Business Wallet-Y then checking merchantType=ENTERPRISE_MERCHANT in fund move api in mapping Service facade logs")
    public void paymentsBusinessWalletReconciliationView_01() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setBWEnabled("TRUE")
                .setBWManualType("BUSINESSWALLET_WITHDRAW")
                .setStaticPreferences("BW_RECON_FLAG")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String aliPayId=withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + aliPayId+ "\" | grep \"FUND_MOVE_CONFIG\" | grep \"REQUEST\" ";
        String mappingServiceFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println("mapping facade logs are----"+mappingServiceFacadeLogs);
        Assertions.assertThat(mappingServiceFacadeLogs).contains("\"merchantType\":\"ENTERPRISE_MERCHANT\"");
    }
    @Feature("PGP-30274")
    @Owner("Nirottam")
    @Test(description = "Create a merchant with BW_RECON_FLAG=False, Business Wallet-Y then checking merchantType is not present in fund move api in mapping Service facade logs")
    public void paymentsBusinessWalletReconciliationView_02() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setBWEnabled("TRUE")
                .setBWManualType("BUSINESSWALLET_WITHDRAW")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String aliPayId=withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + aliPayId+ "\" | grep \"FUND_MOVE_CONFIG\" | grep \"REQUEST\" ";
        String mappingServiceFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println("mapping facade logs are----"+mappingServiceFacadeLogs);
        Assertions.assertThat(mappingServiceFacadeLogs).doesNotContain("merchantType");
    }
    @Feature("PGP-30274")
    @Owner("Nirottam")
    @Test(description = "Create a merchant with BW_RECON_FLAG=True, Business Wallet-Y and CHARGEBACK_HOLD_ENABLED=Y  then checking merchantType is not present in fund move api in mapping Service facade logs")
    public void paymentsBusinessWalletReconciliationView_03() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setBWEnabled("TRUE")
                .setBWManualType("BUSINESSWALLET_WITHDRAW")
                .setStaticPreferences("BW_RECON_FLAG")
                .setStaticPreferences("CHARGEBACK_HOLD_ENABLED")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String aliPayId=withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + aliPayId+ "\" | grep \"FUND_MOVE_CONFIG\" | grep \"REQUEST\" ";
        String mappingServiceFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println("mapping facade logs are----"+mappingServiceFacadeLogs);
        Assertions.assertThat(mappingServiceFacadeLogs).doesNotContain("merchantType");
    }
    @Feature("PGP-30274")
    @Owner("Nirottam")
    @Test(description = "Create a merchant with BW_RECON_FLAG=False, Business Wallet-Y and CHARGEBACK_HOLD_ENABLED=Y  then checking merchantType is not present in fund move api in mapping Service facade logs")
    public void paymentsBusinessWalletReconciliationView_04() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setBWEnabled("TRUE")
                .setBWManualType("BUSINESSWALLET_WITHDRAW")
                .setStaticPreferences("CHARGEBACK_HOLD_ENABLED")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String aliPayId=withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + aliPayId+ "\" | grep \"FUND_MOVE_CONFIG\" | grep \"REQUEST\" ";
        String mappingServiceFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println("mapping facade logs are----"+mappingServiceFacadeLogs);
        Assertions.assertThat(mappingServiceFacadeLogs).doesNotContain("merchantType");
    }
    @Feature("PGP-30274")
    @Owner("Nirottam")
    @Test(description = "Create a merchant with BW_RECON_FLAG=True, Business Wallet-N and CHARGEBACK_HOLD_ENABLED=N  then checking  fund move api is not called in mapping Service facade logs")
    public void paymentsBusinessWalletReconciliationView_05() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setBWEnabled("FALSE")
                .setStaticPreferences("BW_RECON_FLAG")
                .execute().jsonPath();
        validateErrorResponse(response);



    }
}
