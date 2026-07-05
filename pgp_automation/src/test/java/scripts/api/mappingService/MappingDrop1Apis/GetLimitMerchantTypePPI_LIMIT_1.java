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

public class GetLimitMerchantTypePPI_LIMIT_1 extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of GetLimitMerchantTypePPI_LIMIT_1 API ")
    void verifyGetLimitMerchantTypePPI_LIMIT_1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Limit_Merchant_Type_PPI_LIMIT_1();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetLimitMerchantTypePPI_LIMIT_1(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("merchantLimits"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify merchantLimits of GetLimitMerchantTypePPI_LIMIT_1 API ")
    void verifyGetLimitMerchantTypePPI_LIMIT_1_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Limit_Merchant_Type_PPI_LIMIT_1();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetLimitMerchantTypePPI_LIMIT_1(objectHead);
        int s= withDrawJson1.getList("merchantLimits").size();
        if(s==0){
            Assert.fail("merchantLimits is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("merchantLimits["+ 0 +"].payMode"), "CREDIT_CARD");
        Assert.assertEquals(withDrawJson1.getString("merchantLimits["+ 0 +"].limit"), "2000");
        Assert.assertEquals(withDrawJson1.getString("merchantLimits["+ 0 +"].msg"), "Please ensure that you are paying to the genuine merchant. Never make any advance payments to any non-trusted merchant who provides links/QR through WhatsApp, Facebook, Instagram, Ads or SMS/calls for any offers or prizes as it may be a scam.");
        Assert.assertEquals(withDrawJson1.getString("merchantLimits["+ 0 +"].showPopUp"), "true");
    }
}
