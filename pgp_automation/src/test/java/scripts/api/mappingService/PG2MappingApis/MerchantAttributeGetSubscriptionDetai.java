package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantAttributeGetSubscriptionDetai extends PGPBaseTest {
    String IdType = "paytm";
    String subscriptionMaxLimit="5000";
    String subscriptionCountLimit="5";
    @Test(description="verify parameter Response of GetSubscriptionDetail Key Api with paymode")
    void verifyMerchantAttributeGetSubscriptionDetaiapi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_subscription_detail(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), IdType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeGetSubscriptionDetail(objectHead);
        Assert.assertEquals(withDrawJson1.getString("subscriptionMaxLimit"), subscriptionMaxLimit);
        Assert.assertEquals(withDrawJson1.getString("subscriptionCountLimit"), subscriptionCountLimit);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    @Test(description="verify parameter Response of GetSubscriptionDetail Key Api with paymode")
    void verifyMerchantAttributeGetSubscriptionDetaiapi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_subscription_detail(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), IdType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeGetSubscriptionDetail(objectHead);
        Assert.assertEquals(withDrawJson1.getString("subscriptionOnDemandFlag"), "false");
        Assert.assertEquals(withDrawJson1.getString("subscriptionCreationCallBack"), "https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
