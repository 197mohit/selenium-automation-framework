package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class LogoCobrandingDetail extends PGPBaseTest {
    @Test(description = "Verify Successfull response of Logo Cobranding detail api")
    void Logo_Cobranding_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Logo_Cobranding_Details(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyLogoCobrandingDetails(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("mid"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        Assert.assertEquals(withDrawJson1.getString("posProvider"),"HDFC");
        Assert.assertEquals(withDrawJson1.getString("brandingReq"),"true");
        Assert.assertEquals(withDrawJson1.getString("brandReq"),"HDFC");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify Successfull response of URLs Logo Cobranding detail api")
    void Logo_Cobranding_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Get_Logo_Cobranding_Details(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyLogoCobrandingDetails(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("logoUMP"),"https://merchant-static-qa.paytm.com/merchant-dashboard/logos/bank/hdfc/ump/UMP_HDFC_POS_PROVIDER.png");
        Assert.assertEquals(withDrawJson1.getString("logoEDC"),"https://merchant-static-qa.paytm.com/merchant-dashboard/logos/bank/hdfc/edc/EDC_HDFC_POS_PROVIDER.png");
        Assert.assertEquals(withDrawJson1.getString("logoP4BL"),"https://merchant-static-qa.paytm.com/merchant-dashboard/logos/bank/hdfc/p4bl/P4B_MENU_HDFC_POS_PROVIDER.png");
        Assert.assertEquals(withDrawJson1.getString("logoP4BH"),"https://merchant-static-qa.paytm.com/merchant-dashboard/logos/bank/hdfc/p4bh/P4B_HOME_HDFC_POS_PROVIDER.png");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
    @Test(description = "Verify Successfull response of Logo Cobranding  api when Channel Net_banking is Provided")
    void Logo_Cobranding_With_Channel_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Logo_cobranding_mid(Constants.MerchantType.Mapping_PG2_MID.getId().toString(),"NET_BANKING");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"),"00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"),"Success");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
       // pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    @Test(description = "Verify Response parameters of Logo_Cobranding_With_Channel ")
    void Logo_Cobranding_With_Channel_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Logo_cobranding_mid(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"NET_BANKING");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantLogoCobrandingWithChannel(objectHead,withDrawJson1);
        Assert.assertEquals(withDrawJson1.getString("response.mid"),"qa12mi80573803805439");
        Assert.assertEquals(withDrawJson1.getString("response.cobrandingBank"),"ICICI");
        Assert.assertEquals(withDrawJson1.getString("response.logo"),"https://merchant-static-qa.paytm.com/merchant-dashboard/logos/bank/icici/edc_cobranding_receipt/ICICI.png");
        Assert.assertEquals(withDrawJson1.getString("response.cobrandingModelType"),"AGGREGATOR");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
