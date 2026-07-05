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

public class MerchantAgentGetAgentInfoIdType extends PGPBaseTest {

    //works on qa14 only as Qa14 is linked to Merchant center QA4 only which of MC env has data for these cases

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify parentMid & childMid of MerchantAgentGetAgentInfoIdType API with agentinfo test123tests")
    void verifyMerchantAgentGetAgentInfoIdType_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.MerchantAgent_Get_AgentInfo_Id_Type("test123tests", "AGENT_ID");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantAgentGetAgentInfoIdType(objectHead);
        Assert.assertEquals(withDrawJson1.getString("parentMid"), "qa5deL17413987475414");
        Assert.assertEquals(withDrawJson1.getString("childMid"), "testeL17413987475414");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of MerchantAgentGetAgentInfoIdType API with agentinfo test123tests")
    void verifyMerchantAgentGetAgentInfoIdType_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.MerchantAgent_Get_AgentInfo_Id_Type("test123tests", "AGENT_ID");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantAgentGetAgentInfoIdType(objectHead);
        Assert.assertEquals(withDrawJson1.getString("agentId"), "test123tests");
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertNull(withDrawJson1.getString("extendedInfo"));
    }

}
