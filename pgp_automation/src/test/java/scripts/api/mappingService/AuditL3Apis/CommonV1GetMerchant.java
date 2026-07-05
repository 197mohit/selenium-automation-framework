package scripts.api.mappingService.AuditL3Apis;

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

public class CommonV1GetMerchant extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Feature("PG2-12160")
    @Test(description = "Verify Successfull response of CommonV1GetMerchant API when id is of oldpg type")
    void CommonV1GetMerchant_01() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Common_V1_Get_Merchant();
        mappingApisPG2.SetCommonV1Get(Constants.MerchantType.Mapping_PG2_MID.getId().toString(), "oldpg");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyCommonV1GetMerchant(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("response"));
        Assert.assertEquals(withDrawJson1.getString("signature"), "ASDASKSDLDJfk");
    }

    @Owner("Anushka Goldi")
    @Feature("PG2-12160")
    @Test(description = "Verify response head of CommonV1GetMerchant API when id is of oldpg type")
    void CommonV1GetMerchant_02() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Common_V1_Get_Merchant();
        mappingApisPG2.SetCommonV1Get(Constants.MerchantType.Mapping_PG2_MID.getId().toString(), "oldpg");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyCommonV1GetMerchant(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.head.clientId"), "1230000001");
        Assert.assertNull(withDrawJson1.getString("response.head.respTime"));
        Assert.assertEquals(withDrawJson1.getString("response.head.accessToken"), "");
        Assert.assertNull(withDrawJson1.getString("response.head.reserve"));
    }

    @Owner("Anushka Goldi")
    @Feature("PG2-12160")
    @Test(description = "Verify response of body.response of CommonV1GetMerchant API when id is of oldpg type")
    void CommonV1GetMerchant_03() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Common_V1_Get_Merchant();
        mappingApisPG2.SetCommonV1Get(Constants.MerchantType.Mapping_PG2_MID.getId().toString(), "paytm");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyCommonV1GetMerchant(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.body.response.sourceId"), "qa14Qu16505533696224");
        Assert.assertEquals(withDrawJson1.getString("response.body.response.type"), "merchant");
        Assert.assertNotNull(withDrawJson1.getString("response.body.response.merchantData"));
        Assert.assertNull(withDrawJson1.getString("response.body.response.userData"));
    }

    @Owner("Anushka Goldi")
    @Feature("PG2-12160")
    @Test(description = "Verify response of body.response.userData of CommonV1GetMerchant API when id is of oldpg type")
    void CommonV1GetMerchant_04() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Common_V1_Get_Merchant();
        mappingApisPG2.SetCommonV1Get(Constants.MerchantType.Mapping_PG2_MID.getId().toString(), "paytm");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyCommonV1GetMerchant(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.body.response.merchantData.paytmId"), "qa14Qu16505533696224");
        Assert.assertEquals(withDrawJson1.getString("response.body.response.merchantData.oldpgId"), "216820000009644379409");
        Assert.assertEquals(withDrawJson1.getString("response.body.response.merchantData.guid"), null);
        Assert.assertEquals(withDrawJson1.getString("response.body.response.merchantData.ssoId"), null);
        Assert.assertEquals(withDrawJson1.getString("response.body.response.merchantData.officialName"), "ENGLAND XOXO");
        Assert.assertNull(withDrawJson1.getString("response.body.response.merchantData.paytmAccountId"));
        Assert.assertNull(withDrawJson1.getString("response.body.response.merchantData.oldpgAccountId"));
        Assert.assertNull(withDrawJson1.getString("response.body.response.merchantData.merchantType"));
        Assert.assertEquals(withDrawJson1.getString("response.body.response.merchantData.industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("response.body.response.merchantData.entityId"), null);
        Assert.assertNull(withDrawJson1.getString("response.body.response.merchantData.pg2OnboardedMerchant"));
        Assert.assertNull(withDrawJson1.getString("response.body.response.merchantData.businessName"));
    }

    @Owner("Anushka Goldi")
    @Feature("PGP-45207")
    @Test(description = "Verify response of body.resultInfo of CommonV1GetMerchant API when id is of oldpg type")
    void CommonV1GetMerchant_05() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Common_V1_Get_Merchant();
        mappingApisPG2.SetCommonV1Get(Constants.MerchantType.Mapping_PG2_MID.getId().toString(), "paytm");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyCommonV1GetMerchant(objectHead);
        Assert.assertEquals(withDrawJson1.getString("response.body.resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.body.resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("response.body.resultInfo.messaage"), "Success");
    }
}
