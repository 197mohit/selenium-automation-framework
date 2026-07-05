package scripts.api.merchant.migration;

import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.CreateMerchantEDC;
import com.paytm.api.merchant.migration.EditMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Feature("PGP-40118")
public class MerchantRiskRelatedInfoMigration extends merchantMigrationHelper {
    @Feature("PGP-30092")
    @Owner("Nirottam")
    @Test(description = "Migrating Gstin Parameter and Verifying in attributeInfo")
    public void MigratingGstin_01() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setKYCBankName("ICICI")
                .setKYCBankAccountHolderName("NAMAN KHETERPAL")
                .setKYCBankAccounNo("1031019866920")
                .setKYCBusinessPanNo("AKRPD5926Z")
                .setKYCBusinessGstin("09AKRPD5926Z1Z2")
                .setKYCBusinessIfscNo("ICIC0001031")
                .setKYCAuthorizedSignatoryName("Test Name123")
                .setKYCAuthorizedSignatoryIdProofNo("AKRPD5926Z")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.gstin")).toString().contains("09AKRPD5926Z1Z2");
        softly.assertAll();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
                "grep \"" + alipayId+ "\" | grep \"Set Attribute Payload for merchantId\" | grep \"AttributeSetInfos\" ";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\"gstIn\":\"09AKRPD5926Z1Z2\"");


    }
    @Feature("PGP-30092")
    @Owner("Nirottam")
    @Test(description = "Migrating INTERNATIONAL_CARD_SUPPORT_EDC and verifying in attributeInfo in mapping service logs")
    public void MigratingINTERNATIONAL_CARD_SUPPORT_EDC_02() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setStaticPreferences("INTERNATIONAL_CARD_SUPPORT_EDC")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "DCC_ENABLED","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        validatePrefStatus(mid,"INTERNATIONAL_CARD_SUPPORT_EDC");
        validatePrefStatus(mid,"DCC_ENABLED");
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
                "grep \"" + alipayId+ "\" | grep \"Set Attribute Payload for merchantId\" | grep \"AttributeSetInfos\" ";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println("logs are---"+mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("\"internationalCardSupportEdc\":true");
        Assertions.assertThat(mappingServiceLogs).contains("\"dccEnabled\"");

    }
    @Feature("PGP-30092")
    @Owner("Nirottam")
    @Test(description = "Migrating INTERNATIONAL_CARD_SUPPORT_EDC,onboardingLatitude,onboardingLongitude and gstin. verifying in attributeInfo in mapping service logs")
    public void verifyAttributeInfo() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setStaticPreferences("INTERNATIONAL_CARD_SUPPORT_EDC")
                .setKYCBankName("ICICI")
                .setKYCBankAccountHolderName("NAMAN KHETERPAL")
                .setKYCBankAccounNo("1031019866920")
                .setKYCBusinessPanNo("AKRPD5926Z")
                .setKYCBusinessGstin("09AKRPD5926Z1Z2")
                .setKYCBusinessIfscNo("ICIC0001031")
                .setKYCAuthorizedSignatoryName("Test Name123")
                .setKYCAuthorizedSignatoryIdProofNo("AKRPD5926Z")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq1 =
                new MerchantAddPreferenceInfoReq.Builder(mid, "DCC_ENABLED","ACTIVE","Y")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo1 = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq1);
        merchantAddPreferenceInfo1.execute();
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        validatePrefStatus(mid,"INTERNATIONAL_CARD_SUPPORT_EDC");
        validatePrefStatus(mid,"DCC_ENABLED");
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
                "grep \"" + alipayId+ "\" | grep \"Set Attribute Payload for merchantId\" | grep \"AttributeSetInfos\" ";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\"internationalCardSupportEdc\":true");
        Assertions.assertThat(mappingServiceLogs).contains("dccEnabled");
        Assertions.assertThat(mappingServiceLogs).contains("\"gstIn\":\"09AKRPD5926Z1Z2\"");
        Assertions.assertThat(mappingServiceLogs).contains("\"onboardingLatitude\":\"28.09\"");
        Assertions.assertThat(mappingServiceLogs).contains("\"onboardingLongitude\":\"27.19\"");
    }
}
