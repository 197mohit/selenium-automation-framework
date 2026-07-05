package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class MerchantV3MerchantList extends PGPBaseTest {
    @Test(description = "Verify Successfull response of Merchant v1 merchantId List api When both mids are different")
    void MerchantList_01() throws InterruptedException {
        ArrayList<String>merchantIdList=new ArrayList<>();
        merchantIdList.add("qa11PG72611112693255");
        merchantIdList.add("qa12mi80573803805439");
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_V1_MerchantIdList("paytm");
        mappingApisPG2.setMerchantIdList(merchantIdList);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantInfoList(withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
        int s=withDrawJson1.getList("merchantInfoList").size();
        Assert.assertEquals(s,2);
//        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
//                "grep \"" + merchantIdList + "\" | grep \"Traffic routes to Merchant Center for this merchantId "+merchantIdList+"\"";
//        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);


    }
    @Test(description = "Verify Successfull response of Merchant v1 merchantId List api When both mids are same")
    void MerchantList_02() throws InterruptedException {
        ArrayList<String>merchantIdList=new ArrayList<>();
        merchantIdList.add("qa11PG72611112693255");
        merchantIdList.add("qa11PG72611112693255");
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_V1_MerchantIdList("alipay");
        mappingApisPG2.setMerchantIdList(merchantIdList);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantInfoList(withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("response.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("response.resultCode"),"00000");
        int s=withDrawJson1.getList("merchantInfoList").size();
        Assert.assertEquals(s,1);
//        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
//                "grep \"" + merchantIdList + "\" | grep \"Traffic routes to Merchant Center for this merchantId "+merchantIdList+"\"";
//        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);

    }
}
