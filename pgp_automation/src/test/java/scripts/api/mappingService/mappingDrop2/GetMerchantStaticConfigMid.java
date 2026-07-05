package scripts.api.mappingService.mappingDrop2;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetMerchantStaticConfigMid extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify paytmResultInfo of GetMerchantStaticConfigMid API result ")
    void VerifyGetMerchantStaticConfigMid_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Static_Config_mid("qa14as66450521301343");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantStaticConfigMid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify response of GetMerchantStaticConfigMid API result ")
    void VerifyGetMerchantStaticConfigMid_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Static_Config_mid("qa14as66450521301343");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantStaticConfigMid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.mid"), "qa14as66450521301343");
        Assert.assertNotNull(withDrawJson1.getString("response.preference"));
        //Assert.assertTrue(withDrawJson1.getString("response.ff4jProperty").contains("[]"));
        Assert.assertNotNull(withDrawJson1.getString("response.ff4jFlag"));
        Assert.assertNotNull(withDrawJson1.getString("response.staticConfig"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify response.preference of GetMerchantStaticConfigMid API result ")
    void VerifyGetMerchantStaticConfigMid_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Static_Config_mid("qa14as66450521301343");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantStaticConfigMid(objectHead);
        int s= withDrawJson1.getList("response.preference").size();
        if(s==0){
            Assert.fail("response.preference is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("response.preference["+ 0 +"].name"), "JS_APPINVOKEALLOWED");
        Assert.assertEquals(withDrawJson1.getString("response.preference["+ 0 +"].value"), "Y");
        Assert.assertEquals(withDrawJson1.getString("response.preference["+ 1 +"].name"), "ENABLE_CHECKOUT_JS_ON_ENHANCED_FLOW");
        Assert.assertEquals(withDrawJson1.getString("response.preference["+ 1 +"].value"), "Y");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify response.ff4jFlag of GetMerchantStaticConfigMid API result ")
    void VerifyGetMerchantStaticConfigMid_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Static_Config_mid("qa14as66450521301343");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantStaticConfigMid(objectHead);
        int s= withDrawJson1.getList("response.ff4jFlag").size();
        if(s==0){
            Assert.fail("response.ff4jFlag is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("response.ff4jFlag["+ 0 +"].name"), "theia.enableSocketBasedPollingForUPICollect");
//        Assert.assertEquals(withDrawJson1.getString("response.ff4jFlag["+ 0 +"].value"), "true");
        Assert.assertEquals(withDrawJson1.getString("response.ff4jFlag["+ 1 +"].name"), "theia.disableSocketBasedPollingForUPICollect");
//        Assert.assertEquals(withDrawJson1.getString("response.ff4jFlag["+ 1 +"].value"), "false");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify response.staticConfig of GetMerchantStaticConfigMid API result ")
    void VerifyGetMerchantStaticConfigMid_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Static_Config_mid("qa14as66450521301343");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantStaticConfigMid(objectHead);
        int s= withDrawJson1.getList("response.staticConfig").size();
        if(s==0){
            Assert.fail("response.staticConfig is Empty");
        }
        Assert.assertTrue(withDrawJson1.getString("response.staticConfig").contains("[[name:AmountToBePaidNow, value:Amount to be Paid Now], [name:SelectOptiontoSetupSubs, value:Select an option to setup your subscriptions], [name:ActualAmtVaryBill, value:Actual amount may vary as per the bill], [name:BankMandateDebitCard, value:Please make sure you have Debit Card details available for authentication], [name:PayPostText, value:to Subscribe], [name:AmountDeductDays, value:Amount will be deducted within 2-4 days], [name:PayPreText, value:Pay], [name:NextPayment, value:Next Payment], [name:UpiIntent, value:Choose from the list of UPI apps on your phone], [name:Proceed, value:Proceed], [name:SubsDetails, value:Subscription Details], [name:RecurringBillFrequency, value:Recurring Bill Frequency], [name:SavingCardDetails, value:Saving your card details is mandatory to setup subscription], [name:RecurringBillAmount, value:Recurring Bill Amount], [name:Validity, value:Validity], [name:UpiIntentCollect, value:Choose from the list of UPI apps on your phone or Pay by entering UPI ID], [name:ActivateSubs, value:Activate Subscriptions], [name:BankMandateNetBanking, value:Please make sure you have Net Banking enabled on this account and have the required credentials available with you], [name:TobePaidNow, value:To be paid now], [name:ActualAmtVaryBillPayOption, value:Actual amount may vary as per the bill and chosen payment option], [name:Frequency, value:Frequency], [name:SubsPaymentViaWallet, value:Subscription payments will happen via wallet]]"));
    }
}
