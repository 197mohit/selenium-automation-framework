package scripts.api.mappingService.mappingDrop3;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class Getmerchantresponsecode extends PGPBaseTest {

    String mid1 = Constants.MerchantType.Attribute_key_mid3.getId();
    String Resultcode1= "FGW_DECLINED_BY_REME_BANK";

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "Verify codes of getresponsecodedetails API with mid Prabha51443095243939 and resultcode FGW_DECLINED_BY_REME_BANK")
    void verifyGetMerchantResponsecodedetailsPaytmResponseCode_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_MerchantResponsecodedetails_Resultcode(mid1,Resultcode1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.verifyGetMerchantResponsecodedetailsPaytmResponseCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");

        }

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "Verify codes of getresponsecodedetails API with mid Prabha51443095243939 and resultcode FGW_DECLINED_BY_REME_BANK")
    void verifyGetMerchantResponsecodedetailsPaytmResponseCode_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_MerchantResponsecodedetails_Resultcode(mid1,Resultcode1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.verifyGetMerchantResponsecodedetailsPaytmResponseCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.id"), "1192");
        Assert.assertEquals(withDrawJson1.getString("response.paytmMid"), mid1);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "FGW_DECLINED_BY_REME_BANK");
        Assert.assertEquals(withDrawJson1.getString("response.merchantResponseCode"), "3000");
        Assert.assertEquals(withDrawJson1.getString("response.merchantResponseMessage"), null);
        Assert.assertNotNull(withDrawJson1.getString("response.createdOn"));
        Assert.assertNotNull(withDrawJson1.getString("response.updatedOn"));
    }

}