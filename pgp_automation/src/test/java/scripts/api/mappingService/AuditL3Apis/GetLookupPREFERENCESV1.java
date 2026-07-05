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

public class GetLookupPREFERENCESV1 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetLookupPREFERENCESV1 API response")
    void verifyGetLookupPREFERENCESV1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_LookupPREFERENCES_V1("FEE_IDENTIFIER", "SIMPLE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetLookupfromidV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "76543201");
        Assert.assertEquals(withDrawJson1.getString("name"), "SIMPLE");
        Assert.assertEquals(withDrawJson1.getString("category"), "FEE_IDENTIFIER");
        Assert.assertEquals(withDrawJson1.getString("oldpgCode"), null);
    }

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetLookupfromidV1 API response")
    void verifyGetLookupPREFERENCESV1_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_LookupPREFERENCES_V1("COMMISSION_IDENTIFIER", "SIMPLE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetLookupfromidV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "76543204");
        Assert.assertEquals(withDrawJson1.getString("name"), "SIMPLE");
        Assert.assertEquals(withDrawJson1.getString("category"), "COMMISSION_IDENTIFIER");
        Assert.assertEquals(withDrawJson1.getString("oldpgCode"), null);
    }
}
