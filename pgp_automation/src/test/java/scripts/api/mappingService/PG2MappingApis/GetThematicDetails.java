package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetThematicDetails extends PGPBaseTest {
    @Test(description = "Verify Successfull response of Thematic Detail Api")
    void Successfull_Response_of_Thematic_Detail() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Thematic_Details(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantThematicDetail(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("restStatus"),"SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("resultResp.childMid"),Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("resultResp.childThematicPreference"),null);
        Assert.assertEquals(withDrawJson1.getString("resultResp.resellerMid"),null);
        Assert.assertEquals(withDrawJson1.getString("resultResp.resellerThematicPreference"),null);
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
    }
}
