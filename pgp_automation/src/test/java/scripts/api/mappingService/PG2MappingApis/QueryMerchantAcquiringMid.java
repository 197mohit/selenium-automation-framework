package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class QueryMerchantAcquiringMid extends PGPBaseTest {

    @Test(description = "Verify QueryMerchantAcquiringMid API paytmResultInfo result ")
    void verifyQueryMerchantAcquiringMidApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring_mid(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantAcquiring(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    @Test(description = "Verify QueryMerchantAcquiringMid API response result ")
    void verifyQueryMerchantAcquiringMidApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring_mid(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantAcquiring(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCodeId"), "00000000");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultCode"), "SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.resultMsg"), "success");
        Assert.assertEquals(withDrawJson1.getString("response.resultInfo.retryable"), "false");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }

    @Test(description = "Verify QueryMerchantAcquiringMid API acquiringConfigInfos response ")
    void verifyQueryMerchantAcquiringMidApi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring_mid(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantAcquiring(objectHead);
        int s=withDrawJson1.getList("response.acquiringConfigInfos").size();
        int sizeInDB=pg2MappingApisHelper.fetchAcquiringConfigInfosSize("678703827");
        Assert.assertEquals(sizeInDB,s);
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].mcc"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].payMethod"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].serviceInstId"));
            Assert.assertNotNull(withDrawJson1.getString("response.acquiringConfigInfos["+ i +"].merchantId"));
        }
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify QueryMerchantAcquiringMid API Logs response for Wrong Mid ")
    void verifyQueryMerchantAcquiringMidApi_04() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring_mid("ABCD");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
//        pg2MappingApisHelper.verifyWrongMidResponseInLogs();
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertNull(withDrawJson1.getString("response"));

    }
    @Test(description = "Verify QueryMerchantAcquiringMid API Logs response When Mid is Wrong and P+ fallback ff4j flag is disabled ")
    void verifyQueryMerchantAcquiringMidApi_05() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_acquiring_mid("ABCD");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Entry is not available ");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"F");

    }
}
