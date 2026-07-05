package scripts.api.mappingService.MappingAuditL2Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.json.*;
import scripts.api.linkservice.KafkaMessagePayload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class GetPaytmidV1OldpgId extends PGPBaseTest {

    @Owner("Anushka Goldi")
    @Test(description = "Verify Successfull response of Get paytmId with oldpgId qa12mi80573803805439 ")
    void GetPaytmIdV1_01()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paytmId_V1("qa12mi80573803805439");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("oldpgId"),"qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("officialName"),"qamid");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),"345678920");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify null response of Get paytmId with oldpgId qa12mi80573803805439 ")
    void GetPaytmIdV1_02()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paytmId_V1("qa12mi80573803805439");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertNull(withDrawJson1.getString("paytmWalletId"));
        Assert.assertNull(withDrawJson1.getString("oldpgWalletId"));
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertNull(withDrawJson1.getString("businessName"));
        Assert.assertNull(withDrawJson1.getString("merchantType"));
        Assert.assertNull(withDrawJson1.getString("entityId"));
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify Successfull response of Get paytmId with oldpgId qa8mid49895778745987 ")
    void GetPaytmIdV1_03()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paytmId_V1("qa8mid49895778745987");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "qa8mid49895778745987" );
        Assert.assertEquals(withDrawJson1.getString("oldpgId"),"qa8mid49895778745987");
        Assert.assertEquals(withDrawJson1.getString("officialName"),"links");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),"345678920");
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify null response of Get paytmId with oldpgId qa8mid49895778745987 ")
    void GetPaytmIdV1_04()  {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paytmId_V1("qa8mid49895778745987");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);
        Assert.assertNull(withDrawJson1.getString("paytmWalletId"));
        Assert.assertNull(withDrawJson1.getString("oldpgWalletId"));
        Assert.assertNull(withDrawJson1.getString("contractPayload"));
        Assert.assertNull(withDrawJson1.getString("businessName"));
        Assert.assertNull(withDrawJson1.getString("merchantType"));
        Assert.assertNull(withDrawJson1.getString("entityId"));
    }


    public static HashMap<String, String> StoreOldApiResponse() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_paytmId("qa8mid49895778745987");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetAliPayId(objectHead,withDrawJson1);

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
        mappingApisPG2.Get_paytmId_V1("qa8mid49895778745987");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyGetPaytmidV1OldpgId(objectHead,withDrawJson1);

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


    @Owner("Anushka Goldi")
    @Test(description = "Verify Old and New Api Response of GetpaytmId with  except alipay keywords parameters")
    void ComapreOldandNewApiResponse_01() throws InterruptedException {
        HashMap<String,String> Oldapiresponse= StoreOldApiResponse();
        HashMap<String,String> Newapiresponse= StoreNewApiResponse();
        String ExcludealipayId = "oldpgId";
        String ExcludealipayWalletId = "alipayWalletId";
        String Excludepg2DirectOnboarding = "oldpgWalletId";
        String ExcludeoldpgId = "oldpgId";
        String ExcludeoldpgWalletId = "oldpgWalletId";
        String Excludepg2OnboardedMerchant = "pg2OnboardedMerchant";
        Oldapiresponse.remove(ExcludealipayId);
        Oldapiresponse.remove(ExcludealipayWalletId);
        Oldapiresponse.remove(Excludepg2DirectOnboarding);
        Newapiresponse.remove(ExcludeoldpgId);
        Newapiresponse.remove(ExcludeoldpgWalletId);
        Newapiresponse.remove(Excludepg2OnboardedMerchant);
        Assert.assertTrue(Newapiresponse.equals(Oldapiresponse));
        if(!Newapiresponse.equals(Oldapiresponse)){
            Assert.fail("Old and New Api Response is not matched");
        }
    }

    @Owner("Anushka Goldi")
    @Test(description = "Verify alipay keywords parameters of Old and New Api in GetpaytmId")
    void ComapreOldandNewApiResponse_02() throws InterruptedException {
        HashMap<String,String> Oldapiresponse= StoreOldApiResponse();
        HashMap<String,String> Newapiresponse= StoreNewApiResponse();
        String KeyalipayId = "oldpgId";
        String KeyalipayWalletId = "oldpgWalletId";
        String KeyoldpgId = "oldpgId";
        String KeyoldpgWalletId = "oldpgWalletId";
        String ValuealipayIdOld = Oldapiresponse.get(KeyalipayId);
        String ValuealipayWalletIdOld = Oldapiresponse.get(KeyalipayWalletId);
        String ValueoldpgIdNew = Newapiresponse.get(KeyoldpgId);
        String ValueoldpgWalletIdNew = Newapiresponse.get(KeyoldpgWalletId);

        Assert.assertTrue(ValuealipayIdOld.equals(ValueoldpgIdNew));
        if(!ValuealipayIdOld.equals(ValueoldpgIdNew)&& !(ValuealipayWalletIdOld==ValueoldpgWalletIdNew)){
            Assert.fail("Old and New Api Response is not matched");
        }
    }

}
