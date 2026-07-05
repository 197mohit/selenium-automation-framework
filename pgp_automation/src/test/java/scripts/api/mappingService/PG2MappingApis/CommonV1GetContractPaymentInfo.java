package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class CommonV1GetContractPaymentInfo extends PGPBaseTest {
    String Paymethod = "NET_BANKING";
    String ProductCode = "51051000100000000001";
    @Test(description = "Verify CommonV1GetContractPaymentInfo API response ")
    void verifyCommonV1GetContractPaymentInfoApi() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Common_v1_get_contract_paymentInfo(Constants.MerchantType.Mapping_PG2_MID.getId().toString(), Paymethod, ProductCode);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyCommonV1GetContractPaymentInfo(objectHead);
//        pg2MappingApisHelper.verifyPG2Routes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.payMethod"), Paymethod);
        Assert.assertNotNull(withDrawJson1.getString("response.feeRanges"));
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());

    }
}
