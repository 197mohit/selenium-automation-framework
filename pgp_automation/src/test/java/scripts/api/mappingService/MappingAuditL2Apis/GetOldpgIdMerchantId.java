package scripts.api.mappingService.MappingAuditL2Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetOldpgIdMerchantId extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Test(description = "Verify Successfull response of GetOldpgIdMerchantId ")
    void GetGetOldpgIdMerchantId_01()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_oldpgId(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("oldpgId"),"qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("officialName"),"qamid");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),"345678920");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify null response of GetOldpgIdMerchantId")
    void GetPaytmIdV1_02()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_oldpgId(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertNull(withDrawJson1.getString("paytmWalletId"));
        Assert.assertNull(withDrawJson1.getString("oldpgWalletId"));
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertNull(withDrawJson1.getString("businessName"));
        Assert.assertNull(withDrawJson1.getString("merchantType"));
        Assert.assertNull(withDrawJson1.getString("entityId"));
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify Successfull response of Get oldpgId with paytmId qa8mid49895778745987 ")
    void GetPaytmIdV1_03()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_oldpgId("qa8mid49895778745987");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "qa8mid49895778745987" );
        Assert.assertEquals(withDrawJson1.getString("oldpgId"),"qa8mid49895778745987");
        Assert.assertEquals(withDrawJson1.getString("officialName"),"links");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),"345678920");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify null response of Get oldpgId with paytmId qa8mid49895778745987 ")
    void GetPaytmIdV1_04()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_oldpgId("qa8mid49895778745987");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertNull(withDrawJson1.getString("paytmWalletId"));
        Assert.assertNull(withDrawJson1.getString("oldpgWalletId"));
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertNull(withDrawJson1.getString("businessName"));
        Assert.assertNull(withDrawJson1.getString("merchantType"));
        Assert.assertNull(withDrawJson1.getString("entityId"));
    }

}
