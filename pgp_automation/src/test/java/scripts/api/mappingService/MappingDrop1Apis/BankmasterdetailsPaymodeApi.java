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

public class BankmasterdetailsPaymodeApi extends PGPBaseTest {

    String Paymode = "NB";

    @Owner("Anushka")
    @Feature("PGP-45207")
    @Test(description = "Verify Bankmasterdetails Paymode API result ")
    void verifyBankmasterdetailsPaymodeApi_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Bankmasterdetails_Paymode_Api(Paymode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyBankmasterdetailsPaymode(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"SUCCESS");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000000");
    }

    @Owner("Anushka")
    @Feature("PGP-45207")
    @Test(description = "Verify BankMasterDetailsList in Bankmasterdetails Paymode API result ")
    void verifyBankmasterdetailsPaymodeApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Bankmasterdetails_Paymode_Api(Paymode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyBankmasterdetailsPaymode(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("bankMasterDetailsList"));
        pg2MappingApisHelper.verifyBankMasterDetailsList(withDrawJson1);
    }

    @Owner("Anushka")
    @Feature("PGP-45207")
    @Test(description = "Verify BankMasterDetailsList Parameters in Bankmasterdetails Paymode API result ")
    void verifyBankmasterdetailsPaymodeApi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Bankmasterdetails_Paymode_Api(Paymode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyBankmasterdetailsPaymode(objectHead);
        int s= withDrawJson1.getList("bankMasterDetailsList").size();
        if(s==0){
            Assert.fail("BankMasterDetailsList is Empty");
        }
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankId"), "8565576");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankName"), "ALH");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankCode"), "ALH");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].extIfscCode"), "ALLA");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankDisplayName"), "Allahabad Bank");
        Assert.assertNull(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankKey"));
        Assert.assertNull(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].alipayBankCode"));
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankWebLogo"), "allahabadbank.gif");
        Assert.assertNull(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankWapLogo"));
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].status"), "true");
        Assert.assertNull(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankMandate"));
        Assert.assertNull(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].standardBankCode"));
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].mandateNetBanking"), "false");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].mandateDebitCard"), "false");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].payMode"), Paymode);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].displayOrder"), "27");
        Assert.assertNull(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].extendedInfo"));
        Assert.assertNull(withDrawJson1.getString("bankMasterDetailsList["+ 0 +"].bankShortName"));

    }

}
