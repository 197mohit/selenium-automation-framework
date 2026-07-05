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

public class Getformatter extends PGPBaseTest {

    String Bankcode1 = "ICICI";
    String Bankcode2 = "SBI";
    String paymethod1 = "NB";
    String paymethod2 = "CC";

    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of Getformatterdetails API with bankcode ICICI and paymethod NB")
    void VerifyGet_Formatter_Details1() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Formatter_Details(Bankcode1, paymethod1);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetFormatterDetails(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "1");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "ICICI");
        Assert.assertEquals(withDrawJson1.getString("payMethod"), "NB");
        Assert.assertEquals(withDrawJson1.getString("formatterName"), "ICICINetBankingFormatterImpl");
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertEquals(withDrawJson1.getString("params"), "sqet=15;");

    }

    @Owner("Vikash verma")
    @Feature("PG2-12569")
    @Test(description = "Verify Response of Getformatterdetails API with bankcode SBI and paymethod CC")
    void VerifyGet_Formatter_Details2() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Formatter_Details(Bankcode2, paymethod2);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetFormatterDetails(objectHead);
        Assert.assertEquals(withDrawJson1.getString("id"), "6");
        Assert.assertEquals(withDrawJson1.getString("bankCode"), "SBI");
        Assert.assertEquals(withDrawJson1.getString("payMethod"), "CC");
        Assert.assertEquals(withDrawJson1.getString("formatterName"), "HDFCFormatterImpl");
        Assert.assertEquals(withDrawJson1.getString("status"), "true");
        Assert.assertEquals(withDrawJson1.getString("params"), "sqet=15;XID=CdCzZGjEKfLvQ5E4SU27DSC918k=;CAVV=CAACB3mHZ4IggwVGFYdnAAAAAAA=;refund.retry.enabled=n;HANDLE_MULTIPLE_CALLBACKS=true;threeDSTwoFormatter=HDFC3DS2ServiceImpl;");
    }
}
