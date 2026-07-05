package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantGetExtendedInfo extends PGPBaseTest {

    String oldpgMid="qa12mi80573803805439";

    @Test(description = "Verify MerchantGetExtendedInfo Api resultInfo ")
    void verifyMerchantGetExtendedInfoApi_01() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_get_extended_info(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantGetExtendedInfo(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("merchantId"), Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    @Test(description = "Verify MerchantGetExtendedInfo Api extendedInfo response ")
    void verifyMerchantGetExtendedInfoApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_get_extended_info(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantGetExtendedInfo(objectHead);
        String ONPAYTM_ACTUAL=withDrawJson1.getString("extendedInfo.ONPAYTM");
        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.entityId"));
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.status"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.keySize"), "16");
        Assert.assertNotNull("extendedInfo.numberOfRetry");
        Assert.assertNotNull("extendedInfo.walletEnabled");
        Assert.assertNotNull("extendedInfo.extendedInfo.walletRechargeRnabled");
        Assert.assertNotNull("extendedInfo.businessName");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.peonRequestType"), "DEFAULT");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.oldpgMid"), oldpgMid);
        Assert.assertNotNull("extendedInfo.ONPAYTM");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchRefCommPref"), "66");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.custRefCommPref"), "66");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.productCode"), "51051000100000000001,51051000100000000004,51051000100000000044");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isMerchant"), "1");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantLimit"), "1");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.urbanAirshipEnabled"), "true");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        pg2MappingApisHelper1.verifyOnpaytmFalgValueWithDB("678703827",ONPAYTM_ACTUAL);
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }

    
    @Test(description = "Verify MerchantGetExtendedInfo Api extendedInfo response ")
    void verifyMerchantGetExtendedInfoApi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_get_extended_info(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyMerchantGetExtendedInfo(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.sap"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.comment"), "NA");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isDownloaded"), "T");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactFname"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactMname"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactLname"), "qamid");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.eciStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryEmail"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryFirstname"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantWapForcedTheme"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantWebForcedTheme"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.platformType"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.minPartialRenewalPercentage"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.aggregatorMid"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.paymentInvoiceMobile"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.paymentInvoiceEmail"), null);
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        //pg2MappingApisHelper1.verifyMerchantCenterRoutes(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());

    }
}
