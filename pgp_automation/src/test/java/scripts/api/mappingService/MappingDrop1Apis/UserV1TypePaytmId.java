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

public class UserV1TypePaytmId extends PGPBaseTest {

    String TYPE1= "PAYTM";
    String TYPE2= "OLDPG";
    String PaytmId= "1000200135";

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of paytmResultInfo in UserV1TypePaytmId API with TYPE PAYTM & PaytmId 1000200135")
    void verifyUserV1TypePaytmId_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_V1_Type_PaytmId(TYPE1, PaytmId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserV1TypePaytmId(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response in UserV1TypePaytmId API with TYPE PAYTM & PaytmId 1000200135")
    void verifyUserV1TypePaytmId_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_V1_Type_PaytmId(TYPE1, PaytmId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserV1TypePaytmId(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.paytmId"), "1000200135");
        Assert.assertEquals(withDrawJson1.getString("response.alipayId"), "216810000000889540970");
        Assert.assertEquals(withDrawJson1.getString("response.paytmAccountId"), "1000200135");
        Assert.assertNull(withDrawJson1.getString("response.alipayAccountId"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of paytmResultInfo in UserV1TypePaytmId API with TYPE OLDPG & PaytmId 1000200135")
    void verifyUserV1TypePaytmId_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_V1_Type_PaytmId(TYPE1, PaytmId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserV1TypePaytmId(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of UserV1TypePaytmId API with TYPE OLDPG & PaytmId 1000200135")
    void verifyUserV1TypePaytmId_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_V1_Type_PaytmId(TYPE2, PaytmId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserV1TypePaytmId(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.paytmId"), "1000200135");
        Assert.assertEquals(withDrawJson1.getString("response.alipayId"), "216810000000889540970");
        Assert.assertEquals(withDrawJson1.getString("response.paytmAccountId"), "1000200135");
        Assert.assertNull(withDrawJson1.getString("response.alipayAccountId"));
    }


}
