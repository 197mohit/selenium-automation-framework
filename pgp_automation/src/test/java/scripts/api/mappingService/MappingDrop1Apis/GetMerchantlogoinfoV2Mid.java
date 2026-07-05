package scripts.api.mappingService.MappingDrop1Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class GetMerchantlogoinfoV2Mid extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of merchantlogoinfo/v2/ API with fetchLogoFromBossPanel is passed on api url, data come from Paytmlogo table")
    void GetMerchantlogoinfoV2Mid_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantlogoinfo_V2_with_fetchLogoFromBossPanel(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantlogoinfoV2Mid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify parameters in merchantlogoinfo/v2/ API with fetchLogoFromBossPanel is passed on api url, data come from Paytmlogo table")
    void GetMerchantlogoinfoV2Mid_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantlogoinfo_V2_with_fetchLogoFromBossPanel(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantlogoinfoV2Mid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("merchantBusinessName"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("merchantDisplayName"), "qamid");
        Assert.assertTrue(withDrawJson1.getString("merchantImageName").contains("/merchant-dashboard/logos/default/category/testDefaultCategory.png"));
        Assert.assertEquals(withDrawJson1.getString("paytmMid"), "qa12mi80573803805439");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of merchantlogoinfo/v2/ API when logo url is Y")
    void GetMerchantlogoinfoV2Mid_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantlogoinfo_V2("qa12mi80573803805439");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantlogoinfoV2Mid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify parameters in merchantlogoinfo/v2/ API when logo url is Y")
    void GetMerchantlogoinfoV2Mid_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantlogoinfo_V2("qa12mi80573803805439");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantlogoinfoV2Mid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("merchantBusinessName"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("merchantDisplayName"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("merchantImageName"), null);
        Assert.assertEquals(withDrawJson1.getString("paytmMid"), "qa12mi80573803805439");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of  merchantlogoinfo/v2/ API when logo url is N")
    void GetMerchantlogoinfoV2Mid_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantlogoinfo_V2(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantlogoinfoV2Mid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.messaage"), "Success");
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify parameters in merchantlogoinfo/v2/ API when logo url is N")
    void GetMerchantlogoinfoV2Mid_06() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchantlogoinfo_V2(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyGetMerchantlogoinfoV2Mid(objectHead);
        Assert.assertEquals(withDrawJson1.getString("merchantBusinessName"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("merchantDisplayName"), "qamid");
        Assert.assertNull(withDrawJson1.getString("merchantImageName"));
        Assert.assertEquals(withDrawJson1.getString("paytmMid"), "qa12mi80573803805439");
    }
}
