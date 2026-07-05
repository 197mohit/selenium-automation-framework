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

public class GetMerchantdataPaytmidV1 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetMerchantdataPaytmidV1 API response")
    void verifyGetMerchantdataPaytmidV1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantdata_Paytmid_V1(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetMerchantdataPaytmidV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), null);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgId"), "qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("oldpgWalletId"), null);
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertEquals(withDrawJson1.getString("officialName"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("businessName"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("entityId"), "678703827");
    }

    @Owner("Anushka")
    @Feature("PG2-12161")
    @Test(description = "Verify GetMerchantdataPaytmidV1 API response")
    void verifyGetMerchantdataPaytmidV1_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantdata_Paytmid_V1(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetMerchantdataPaytmidV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), null);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "qa14Qu16505533696224");
        Assert.assertEquals(withDrawJson1.getString("paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgId"), "216820000009644379409");
        Assert.assertEquals(withDrawJson1.getString("oldpgWalletId"), null);
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertEquals(withDrawJson1.getString("officialName"), "ENGLAND XOXO");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"), "345678920");
//        Assert.assertEquals(withDrawJson1.getString("businessName"), "links");
        Assert.assertEquals(withDrawJson1.getString("merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("entityId"), "678706539");
    }

}
