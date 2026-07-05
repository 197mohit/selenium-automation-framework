package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantProfile extends PGPBaseTest {
    String IdType = "retail";
    String merchantType="CORPORATION";
    String officialName="qamid";
    String englishName="qamid";
    String localName="qamid";
    String category="BFSI";
    String merchantBankName="qa";
    @Test(description = "Verify MerchantProfile API response ")
    void verifyMerchantProfile_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_profile(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), IdType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantProfile(objectHead);
        Assert.assertEquals(withDrawJson1.getString("merchantId"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("merchantType"), merchantType);
        Assert.assertEquals(withDrawJson1.getString("officialName"), officialName);
        Assert.assertEquals(withDrawJson1.getString("englishName"), englishName);
        Assert.assertEquals(withDrawJson1.getString("localName"), localName);
        Assert.assertEquals(withDrawJson1.getString("merchantStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("category"), category);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify MerchantProfile API response ")
    void verifyMerchantProfile_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_profile(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), IdType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantProfile(objectHead);
        Assert.assertEquals(withDrawJson1.getString("subCategory"), "MUTUAL FUND");
        Assert.assertNotNull("mcc");
        Assert.assertEquals(withDrawJson1.getString("paytmId"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("isAggregatorMerchant"), "false");
        Assert.assertEquals(withDrawJson1.getString("aesKey"), "WXW4dSJckPBIzj+V2ld1/wP3Um8V1CnYOrAHLzqADCo=");
        Assert.assertNull(withDrawJson1.getString("offlinePostConvenience"));
        Assert.assertEquals(withDrawJson1.getString("merchantBankName"), merchantBankName);
        Assert.assertNull(withDrawJson1.getString("postPaidOnAddNPay"));
        Assert.assertEquals(withDrawJson1.getString("merchantSolutiontype"), "OFFLINE");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
