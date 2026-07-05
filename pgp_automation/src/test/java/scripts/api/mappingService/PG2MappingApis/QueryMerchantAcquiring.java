package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class QueryMerchantAcquiring extends PGPBaseTest {
    String Paymethod = "NB";

    @Test(description = "Verify QueryMerchantAcquiringapi API paytmResultInfo result ")
    void verifyQueryMerchantAcquiringapi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), Paymethod);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantAcquiringPaymode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify QueryMerchantAcquiringapi API response result")
    void verifyQueryMerchantAcquiringapi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), Paymethod);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantAcquiringPaymode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCodeId"), "00000000");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCode"), "SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultMsg"), "success");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.retryable"), "false");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    @Test(description = "Verify QueryMerchantAcquiringapi API acquiringConfigInfos response ")
    void verifyQueryMerchantAcquiringapi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), Paymethod);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantAcquiringPaymode(objectHead);
        int s=withDrawJson1.getList("response.acquiringConfigInfos").size();
        if(s==0){
            Assert.fail("acquiringConfigInfos is Empty");
        }
        for(int i=0;i<s;i++)
        {
            Assert.assertNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].recordId"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].mcc"));
            Assert.assertEquals(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].payMethod"), "NET_BANKING");
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].serviceInstId"));
            Assert.assertNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].serviceInstName"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].merchantId"));
        }
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
