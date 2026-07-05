package scripts.api.merchant.migration;

import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.EditMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


public class SolutionTypeForBlockedMerchant extends merchantMigrationHelper {
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= ONLINE for Blocked Merchant in MappingFacade Logs")
    public void BlockedMerchant_01() throws Exception {
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
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);

        String alipayId =getAlipayId(mid);
        validateAlipayId(alipayId);
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":\\\"ONLINE\\\"");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= OFFLINE for Blocked Merchant in MappingFacade Logs")
    public void BlockedMerchant_02() throws Exception {
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
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);

        String alipayId =getAlipayId(mid);
        validateAlipayId(alipayId);
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":\\\"OFFLINE\\");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= CORPORATE for Blocked Merchant in MappingFacade Logs")
    public void BlockedMerchant_03() throws Exception {
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
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);
        String alipayId =getAlipayId(mid);
        validateAlipayId(alipayId);

        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":null");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= ONLINE for Blocked Merchant in MappingFacade Logs WITH EDC REQUEST TYPE")
    public void BlockedMerchant_04() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMerchantIndustryType("SMALL")
                .setSolutionType("ONLINE")
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);

        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":\\\"ONLINE\\");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= OFFLINE for Blocked Merchant in MappingFacade Logs WITH EDC REQUEST TYPE")
    public void BlockedMerchant_05() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMerchantIndustryType("SMALL")
                .setSolutionType("OFFLINE")
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);

        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":\\\"OFFLINE\\");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= CORPORATE for Blocked Merchant in MappingFacade Logs With EDC Request Type")
    public void BlockedMerchant_06() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMerchantIndustryType("SMALL")
                .setSolutionType("CORPORATE")
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);

        String alipayId =getAlipayId(mid);
        validateAlipayId(alipayId);

        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":null");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= ONLINE for Blocked Merchant in MappingFacade Logs WITH Paymodes")
    public void BlockedMerchant_07() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMerchantIndustryType("SMALL")
                .setSolutionType("ONLINE")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);


        String alipayId =getAlipayId(mid);
        validateAlipayId(alipayId);
        ArrayList<String> ExpectedPaymodes=new ArrayList<>();
        ExpectedPaymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(ExpectedPaymodes);
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":\\\"ONLINE\\");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= OFFLINE for Blocked Merchant in MappingFacade Logs WITH Paymodes")
    public void BlockedMerchant_08() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMerchantIndustryType("SMALL")
                .setSolutionType("OFFLINE")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);

        String alipayId =getAlipayId(mid);
        validateAlipayId(alipayId);
        ArrayList<String> ExpectedPaymodes=new ArrayList<>();
        ExpectedPaymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(ExpectedPaymodes);
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":\\\"OFFLINE\\");
    }
    @Feature("PGP-34850")
    @Owner("Nirottam Singh")
    @Test(description = "Verifying merchantSolutionType= CORPORATE for Blocked Merchant in MappingFacade Logs WITH Paymodes")
    public void BlockedMerchant_09() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMerchantIndustryType("SMALL")
                .setSolutionType("CORPORATE")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        blockMerchant(mid);
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .execute().jsonPath();
        validateResponse(resp);

        String alipayId =getAlipayId(mid);
        validateAlipayId(alipayId);
        ArrayList<String> ExpectedPaymodes=new ArrayList<>();
        ExpectedPaymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(ExpectedPaymodes);
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"SET_ATTRIBUTE\" | grep \"attributeInfos\" | grep \"attributeKey\" | grep \"REQUEST\" | grep \"merchantSolutionType\"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        Assertions.assertThat(mappingServiceLogs).contains("\\\"merchantSolutionType\\\":null");
    }

}
