package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantApiUrlInfo extends PGPBaseTest {
    String alipayId="qa12mi80573803805439";

    @Test(description = "Verify Successfull response of Merchant Api Url Info Api and validiate url")
    void verifyMerchantApiUrlInfoApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Api_UrlInfo(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantApiUrlInfo(objectHead);
       // pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("merchantId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("merchantApiUrlInfoList["+"0"+"].urlType"),"app");
        Assert.assertEquals(withDrawJson1.getString("merchantApiUrlInfoList["+"0"+"].url"),"https://trymanaged.website.com/fdsfds");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify Successfull response of Merchant Api Url Info Api When alipayid is passed")
    void verifyMerchantApiUrlInfoApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Api_UrlInfo(alipayId,"alipay");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantApiUrlInfo(objectHead);
       // pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("merchantId"),alipayId);
        Assert.assertEquals(withDrawJson1.getString("merchantApiUrlInfoList["+"0"+"].urlType"),"app");
        Assert.assertEquals(withDrawJson1.getString("merchantApiUrlInfoList["+"0"+"].url"),"https://trymanaged.website.com/fdsfds");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(alipayId);

    }
}
