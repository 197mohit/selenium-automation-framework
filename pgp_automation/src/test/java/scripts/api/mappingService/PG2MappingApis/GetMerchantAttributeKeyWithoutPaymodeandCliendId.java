package scripts.api.mappingService.PG2MappingApis;
import com.paytm.base.test.PGPBaseTest;
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


public class GetMerchantAttributeKeyWithoutPaymodeandCliendId  extends PGPBaseTest {
    String idType="paytm";

    @Test(description = "verify Successfull response of merchant attribute keys api with only idType")
    void verifySuccessfullResponseOfMerchantAttributeApiWithoutPaymodeandClientId_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_Without_PaymodeAndClientId("qa12mi80573803805439",idType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKey(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("aesKey"),"WXW4dSJckPBIzj+V2ld1/wP3Um8V1CnYOrAHLzqADCo=");
        Assert.assertEquals(withDrawJson1.getString("utilCode"),"PGPTM");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());

    }

    @Test(description = "verify Merchant Routes to MC for merchant attribute keys api with only idType")
    void verifySuccessfullResponseOfMerchantAttributeApiWithoutPaymodeandClientId_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_Without_PaymodeAndClientId("qa12mi80573803805439",idType);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKey(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());

    }

}
