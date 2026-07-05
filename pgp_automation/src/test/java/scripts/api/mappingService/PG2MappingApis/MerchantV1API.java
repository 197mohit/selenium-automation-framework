package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantV1API extends PGPBaseTest {
    String alipayId="qa12mi80573803805439";
    String officialName="qamid";
    String merchantType="CORPORATION";
    String industryTypeId="345678920";

    @Test(description = "Verify Successfull response of Merchant V1 API")
    void verifyMerchantV1APIApi() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v1(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV1Api(objectHead,withDrawJson1);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
    @Test(description = "Verify response object  of Merchant V1 API")
    void MerchantV1Api_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v1(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV1Api(objectHead,withDrawJson1);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("response.paytmId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("response.oldpgId"),alipayId);
        Assert.assertEquals(withDrawJson1.getString("response.guid"),"N");
        Assert.assertEquals(withDrawJson1.getString("response.officialName"),officialName);
        Assert.assertEquals(withDrawJson1.getString("response.merchantType"),merchantType);
        Assert.assertEquals(withDrawJson1.getString("response.industryTypeId"),industryTypeId);
        Assert.assertNotNull(withDrawJson1.getString("response.entityId"));
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify Entity Id is not Null in Response object")
    void MerchantV1Api_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v1(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV1Api(objectHead,withDrawJson1);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertNotNull(withDrawJson1.getString("response.entityId"));
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
