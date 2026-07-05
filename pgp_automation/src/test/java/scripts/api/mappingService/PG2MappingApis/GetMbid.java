package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetMbid extends PGPBaseTest {
    String bankId="8565560";
    String payMethodId="345678914";
    String authModeId="345678917";
    @Test(description = "Verify id in Response of Get mbid api")
    void verifyGetMbidApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_MBID(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), bankId, payMethodId, authModeId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetMbid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "9000770550077917115");
        Assert.assertEquals(withDrawJson1.getString("merchantId"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("mbId"), "70007981");
        Assert.assertEquals(withDrawJson1.getString("bankId"), bankId);
        Assert.assertEquals(withDrawJson1.getString("payMethodId"), payMethodId);
        Assert.assertEquals(withDrawJson1.getString("authModeId"), authModeId);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
    @Test(description = "Verify other Response of Get mbid api")
    void verifyGetMbidApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_MBID(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), bankId, payMethodId, authModeId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetMbid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("key"), "70007981");
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertEquals(withDrawJson1.getString("emi"), "false");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
}
