package scripts.api.mappingService.AuditL3Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetV1Bankmasterdetails extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetV1Bankmasterdetails API response")
    void verifyGetV1Bankmasterdetails_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_V1_Bankmasterdetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetV1Bankmasterdetails(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "SUCCESS");
    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetV1Bankmasterdetails API bankMasterDetailsList")
    void verifyGetV1Bankmasterdetails_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_V1_Bankmasterdetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetV1Bankmasterdetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("bankMasterDetailsList"), "LIST");
    }
}
