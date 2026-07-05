package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetEntityurlinformid extends PGPBaseTest {
    String urlType = "REQUEST";
    String websiteName = "retail";
    String requestName="qamid";
    String imageName="MID_LOgo";

    @Test(description = "Verify Status response of getEntityUrlInfoMid api")
    void verifyEntityurlinformidapi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Entityurlinformid(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), urlType, websiteName);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetEntityurlinformid(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("mid"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("urlTypeId"),urlType );
        Assert.assertEquals(withDrawJson1.getString("status"), "ACTIVE");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify other response of getEntityUrlInfoMid api")
    void verifyEntityurlinformidapi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Entityurlinformid(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), urlType, websiteName);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetEntityurlinformid(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("websiteName"), websiteName);
        Assert.assertEquals(withDrawJson1.getString("requestName"), requestName);
        Assert.assertEquals(withDrawJson1.getString("imageName"), imageName);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
}
