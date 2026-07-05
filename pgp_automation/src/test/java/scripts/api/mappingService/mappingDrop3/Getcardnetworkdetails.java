package scripts.api.mappingService.mappingDrop3;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class Getcardnetworkdetails extends PGPBaseTest {
    @Owner("vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify response of Getcardnetworkdetails API is not null ")
    void verifycardnetworkdetails_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_cardnetworkdetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetCardnetworkdetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("response"));
    }

    @Owner("vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify response of Getcardnetworkdetails API ")
    void verifycardnetworkdetails_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_cardnetworkdetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetCardnetworkdetails(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
    }

    @Owner("vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify cardNetworkDetailsList of Getcardnetworkdetails API ")
    void verifycardnetworkdetails_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_cardnetworkdetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetCardnetworkdetails(objectHead);
        int s= withDrawJson1.getList("cardNetworkDetailsList").size();
        if(s==0){
            Assert.fail("Get card network details is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 0 +"].cardNetwork"), "VISA");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 0 +"].logoUrl"), "visa.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 0 +"].displayName"), "VISA");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 1 +"].cardNetwork"), "MASTER");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 1 +"].logoUrl"), "master.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 1 +"].displayName"), "MASTER");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 2 +"].cardNetwork"), "AMEX");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 2 +"].logoUrl"), "amex.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 2 +"].displayName"), "AMEX");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 3 +"].cardNetwork"), "MAESTRO");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 3 +"].logoUrl"), "maestro.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 3 +"].displayName"), "MAESTRO");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 4 +"].cardNetwork"), "DINERS");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 4 +"].logoUrl"), "diners.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 4 +"].displayName"), "DINERS");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 5 +"].cardNetwork"), "RUPAY");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 5 +"].logoUrl"), "rupay.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 5 +"].displayName"), "RUPAY");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 6 +"].cardNetwork"), "DISCOVER");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 6 +"].logoUrl"), "discover.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 6 +"].displayName"), "DISCOVER");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 7 +"].cardNetwork"), "BAJAJ");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 7 +"].logoUrl"), "bajaj.png");
        Assert.assertEquals(withDrawJson1.getString("cardNetworkDetailsList["+ 7 +"].displayName"), "BAJAJ");
    }
}
