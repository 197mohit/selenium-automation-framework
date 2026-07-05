package scripts.api.mappingService.mappingDrop2;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.elasticsearch.common.recycler.Recycler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetEmiOnDcEligibilityBy extends PGPBaseTest {

    String Contact1 = "8006006993";
    String Contact2 = "8826170616";
    String BankName1 = "ICICI";
    String BankName2 = "HDFC";

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Response of GetEmiOnDcEligibilityBy API with contact 8006006993 and BankName ICICI")
    void VerifyGet_Emi_On_Dc_EligibilityBy_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Emi_On_Dc_EligibilityBy(Contact1, BankName1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetEmiOnDcEligibilityBy(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankName"), "ICICI");
        Assert.assertEquals(withDrawJson1.getString("emiOnDcEnable"), "true");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify Response of GetEmiOnDcEligibilityBy API with contact 8826170616 and BankName HDFC")
    void VerifyGet_Emi_On_Dc_EligibilityBy_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Emi_On_Dc_EligibilityBy(Contact2, BankName2);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetEmiOnDcEligibilityBy(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankName"), "HDFC");
        Assert.assertEquals(withDrawJson1.getString("emiOnDcEnable"), "true");
    }
}
