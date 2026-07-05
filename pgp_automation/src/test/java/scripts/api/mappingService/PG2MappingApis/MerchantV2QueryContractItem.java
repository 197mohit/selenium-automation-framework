package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class MerchantV2QueryContractItem extends PGPBaseTest {

    @Test(description = "Verify Successfull response of Merchant  V2 Query Contract Item api response")
    void MerchantV2QueryContractItem_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_V2_Query_Contract();
        mappingApisPG2.buildMerchantV2QueryContractItemRequest(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"EFFECTIVE","51051000100000000001","1","1");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();

        Assert.assertEquals(withDrawJson1.getString("restStatus"),"SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());

    }
    @Test(description = "Verify resultResp Object in response of Merchant  V2 Query Contract Item api response")
    void MerchantV2QueryContractItem_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_V2_Query_Contract();
        mappingApisPG2.buildMerchantV2QueryContractItemRequest(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"EFFECTIVE","51051000100000000001","1","1");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();

        Assert.assertEquals(withDrawJson1.getString("resultResp.resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultResp.resultInfo.resultMsg"),"SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("resultResp.resultInfo.resultCode"),"SUCCESS");
        Assert.assertNotNull(withDrawJson1.getString("resultResp"));
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());

    }
    @Test(description = "Verify contractBasics  Is Not Null in response of Merchant  V2 Query Contract Item api response")
    void MerchantV2QueryContractItem_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_V2_Query_Contract();
        mappingApisPG2.buildMerchantV2QueryContractItemRequest(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"EFFECTIVE","51051000100000000001","1","1");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        List<JSONObject> contractBasicsList=withDrawJson1.getList("resultResp.contractBasics");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Assert.assertEquals(withDrawJson1.getString("resultResp.resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultResp.resultInfo.resultMsg"),"SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("resultResp.resultInfo.resultCode"),"SUCCESS");
        Assert.assertNotNull(withDrawJson1.getString("resultResp"));
        Assert.assertNotNull(withDrawJson1.getString("resultResp.contractBasics"));
        pg2MappingApisHelper.verifyContractBasics(contractBasicsList,withDrawJson1);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());

    }
}
