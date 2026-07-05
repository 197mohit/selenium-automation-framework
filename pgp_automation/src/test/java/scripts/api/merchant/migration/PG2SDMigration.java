package scripts.api.merchant.migration;

import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.EditMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class PG2SDMigration extends merchantMigrationHelper {

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode")
    public void CreatePG2Merchant_01() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantType("UNLIMITED_SD")
                .setMerchantPPILimit("4")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode ")
    public void CreatePG2Merchant_02() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantType("UNLIMITED_SD")
                .setMerchantPPILimit("4")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With DC Paymode ")
    public void CreatePG2Merchant_03() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantType("UNLIMITED_SD")
                .setMerchantPPILimit("4")
                .setTxnType("Payments",0)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("DEBIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With UPI Paymode ")
    public void CreatePG2Merchant_04() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantType("UNLIMITED_SD")
                .setMerchantPPILimit("4")
                .setTxnType("Payments",0)
                .setPaymode("UPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("UPI");
        validatePaymodesInMigrationDetail(paymodes);

    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With NB Paymode ")
    public void CreatePG2Merchant_05() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantType("UNLIMITED_SD")
                .setMerchantPPILimit("4")
                .setTxnType("Payments",0)
                .setPaymode("NB","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("NET_BANKING");
        validatePaymodesInMigrationDetail(paymodes);

    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With PPI Paymode ")
    public void CreatePG2Merchant_06() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantType("UNLIMITED_SD")
                .setMerchantPPILimit("4")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }

    //************ ONLINE SETTLEMENT MERCHANT ***********//
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant ")
    public void CreatePG2Merchant_07() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantType("UNLIMITED_SD")
                .setMerchantPPILimit("4")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode ")
    public void CreatePG2Merchant_08() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With DC Paymode ")
    public void CreatePG2Merchant_09() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("DEBIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With UPI Paymode ")
    public void CreatePG2Merchant_010() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("UPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("UPI");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With NB Paymode ")
    public void CreatePG2Merchant_011() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("NB","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With PPI Paymode ")
    public void CreatePG2Merchant_012() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }

//***********BUSINESS WALLET MERCHANT ******************//
@Feature("42496")
@Owner("Nirottam")
@Test(description = "Creating and Checking Migration PG2 Merchant ")
public void CreatePG2Merchant_013() throws Exception {
    ArrayList<String> staticPref=new ArrayList<>();
    staticPref.add("PG2_DATA_MIGRATION");
    staticPref.add("GST_EXEMPTED");
    staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
    CreateMerchant createMerchant = new CreateMerchant("true");
    JsonPath response = createMerchant.setRequestID(requestID)
            .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
            .setMobileNumber(phoneNumber)
            .setPhoneNumber(phoneNumber)
            .setCustId(AuthHelpers.getCustomerID(phoneNumber))
            .setMultipleStaticpref(staticPref)
            .setMerchantIndustryType("SMALL")
            .setCustomName("qa8Aut")
            .setMerchantPPILimit("4")
            .setMerchantType("UNLIMITED_SD")
            .setBWEnabled("TRUE")
            .setTxnType("Payments",1)
            .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
            .execute().jsonPath();
    validateResponse(response);
    mid = getMidViaBOSSAPI(phoneNumber);
    validatePrefStatus(mid,"PG2_DATA_MIGRATION");
    validatePrefStatus(mid,"GST_EXEMPTED");
    validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
}

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode ")
    public void CreatePG2Merchant_014() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With DC Paymode ")
    public void CreatePG2Merchant_015() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("DEBIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With UPI Paymode ")
    public void CreatePG2Merchant_016() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("UPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("UPI");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With NB Paymode ")
    public void CreatePG2Merchant_017() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("NB","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With PPI Paymode ")
    public void CreatePG2Merchant_018() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }


    //******************ADD & MONEY CASES ****************//
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE ")
    public void CreatePG2Merchant_019() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With CC Paymode")
    public void CreatePG2Merchant_020() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }

    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With DC Paymode")
    public void CreatePG2Merchant_021() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("DEBIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With UPI Paymode")
    public void CreatePG2Merchant_022() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("UPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("UPI");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With CC Paymode")
    public void CreatePG2Merchant_023() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("NB","simple",2.00,"FALSE",0,"HDFC",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With CC Paymode")
    public void CreatePG2Merchant_024() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("PAYTM_DIGITAL_CREDIT","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        validatePaymodesInMigrationDetail(paymodes);
    }


    //************POSTPAID CASES ********//
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With POSTPAID Paymode")
    public void CreatePG2Merchant_025() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("PAYTM_DIGITAL_CREDIT","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        validatePaymodesInMigrationDetail(paymodes);
    }

    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT REQUEST TYPE With POSTPAID Paymode")
    public void CreatePG2Merchant_026() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setTxnType("Payments",0)
                .setPaymode("PAYTM_DIGITAL_CREDIT","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        validatePaymodesInMigrationDetail(paymodes);
    }

    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For BWENABLED MERCHANT With POSTPAID Paymode")
    public void CreatePG2Merchant_027() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PAYTM_DIGITAL_CREDIT","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ONLINE SETTELEMENT MERCHANT With POSTPAID Paymode")
    public void CreatePG2Merchant_028() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PAYTM_DIGITAL_CREDIT","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        validatePaymodesInMigrationDetail(paymodes);
    }


    //***************PPI CASES *****************//

    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant  With PPI Paymode ")
    public void CreatePG2Merchant_029() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");

    }


    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For BW  With PPI Paymode ")
    public void CreatePG2Merchant_030() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }

    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ONLINE SETTELEMENT  With PPI Paymode ")
    public void CreatePG2Merchant_031() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD&PAY  With PPI Paymode ")
    public void CreatePG2Merchant_032() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }



    //************TRANSACTION WISE SETTELEMENT CASES********//
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD&PAY,  Transaction Wise Settelement With PPI Paymode ")
    public void CreatePG2Merchant_033() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setPreference("Add & Pay")
                .setTransactionWiseSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT, Transaction Wise Settelement With DC Paymode ")
    public void CreatePG2Merchant_034() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setTransactionWiseSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("DEBIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT,  Transaction Wise Settelement With CC Paymode ")
    public void CreatePG2Merchant_035() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setTransactionWiseSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT,  Transaction Wise Settelement With POSTPAID Paymode ")
    public void CreatePG2Merchant_036() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setTransactionWiseSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PAYTM_DIGITAL_CREDIT","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        validatePaymodesInMigrationDetail(paymodes);
    }

    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT,  Transaction Wise Settelement With UPI Paymode ")
    public void CreatePG2Merchant_037() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setTransactionWiseSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("UPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
        ArrayList<String> paymodes=new ArrayList<>();
        paymodes.add("UPI");
        validatePaymodesInMigrationDetail(paymodes);
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT,  Transaction Wise Settelement With UPI Paymode ")
    public void CreatePG2Merchant_038() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,SELF_DECLARED_MERCHANT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8Aut")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setTransactionWiseSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("NB","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
        validatePrefStatus(mid,"FULL_PG2_TRAFFIC_ENABLED");
    }
    @Feature("PGP-29552")
    @Owner("Sourav")
    @Test(description = "Creating a Default and EDC merchant with PPI and GV paymodes adding commission and validating gv for product code 51051000100000000047")
    public void CreateEDCMerchantTest() throws Exception {
        String mid="ALI00794701502328614";
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("FULL_PG2_TRAFFIC_ENABLED");
        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(mid)
                .setRequestID(requestID+"1")
                .setMerchantPPILimit("4")
                .setMerchantType("UNLIMITED_SD")
                .setMultipleStaticpref(staticPref)
                .execute().jsonPath();

        validateResponse(resp);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000047");
        softly.assertAll();
    }
}
