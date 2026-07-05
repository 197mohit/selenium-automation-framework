package scripts.api.mappingService.AuditL3Apis;

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

public class QueryMerchantMigrationDetailsV1 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantMigrationDetailsV1  API result ")
    void verifyQueryMerchantMigrationDetailsV1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_Merchant_MigrationDetails_V1(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyQueryMerchantMigrationDetailsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("CONTRACT-AVALABLE"), "2");
    }
}
