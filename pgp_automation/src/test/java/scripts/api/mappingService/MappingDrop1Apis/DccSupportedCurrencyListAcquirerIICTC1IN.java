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

public class DccSupportedCurrencyListAcquirerIICTC1IN extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of DccSupportedCurrencyListAcquirerIICTC1IN API ")
    void verifyGetPspSchema_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Dcc_Supported_CurrencyList_Acquirer_IICTC1IN();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDccSupportedCurrencyListAcquirerIICTC1IN(objectHead);
        Assert.assertEquals(withDrawJson1.getString("acquirer"), "IICTC1IN");
        Assert.assertNotNull(withDrawJson1.getString("currencies"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify currencies of DccSupportedCurrencyListAcquirerIICTC1IN API ")
    void verifyGetPspSchema_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Dcc_Supported_CurrencyList_Acquirer_IICTC1IN();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyDccSupportedCurrencyListAcquirerIICTC1IN(objectHead);
        int s= withDrawJson1.getList("currencies").size();
        if(s==71){
            Assert.fail("currencies Size should be 71");
        }
    }
}
