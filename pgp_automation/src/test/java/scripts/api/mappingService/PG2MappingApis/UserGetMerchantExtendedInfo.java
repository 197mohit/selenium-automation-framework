package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class UserGetMerchantExtendedInfo extends PGPBaseTest {

    @Test(description = "Verify UserGetMerchantExtendedInfo Api resultInfo When userId is 1107233579 ")
    void verifyUserGetMerchantExtendedInfoApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_getMerchantExtendedInfo("11065275");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        Map<String,String> objectHead1= withDrawJson1.getMap("extendedInfo");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyUserGetMerchantExtendedInfo(objectHead,objectHead1);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("merchantId"), "MerjSI86612930389949");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes("dolcee32096288141476");
    }

    @Test(description = "Verify UserGetMerchantExtendedInfo Api extendedInfo response When userId is 1107233579 ")
    void verifyUserGetMerchantExtendedInfoApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_getMerchantExtendedInfo("11065275");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        Map<String,String> objectHead2= withDrawJson1.getMap("extendedInfo");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyUserGetMerchantExtendedInfo(objectHead,objectHead2);
        String numberOfRetryInDB= pg2MappingApisHelper.fetchRetryCountValueFromDB("MerjSI86612930389949");
        String numberOfRetry =withDrawJson1.getString("extendedInfo.numberOfRetry");
//        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.entityId"));
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.status"), "ACTIVE");
        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.keySize"));
        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.numberOfRetry"));
        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.entityKey"));
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes("dolcee32096288141476");
    }
}
