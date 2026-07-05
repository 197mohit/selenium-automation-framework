package scripts.api.mappingService.AuditL3Apis;

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

public class MerchantGetExtendedInfoV4 extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify  MerchantGetExtendedInfoV4 API resultInfo ")
    void verifyMerchantGetExtendedInfoV4_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Get_ExtendedInfo_V4(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantGetExtendedInfoV4(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get MerchantGetExtendedInfo V4 API merchantId ")
    void verifyMerchantGetExtendedInfoV4_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Get_ExtendedInfo_V4(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantGetExtendedInfoV4(objectHead);
        Assert.assertEquals(withDrawJson1.getString("merchantId"), Constants.MerchantType.Mapping_PG2_MID.getId().toString());
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify MerchantGetExtendedInfoV4 API DetailsPart1 ")
    void verifyMerchantGetExtendedInfoV4_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Get_ExtendedInfo_V4(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantGetExtendedInfoV4(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.entityId"), "678706539");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.status"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.keySize"), "16");
        Assert.assertNotNull("extendedInfo.numberOfRetry");
        Assert.assertNotNull("extendedInfo.walletEnabled");
        Assert.assertNotNull("extendedInfo.extendedInfo.walletRechargeRnabled");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.callbackUrlEnabled"), "true");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.sap"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.comment"), "NA");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.s2sCallbackEnabled"), "false");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.businessName"), "ENGLAND XOXO");
        Assert.assertNotNull("extendedInfo.signedTime");
        Assert.assertNotNull("extendedInfo.entityKey");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify MerchantGetExtendedInfoV4 API DetailsPart2 ")
    void verifyMerchantGetExtendedInfoV4_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Get_ExtendedInfo_V4(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantGetExtendedInfoV4(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isDownloaded"), "T");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactFname"), "Queen");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactMname"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactLname"), "Elizabeth");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactMobile"), "8580417361");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.eciStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryEmail"), "");
        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.primaryEmail"));
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryFirstname"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryLastname"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryMobileno"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryPhoneno"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.invoiceEmail"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchCommPref"), "7");

    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify MerchantGetExtendedInfoV4 API DetailsPart3 ")
    void verifyMerchantGetExtendedInfoV4_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Get_ExtendedInfo_V4(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantGetExtendedInfoV4(objectHead);

        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.productCode"));

    }


    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify MerchantGetExtendedInfoV4 DetailsPart4 ")
    void verifyMerchantGetExtendedInfoV4_06() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Get_ExtendedInfo_V4(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantGetExtendedInfoV4(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchRefCommPref"), "0");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.custRefCommPref"), "0");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isOtpThemeEnabled"), "0");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isApiRefundAllowed"), "0");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.maxAmountForComplexRefund"), "0");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.peonRequestType"), "DEFAULT");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.peonServiceName"), "PeonSentServiceImpl");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantWebForcedTheme"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantWapForcedTheme"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secureStatusEnabled"), "0");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.urbanAirshipHash"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.urbanAirshipEnabled"), "true");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify MerchantGetExtendedInfoV4 API DetailsPart5 ")
    void verifyMerchantGetExtendedInfoV4_07() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_Get_ExtendedInfo_V4(Constants.MerchantType.Mapping_PG2_MID.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyMerchantGetExtendedInfoV4(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.platformType"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.minPartialRenewalPercentage"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.aggregatorMid"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.blocked"), "false");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.additionalEmails"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.oldpgMid"), "216820000009644379409");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isMerchant"), "1");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantLimit"), "0");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.userId"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.paymentInvoiceMobile"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.paymentInvoiceEmail"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.chargeBackEmails"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.kybId"), "A0g6pel2d7prc090");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.communicationContact"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.ONPAYTM"), "false");
    }
}
