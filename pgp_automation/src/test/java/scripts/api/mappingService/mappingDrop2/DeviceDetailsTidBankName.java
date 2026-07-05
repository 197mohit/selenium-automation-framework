package scripts.api.mappingService.mappingDrop2;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class DeviceDetailsTidBankName extends PGPBaseTest {
    String Tid = "15034741";

    String Tid1 = "15034743";
    String Bankname = "IEDC";

    @Owner("Anushka Goldi")
    @Test(description = "Verify Response of DeviceDetailsTidBankName API with tid 15034741")
    void VerifyDeviceDetailsTidBankNamed_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify Data ids of DeviceDetailsTidBankName API with tid 15034741")
    void VerifyDeviceDetailsTidBankName_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.tid"), "15034741");
        Assert.assertEquals(withDrawJson1.getString("response.mid"), "Avenge13915778205253");
        Assert.assertEquals(withDrawJson1.getString("response.monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("response.serialNo"), "auto004567793");
        Assert.assertEquals(withDrawJson1.getString("response.tmsTid"), "1508849896914998");
        Assert.assertNull(withDrawJson1.getString("response.kybId"));
        Assert.assertNull(withDrawJson1.getString("response.ecrCallBackUrl"));
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify Data of DeviceDetailsTidBankName API with tid 15034741")
    void VerifyDeviceDetailsTidBankName_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("response.vendorName"), "PAX");
        Assert.assertEquals(withDrawJson1.getString("response.bankTid"), "IEDC12345");
        Assert.assertEquals(withDrawJson1.getString("response.bankName"), "IEDC");
        Assert.assertEquals(withDrawJson1.getString("response.bankZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("response.bankZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("response.hsmZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("response.hsmZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify Data status of DeviceDetailsTidBankName API with tid 15034741")
    void VerifyDeviceDetailsTidBankName_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.bankStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("response.bankStatusMsg"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatusMsg"), "Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatusMsg"), "Terminal Successfully onboarded.");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify other Data of DeviceDetailsTidBankName API with tid 15034741")
    void VerifyDeviceDetailsTidBankName_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.isUsed"), "1");
        Assert.assertEquals(withDrawJson1.getString("response.createdDate"), "1666160497000");
        Assert.assertEquals(withDrawJson1.getString("response.modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("response.merchantName"), "Cricket Dragons");
        Assert.assertEquals(withDrawJson1.getString("response.bankMid"), "5PT000000000049");
        Assert.assertEquals(withDrawJson1.getString("response.merchantCategory"), "Food");
        Assert.assertEquals(withDrawJson1.getString("response.merchantSubCategory"), "Online Food Delivery");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify address Data of DeviceDetailsTidBankName API with tid 15034741")
    void VerifyDeviceDetailsTidBankName_06() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
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
        Assert.assertEquals(withDrawJson1.getString("response.mccCode"), "5812");
        Assert.assertEquals(withDrawJson1.getString("response.merchantIndustryType"), "BIG");
    }


    @Owner("Vikash verma")
    @Test(description = "****Verify Response of DeviceDetailsTidBankName API with tid 15034743****")
    void VerifyDeviceDetailsTidBankName_07() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid1, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Vikash verma")
    @Test(description = "****Verify Data ids of DeviceDetailsTidBankName API with tid 15034743****")
    void VerifyDeviceDetailsTidBankName_08() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid1, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.tid"), "15034743");
        Assert.assertEquals(withDrawJson1.getString("response.mid"), "JnfpoR94692072462401");
        Assert.assertEquals(withDrawJson1.getString("response.monthlyRental"), "2.0");
        Assert.assertEquals(withDrawJson1.getString("response.serialNo"), "auto004567795");
        Assert.assertEquals(withDrawJson1.getString("response.tmsTid"), "1508849959829558");
        Assert.assertEquals(withDrawJson1.getString("response.kybId"), "1234");
        Assert.assertNull(withDrawJson1.getString("response.ecrCallBackUrl"));
    }

    @Owner("Vikash Verma")
    @Test(description = "****Verify Data of DeviceDetailsTidBankName API with tid 15034743****")
    void VerifyDeviceDetailsTidBankName_09() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid1, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.modelName"), "A910");
        Assert.assertEquals(withDrawJson1.getString("response.vendorName"), "PAX");
        Assert.assertEquals(withDrawJson1.getString("response.bankTid"), "IEDC12347");
        Assert.assertEquals(withDrawJson1.getString("response.bankName"), "IEDC");
        Assert.assertEquals(withDrawJson1.getString("response.bankZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("response.bankZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("response.hsmZdk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
        Assert.assertEquals(withDrawJson1.getString("response.hsmZpk"), "DUMMY_KEY_ZDK_ZPK_ICICI");
    }

    @Owner("Vikash Verma")
    @Test(description = "****Verify Data status of DeviceDetailsTidBankName API with tid 15034743****")
    void VerifyDeviceDetailsTidBankName_10() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid1, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.bankStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("response.bankStatusMsg"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatusMsg"), "Terminal Successfully returned.");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatus"), "RETURNED");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatusMsg"), "Terminal Successfully returned.");
    }

    @Owner("Vikash Verma")
    @Test(description = "****Verify other Data of DeviceDetailsTidBankName API with tid 15034743****")
    void VerifyDeviceDetailsTidBankName_11() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid1, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
        Assert.assertNull(withDrawJson1.getString("response.isUsed"));
        Assert.assertEquals(withDrawJson1.getString("response.createdDate"), "1666160527000");
        Assert.assertEquals(withDrawJson1.getString("response.modifiedDate"), "1707459607000");
        Assert.assertEquals(withDrawJson1.getString("response.merchantName"), "AutomationMerchant");
        Assert.assertEquals(withDrawJson1.getString("response.bankMid"), "5PT000000000048");
        Assert.assertEquals(withDrawJson1.getString("response.merchantCategory"), "Education");
        Assert.assertEquals(withDrawJson1.getString("response.merchantSubCategory"), "School");
    }

    @Owner("Vikash Verma")
    @Test(description = "****Verify address Data of DeviceDetailsTidBankName API with tid 15034743****")
    void VerifyDeviceDetailsTidBankName_12() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Device_Details_Tid_Bankname(Tid, Bankname);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("response");
        pg2MappingApisHelper.VerifyResponseOfDeviceDetailsTidBankname(objectHead);
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
        Assert.assertEquals(withDrawJson1.getString("response.mccCode"), "5812");
        Assert.assertEquals(withDrawJson1.getString("response.merchantIndustryType"), "BIG");
    }
}
