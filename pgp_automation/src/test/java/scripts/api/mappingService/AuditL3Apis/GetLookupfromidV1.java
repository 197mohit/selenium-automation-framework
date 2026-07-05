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

public class GetLookupfromidV1 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetLookupfromidV1 API response")
    void verifyGetLookupfromidV1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Lookupfrom_id_V1("76543202");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetLookupfromidV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "76543202");
        Assert.assertEquals(withDrawJson1.getString("name"), "SLAB_TXN_AMT");
        Assert.assertEquals(withDrawJson1.getString("category"), "FEE_IDENTIFIER");
        Assert.assertEquals(withDrawJson1.getString("oldpgCode"), null);
    }

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetLookupfromidV1 API response")
    void verifyGetLookupfromidV1_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Lookupfrom_id_V1("76543203");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetLookupfromidV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "76543203");
        Assert.assertEquals(withDrawJson1.getString("name"), "SLAB_DAILY_TXN_AMT");
        Assert.assertEquals(withDrawJson1.getString("category"), "FEE_IDENTIFIER");
        Assert.assertEquals(withDrawJson1.getString("oldpgCode"), null);
    }
}
