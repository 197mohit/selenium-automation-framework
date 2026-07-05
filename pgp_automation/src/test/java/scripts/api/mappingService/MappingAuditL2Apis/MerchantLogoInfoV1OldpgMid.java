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

public class MerchantLogoInfoV1OldpgMid extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Test(description = "Verify paytmMid  of MerchantLogoInfoV1OldpgMid")
    void MerchantLogoInfoV1OoldpgMid_01()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_LogoInfo_V1_OldpgMid("216820000009356969000");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantLogoInfoV1OldpgId(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("paytmMid"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
    @Owner("Anushka Goldi")
    @Test(description = "Verify merchantNames of MerchantLogoInfoV1OldpgMid")
    void MerchantLogoInfoV1OoldpgMid_02()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_LogoInfo_V1_OldpgMid("216820000009356969000");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantLogoInfoV1OldpgId(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("merchantBusinessName"),"qamid");
        Assert.assertEquals(withDrawJson1.getString("merchantDisplayName"),"qamid");
        Assert.assertNull(withDrawJson1.getString("merchantImageName"));
    }
    @Owner("Anushka Goldi")
    @Test(description = "Verify Successfull response of MerchantLogoInfoV1OldpgMid")
    void MerchantLogoInfoV1OoldpgMid_03()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_LogoInfo_V1_OldpgMid("216820000009356969000");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantLogoInfoV1OldpgId(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
    }
}
