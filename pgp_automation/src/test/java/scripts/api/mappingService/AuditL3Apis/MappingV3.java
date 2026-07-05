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

public class MappingV3  extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify UserV3 API paytmResultInfo with Type PAYTM ")
    void verifyMappingV3_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Mapping_V3("PAYTM", Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.MappingV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify MappingV3 API paytmResultInfo with Type PAYTM ")
    void verifyMappingV3_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Mapping_V3("PAYTM", Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.MappingV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.paytmId"), "qa14Qu16505533696224");
        Assert.assertEquals(withDrawJson1.getString("response.oldpgId"), "216820000009644379409");
        Assert.assertEquals(withDrawJson1.getString("response.guid"), "N");
        Assert.assertEquals(withDrawJson1.getString("response.ssoId"), "1107228639");
        Assert.assertEquals(withDrawJson1.getString("response.officialName"), "ENGLAND XOXO");
        Assert.assertEquals(withDrawJson1.getString("response.paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("response.oldpgWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("response.merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("response.industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("response.entityId"), "678706539");
        Assert.assertEquals(withDrawJson1.getString("response.pg2OnboardedMerchant"), "N");
        Assert.assertEquals(withDrawJson1.getString("response.businessName"), null);
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify UserV3 API paytmResultInfo with Type OLDPG ")
    void verifyMappingV3_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Mapping_V3("OLDPG", Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.MappingV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify MappingV3 API paytmResultInfo with Type OLDPG ")
    void verifyMappingV3_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Mapping_V3("OLDPG", Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.MappingV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.paytmId"), "qa14Qu16505533696224");
        Assert.assertEquals(withDrawJson1.getString("response.oldpgId"), "216820000009644379409");
        Assert.assertEquals(withDrawJson1.getString("response.guid"), "N");
        Assert.assertEquals(withDrawJson1.getString("response.ssoId"), "1107228639");
        Assert.assertEquals(withDrawJson1.getString("response.officialName"), "ENGLAND XOXO");
        Assert.assertEquals(withDrawJson1.getString("response.paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("response.oldpgWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("response.merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("response.industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("response.entityId"), "678706539");
        Assert.assertEquals(withDrawJson1.getString("response.pg2OnboardedMerchant"), "N");
        Assert.assertEquals(withDrawJson1.getString("response.businessName"), null);
    }
}
