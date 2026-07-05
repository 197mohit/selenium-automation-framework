package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantAttributeAdditional  extends PGPBaseTest {
    String IdType = "paytm";
    @Test(description = "Verify Successfull response of MerchantAttributeAdditional Api")
    void verifyMerchantAttributeAdditionalapi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_attribute_additional(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), IdType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeAdditional(objectHead);
        Assert.assertEquals(withDrawJson1.getString("isAggregator"), "0");
        Assert.assertEquals(withDrawJson1.getString("category"), "BFSI");
        Assert.assertEquals(withDrawJson1.getString("subCategory"), "MUTUAL FUND");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify Successfull response of MerchantAttributeAdditional Api")
    void verifyMerchantAttributeAdditionalapi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_attribute_additional(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), IdType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeAdditional(objectHead);
        Assert.assertEquals(withDrawJson1.getString("ifscCode"), "INDB0000018");
        Assert.assertEquals(withDrawJson1.getString("panVerified"), "9376503");
        Assert.assertEquals(withDrawJson1.getString("accountNumber"), "12345678909");
        Assert.assertEquals(withDrawJson1.getString("accountHolderName"), "qa");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
