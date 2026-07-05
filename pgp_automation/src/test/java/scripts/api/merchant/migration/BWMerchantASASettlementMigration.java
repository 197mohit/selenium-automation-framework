package scripts.api.merchant.migration;
import com.paytm.api.MappingService.ClearCache.RemoveMerchantAttributePreferenceInfoCache;
import com.paytm.api.MappingService.GetMerchantAttributePreferenceInfo;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.api.merchant.migration.CreateMerchantEDC;
import com.paytm.api.merchant.migration.EditMerchant;
import com.paytm.api.merchant.migration.merchantMigrationHelper;
import com.paytm.apphelpers.AuthHelpers;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import java.util.ArrayList;
import java.util.HashMap;
@Feature("PGP-31209")
public class BWMerchantASASettlementMigration extends merchantMigrationHelper {
    String NCMS_Error_MSG="NCMC_FLOWS Request Type can not be enable with BW_ENABLE/PWP_ENABLED/SPLIT_SETTLEMENT and payout days not equal to 1";
    String OFFLINE_SALE_ERROR_MSG="OFFLINE_SALE Request Type can not be enable with BW_ENABLE/PWP_ENABLED/SPLIT_SETTLEMENT/Instant Settlement and payout days not equal to 1";
    String ONLINE_SETTELEMENT_ERROR_MSG="Please select either Online Settlement or Business Wallet or txn wise settlement";
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000001-->DEFAULT)")
    public void MigrationOfBWAsSettlement_01() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000001");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000024-->PAYTM_EXPRESS)")
    public void MigrationOfBWAsSettlement_02() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("PAYTM_EXPRESS")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000024");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000026-->QR_PRN)")
    public void MigrationOfBWAsSettlement_03() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("QR_PRN")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000026");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000014-->EDC)")
    public void MigrationOfBWAsSettlement_04() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000014");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000048-->CASH_POS)")
    public void MigrationOfBWAsSettlement_05() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("EDC,CASH_POS")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000048");
        pcode.add("51051000100000000046");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000050-->NCMC_FLOWS)")
    public void MigrationOfBWAsSettlement_06() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("NCMC_FLOWS,DEFAULT,EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setMerchantIndustryType("SMALL")
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        validateAdminLogsResposeForMerchantCreation(requestID,NCMS_Error_MSG);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000051-->OFFLINE_SALE)")
    public void MigrationOfBWAsSettlement_07() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("OFFLINE_SALE")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        validateAdminLogsResposeForMerchantCreation(requestID,OFFLINE_SALE_ERROR_MSG);
    }
//    @Feature("PGP-30054")
//    @Owner("Nirottam")
//    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000004-->SUBSCRIBE/RENEW SUBSCRIPTION)", enabled = false)
    public void MigrationOfBWAsSettlement_08() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("SUBSCRIBE,RENEW_SUBSCRIPTION,DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000004");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000010-->SEAMLESS/SEAMLESS_NATIVE)")
    public void MigrationOfBWAsSettlement_09() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("SEAMLESS,SEAMLESS_NATIVE")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000010");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000010-->SEAMLESS/SEAMLESS_NATIVE) With Paymodes")
    public void MigrationOfBWAsSettlement_010() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("SEAMLESS,SEAMLESS_NATIVE")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);


        mid =getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.BW_ENABLED.toString());
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000010");
        validateProductCodes(productsCodes,pcode);
        ArrayList<String>ExpectedPaymodes=new ArrayList<>();
        ExpectedPaymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(ExpectedPaymodes);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration of BW merchant having BW supported product code (51051000100000000051-->OFFLINE_SALE) With paymodes")
    public void MigrationOfBWAsSettlement_011() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("OFFLINE_SALE")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        validateAdminLogsResposeForMerchantCreation(requestID,OFFLINE_SALE_ERROR_MSG);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not get Migrate) of BW merchant having non BW supported product code (51051000100000000003-->PRE_AUTH_CAPTURE)")
    public void MigrationOfBWAsSettlement_012() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("PRE_AUTH_CAPTURE")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        String expectedStatus="REDUNDANT | PENDING | NOT_ELIGIBLE";
        validateMigrationStatusInDB(mid,expectedStatus);

    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51051000100000000020-->SEAMLESS_NETBANKING_STOCK)")
    public void MigrationOfBWAsSettlement_013() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("SEAMLESS_NETBANKING_STOCK")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validateMigrationInDB(mid);
        String expectedStatus="REDUNDANT | PENDING | NOT_ELIGIBLE";
        validateMigrationStatusInDB(mid,expectedStatus);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51051000100000000020-->SEAMLESS_NETBANKING_STOCK)")
    public void MigrationOfBWAsSettlement_014() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("SEAMLESS_NETBANKING_STOCK")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validateMigrationInDB(mid);
        String expectedStatus="REDUNDANT | PENDING | NOT_ELIGIBLE";
        validateMigrationStatusInDB(mid,expectedStatus);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51051000100000000030-->MOTO_CHANNEL)")
    public void MigrationOfBWAsSettlement_015() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("MOTO_CHANNEL")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validateMigrationInDB(mid);
        String expectedStatus="REDUNDANT | PENDING | NOT_ELIGIBLE";
        validateMigrationStatusInDB(mid,expectedStatus);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51051000100000000041-->MUTUAL_FUNDS)")
    public void MigrationOfBWAsSettlement_016() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("MUTUAL_FUNDS")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validateMigrationInDB(mid);
        String expectedStatus="REDUNDANT | PENDING | NOT_ELIGIBLE";
        validateMigrationStatusInDB(mid,expectedStatus);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51051000100000000043-->MUTUAL_FUNDS_SIP)")
    public void MigrationOfBWAsSettlement_017() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("MUTUAL_FUNDS_SIP")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validateMigrationInDB(mid);
        String expectedStatus="REDUNDANT | PENDING | NOT_ELIGIBLE";
        validateMigrationStatusInDB(mid,expectedStatus);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51051000100000000044-->REPAYMENT)")
    public void MigrationOfBWAsSettlement_018() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("REPAYMENT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        validateMigrationInDB(mid);
        String expectedStatus="REDUNDANT | PENDING | NOT_ELIGIBLE";
        validateMigrationStatusInDB(mid,expectedStatus);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51170100100000000001-->SETTLEMENT) ==>For this product code ONLINE SETTLEMENT must be enabled on the merchant and business wallet and online setlement cannot be applied simultaneously on a merchant, restriction from panel end")
    public void MigrationOfBWAsSettlement_019() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setOnlineSettlement("TRUE")
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        validateAdminLogsResposeForMerchantCreation(requestID,ONLINE_SETTELEMENT_ERROR_MSG);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify migration(Merchant should not be migrate) of BW merchant having non BW supported product code (51170100100000000002-->AGGREGATOR_PAYOUT)")
    public void MigrationOfBWAsSettlement_020() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("AGGREGATOR_PAYOUT")
                .setStaticPreferences("AGGREGATOR_PAYOUT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setOnlineSettlement("TRUE")
                .setBWEnabled("TRUE")
                .execute().jsonPath();
        validateResponse(response);
        validateAdminLogsResposeForMerchantCreation(requestID,ONLINE_SETTELEMENT_ERROR_MSG);
    }

    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify Migration Of Non BW Merchant")
    public void MigrationOfBWAsSettlement_022() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String productsCodes=withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode");
        ArrayList<String> pcode=new ArrayList<>();
        pcode.add("51051000100000000001");
        validateProductCodes(productsCodes,pcode);
    }
    @Feature("PGP-30054")
    @Owner("Nirottam")
    @Test(description = "Verify Migration Of Non BW Merchant With paymodes")
    public void MigrationOfBWAsSettlement_023() throws InterruptedException {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        ArrayList<String>ExpectedPaymodes=new ArrayList<>();
        ExpectedPaymodes.add("CREDIT_CARD");
        validatePaymodesInMigrationDetail(ExpectedPaymodes);
    }


}
