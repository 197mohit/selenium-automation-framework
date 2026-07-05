package scripts.api.mappingService.AuditL3Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetBanksdetailslistfromidsV1 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromidsV1 API response")
    void verifyGetBanksdetailslistfromidsV1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Banksdetailslistfromids_V1("8565821,8565601,8565803");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromidsV1 API BankIds")
    void verifyGetBanksdetailslistfromidsV1_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Banksdetailslistfromids_V1("8565821,8565601,8565803");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankIds"), "8565821,8565601,8565803");
    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromidsV1 API's bankMasterDetailsList[0]")
    void verifyGetBanksdetailslistfromidsV1_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Banksdetailslistfromids_V1("8565821,8565601,8565803");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankId"), "8565601");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankCode"), "YES");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].extIfscCode"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankDisplayName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankKey"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].oldpgBankCode"), "YESBC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankWebLogo"), "yes.png");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankWapLogo"), "yes.png");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].status"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankMandate"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].standardBankCode"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].mandateDebitCard"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].payMode"), "NB");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].displayOrder"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].extendedInfo"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"0"+"].bankShortName"), null);

    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromidsV1 API's bankMasterDetailsList[1]")
    void verifyGetBanksdetailslistfromidsV1_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Banksdetailslistfromids_V1("8565821,8565601,8565803");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankId"), "8565821");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankName"), "Paytm Payments Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankCode"), "PPBL");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].extIfscCode"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankDisplayName"), "Paytm Payments Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankKey"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].oldpgBankCode"), "PPBLC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankWebLogo"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankWapLogo"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].status"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankMandate"), "EMANDATE");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].standardBankCode"), "PYTM");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].mandateDebitCard"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].payMode"), "NB");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].displayOrder"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].extendedInfo"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"1"+"].bankShortName"), "PPBL");

    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromidsV1 API'S notFound")
    void verifyGetBanksdetailslistfromidsV1_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Banksdetailslistfromids_V1("8565821,8565601,8565803");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("notFound"), "[8565803]");
    }
}
