package scripts.api.mappingService.MappingAuditL2Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MerchantV3Paytm extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Test(description = "Verify Successfull response of Merchant V3 API")
    void verifyMerchantV3APIApi_01()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v3(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV3Api(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");
    }
    @Owner("Anushka Goldi")
    @Test(description = "Verify response object  of Merchant V3 API")
    void MerchantV3Api_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v3(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV3Api(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.paytmId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("response.oldpgId"),"qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("response.guid"),"N");
        Assert.assertEquals(withDrawJson1.getString("response.ssoId"),"");
        Assert.assertEquals(withDrawJson1.getString("response.officialName"),"qamid");
        Assert.assertNull(withDrawJson1.getString("response.paytmWalletId"));
        Assert.assertNull(withDrawJson1.getString("response.oldpgWalletId"));
        Assert.assertEquals(withDrawJson1.getString("response.merchantType"),"CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("response.industryTypeId"),"345678920");


    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify Entity Id & pg2OnboardedMerchant in Response object")
    void MerchantV3Api_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v3(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV3Api(objectHead,withDrawJson1);
        Assert.assertNotNull(withDrawJson1.getString("response.entityId"));
        Assert.assertEquals(withDrawJson1.getString("response.entityId"), "678703827");
        Assert.assertEquals(withDrawJson1.getString("response.pg2OnboardedMerchant"), "Y");
        Assert.assertNull(withDrawJson1.getString("response.businessName"));
    }

    public static HashMap<String, String> StoreOldApiResponse() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v1(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV1Api(objectHead,withDrawJson1);

        HashMap<String,String>OldAPIResponse=new HashMap<>();
        Iterator<String> keys = objectHead.keySet().iterator();
        System.out.println("Old Api Response " + objectHead);
        while (keys.hasNext()) {
            String key = keys.next();
            String value= withDrawJson1.getString(key);
            OldAPIResponse.put(key, value);
        }
        return OldAPIResponse;
    }

    public static HashMap<String, String> StoreNewApiResponse() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_v3(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantV3Api(objectHead,withDrawJson1);

        HashMap<String, String>NewAPIResponse=new HashMap<>();
        Iterator<String> keys = objectHead.keySet().iterator();
        System.out.println("New Api Response " + objectHead);
        while (keys.hasNext()) {
            String key = keys.next();
            String value= withDrawJson1.getString(key);
            NewAPIResponse.put(key, value);
        }
        return NewAPIResponse;
    }


//    @Owner("Anushka Goldi")
//    @Test(description = "Verify Old and New Api Response of MerchantV3 except alipay keywords parameters")
//    void ComapreOldandNewApiResponse_01() throws InterruptedException {
//        HashMap<String,String> Oldapiresponse= StoreOldApiResponse();
//        HashMap<String,String> Newapiresponse= StoreNewApiResponse();
//
//        String ExcludealipayId = "alipayId";
//        String ExcludeoldpgId = "oldpgId";
//        String ExcludealipayWalletId = "alipayWalletId";
//        String ExcludeoldpgWalletId = "oldpgWalletId";
//        String ExcludeenityId = "enityId";
//        String ExcludeentityId = "entityId";
//        String Excludepg2DirectOnboarding = "pg2DirectOnboarding";
//        String Excludepg2OnboardedMerchant = "pg2OnboardedMerchant";
//        String tocompare= Oldapiresponse.get("response").replace(ExcludealipayId, ExcludeoldpgId).replace(ExcludealipayWalletId, ExcludeoldpgWalletId).replace(ExcludeenityId,ExcludeentityId).replace(Excludepg2DirectOnboarding,Excludepg2OnboardedMerchant);
//        Oldapiresponse.replace("response", tocompare);
//        Assert.assertFalse(Oldapiresponse.get("response").contains("enityId:678703827") && Oldapiresponse.get("response").contains("alipayId:qa12mi80573803805439"));
//        Assert.assertTrue(Oldapiresponse.get("response").contains("oldpgWalletId:null") && Oldapiresponse.get("response").contains("oldpgId:qa12mi80573803805439"));
//
//    }
}
