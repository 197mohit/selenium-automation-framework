package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class PaymentInfoFee extends PGPBaseTest {
    @Test(description = "Verify Successfull response of Payment Info Api When payMethod is Credit Card")
    void PaymentInfoFee_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Payment_Info_Fee(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"51051000100000000001");
        mappingApisPG2.buildPaymentInfoFeeRequest("CREDIT_CARD");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString());
    }
    @Test(description = "Verify payMethod is CREDIT_CARD when we passed payMethod CREDIT_CARD IN REQUEST")
    void PaymentInfoFee_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Payment_Info_Fee(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymode.getId().toString(),"51051000100000000001");
        mappingApisPG2.buildPaymentInfoFeeRequest("CREDIT_CARD");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        List<JSONObject> responseList= withDrawJson1.getList("response");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        int s=withDrawJson1.getList("response").size();
        if(s==0){
            Assert.fail("Response object is Empty");
        }
        for(int i=0;i<s;i++) {
            String payMethod=withDrawJson1.getString("response[" + i + "].payMethod");
            if(payMethod.equals("CREDIT_CARD")){
                Assert.assertEquals(payMethod,"CREDIT_CARD");
                break;
            }
        }
    }
}
