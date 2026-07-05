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

public class DeviceDetailsV2Tid extends PGPBaseTest {
    String Tid = "15034741";

    String Tid1 = "15034743";

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Response of DeviceDetailsV2Tid API with tid 15034741")
    void VerifyDeviceDetailsV2Tid_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data ids of DeviceDetailsV2Tid API with tid 15034741")
    void VerifyDeviceDetailsV2Tid_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.tid"), "15034741");
        Assert.assertEquals(withDrawJson1.getString("response.mid"), "Avenge13915778205253");
        Assert.assertEquals(withDrawJson1.getString("response.monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("response.serialNo"), "auto004567793");
        Assert.assertEquals(withDrawJson1.getString("response.tmsTid"), "1508849896914998");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data of DeviceDetailsV2Tid API with tid 15034741")
    void VerifyDeviceDetailsV2Tid_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("response.vendorName"), "PAX");
        Assert.assertNull(withDrawJson1.getString("response.kybId"));
        Assert.assertNull(withDrawJson1.getString("response.ecrCallBackUrl"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Data status of DeviceDetailsV2Tid API with tid 15034741")
    void VerifyDeviceDetailsV2Tid_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatusMsg"), "Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatusMsg"), "Terminal Successfully onboarded.");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify other Data of DeviceDetailsV2Tid API with tid 15034741")
    void VerifyDeviceDetailsV2Tid_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.isUsed"), "1");
        Assert.assertEquals(withDrawJson1.getString("response.createdDate"), "1666160497000");
        Assert.assertEquals(withDrawJson1.getString("response.modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("response.merchantName"), "Cricket Dragons");
        Assert.assertEquals(withDrawJson1.getString("response.merchantCategory"), "Food");
        Assert.assertEquals(withDrawJson1.getString("response.merchantSubCategory"), "Online Food Delivery");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify address Data of DeviceDetailsV2Tid API with tid 15034741")
    void VerifyDeviceDetailsV2Tid_06() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.terminalLatitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("response.terminalLongitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("response.addressOne"), "00 opp bj tower");
        Assert.assertEquals(withDrawJson1.getString("response.addressTwo"), "mayu vihar");
        Assert.assertEquals(withDrawJson1.getString("response.industryType"), "Retail");
        Assert.assertEquals(withDrawJson1.getString("response.addressThree"), "1st B Road Sardart");
        Assert.assertEquals(withDrawJson1.getString("response.city"), "Deoria");
        Assert.assertEquals(withDrawJson1.getString("response.stateName"), "Uttar Pradesh");
        Assert.assertEquals(withDrawJson1.getString("response.countryName"), "India");
        Assert.assertEquals(withDrawJson1.getString("response.zipcode"), "274001");
    }



    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Response of DeviceDetailsV2Tid API with tid 15034743****")
    void VerifyDeviceDetailsV2Tid_07() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data ids of DeviceDetailsV2Tid API with tid 15034743****")
    void VerifyDeviceDetailsV2Tid_08() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.tid"), "15034743");
        Assert.assertEquals(withDrawJson1.getString("response.mid"), "JnfpoR94692072462401");
        Assert.assertEquals(withDrawJson1.getString("response.monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("response.serialNo"), "auto004567795");
        Assert.assertEquals(withDrawJson1.getString("response.tmsTid"), "1508849959829558");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data of DeviceDetailsV2Tid API with tid 15034743****")
    void VerifyDeviceDetailsV2Tid_09() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("response.vendorName"), "PAX");
        Assert.assertEquals(withDrawJson1.getString("response.kybId"), "1234");
        Assert.assertNull(withDrawJson1.getString("response.ecrCallBackUrl"));
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify Data status of DeviceDetailsV2Tid API with tid 15034743****")
    void VerifyDeviceDetailsV2Tid_10() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatusMsg"), "Terminal Successfully returned.");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatusMsg"), "Terminal Successfully returned.");
    }

    @Owner("Vikashh Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify other Data of DeviceDetailsV2Tid API with tid 15034743****")
    void VerifyDeviceDetailsV2Tid_11() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertNull(withDrawJson1.getString("response.isUsed"));
        Assert.assertEquals(withDrawJson1.getString("response.createdDate"), "1666160527000");
        Assert.assertEquals(withDrawJson1.getString("response.modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("response.merchantName"), "AutomationMerchant");
        Assert.assertEquals(withDrawJson1.getString("response.merchantCategory"), "Education");
        Assert.assertEquals(withDrawJson1.getString("response.merchantSubCategory"), "School");
    }

    @Owner("Vikash Verma")
    @Feature("PGP-45653")
    @Test(description = "****Verify address Data of DeviceDetailsV2Tid API with tid 15034743****")
    void VerifyDeviceDetailsV2Tid_12() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_V2_Tid(Tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsV2Tid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.terminalLatitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("response.terminalLongitude"), "0.0");
        Assert.assertEquals(withDrawJson1.getString("response.addressOne"), "00 opp bj tower");
        Assert.assertEquals(withDrawJson1.getString("response.addressTwo"), "mayu vihar");
        Assert.assertNotNull(withDrawJson1.getString("response.industryType"));
        Assert.assertEquals(withDrawJson1.getString("response.addressThree"), "1st B Road Sardart");
        Assert.assertEquals(withDrawJson1.getString("response.city"), "Deoria");
        Assert.assertEquals(withDrawJson1.getString("response.stateName"), "Uttar Pradesh");
        Assert.assertEquals(withDrawJson1.getString("response.countryName"), "India");
        Assert.assertEquals(withDrawJson1.getString("response.zipcode"), "274001");
    }
}
