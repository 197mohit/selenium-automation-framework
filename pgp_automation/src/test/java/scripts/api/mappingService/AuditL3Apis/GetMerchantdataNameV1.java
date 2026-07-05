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

public class GetMerchantdataNameV1 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetMerchantdataNameV1 API response")
    void verifyGetMerchantdataNameV1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantdata_Name_V1("paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetMerchantdataNameV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "27399");
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "paytm149199080904762");
        Assert.assertEquals(withDrawJson1.getString("paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgId"), "216820000000134827675");
        Assert.assertEquals(withDrawJson1.getString("oldpgWalletId"), null);
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertEquals(withDrawJson1.getString("officialName"), "paytm");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("businessName"), null);
        Assert.assertEquals(withDrawJson1.getString("merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("entityId"), null);
    }

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetMerchantdataNameV1 API response")
    void verifyGetMerchantdataNameV1_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantdata_Name_V1("links");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetMerchantdataNameV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "42979");
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "qa8mid49895778745987");
        Assert.assertEquals(withDrawJson1.getString("paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgId"), "216820000009680096904");
        Assert.assertEquals(withDrawJson1.getString("oldpgWalletId"), null);
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertEquals(withDrawJson1.getString("officialName"), "links");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("businessName"), null);
        Assert.assertEquals(withDrawJson1.getString("merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("entityId"), null);
    }
}
