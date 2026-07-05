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

public class Fetchentityignoreparams extends PGPBaseTest {

    String Entityid1 = "8737487";
    String Entityid2 = "8737489";

    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of Fetchentityignoreparamas API with entityid 8737487 ")
    void VerifyFetchentityignoreparams1() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Fetchentityignoreparamas(Entityid1);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifyfetchentityignoreparams(objectHead);
        int s= withDrawJson1.getList("paramsList").size();
        if(s==0){
            Assert.fail("Get param list is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 0 +"].entityId"), "8737487");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 0 +"].fieldName"), "THEME");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 1 +"].entityId"), "8737487");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 1 +"].fieldName"), "ORDER_DETAILS");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 2 +"].entityId"), "8737487");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 2 +"].fieldName"), "PAYMENT_MODE_ONLY");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 3 +"].entityId"), "8737487");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 3 +"].fieldName"), "TOKEN_TYPE");
        Assert.assertEquals(withDrawJson1.getString("successfullyProcessed"), "true");
        Assert.assertEquals(withDrawJson1.getString("entityId"), "8737487");


    }
    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of Fetchentityignoreparamas API with entityid 8737487 ")
    void VerifyFetchentityignoreparams2() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Fetchentityignoreparamas(Entityid2);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifyfetchentityignoreparams(objectHead);
        int s= withDrawJson1.getList("paramsList").size();
        if(s==0){
            Assert.fail("Get param list is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 0 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 0 +"].fieldName"), "TOKEN_TYPE");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 1 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 2 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 3 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 4 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 5 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 6 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 7 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 8 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 9 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("paramsList["+ 10 +"].entityId"), "8737489");
        Assert.assertEquals(withDrawJson1.getString("successfullyProcessed"), "true");
        Assert.assertEquals(withDrawJson1.getString("entityId"), "8737489");
    }
}
