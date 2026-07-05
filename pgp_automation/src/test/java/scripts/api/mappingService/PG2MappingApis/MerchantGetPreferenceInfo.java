package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantGetPreferenceInfo extends PGPBaseTest {

    @Test(description = "Verify MerchantGetPreferenceInfo Api response ")
    void verifyMerchantGetPreferenceInfoAPI() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_get_preference_info(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantPreferenceInfo(objectHead);
        //pg2MappingApisHelper.verifyPG2Routes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("merchantId"), Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertNotNull(withDrawJson1.getString("merchantPreferenceInfos"));
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
    }
}
