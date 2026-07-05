package scripts.api.mappingService.Auditapichanges;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class merchantidmapv3 extends PGPBaseTest {

    String mid = Constants.MerchantType.Attribute_key_mid1.getId();
    String guid = "4ea3e3a8-ee80-4f97-9dc3-1dbae925daf5";
    String idtype1 = "paytm";
    String idtype2 = "oldpg";

    String idtype3 = "guid";

    @Owner("Vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify Response of merchantidmap API with mid AUTOQ847569449405149 and idtype paytm")
    void Verifymerchantidmap1() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Getmerchantidmapv3(mid, idtype1);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifymerchantidmapv3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "AUTOQ847569449405149");
        Assert.assertEquals(withDrawJson1.getString("oldpgId"), "AUTOQ847569449405149");
        Assert.assertEquals(withDrawJson1.getString("guid"), "N");
        Assert.assertEquals(withDrawJson1.getString("ssoId"), "");
        Assert.assertEquals(withDrawJson1.getString("officialName"), "ab");
        Assert.assertEquals(withDrawJson1.getString("paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("entityId"), "649907819");
        Assert.assertEquals(withDrawJson1.getString("pg2OnboardedMerchant"), "Y");
        Assert.assertEquals(withDrawJson1.getString("businessName"), null);

    }

    @Owner("Vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify Response of merchantidmap API with mid AUTOQ847569449405149 and idtype alipay")
    void Verifymerchantidmap2() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Getmerchantidmapv3(mid, idtype2);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifymerchantidmapv3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "AUTOQ847569449405149");
        Assert.assertEquals(withDrawJson1.getString("oldpgId"), "AUTOQ847569449405149");
        Assert.assertEquals(withDrawJson1.getString("guid"), "N");
        Assert.assertEquals(withDrawJson1.getString("ssoId"), "");
        Assert.assertEquals(withDrawJson1.getString("officialName"), "ab");
        Assert.assertEquals(withDrawJson1.getString("paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("entityId"), "649907819");
        Assert.assertEquals(withDrawJson1.getString("pg2OnboardedMerchant"), "Y");
        Assert.assertEquals(withDrawJson1.getString("businessName"), null);

    }

    @Owner("Vikash verma")
    @Feature("PG2-13264")
    @Test(description = "Verify Response of merchantidmap API with guid 4ea3e3a8-ee80-4f97-9dc3-1dbae925daf5 and idtype guid")
    void Verifymerchantidmap3() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.Getmerchantidmapv3(guid, idtype3);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        pg2MappingApisHelper.Verifymerchantidmapv3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("paytmId"), "qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("oldpgId"), "qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("guid"), "4ea3e3a8-ee80-4f97-9dc3-1dbae925daf5");
        Assert.assertEquals(withDrawJson1.getString("ssoId"), "");
        Assert.assertEquals(withDrawJson1.getString("officialName"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("paytmWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("oldpgWalletId"), null);
        Assert.assertEquals(withDrawJson1.getString("merchantType"), "CORPORATION");
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"), "345678920");
        Assert.assertEquals(withDrawJson1.getString("entityId"), "678703827");
        Assert.assertEquals(withDrawJson1.getString("pg2OnboardedMerchant"), "Y");
        Assert.assertEquals(withDrawJson1.getString("businessName"), "qamid");

    }

    public static HashMap<String, String> StoreOldApiResponse() throws InterruptedException {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        String mid = Constants.MerchantType.Attribute_key_mid1.getId();
        String idtype1 = "alipay";
        mappingApisPG2.Getmerchantidmap(mid, idtype1);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        pg2MappingApisHelper.Verifymerchantidmap(objectHead);

        HashMap<String, String> OldAPIResponse = new HashMap<>();
        Iterator<String> keys = objectHead.keySet().iterator();
        System.out.println("Old Api Response " + objectHead);
        while (keys.hasNext()) {
            String key = keys.next();
            String value = withDrawJson1.getString(key);
            OldAPIResponse.put(key, value);
        }
        return OldAPIResponse;
    }

    public static HashMap<String, String> StoreNewApiResponse() throws InterruptedException {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        String mid = Constants.MerchantType.Attribute_key_mid1.getId();
        String idtype1 = "oldpg";
        mappingApisPG2.Getmerchantidmapv3(mid, idtype1);
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        pg2MappingApisHelper.Verifymerchantidmapv3(objectHead);

        HashMap<String, String> NewAPIResponse = new HashMap<>();
        Iterator<String> keys = objectHead.keySet().iterator();
        System.out.println("New Api Response " + objectHead);
        while (keys.hasNext()) {
            String key = keys.next();
            String value = withDrawJson1.getString(key);
            NewAPIResponse.put(key, value);
        }
        return NewAPIResponse;
    }
}