package scripts.api.mappingService.AuditL3Apis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class UserGetMerchantExtendedInfoV3 extends PGPBaseTest {

    String UserId = "1107233579";

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantExtendedInfo V3 API resultInfo ")
    void verifyUserGetMerchantExtendedInfoV3_01() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_Get_Merchant_ExtendedInfo_V3(UserId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserGetMerchantExtendedInfoV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantExtendedInfo V3 API resultInfo ")
    void verifyUserGetMerchantExtendedInfoV3_02() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_Get_Merchant_ExtendedInfo_V3(UserId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserGetMerchantExtendedInfoV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("merchantId"), "yQZrWj40877019255969");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantExtendedInfo V3 API ExtendedInfo_1 ")
    void verifyUserGetMerchantExtendedInfoV3_03() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_Get_Merchant_ExtendedInfo_V3(UserId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserGetMerchantExtendedInfoV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.entityId"), "9740293");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.status"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.keySize"), "16");
        Assert.assertNotNull("extendedInfo.numberOfRetry");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.walletEnabled"), "false");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.walletRechargeRnabled"), "MANUAL_RECHARGE");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.callbackUrlEnabled"), "true");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.sap"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.comment"), "NA");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.s2sCallbackEnabled"), "false");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.businessName"), "neha");
        Assert.assertNotNull("extendedInfo.signedTime");
        Assert.assertNotNull("extendedInfo.entityKey");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantExtendedInfo V3 API ExtendedInfo_2 ")
    void verifyUserGetMerchantExtendedInfoV3_04() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_Get_Merchant_ExtendedInfo_V3(UserId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserGetMerchantExtendedInfoV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isDownloaded"), "F");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactFname"), "rahul");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactMname"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactLname"), "kujur");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.contactMobile"), "9986221387");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.eciStatus"), "ACTIVE");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryEmail"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.primaryEmail"), "nikitanew0009@mailinator.com");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryFirstname"), "rahul");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryLastname"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryMobileno"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.secondaryPhoneno"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.invoiceEmail"), "nikitanew0009@mailinator.com");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchCommPref"), "3");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantExtendedInfo V3 API ExtendedInfo_3 ")
    void verifyUserGetMerchantExtendedInfoV3_05() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_Get_Merchant_ExtendedInfo_V3(UserId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserGetMerchantExtendedInfoV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.panNoBusiness"), "AAAAA0000A");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.bankAccNo"), "607710110000667");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.kycStatus"), "9376503");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.platformType"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.ifscCode"), "BKID0006077");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.panNoPersonal"), "ASDFG1234B");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.addProofnoPersonal"), "aa");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.idProofnoPersonal"), "aa");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.signatoryName"), "Deepankar2");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isPeonEnable"), "false");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantName"), "Nikita");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.custCommPref"), "3");
        Assert.assertNotNull(withDrawJson1.getString("extendedInfo.productCode"));
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantExtendedInfo V3 API ExtendedInfo_4 ")
    void verifyUserGetMerchantExtendedInfoV3_06() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_Get_Merchant_ExtendedInfo_V3(UserId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserGetMerchantExtendedInfoV3(objectHead);
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
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.urbanAirshipEnabled"), "false");
    }

    @Owner("Anushka")
    @Feature("PG2-12162")
    @Test(description = "Verify Get QueryMerchantExtendedInfo V3 API ExtendedInfo_5 ")
    void verifyUserGetMerchantExtendedInfoV3_07() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.User_Get_Merchant_ExtendedInfo_V3(UserId);
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        pg2MappingApisHelper.VerifyUserGetMerchantExtendedInfoV3(objectHead);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.platformType"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.minPartialRenewalPercentage"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.aggregatorMid"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.blocked"), "false");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.additionalEmails"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.oldpgMid"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.isMerchant"), "1");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.merchantLimit"), "1");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.userId"), "1107233579");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.gstin"), "22AAAAA0000A1Z1");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.paymentInvoiceMobile"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.paymentInvoiceEmail"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.chargeBackEmails"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.kybId"), "");
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.communicationContact"), null);
        Assert.assertEquals(withDrawJson1.getString("extendedInfo.ONPAYTM"), "false");
    }
}
