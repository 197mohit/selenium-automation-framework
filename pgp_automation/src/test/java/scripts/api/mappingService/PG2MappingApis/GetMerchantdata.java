package scripts.api.mappingService.PG2MappingApis;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.junit.runner.Description;
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

public class GetMerchantdata extends PGPBaseTest {
    String officialName="qamid";
    String industryTypeId="345678920";

    @Test(description = "Verify Successfull response of Merchant Data api")
    void verifySuccessfullResponseOfMerchantAttributeApi() throws InterruptedException {

        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Merchant_Data(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantData(objectHead);
       // pg2MappingApisHelper.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("paytmId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertNull(withDrawJson1.getString("alipayId"));
        Assert.assertEquals(withDrawJson1.getString("officialName"),officialName);
        Assert.assertEquals(withDrawJson1.getString("industryTypeId"),industryTypeId);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
    }
}
