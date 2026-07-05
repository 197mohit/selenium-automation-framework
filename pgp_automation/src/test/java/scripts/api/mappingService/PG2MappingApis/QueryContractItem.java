package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class QueryContractItem extends PGPBaseTest {

    @Test(description = "Verify Successfull response of Query Contract Item Api")
    void verifySuccessfullResponseOfQueryContractItemApi() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_query_contract_item(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"EFFECTIVE","1","10");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        Map<String,String> objectHead2= withDrawJson1.getMap("response");
        pg2MappingApisHelper.verifyQueryContractItem(objectHead,objectHead2);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");

    }
    @Test(description = "Verify totalPage parameter in response of Query Contract Api response")
    void QueryContractApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_query_contract_item(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"EFFECTIVE","1","10");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        Map<String,String> objectHead2= withDrawJson1.getMap("response");
        pg2MappingApisHelper.verifyQueryContractItem(objectHead,objectHead2);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");
    }
    @Test(description = "Verify Successfull contractBasics detail")
    void QueryContractApi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_query_contract_item(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"EFFECTIVE","1","10");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultMsg"),"SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCode"),"SUCCESS");
        pg2MappingApisHelper.verifyContractBasicsDetailsResponse(withDrawJson1);
    }

    @Test(description = "Verify Successfull response of Query Contract Item Api")
    void QueryMerchantContractItem_04() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_contract_item(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"EFFECTIVE","1","10");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");

    }
    @Test(description = "Verify totalPage parameter is present in Query Contract Api response")
    void QueryContractApi_05() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_contract_item(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"EFFECTIVE","1","10");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        Map<String,String> objectHead2= withDrawJson1.getMap("response");
        pg2MappingApisHelper.verifyQueryContractItem(objectHead,objectHead2);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");
    }
    @Test(description = "Verify Successfull contractBasics detail")
    void QueryContractApi_06() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_contract_item(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"EFFECTIVE","1","10");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultMsg"),"SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCode"),"SUCCESS");
        pg2MappingApisHelper.verifyContractBasicsDetailsResponse(withDrawJson1);
    }

}
