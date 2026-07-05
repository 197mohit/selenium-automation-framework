package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class merchantDeviceDetails extends PGPBaseTest {
    String tid="15036693";
    @Test(description = "Verify Successfull response of Merchant Device Detail Api when V2 tid is passed")
    void verifyMerchantDeviceDetailWithV2TID() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_V2_TID(tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantDeviceDetailV2TID(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");

    }
    @Test(description = "Verify  Device Detail  when V2 tid is passed")
    void verifyMerchantDeviceDetailWithV2TID_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_V2_TID(tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantDeviceDetailV2TID(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.tid"),tid);
        Assert.assertEquals(withDrawJson1.getString("response.mid"),"qa14LL61398321545845");
        Assert.assertEquals(withDrawJson1.getString("response.monthlyRental"),"200.0");
        Assert.assertEquals(withDrawJson1.getString("response.serialNo"),"81430328");
        Assert.assertEquals(withDrawJson1.getString("response.tmsTid"),"1525192178270245");
        Assert.assertEquals(withDrawJson1.getString("response.modelName"),"A910");
        Assert.assertEquals(withDrawJson1.getString("response.vendorName"),"PAX");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatus"),"PENDING_VERIFICATION");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatusMsg"),"Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatus"),"PENDING_VERIFICATION");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatusMsg"),"Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("response.isUsed"),"1");
        Assert.assertEquals(withDrawJson1.getString("response.createdDate"),"1673953103000");
        Assert.assertEquals(withDrawJson1.getString("response.modifiedDate"),"1707459607000");
        Assert.assertEquals(withDrawJson1.getString("response.merchantName"),"BHARAT Chaudhary");
        Assert.assertEquals(withDrawJson1.getString("response.merchantCategory"),"BFSI");
        Assert.assertEquals(withDrawJson1.getString("response.merchantSubCategory"),"Loans");
        Assert.assertEquals(withDrawJson1.getString("response.terminalLatitude"),"28.5355");
        Assert.assertEquals(withDrawJson1.getString("response.terminalLongitude"),"77.391");
        Assert.assertEquals(withDrawJson1.getString("response.ecrCallBackUrl"),null);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");

    }

    @Test(description = "Verify Successfull response of Merchant Device Detail Api when  tid and BankName PEDC are passed")
    void verifyMerchantDeviceDetailWithTIDAndbankname_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_TID_And_BankName(tid,"PEDC");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");
    }
    @Test(description = "Verify  Device Detail  when  tid  and bankname PEDC are passed")
    void verifyMerchantDeviceDetailWithTIDAndbankname_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_TID_And_BankName(tid,"PEDC");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantDeviceDetailTIDAndbankName(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.tid"),tid);
        Assert.assertEquals(withDrawJson1.getString("response.mid"),"qa14LL61398321545845");
        Assert.assertEquals(withDrawJson1.getString("response.monthlyRental"),"200.0");
        Assert.assertEquals(withDrawJson1.getString("response.serialNo"),"81430328");
        Assert.assertEquals(withDrawJson1.getString("response.tmsTid"),"1525192178270245");
        Assert.assertEquals(withDrawJson1.getString("response.modelName"),"A910");
        Assert.assertEquals(withDrawJson1.getString("response.vendorName"),"PAX");
        Assert.assertEquals(withDrawJson1.getString("response.tmsStatusMsg"),"Terminal Successfully onboarded.");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatus"),"PENDING_VERIFICATION");
        Assert.assertEquals(withDrawJson1.getString("response.terminalStatusMsg"),"Terminal Successfully onboarded.");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");
    }

    @Test(description = "Verify  BankName,bankTid,bankStatus,bankStatusMsg,bankMid  when  tid  and bankname PEDC are passed")
    void verifyMerchantDeviceDetailWithTIDAndbankname_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_TID_And_BankName(tid,"PEDC");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantDeviceDetailTIDAndbankName(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.bankName"),"PEDC");
        Assert.assertEquals(withDrawJson1.getString("response.bankTid"),"5P129787");
        Assert.assertEquals(withDrawJson1.getString("response.bankStatus"),"ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("response.bankStatusMsg"),"ACTIVE");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");
    }

    @Test(description = "Verify Successfull response of Merchant Device Detail Api when only tid is passed")
    void verifyMerchantDeviceDetailWithTID() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_TID(tid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");

    }

    @Test(description = "Verify  Merchant Device Detail Api when only tid is passed")
    void verifyMerchantDeviceDetailWithTID_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_TID("15036693");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantDeviceDetailWithTID(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");
    }
    @Test(description = "Verify  Id and merchantDisplayName Name")
    void verifyMerchantDeviceDetailWithTID_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_TID("15036693");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantDeviceDetailWithTID(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        Assert.assertEquals(withDrawJson1.getString("data.id"),"197919");
        Assert.assertEquals(withDrawJson1.getString("data.merchantDisplayName"),"CUSTOM");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");
    }

    @Test(description = "Verify EOS_MERCHANT_DEVICE_DETAILS_BANKSLIST_TID API")
    void verifyMerchantDeviceDetailsBankslistTidApi() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Device_Details_With_BANKLIST_TID("15036693");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantDeviceDetailsBankslistTid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        int s=withDrawJson1.getList("data").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("data["+ i +"].mid"));
            Assert.assertEquals(withDrawJson1.getString("data["+ i +"].tid"), "15036693");

        }
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes("qa14LL61398321545845");
    }
}
