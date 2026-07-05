package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetMerchantidMap extends PGPBaseTest {
    String oldpgId="qa12mi80573803805439";
    String officialName="qamid";
    String industryTypeId="345678920";
    String merchantType="CORPORATION";

    @Test(description = "Verify id response of merchant idMap api")
    void GetAlipayId_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_idmap(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("paytmId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("oldpgId"),oldpgId);
        Assert.assertEquals(withDrawJson1.getString("guid"),"N");
        Assert.assertNotNull(withDrawJson1.getString("entityId"));
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify other response of merchant idMap api")
    void GetAlipayId_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_idmap(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("officialName"),officialName);
        Assert.assertEquals(withDrawJson1.getString("merchantType"),merchantType);
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),industryTypeId);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

}
