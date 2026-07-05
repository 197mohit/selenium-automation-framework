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

public class GetBanksdetailslistfromCodesV1 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromCodesV1 API response")
    void verifyGetBanksdetailslistfromCodesV1_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksdetailslistfromCodes_V1("TNMB,PPBL,YES,CANARA,GOLDEN");
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
    void verifyGetBanksdetailslistfromCodesV1_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksdetailslistfromCodes_V1("TNMB,PPBL,YES,CANARA,GOLDEN");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankIds"), "8565571,8565821,8565597,8565601");
    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromidsV1 API's bankMasterDetailsList[0]")
    void verifyGetBanksdetailslistfromCodesV1_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksdetailslistfromCodes_V1("TNMB,PPBL,YES,CANARA,GOLDEN");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankId"), "8565601");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankCode"), "YES");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].extIfscCode"), "YESB");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankDisplayName"), "Yes Bank");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankKey"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].oldpgBankCode"), "YESBC1IN");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankWebLogo"), "yes.png");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankWapLogo"), "yes.png");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].status"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankMandate"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].standardBankCode"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].mandateNetBanking"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].mandateDebitCard"), "true");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].payMode"), "NB");
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].displayOrder"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].extendedInfo"), null);
        Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+"3"+"].bankShortName"), null);

    }

    @Owner("Anushka")
    @Feature("PG2-12160")
    @Test(description = "Verify GetBanksdetailslistfromidsV1 API's bankMasterDetailsList[1]")
    void verifyGetBanksdetailslistfromCodesV1_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksdetailslistfromCodes_V1("TNMB,PPBL,YES,CANARA,GOLDEN");
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
    void verifyGetBanksdetailslistfromCodesV1_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_BanksdetailslistfromCodes_V1("TNMB,PPBL,YES,CANARA,GOLDEN");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.GetBanksdetailslistfromidsV1(objectHead);
        Assert.assertEquals(withDrawJson1.getString("notFound"), "[golden]");
    }
}
