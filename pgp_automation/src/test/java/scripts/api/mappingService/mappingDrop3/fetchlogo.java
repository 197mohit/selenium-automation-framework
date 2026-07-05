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

public class fetchlogo extends PGPBaseTest {

    String Logotype1 = "ifsc";
    String Logotype2 = "bank";
    String Identifier1 = "nesf0000025";
    String Identifier2 = "hdfc";

    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of fetch logo API with logotype ifsc and Identifier nesf0000025")
    void VerifyFetchlogo1() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Fetch_logo(Logotype1, Identifier1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifyfetchlogo(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultResp.logoUrl"), "https://merchant-static.paytm.com/merchant-dashboard/logos/ifsc/nesf0000025/bank_NESFBL_ifsc.png");

    }

    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of fetch logo API with logotype bank and Identifier hdfc")
    void Verifyfetchlogo2() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Fetch_logo(Logotype2, Identifier2);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifyfetchlogo(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultResp.logoUrl"), "https://merchant-static.paytm.com/merchant-dashboard/logos/default/bank/DefaultBank.png");
    }
}
