package scripts.api.mappingService.mappingDrop2;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.regex.Matcher;

public class DeviceDetailsBankslistTid extends PGPBaseTest {

    String Tid = "15034741";

    String Tid1 = "15034743";


    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Response of DeviceDetailsBankslistTid API with tid 15034741")
    void VerifyDeviceDetailsBankslistTid_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data ids of DeviceDetailsBankslistTid API with tid 15034741")
    void VerifyDeviceDetailsBankslistTid_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tid"), "15034741");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].mid"), "Avenge13915778205253");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].serialNo"), "auto004567793");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tmsTid"), "1508849896914998");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data of DeviceDetailsBankslistTid API with tid 15034741")
    void VerifyDeviceDetailsBankslistTid_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].vendorName"), "PAX");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankTid"), "IEDC12345");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankName"), "IEDC");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].hsmZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].hsmZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data status of DeviceDetailsBankslistTid API with tid 15034741")
    void VerifyDeviceDetailsBankslistTid_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankStatusMsg"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tmsStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tmsStatusMsg"), "Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalStatusMsg"), "Terminal Successfully onboarded.");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify other Data of DeviceDetailsBankslistTid API with tid 15034741")
    void VerifyDeviceDetailsBankslistTid_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].isUsed"), "1");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].createdDate"), "1666160497000");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].merchantName"), "Cricket Dragons");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankMid"), "5PT000000000049");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].merchantCategory"), "Food");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].merchantSubCategory"), "Online Food Delivery");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify address Data of DeviceDetailsBankslistTid API with tid 15034741")
    void VerifyDeviceDetailsBankslistTid_06() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalLatitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalLongitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].addressOne"), "00 opp bj tower");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].addressTwo"), "mayu vihar");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].industryType"), "Retail");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].addressThree"), "1st B Road Sardart");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].city"), "Deoria");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].stateName"), "Uttar Pradesh");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].countryName"), "India");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].zipcode"), "274001");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].mccCode"), "5812");
        Assert.assertNull(withDrawJson1.getString("data["+ 0 +"].kybId"));
    }


    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Response of DeviceDetailsBankslistTid API with tid 15034743****")
    void VerifyDeviceDetailsBankslistTid_07() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }


    @Owner("Vikash verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data ids of DeviceDetailsBankslistTid API with tid 15034743****")
    void VerifyDeviceDetailsBankslistTid_08() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tid"), "15034743");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].mid"), "JnfpoR94692072462401");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].serialNo"), "auto004567795");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tmsTid"), "1508849959829558");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data of DeviceDetailsBankslistTid API with tid 15034743****")
    void VerifyDeviceDetailsBankslistTid_09() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].vendorName"), "PAX");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankTid"), "IEDC12347");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankName"), "IEDC");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].hsmZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].hsmZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
    }


    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data status of DeviceDetailsBankslistTid API with tid 15034743****")
    void VerifyDeviceDetailsBankslistTid_10() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankStatusMsg"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tmsStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].tmsStatusMsg"), "Terminal Successfully returned.");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalStatusMsg"), "Terminal Successfully returned.");
    }


    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify other Data of DeviceDetailsBankslistTid API with tid 15034743****")
    void VerifyDeviceDetailsBankslistTid_11() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        //Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].isUsed"), "null");
        Assert.assertNull(withDrawJson1.getString("data["+ 0 +"].isUsed"));
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].createdDate"), "1666160527000");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].merchantName"), "AutomationMerchant");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].bankMid"), "5PT000000000048");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].merchantCategory"), "Education");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].merchantSubCategory"), "School");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].mccCardOverride"), "8888");

    }


    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify address Data of DeviceDetailsBankslistTid API with tid 15034743****")
    void VerifyDeviceDetailsBankslistTid_12() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Eos_Merchant_Device_Details_Bankslist_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalLatitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].terminalLongitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].addressOne"), "00 opp bj tower");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].addressTwo"), "mayu vihar");
        Assert.assertNull(withDrawJson1.getString("data["+ 0 +"].industryType"));
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].addressThree"), "1st B Road Sardart");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].city"), "Deoria");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].stateName"), "Uttar Pradesh");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].countryName"), "India");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].zipcode"), "274001");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].mccCode"), "8211");
        Assert.assertEquals(withDrawJson1.getString("data["+ 0 +"].kybId"), "1234");

    }

}
