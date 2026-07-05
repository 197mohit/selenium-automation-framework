package scripts.api.mappingService.MappingDrop1Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetBanksDetailslistfromCodes extends PGPBaseTest {

    String BankCodes= "TNMB,PPBL,YES";
    String BankCodesWithNotfoundCode= "TNMB,PPBL,YES,ABC";


    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of GetBanksDetailslistfromCodes API")
    void verifyGetBanksDetailslistfromCodes_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksDetailslistfrom_Codes(BankCodes);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBanksDetailslistfromCodes(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify bankIds of GetBanksDetailslistfromCodes API")
    void verifyGetBanksDetailslistfromCodes_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksDetailslistfrom_Codes(BankCodes);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBanksDetailslistfromCodes(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankIds"), "8565821,8565597,8565601");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify bankMasterDetailsList of GetBanksDetailslistfromCodes API when all Bankcodes are found")
    void verifyGetBanksDetailslistfromCodes_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksDetailslistfrom_Codes(BankCodes);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBanksDetailslistfromCodes(objectHead);
        int s= withDrawJson1.getList("bankMasterDetailsList").size();
        if(s!=3){
            Assert.fail("BankMasterDetailsList is not equals to bankCode passed");
        }
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankId"), "8565821");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankName"), "Paytm Payments Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankCode"), "PPBL");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].oldpgBankCode"), "PPBLC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 1 +"].bankId"), "8565597");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 1 +"].bankName"), "TNMB");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 1 +"].bankCode"), "TNMB");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 1 +"].oldpgBankCode"), "TAMEC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 2 +"].bankId"), "8565601");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 2 +"].bankName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 2 +"].bankCode"), "YES");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 2 +"].oldpgBankCode"), "YESBC1IN");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify notFound of GetBanksDetailslistfromCodes API when an invalid Bankcode is passed")
    void verifyGetBanksDetailslistfromCodes_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksDetailslistfrom_Codes(BankCodesWithNotfoundCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBanksDetailslistfromCodes(objectHead);
        Assert.assertEquals(withDrawJson1.getString("notFound["+ 0 +"]"), "abc");
    }
}
