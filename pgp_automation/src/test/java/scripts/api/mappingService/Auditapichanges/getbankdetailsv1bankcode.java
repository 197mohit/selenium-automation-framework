package scripts.api.mappingService.Auditapichanges;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class getbankdetailsv1bankcode extends PGPBaseTest {
    String BankCode= "YES";

    @Owner("vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify response of GetBankdetailsBankCode v1 API")
    void verifyGetBankdetailsBankCode_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_BankCode_V1(BankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsBankCodev1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankId"), "8565601");
        Assert.assertEquals(withDrawJson1.getString("bankName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "YES");
        Assert.assertEquals(withDrawJson1.getString("extIfscCode"), "YESB");
        Assert.assertEquals(withDrawJson1.getString("bankDisplayName"), "Yes Bank");
        Assert.assertNull(withDrawJson1.getString("bankKey"));
    }


    @Owner("vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify parameters of response in GetBankdetailsBankCode v1 API ")
    void verifyBankdetailsoldpgcodeApi_03() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_BankCode_V1(BankCode);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsBankCodev1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("mandateDebitCard"), "true");
        Assert.assertEquals(withDrawJson1.getString("payMode"), "NB");
        Assert.assertEquals(withDrawJson1.getString("displayOrder"), "14");
        Assert.assertNull(withDrawJson1.getString("bankShortName"));
    }

    @Owner("vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify parameters of response in GetBankdetailsBankCode v1 API ")
    void verifyBankdetailsoldpgcodeApi_04() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_BankCode_V1(BankCode);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsBankCodev1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.tpNBBankCode"), "9560");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.tpDCBankCode"), "9570");
    }

}
