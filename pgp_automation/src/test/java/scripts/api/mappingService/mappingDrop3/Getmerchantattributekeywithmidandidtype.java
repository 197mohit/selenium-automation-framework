package scripts.api.mappingService.mappingDrop3;

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

public class Getmerchantattributekeywithmidandidtype extends PGPBaseTest {

    String idType="paytm";

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "verify aesKey & userKey response of get merchant attribute api when mid is qa12mi80573803805439 and  idType paytm ")
    void verifySuccessfullResponseOfMerchantAttributeApi_01(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_withmidandidtype(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),idType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeywithmidandidtype(objectHead);
        Assert.assertEquals(withDrawJson1.getString("aesKey"),"WXW4dSJckPBIzj+V2ld1/wP3Um8V1CnYOrAHLzqADCo=");
        Assert.assertEquals(withDrawJson1.getString("userKey"),"so6tel4hnjew972nyc1crcsivc0sw6ir");
        Assert.assertEquals(withDrawJson1.getString("sharedSecret"),"rej5zdfrwhtwbxz41hwbi79ubigo7sc3");
        Assert.assertEquals(withDrawJson1.getString("utilCode"),"PGPTM");
        Assert.assertEquals(withDrawJson1.getString("catCode"),null);
        Assert.assertEquals(withDrawJson1.getString("name"),null);
        Assert.assertEquals(withDrawJson1.getString("catDesc"),null);
        Assert.assertEquals(withDrawJson1.getString("clientId"),null);
        Assert.assertEquals(withDrawJson1.getString("merchantAccRef"),null);
        Assert.assertEquals(withDrawJson1.getString("subUserEmail"),null);
        Assert.assertEquals(withDrawJson1.getString("corpConfigId"),null);
        Assert.assertEquals(withDrawJson1.getString("clientSecret"),null);


    }

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "verify aesKey & userKey response of get merchant attribute api when mid is AUTOQ847569449405149 and  idType paytm ")
    void verifySuccessfullResponseOfMerchantAttributeApi_02(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_withmidandidtype(Constants.MerchantType.Attribute_key_mid1.getId().toString(),idType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeywithmidandidtype(objectHead);
        Assert.assertEquals(withDrawJson1.getString("aesKey"),"gZou6HTyfu2z8hachAdCnR7LXp2uPO2g9uM/UZ/8CAE=");
        Assert.assertEquals(withDrawJson1.getString("userKey"),"pi27uwfrrlvosm4pevilgjgxmsjdarow");
        Assert.assertEquals(withDrawJson1.getString("sharedSecret"),"o9l29pgxgpgsyx1hozjjbpmck6y8mnip");
        Assert.assertEquals(withDrawJson1.getString("utilCode"),"919999999999");
        Assert.assertEquals(withDrawJson1.getString("catCode"),null);
        Assert.assertEquals(withDrawJson1.getString("name"),null);
        Assert.assertEquals(withDrawJson1.getString("catDesc"),null);

    }

}
