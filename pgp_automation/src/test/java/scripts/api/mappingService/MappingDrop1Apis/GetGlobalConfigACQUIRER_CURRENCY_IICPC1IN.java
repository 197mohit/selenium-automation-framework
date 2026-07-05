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

public class GetGlobalConfigACQUIRER_CURRENCY_IICPC1IN extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of GetGlobalConfigACQUIRER_CURRENCY_IICPC1IN API by set the value of ff4j property MerchantCenterTrafficRoutingPercentageForGlobalConfig to 100 for get the data from Merchant center")
    void verifyGetGlobalConfigACQUIRER_CURRENCY_IICPC1IN_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Global_Config_ACQUIRER_CURRENCY_IICPC1IN();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetGlobalConfigACQUIRER_CURRENCY_IICPC1IN(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.key"), "ACQUIRER_CURRENCY_IICPC1IN");
        Assert.assertEquals(withDrawJson1.getString("response.value"), "{\"currencies\": [\"AED\",\"AFN\",\"ARS\",\"AUD\",\"AZN\",\"BBD\",\"BDT\",\"BGN\",\"BHD\",\"BMD\",\"BND\",\"BRL\",\"BWP\",\"BZD\",\"CAD\",\"CHF\",\"CLP\",\"CNY\",\"COP\",\"CRC\",\"CZK\",\"DKK\",\"DOP\",\"EGP\",\"EUR\",\"GBP\",\"GTQ\",\"HKD\",\"HNL\",\"HRK\",\"HUF\",\"ILS\",\"JMD\",\"JOD\",\"JPY\",\"KES\",\"KRW\",\"KWD\",\"KZT\",\"LBP\",\"LKR\",\"LTL\",\"LVL\",\"MOP\",\"MUR\",\"MXN\",\"MYR\",\"NGN\",\"NOK\",\"NZD\",\"OMR\",\"PEN\",\"PHP\",\"PKR\",\"PLN\",\"QAR\",\"RON\",\"RUB\",\"SAR\",\"SCR\",\"SEK\",\"SGD\",\"THB\",\"TRY\",\"TTD\",\"TWD\",\"TZS\",\"UAH\",\"USD\",\"XCD\",\"XOF\",\"ZAR\"]}");

    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify resultinfo of GetGlobalConfigACQUIRER_CURRENCY_IICPC1IN API by set the value of ff4j property MerchantCenterTrafficRoutingPercentageForGlobalConfig to 100 for get the data from Merchant center ")
    void verifyGetGlobalConfigACQUIRER_CURRENCY_IICPC1IN_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Global_Config_ACQUIRER_CURRENCY_IICPC1IN();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetGlobalConfigACQUIRER_CURRENCY_IICPC1IN(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }
}
