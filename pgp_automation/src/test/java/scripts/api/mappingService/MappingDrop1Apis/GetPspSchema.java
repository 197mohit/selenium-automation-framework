package scripts.api.mappingService.MappingDrop1Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetPspSchema extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of GetPspSchema API ")
    void verifyGetPspSchema_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_PspSchema();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetPspSchema(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("response"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify resultInfo of GetPspSchema API ")
    void verifyGetPspSchema_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_PspSchema();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetPspSchema(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify pspSchemas of GetPspSchema API ")
    void verifyGetPspSchema_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_PspSchema();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetPspSchema(objectHead);
        int s= withDrawJson1.getList("response.pspSchemas").size();
        if(s==0){
            Assert.fail("pspSchemas is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 0 +"].displayName"), "PhonePe");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 0 +"].name"), "PhonePe");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 0 +"].icon"), "phonepe.png");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 0 +"].scheme"), "phonepe");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 1 +"].displayName"), "Paytm");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 1 +"].name"), "paytm payments bank");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 1 +"].icon"), "paytm.png");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 1 +"].scheme"), "paytmmp");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 2 +"].displayName"), "Google Pay");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 2 +"].name"), "google pay");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 2 +"].icon"), "gpay.png");
        Assert.assertEquals(withDrawJson1.getString("response.pspSchemas["+ 2 +"].scheme"), "gpay");
    }
}
