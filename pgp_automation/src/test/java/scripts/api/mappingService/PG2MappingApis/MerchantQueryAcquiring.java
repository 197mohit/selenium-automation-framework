package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantQueryAcquiring extends PGPBaseTest {
    String Paymethod = "NET_BANKING";

    @Test(description = "Verify MerchantQueryAcquiring API paytmResultInfo result ")
    void verifyMerchantQueryAcquiringApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_query_acquiring(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), Paymethod);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantQueryAcquiring(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    @Test(description = "Verify MerchantQueryAcquiring API response result ")
    void verifyMerchantQueryAcquiringApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_query_acquiring(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), Paymethod);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantQueryAcquiring(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCodeId"), "00000000");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCode"), "SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultMsg"), "success");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.retryable"), "false");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    @Test(description = "Verify MerchantQueryAcquiring API acquiringConfigInfos response ")
    void verifyMerchantQueryAcquiringApi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_query_acquiring(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), Paymethod);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantQueryAcquiring(objectHead);
        int s=withDrawJson1.getList("response.acquiringConfigInfos").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].recordId"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].mcc"));
            Assert.assertEquals(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].payMethod"), Paymethod);
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].serviceInstId"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].serviceInstName"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].merchantId"));
        }
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
