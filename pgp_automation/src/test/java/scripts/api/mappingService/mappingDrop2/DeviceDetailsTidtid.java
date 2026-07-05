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

public class DeviceDetailsTidtid extends PGPBaseTest {

    String Tid = "15034741";

    String Tid1 = "15034743";

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Response of DeviceDetailsTidtid API with tid 15034741")
    void VerifyDeviceDetailsTidtid_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid);
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
    @Test(description = "Verify Data ids of DeviceDetailsTidtid API with tid 15034741")
    void VerifyDeviceDetailsTidtid_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.id"), "19508");
        Assert.assertEquals(withDrawJson1.getString("data.tid"), "15034741");
        Assert.assertEquals(withDrawJson1.getString("data.mid"), "Avenge13915778205253");
        Assert.assertEquals(withDrawJson1.getString("data.monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("data.serialNo"), "auto004567793");
        Assert.assertEquals(withDrawJson1.getString("data.tmsTid"), "1508849896914998");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data of DeviceDetailsTidtid API with tid 15034741")
    void VerifyDeviceDetailsTidtid_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("data.vendorName"), "PAX");
        Assert.assertEquals(withDrawJson1.getString("data.bankTid"), "IEDC12345");
        Assert.assertEquals(withDrawJson1.getString("data.bankName"), "IEDC");
        Assert.assertEquals(withDrawJson1.getString("data.bankZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data.bankZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data.hsmZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data.hsmZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data status of DeviceDetailsTidtid API with tid 15034741")
    void VerifyDeviceDetailsTidtid_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.bankStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("data.bankStatusMsg"), "Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("data.tmsStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("data.tmsStatusMsg"), "Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("data.terminalStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("data.terminalStatusMsg"), "Terminal successfully onboarded.");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify other Data of DeviceDetailsTidtid API with tid 15034741")
    void VerifyDeviceDetailsTidtid_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.isUsed"), "1");
        Assert.assertEquals(withDrawJson1.getString("data.createdDate"), "1666160497000");
        Assert.assertEquals(withDrawJson1.getString("data.modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("data.merchantName"), "Cricket Dragons");
        Assert.assertEquals(withDrawJson1.getString("data.merchantDisplayName"), "Dragon");
        Assert.assertEquals(withDrawJson1.getString("data.bankMid"), "5PT000000000049");
        Assert.assertEquals(withDrawJson1.getString("data.merchantCategory"), "Food");
        Assert.assertEquals(withDrawJson1.getString("data.merchantSubCategory"), "Online Food Delivery");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify address Data of DeviceDetailsTidtid API with tid 15034741")
    void VerifyDeviceDetailsTidtid_06() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.terminalLatitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data.terminalLongitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data.addressOne"), "00 opp bj tower");
        Assert.assertEquals(withDrawJson1.getString("data.addressTwo"), "mayu vihar");
        Assert.assertEquals(withDrawJson1.getString("data.industryType"), "Retail");
        Assert.assertEquals(withDrawJson1.getString("data.addressThree"), "1st B Road Sardart");
        Assert.assertEquals(withDrawJson1.getString("data.city"), "Deoria");
        Assert.assertEquals(withDrawJson1.getString("data.stateName"), "Uttar Pradesh");
        Assert.assertEquals(withDrawJson1.getString("data.countryName"), "India");
        Assert.assertEquals(withDrawJson1.getString("data.zipcode"), "274001");
        Assert.assertEquals(withDrawJson1.getString("data.mccCode"), "5812");
        Assert.assertNull(withDrawJson1.getString("data.kybId"));
    }



    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Response of DeviceDetailsTidtid API with tid 15034743****")
    void VerifyDeviceDetailsTidtid_07() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidtid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data ids of DeviceDetailsTidtid API with tid 15034743****")
    void VerifyDeviceDetailsTidtid_08() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.id"), "19510");
        Assert.assertEquals(withDrawJson1.getString("data.tid"), "15034743");
        Assert.assertEquals(withDrawJson1.getString("data.mid"), "JnfpoR94692072462401");
        Assert.assertEquals(withDrawJson1.getString("data.monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("data.serialNo"), "auto004567795");
        Assert.assertEquals(withDrawJson1.getString("data.tmsTid"), "1508849959829558");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data of DeviceDetailsTidtid API with tid 15034743****")
    void VerifyDeviceDetailsTidtid_09() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("data.vendorName"), "PAX");
        Assert.assertEquals(withDrawJson1.getString("data.bankTid"), "IEDC12347");
        Assert.assertEquals(withDrawJson1.getString("data.bankName"), "IEDC");
        Assert.assertEquals(withDrawJson1.getString("data.bankZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data.bankZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data.hsmZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("data.hsmZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data status of DeviceDetailsTidtid API with tid 15034743****")
    void VerifyDeviceDetailsTidtid_10() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.bankStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("data.bankStatusMsg"), "Terminal Successfully returned.");
        Assert.assertEquals(withDrawJson1.getString("data.tmsStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("data.tmsStatusMsg"), "Terminal Successfully returned.");
        Assert.assertEquals(withDrawJson1.getString("data.terminalStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("data.terminalStatusMsg"), "hj");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify other Data of DeviceDetailsTidtid API with tid 15034743****")
    void VerifyDeviceDetailsTidtid_11() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertNull(withDrawJson1.getString("data.isUsed"));
        Assert.assertEquals(withDrawJson1.getString("data.createdDate"), "1666160527000");
        Assert.assertEquals(withDrawJson1.getString("data.modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("data.merchantName"), "AutomationMerchant");
        Assert.assertEquals(withDrawJson1.getString("data.merchantDisplayName"), "AutomationMerchant002");
        Assert.assertEquals(withDrawJson1.getString("data.bankMid"), "5PT000000000048");
        Assert.assertEquals(withDrawJson1.getString("data.merchantCategory"), "Education");
        Assert.assertEquals(withDrawJson1.getString("data.merchantSubCategory"), "School");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify address Data of DeviceDetailsTidtid API with tid 15034743****")
    void VerifyDeviceDetailsTidtid_12() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("data");
        pg2MappingApisHelper.VerifyDataOfDeviceDetailsTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("data.terminalLatitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data.terminalLongitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("data.addressOne"), "00 opp bj tower");
        Assert.assertEquals(withDrawJson1.getString("data.addressTwo"), "mayu vihar");
        Assert.assertNotNull(withDrawJson1.getString("data.industryType"));
        Assert.assertEquals(withDrawJson1.getString("data.addressThree"), "1st B Road Sardart");
        Assert.assertEquals(withDrawJson1.getString("data.city"), "Deoria");
        Assert.assertEquals(withDrawJson1.getString("data.stateName"), "Uttar Pradesh");
        Assert.assertEquals(withDrawJson1.getString("data.countryName"), "India");
        Assert.assertEquals(withDrawJson1.getString("data.zipcode"), "274001");
        Assert.assertEquals(withDrawJson1.getString("data.mccCode"), "5812");
        Assert.assertEquals(withDrawJson1.getString("data.kybId"), null);

    }
}
