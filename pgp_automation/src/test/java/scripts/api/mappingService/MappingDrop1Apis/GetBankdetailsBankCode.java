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

public class GetBankdetailsBankCode extends PGPBaseTest {
    String BankCode= "YES";

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of GetBankdetailsBankCode API")
    void verifyGetBankdetailsBankCode_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_BankCode(BankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsBankCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankId"), "8565601");
        Assert.assertEquals(withDrawJson1.getString("bankName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "YES");
        Assert.assertEquals(withDrawJson1.getString("extIfscCode"), "YESB");
        Assert.assertEquals(withDrawJson1.getString("bankDisplayName"), "Yes Bank");
        Assert.assertNull(withDrawJson1.getString("bankKey"));
    }
    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response in GetBankdetailsBankCode API ")
    void verifyBankdetailsAlipaycodeApi_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_BankCode(BankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsBankCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("oldpgBankCode"), "YESBC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankWebLogo"), "yes.png");
        Assert.assertEquals(withDrawJson1.getString("bankWapLogo"), "yes.png");
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertNull(withDrawJson1.getString("bankMandate"));
        Assert.assertNull(withDrawJson1.getString("standardBankCode"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify parameters of response in GetBankdetailsBankCode API ")
    void verifyBankdetailsAlipaycodeApi_03(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_BankCode(BankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsBankCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("mandateDebitCard"), "true");
        Assert.assertEquals(withDrawJson1.getString("payMode"), "NB");
        Assert.assertEquals(withDrawJson1.getString("displayOrder"), "14");
        Assert.assertNull(withDrawJson1.getString("bankShortName"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify parameters of response in GetBankdetailsBankCode API ")
    void verifyBankdetailsAlipaycodeApi_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_BankCode(BankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsBankCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.tpNBBankCode"), "9560");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.tpDCBankCode"), "9570");
    }
}
