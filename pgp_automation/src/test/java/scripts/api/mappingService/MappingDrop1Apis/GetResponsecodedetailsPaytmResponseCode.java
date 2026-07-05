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

public class GetResponsecodedetailsPaytmResponseCode extends PGPBaseTest {

    String PaytmResponseCode1= "227";
    String PaytmResponseCode2= "100270";


    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify codes of getresponsecodedetails API with PaytmResponseCode 227")
    void verifyGetResponsecodedetailsPaytmResponseCode_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Responsecodedetails_PaytmResponseCode(PaytmResponseCode1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.verifyGetResponsecodedetailsPaytmResponseCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "112");
        Assert.assertEquals(withDrawJson1.getString("paytmResponseCode"), PaytmResponseCode1);
        Assert.assertEquals(withDrawJson1.getString("resultCodeId"), "23013224");
        Assert.assertEquals(withDrawJson1.getString("resultCode"), "FGW_NOT_SUFFICIENT_FUNDS");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of getresponsecodedetails API with PaytmResponseCode 227")
    void verifyGetResponsecodedetailsPaytmResponseCode_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Responsecodedetails_PaytmResponseCode(PaytmResponseCode1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.verifyGetResponsecodedetailsPaytmResponseCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultStatus"), "Y");
        Assert.assertEquals(withDrawJson1.getString("remark"), "Txn Failed.");
        Assert.assertEquals(withDrawJson1.getString("displayMessage"), "Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        Assert.assertEquals(withDrawJson1.getString("responseCode"), "227");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify codes of getresponsecodedetails API with PaytmResponseCode 100270")
    void verifyGetResponsecodedetailsPaytmResponseCode_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Responsecodedetails_PaytmResponseCode(PaytmResponseCode2);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.verifyGetResponsecodedetailsPaytmResponseCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "3541");
        Assert.assertEquals(withDrawJson1.getString("paytmResponseCode"), PaytmResponseCode2);
        Assert.assertEquals(withDrawJson1.getString("resultCodeId"), "100270");
        Assert.assertEquals(withDrawJson1.getString("resultCode"), "ECH_0092");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of getresponsecodedetails API with PaytmResponseCode 100270")
    void verifyGetResponsecodedetailsPaytmResponseCode_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Responsecodedetails_PaytmResponseCode(PaytmResponseCode2);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.verifyGetResponsecodedetailsPaytmResponseCode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultStatus"), "F");
        Assert.assertEquals(withDrawJson1.getString("remark"), "Transaction declined by customer bank. Please ask the customer to contact their bank");
        Assert.assertEquals(withDrawJson1.getString("displayMessage"), "Transaction declined by customer bank. Please ask the customer to contact their bank");
        Assert.assertEquals(withDrawJson1.getString("responseCode"), "100270");
    }
}
