package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetAlipayId extends PGPBaseTest {
    String officialName="qamid";
    String industryTypeId="345678920";
    String oldpgId="qa12mi80573803805439";
    @Test(description = "Verify Successfull response of Get Alipay Id Api")
    void GetPaytmId_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paytmId(oldpgId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("paytmId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("oldpgId"),"qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("officialName"),officialName);
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),industryTypeId);
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
    @Test(description = "Verify All Parameters in GetAliPay Id api")
    void GetPaytmId_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paytmId(oldpgId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetAliPayId(objectHead,withDrawJson1);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify Successfull response of Get Alipay Id Api When Mid is passed")
    void GetAlipayId_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_alipayid(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("paytmId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("oldpgId"),oldpgId);
        Assert.assertEquals(withDrawJson1.getString("officialName"),officialName);
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),industryTypeId);
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetAliPayId(objectHead,withDrawJson1);
        PG2MappingApisHelper pg2MappingApisHelper2=new PG2MappingApisHelper();
        //pg2MappingApisHelper2.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
}
