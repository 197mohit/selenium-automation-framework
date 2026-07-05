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

public class UserV3willcreate extends PGPBaseTest {
    String USER = "1107233579";

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify UserV3 API paytmResultInfo with Type PAYTM ")
    void verifyUserV3_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_V3("PAYTM", USER, true);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.UserV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify UserV3 API paytmResultInfo with Type PAYTM ")
    void verifyUserV3_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_V3("PAYTM", USER, true);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.UserV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.paytmId"), "1107233579");
        Assert.assertEquals(withDrawJson1.getString("response.oldpgId"), "216810000000359216868");
        Assert.assertEquals(withDrawJson1.getString("response.paytmAccountId"), "1107233579");
        Assert.assertEquals(withDrawJson1.getString("response.oldpgAccountId"), null);
    }
}
