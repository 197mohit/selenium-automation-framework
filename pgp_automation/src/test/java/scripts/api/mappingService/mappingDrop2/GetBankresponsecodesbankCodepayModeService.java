package scripts.api.mappingService.mappingDrop2;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetBankresponsecodesbankCodepayModeService extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify GetBankresponsecodesbankCodepayModeService API result ")
    void GetBankresponsecodesbankCodepayModeService_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankresponsecodes_bankCodepay_ModeService("HDFC", "CC", "PAYMENT");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetBankresponsecodesbankCodepayModeService(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "HDFC");
        Assert.assertEquals(withDrawJson1.getString("payMode"), "CC");
        Assert.assertEquals(withDrawJson1.getString("service"), "PAYMENT");
        Assert.assertNotNull(withDrawJson1.getString("bankResponseCodes"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify response of bankResponseCodes J in GetBankresponsecodesbankCodepayModeService API  ")
    void GetBankresponsecodesbankCodepayModeService_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankresponsecodes_bankCodepay_ModeService("HDFC", "CC", "PAYMENT");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("bankResponseCodes.J");
        pg2MappingApisHelper.VerifyBankresponsecodesSchema(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.id"), "27");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.bankCode"), "HDFC");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.payMode"), "CC");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.service"), "PAYMENT");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.bankResponseCode"), "J");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.paytmResponseCode"), "879");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.bankMessage"), "Risk denied");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.platformResponseCode"), "FGW_DENIED_BY_RISK");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.createdOn"), "1699513408000");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.updatedOn"), "1699513408000");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.maxRetryCount"), "0");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.retryDelay"), "0");
        Assert.assertNotNull(withDrawJson1.getString("bankResponseCodes.J.retryDetails"));
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.J.retriable"), "false");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify response of bankResponseCodes NOT_CAPTURED in GetBankresponsecodesbankCodepayModeService API  ")
    void GetBankresponsecodesbankCodepayModeService_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankresponsecodes_bankCodepay_ModeService("HDFC", "CC", "PAYMENT");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("bankResponseCodes.NOT_CAPTURED");
        pg2MappingApisHelper.VerifyBankresponsecodesSchema(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.id"), "1");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.bankCode"), "HDFC");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.payMode"), "CC");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.service"), "PAYMENT");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.bankResponseCode"), "NOT_CAPTURED");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.paytmResponseCode"), "228");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.bankMessage"), "NOT_CAPTURED");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.platformResponseCode"), "23013225");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.createdOn"), "1699513408000");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.updatedOn"), "1699513408000");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.maxRetryCount"), "0");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.retryDelay"), "0");
        Assert.assertNotNull(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.retryDetails"));
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.NOT_CAPTURED.retriable"), "false");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45653")
    @Test(description = "Verify response of bankResponseCodes GW00852 in GetBankresponsecodesbankCodepayModeService API  ")
    void GetBankresponsecodesbankCodepayModeService_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Bankresponsecodes_bankCodepay_ModeService("HDFC", "CC", "PAYMENT");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("bankResponseCodes.GW00852");
        pg2MappingApisHelper.VerifyBankresponsecodesSchema(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.id"), "24");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.bankCode"), "HDFC");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.payMode"), "CC");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.service"), "PAYMENT");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.bankResponseCode"), "GW00852");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.paytmResponseCode"), "296");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.bankMessage"), "3d failed");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.platformResponseCode"), "FGW_BANK_FORM_RETRIEVE_FAILED");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.createdOn"), "1699513408000");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.updatedOn"), "1699513408000");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.maxRetryCount"), "0");
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.retryDelay"), "0");
        Assert.assertNotNull(withDrawJson1.getString("bankResponseCodes.GW00852.retryDetails"));
        Assert.assertEquals(withDrawJson1.getString("bankResponseCodes.GW00852.retriable"), "false");
    }
}
