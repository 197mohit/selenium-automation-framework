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

public class BankdetailsAlipaycodeApi extends PGPBaseTest {

    String oldpgBankCode= "PPBLC1IN";

    @Owner("Anushka")
    @Feature("PGP-45207")
    @Test(description = "Verify Bankdetails Alipaycode API result ")
    void verifyBankdetailsAlipaycodeApi_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Bankdetails_Alipaycode_Api(oldpgBankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyBankdetailsAlipaycode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankId"), "8565821");
        Assert.assertEquals(withDrawJson1.getString("bankName"), "Paytm Payments Bank");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "PPBL");
        Assert.assertNull(withDrawJson1.getString("extIfscCode"));
        Assert.assertEquals(withDrawJson1.getString("bankDisplayName"), "Paytm Payments Bank");
        Assert.assertNull(withDrawJson1.getString("bankKey"));
    }
    @Owner("Anushka")
    @Feature("PGP-45207")
    @Test(description = "Verify response in Bankdetails Alipaycodee API ")
    void verifyBankdetailsAlipaycodeApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Bankdetails_Alipaycode_Api(oldpgBankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyBankdetailsAlipaycode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("oldpgBankCode"), oldpgBankCode);
        Assert.assertNull(withDrawJson1.getString("bankWebLogo"));
        Assert.assertNull(withDrawJson1.getString("bankWapLogo"));
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertNull(withDrawJson1.getString("bankMandate"));
        Assert.assertNull(withDrawJson1.getString("standardBankCode"));
        Assert.assertEquals(withDrawJson1.getString("mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("mandateDebitCard"), "true");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify parameters of response in Bankdetails Alipaycodee API ")
    void verifyBankdetailsAlipaycodeApi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Bankdetails_Alipaycode_Api(oldpgBankCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyBankdetailsAlipaycode(objectHead);
        Assert.assertNull(withDrawJson1.getString("payMode"));
        Assert.assertNull(withDrawJson1.getString("displayOrder"));
        Assert.assertNull(withDrawJson1.getString("extendedInfo"));
        Assert.assertEquals(withDrawJson1.getString("bankShortName"), "PPBL");
    }

}
