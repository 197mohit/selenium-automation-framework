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

public class Usergetusermid extends PGPBaseTest {

    String Userid1 = "1001616823";
    String Userid2 = "1107233355";


    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of Usergetusermid API with userid 1001616823")
    void VerifyUsergetusermid1() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_get_usermid(Userid1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifyusergetusermid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("mid"), "FINALL78522367408287");
    }

    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of Usergetusermid API with userid 1001616823")
    void VerifyUsergetusermid2() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_get_usermid(Userid2);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifyusergetusermid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("mid"), "azDgUn77994526987351");
    }
}
