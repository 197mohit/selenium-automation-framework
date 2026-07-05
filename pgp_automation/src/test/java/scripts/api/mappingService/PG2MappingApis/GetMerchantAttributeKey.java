package scripts.api.mappingService.PG2MappingApis;

import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Map;
import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.Test;

import java.util.Map;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class GetMerchantAttributeKey extends PGPBaseTest {
    String clientId="DEFAULT_VALUE_KEY";
    String idType="paytm";
    @Test(description = "verify aesKey & userKey response of get merchant attribute api when mid idType and client Id is passed")
    void verifySuccessfullResponseOfMerchantAttributeApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),clientId,idType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKey(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("aesKey"),"WXW4dSJckPBIzj+V2ld1/wP3Um8V1CnYOrAHLzqADCo=");
        Assert.assertEquals(withDrawJson1.getString("userKey"),"so6tel4hnjew972nyc1crcsivc0sw6ir");
        Assert.assertEquals(withDrawJson1.getString("sharedSecret"),"rej5zdfrwhtwbxz41hwbi79ubigo7sc3");
        Assert.assertEquals(withDrawJson1.getString("utilCode"),"PGPTM");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "verify sharedSecret & utilCode response of get merchant attribute api when mid idType and client Id is passed")
    void verifySuccessfullResponseOfMerchantAttributeApi_2() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),clientId,idType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKey(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("sharedSecret"),"rej5zdfrwhtwbxz41hwbi79ubigo7sc3");
        Assert.assertEquals(withDrawJson1.getString("utilCode"),"PGPTM");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
    @Test(description = "verify Merchant Center Route  for get merchant attribute api when mid idType and client Id is passed")
    void verifySuccessfullResponseOfMerchantAttributeApi_3() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),clientId,idType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKey(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
