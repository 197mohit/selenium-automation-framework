package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetMerchantLogoInfo extends PGPBaseTest {
    String merchantBusinessName="qamid";
    String merchantDisplayName="qamid";
    @Test(description = "Verify Successfull response of Merchant Logo Info api")
    void Logo_info_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_merchantlogoinfo_v2(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify merchantBusinessName,merchantDisplayName,paytmMid")
    void Logo_info_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_merchantlogoinfo_v2(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("merchantBusinessName"),merchantBusinessName);
        Assert.assertEquals(withDrawJson1.getString("merchantDisplayName"),merchantDisplayName);
        Assert.assertEquals(withDrawJson1.getString("paytmMid"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify All parameters for merchant logo info api")
    void Logo_info_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_merchantlogoinfo_v2(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantLogoInfo(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("merchantBusinessName"),merchantBusinessName);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
}
