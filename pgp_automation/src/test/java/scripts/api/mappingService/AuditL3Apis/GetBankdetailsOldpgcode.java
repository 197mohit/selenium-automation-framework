package scripts.api.mappingService.AuditL3Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetBankdetailsOldpgcode extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBankdetailsOldpgcode API response")
    void verifyGetBankdetailsOldpgcode_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.GetBankdetailsOldpgcode("PPBLC1IN");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBankdetailsOldpgcode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankId"), "8565821");
        Assert.assertEquals(withDrawJson1.getString("bankName"), "Paytm Payments Bank");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "PPBL");
        Assert.assertEquals(withDrawJson1.getString("extIfscCode"), null);
        Assert.assertEquals(withDrawJson1.getString("bankDisplayName"), "Paytm Payments Bank");
        Assert.assertEquals(withDrawJson1.getString("bankKey"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgBankCode"), "PPBLC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankWebLogo"), null);
        Assert.assertEquals(withDrawJson1.getString("bankWapLogo"), null);
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMandate"), null);
        Assert.assertEquals(withDrawJson1.getString("standardBankCode"), null);
        Assert.assertEquals(withDrawJson1.getString("mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("mandateDebitCard"), "true");
        Assert.assertEquals(withDrawJson1.getString("payMode"), null);
        Assert.assertEquals(withDrawJson1.getString("displayOrder"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo"), null);
        Assert.assertEquals(withDrawJson1.getString("bankShortName"), "PPBL");
    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBankdetailsOldpgcode API response With oldpgcode YESBC1IN")
    void verifyGetBankdetailsOldpgcode_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.GetBankdetailsOldpgcode("YESBC1IN");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBankdetailsOldpgcode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankId"), "8565601");
        Assert.assertEquals(withDrawJson1.getString("bankName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "YES");
        Assert.assertEquals(withDrawJson1.getString("extIfscCode"), null);
        Assert.assertEquals(withDrawJson1.getString("bankDisplayName"), "Yes Bank");
        Assert.assertNull(withDrawJson1.getString("bankKey"));
        Assert.assertEquals(withDrawJson1.getString("oldpgBankCode"), "YESBC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankWebLogo"), null);
    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBankdetailsOldpgcode API With oldpgcode YESBC1IN response")
    void verifyGetBankdetailsOldpgcode_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.GetBankdetailsOldpgcode("YESBC1IN");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBankdetailsOldpgcode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankWapLogo"), null);
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertNull(withDrawJson1.getString("bankMandate"));
        Assert.assertNull(withDrawJson1.getString("standardBankCode"));
        Assert.assertEquals(withDrawJson1.getString("mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("mandateDebitCard"), "true");
        Assert.assertNull(withDrawJson1.getString("paymode"));
        Assert.assertNull(withDrawJson1.getString("displayOrder"));
        Assert.assertNull(withDrawJson1.getString("extendedInfo"));
        Assert.assertNull(withDrawJson1.getString("bankShortName"));
    }
}
