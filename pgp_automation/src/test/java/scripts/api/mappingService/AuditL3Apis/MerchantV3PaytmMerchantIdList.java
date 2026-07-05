package scripts.api.mappingService.AuditL3Apis;


import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class MerchantV3PaytmMerchantIdList extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Successfull response of Merchant V3 merchantId List api When both mids are different")
    void MerchantIdList_V3_01() {
        ArrayList<String>merchantIdList=new ArrayList<>();
        merchantIdList.add("qa11PG72611112693255");
        merchantIdList.add("qa11PG72611112693255");
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_V3_MerchantIdList("paytm");
        mappingApisPG2.setMerchantIdList(merchantIdList);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantInfoListV3(withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Successfull response of Merchant v1 merchantId List api When both mids are same")
    void MerchantIdList_V3_02()  {
        ArrayList<String>merchantIdList=new ArrayList<>();
        merchantIdList.add("qa11PG72611112693255");
        merchantIdList.add("qa11PG72611112693255");
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_V3_MerchantIdList("alipay");
        mappingApisPG2.setMerchantIdList(merchantIdList);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantInfoListV3(withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
    }
}
