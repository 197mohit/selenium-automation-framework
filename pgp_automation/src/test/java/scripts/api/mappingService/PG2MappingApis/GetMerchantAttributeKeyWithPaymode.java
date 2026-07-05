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
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
public class GetMerchantAttributeKeyWithPaymode extends PGPBaseTest {

    @Test(description="verify Successfull Response of GetMerchantAttribute Key Api with paymode")
    void verifySuccessfullResponseOfMerchantAttributeApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_Paymode(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"NB","paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeyWithPaymode(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("merchantKeys["+"0"+"].aesKey"),"WXW4dSJckPBIzj+V2ld1/wP3Um8V1CnYOrAHLzqADCo=");
        Assert.assertEquals(withDrawJson1.getString("merchantKeys["+"0"+"].utilCode"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description="verify other parameter Response of GetMerchantAttribute Key Api with paymode")
    void verifySuccessfullResponseOfMerchantAttributeApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Attribute_Key_Paymode(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"NB","paytm");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantAttributeKeyWithPaymode(objectHead);
        //pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("merchantKeys["+"0"+"].userKey"),null);
        Assert.assertEquals(withDrawJson1.getString("merchantKeys["+"0"+"].sharedSecret"),null);
        Assert.assertEquals(withDrawJson1.getString("merchantKeys["+"0"+"].catCode"),null);
        Assert.assertEquals(withDrawJson1.getString("merchantKeys["+"0"+"].name"),null);
        Assert.assertEquals(withDrawJson1.getString("merchantKeys["+"0"+"].catDesc"),null);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
