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

public class Getmerchantattributekeywithmididtypeandpaymode extends PGPBaseTest {

    String idType="paytm";
    String paymode="NB";

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "verify aesKey & userKey response of get merchant attribute api when mid is qa12mi80573803805439 and  idType paytm ")
    void verifySuccessfullResponseOfMerchantAttributeApiwithpaymode_01(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_withmididtypeandpaymode(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),idType,paymode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeywithmididtypeandpaymode(objectHead);
        int s=withDrawJson1.getList("merchantKeys").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].aesKey"), "WXW4dSJckPBIzj+V2ld1/wP3Um8V1CnYOrAHLzqADCo=");
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].userKey"), null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].sharedSecret"), null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].utilCode"), Constants.MerchantType.Mapping_PG2_Attribute.getId());
        }

    }

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "verify aesKey & userKey response of get merchant attribute api when mid is qa12mi80573803805439 and  idType paytm ")
    void verifySuccessfullResponseOfMerchantAttributeApiwithpaymode_02(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_withmididtypeandpaymode(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),idType,paymode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeywithmididtypeandpaymode(objectHead);
        int s=withDrawJson1.getList("merchantKeys").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].catCode"),null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].name"),null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].catDesc"),null);
        }

    }

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "verify aesKey & userKey response of get merchant attribute api when mid is AUTOQ847569449405149 and  idType paytm ")
    void verifySuccessfullResponseOfMerchantAttributeApiwithpaymode_03(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_withmididtypeandpaymode(Constants.MerchantType.Attribute_key_mid1.getId().toString(),idType,paymode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeywithmididtypeandpaymode(objectHead);
        int s=withDrawJson1.getList("merchantKeys").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].aesKey"), "gZou6HTyfu2z8hachAdCnR7LXp2uPO2g9uM/UZ/8CAE=");
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].userKey"), null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].sharedSecret"), null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].utilCode"), Constants.MerchantType.Attribute_key_mid1.getId());
        }
    }

    @Owner("Vikash_verma")
    @Feature("PG2-12569")
    @Test(description = "verify aesKey & userKey response of get merchant attribute api when mid is AUTOQ847569449405149 and  idType paytm ")
    void verifySuccessfullResponseOfMerchantAttributeApiwithpaymode_04(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_withmididtypeandpaymode(Constants.MerchantType.Attribute_key_mid1.getId().toString(),idType,paymode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeywithmididtypeandpaymode(objectHead);
        int s=withDrawJson1.getList("merchantKeys").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].catCode"),null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].name"),null);
            Assert.assertEquals(withDrawJson1.getString("merchantKeys["+ i +"].catDesc"),null);
        }
    }
}
