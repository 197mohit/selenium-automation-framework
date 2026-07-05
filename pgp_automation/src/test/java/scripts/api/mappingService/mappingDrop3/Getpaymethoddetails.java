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

public class Getpaymethoddetails extends PGPBaseTest {
    @Owner("vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify response of Getpaymethoddetails API is not null ")
    void verifypaymethoddetails_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paymethoddetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetPaymethoddetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("response"));
    }

    @Owner("vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify response of Getpaymethoddetails API ")
    void verifypaymethoddetails_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paymethoddetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetPaymethoddetails(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
    }

    @Owner("vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify paymethodDetailsList of Getpaymethoddetails API ")
    void verifypaymethoddetails_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paymethoddetails();
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetPaymethoddetails(objectHead);
        int s= withDrawJson1.getList("payMethodDetailsList").size();
        if(s==0){
            Assert.fail("Get pay method details is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 0 +"].payMethod"), "BALANCE");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 0 +"].payMethodName"), "BALANCE");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 0 +"].type"), "Paytm");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 1 +"].payMethod"), "NET_BANKING");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 1 +"].payMethodName"), "NET_BANKING");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 1 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 2 +"].payMethod"), "CREDIT_CARD");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 2 +"].payMethodName"), "CREDIT_CARD");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 2 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 3 +"].payMethod"), "DEBIT_CARD");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 3 +"].payMethodName"), "DEBIT_CARD");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 3 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 4 +"].payMethod"), "IMPS");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 4 +"].payMethodName"), "IMPS");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 4 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 5 +"].payMethod"), "ATM");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 5 +"].payMethodName"), "ATM");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 5 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 6 +"].payMethod"), "EMI");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 6 +"].payMethodName"), "EMI");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 6 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 7 +"].payMethod"), "UPI");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 7 +"].payMethodName"), "UPI");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 7 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 8 +"].payMethod"), "PAYTM_DIGITAL_CREDIT");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 8 +"].payMethodName"), "PAYTM_DIGITAL_CREDIT");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 8 +"].type"), "Paytm");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 9 +"].payMethod"), "PREPAID_CARD");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 9 +"].payMethodName"), "PREPAID_CARD");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 9 +"].type"), "Paytm");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 10 +"].payMethod"), "PPBL");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 10 +"].payMethodName"), "PPBL");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 10 +"].type"), "Paytm");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 11 +"].payMethod"), "LOYALTY_POINT");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 11 +"].payMethodName"), "LOYALTY_POINT");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 11 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 12 +"].payMethod"), "EMI_DC");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 12 +"].payMethodName"), "EMI_DC");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 12 +"].type"), "PG");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 13 +"].payMethod"), "WALLET");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 13 +"].payMethodName"), "WALLET");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 13 +"].type"), "AOA");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 14 +"].payMethod"), "ADVANCE_DEPOSIT_ACCOUNT");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 14 +"].payMethodName"), "ADVANCE_DEPOSIT_ACCOUNT");
        Assert.assertEquals(withDrawJson1.getString("payMethodDetailsList["+ 14 +"].type"), "Paytm");

    }
}
