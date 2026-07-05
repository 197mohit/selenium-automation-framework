package scripts.api.merchant.migration;

import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class PG2Migration extends merchantMigrationHelper {

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant ")
    public void CreatePG2Merchant_01() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode ")
    public void CreatePG2Merchant_02() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With DC Paymode ")
    public void CreatePG2Merchant_03() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setTxnType("Payments",0)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With UPI Paymode ")
    public void CreatePG2Merchant_04() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setTxnType("Payments",0)
                .setPaymode("UPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With NB Paymode ")
    public void CreatePG2Merchant_05() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setTxnType("Payments",0)
                .setPaymode("NET_BANKING","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With PPI Paymode ")
    public void CreatePG2Merchant_06() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }

    //************ ONLINE SETTLEMENT MERCHANT ***********//
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant ")
    public void CreatePG2Merchant_07() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode ")
    public void CreatePG2Merchant_08() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With DC Paymode ")
    public void CreatePG2Merchant_09() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With UPI Paymode ")
    public void CreatePG2Merchant_010() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With NB Paymode ")
    public void CreatePG2Merchant_011() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("NET_BANKING","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With PPI Paymode ")
    public void CreatePG2Merchant_012() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setOnlineSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }

//***********BUSINESS WALLET MERCHANT ******************//
@Feature("42496")
@Owner("Nirottam")
@Test(description = "Creating and Checking Migration PG2 Merchant ")
public void CreatePG2Merchant_013() throws Exception {
    ArrayList<String> staticPref=new ArrayList<>();
    staticPref.add("PG2_DATA_MIGRATION");
    staticPref.add("GST_EXEMPTED");
    CreateMerchant createMerchant = new CreateMerchant("true");
    JsonPath response = createMerchant.setRequestID(requestID)
            .setRequestType("DEFAULT,RETRY")
            .setMobileNumber(phoneNumber)
            .setPhoneNumber(phoneNumber)
            .setCustId(AuthHelpers.getCustomerID(phoneNumber))
            .setMultipleStaticpref(staticPref)
            .setMerchantIndustryType("SMALL")
            .setCustomName("qa8mid")
            .setBWEnabled("TRUE")
            .setTxnType("Payments",1)
            .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
            .execute().jsonPath();
    validateResponse(response);
    mid = getMidViaBOSSAPI(phoneNumber);
    validatePrefStatus(mid,"PG2_DATA_MIGRATION");
    validatePrefStatus(mid,"GST_EXEMPTED");
}

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode ")
    public void CreatePG2Merchant_014() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With DC Paymode ")
    public void CreatePG2Merchant_015() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With UPI Paymode ")
    public void CreatePG2Merchant_016() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With NB Paymode ")
    public void CreatePG2Merchant_017() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("NET_BANKING","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With PPI Paymode ")
    public void CreatePG2Merchant_018() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setCustomName("qa8mid")
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }


    //******************ADD & MONEY CASES ****************//
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE ")
    public void CreatePG2Merchant_019() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .setPreference("Add & Pay")
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With CC Paymode")
    public void CreatePG2Merchant_020() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With DC Paymode")
    public void CreatePG2Merchant_021() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With UPI Paymode")
    public void CreatePG2Merchant_022() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With CC Paymode")
    public void CreatePG2Merchant_023() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With CC Paymode")
    public void CreatePG2Merchant_024() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }


    //************POSTPAID CASES ********//
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD_MONEY REQUEST TYPE With POSTPAID Paymode")
    public void CreatePG2Merchant_025() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT REQUEST TYPE With POSTPAID Paymode")
    public void CreatePG2Merchant_026() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .setTxnType("Payments",0)
                .setPaymode("PAYTM_DIGITAL_CREDIT","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For BWENABLED MERCHANT With POSTPAID Paymode")
    public void CreatePG2Merchant_027() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ONLINE SETTELEMENT MERCHANT With POSTPAID Paymode")
    public void CreatePG2Merchant_028() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }


    //***************PPI CASES *****************//

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant  With PPI Paymode FOR PCF merchant")
    public void CreatePG2Merchant_029() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)

                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }


    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For BW AND PCF MERCHANT With PPI Paymode ")
    public void CreatePG2Merchant_030() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ONLINE SETTELEMENT AND PCF MERCHANT With PPI Paymode ")
    public void CreatePG2Merchant_031() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD&PAY AND PCF MERCHANT With PPI Paymode ")
    public void CreatePG2Merchant_032() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }



    //************TRANSACTION WISE SETTELEMENT CASES********//
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For ADD&PAY, PCF MERCHANT and Transaction Wise Settelement With PPI Paymode ")
    public void CreatePG2Merchant_033() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("ADD_MONEY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .setPreference("Add & Pay")
                .setTransactionWiseSettlement("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("PPI","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT, PCF MERCHANT and Transaction Wise Settelement With DC Paymode ")
    public void CreatePG2Merchant_034() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT, PCF MERCHANT and Transaction Wise Settelement With CC Paymode ")
    public void CreatePG2Merchant_035() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT, PCF MERCHANT and Transaction Wise Settelement With POSTPAID Paymode ")
    public void CreatePG2Merchant_036() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }

    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT, PCF MERCHANT and Transaction Wise Settelement With UPI Paymode ")
    public void CreatePG2Merchant_037() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
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
    }
    @Feature("42496")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant For DEFAULT, PCF MERCHANT and Transaction Wise Settelement With UPI Paymode ")
    public void CreatePG2Merchant_038() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,ADD_MONEY,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mPG2")
                .setSolutionType("OFFLINE")
                .setBWEnabled("FALSE")
                .setPreference("Add & Pay")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }
    @Feature("")
    @Owner("Nirottam")
    @Test(description = "Creating and Checking Migration PG2 Merchant With CC Paymode ")
    public void CreatePG2Merchant_041() throws Exception {
        ArrayList<String> staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant("true");
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY,SEAMLESS_NATIVE,SEAMLESS")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setSolutionType("OFFLINE")
                .setBWEnabled("FALSE")
                .setCustomName("qa8PG2")
                .setTxnType("Payments",0)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("Payments",1)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",1)
                .setTxnType("Payments",2)
                .setPaymode("DC","simple",2.00,"FALSE",0,"",2)
                .execute().jsonPath();
        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
        validatePrefStatus(mid,"GST_EXEMPTED");
    }

}
