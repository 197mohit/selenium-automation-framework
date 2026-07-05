package scripts.api.mappingService.Auditapichanges;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.utils.merchant.merchant.util.annotations.AMerchant;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class bankdetailsfromidoldandnew extends PGPBaseTest {

    String Userid= "8565601";

    @Owner("vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify response of GetBankdetailsuserid new v1 API")
    void verifyGetBankdetailsuseridNewv1Api1() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_Userid_New_V1(Userid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsUserIdONewv1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankId"), "8565601");
        Assert.assertEquals(withDrawJson1.getString("bankName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "YES");
        Assert.assertEquals(withDrawJson1.getString("extIfscCode"), null);
        Assert.assertEquals(withDrawJson1.getString("bankDisplayName"), "Yes Bank");
        Assert.assertNull(withDrawJson1.getString("bankKey"));
        Assert.assertEquals(withDrawJson1.getString("oldpgBankCode"), "YESBC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankWebLogo"), "yes.png");
    }

    @Owner("vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify parameters of response in GetBankdetailsuserid new v1 API ")
    void verifyGetBankdetailsuseridNewv1Api2(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankdetails_Userid_New_V1(Userid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankdetailsUserIdONewv1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankWapLogo"), "yes.png");
        Assert.assertEquals(withDrawJson1.getString("status"), "false");
        Assert.assertNull(withDrawJson1.getString("bankMandate"));
        Assert.assertNull(withDrawJson1.getString("standardBankCode"));
        Assert.assertEquals(withDrawJson1.getString("mandateNetBanking"), "false");
        Assert.assertEquals(withDrawJson1.getString("mandateDebitCard"), "false");
        Assert.assertNull(withDrawJson1.getString("paymode"));
        Assert.assertNull(withDrawJson1.getString("displayOrder"));
        Assert.assertNull(withDrawJson1.getString("extendedInfo"));
        Assert.assertNull(withDrawJson1.getString("bankShortName"));
    }




    public static HashMap<String, String> StoreNewApiResponse() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        String Userid= "8565601";
        mappingApisPG2.Get_Bankdetails_Userid_New_V1(Userid);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.VerifyGetBankdetailsUserIdONewv1(objectHead);

        HashMap<String, String>NewAPIResponse=new HashMap<>();
        Iterator<String> keys = objectHead.keySet().iterator();
        System.out.println("New Api Response " + objectHead);
        while (keys.hasNext()) {
            String key = keys.next();
            String value= withDrawJson1.getString(key);
            NewAPIResponse.put(key, value);
        }
        return NewAPIResponse;
    }


}
