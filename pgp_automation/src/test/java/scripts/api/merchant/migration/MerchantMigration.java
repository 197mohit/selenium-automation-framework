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
import java.util.List;


@Listeners(merchantMigrationHelper.class)
public class MerchantMigration extends merchantMigrationHelper {

    private static String True = "TRUE";
    private static String False = "FALSE";


    @Feature("PGP-29552")
    @Owner("Sourav")
    @Test(description = "Creating a Default and EDC merchant with PPI and GV paymodes adding commission and validating gv for product code 51051000100000000047")
    public void CreateEDCMerchantTest() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        EditMerchant editMerchant = new EditMerchant();
            JsonPath resp = editMerchant
                    .setMID(mid)
                    .setRequestID(requestID+"1")
                    .setPaymodes(PAYMODES.GIFT_VOUCHER.toString())
                    .execute().jsonPath();

            validateResponse(resp);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000047");
        softly.assertAll();
      }


    @Feature("PGP-36899")
    @Owner("Sourav")
    @Test(description = "Repayment product code not to be added for aggregator merchants")
    public void CreatingAggregatorMerchant() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("AGGREGATOR_PAYOUT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setIsAggregator(True)
                .setAggregatorEnable(True)
                .setOnlineSettlement(True)
                .setStaticPreferences(STATICPREFERENCE.AGGREGATOR_PAYOUT.toString())

                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000053");
        softly.assertAll();

    }


    @Feature("PGP-34332")
    @Owner("Sourav")
    @Test(description = "Mapping | Escrow based delayed settlement")
    public void CreatingEscrowBasedDelayedSettlement() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("PAY_CONFIRM_ACQUIRING_UNIVERSAL_PROD")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setOnlineSettlement(True)
                .setStaticPreferences(STATICPREFERENCE.IS_VOID_FEE_SUPPORTED.toString())
                .execute().jsonPath();
        validateResponse(response);
        mid =getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000059");
        softly.assertAll();

    }


    @Feature("PGP-35340")
    @Owner("Sourav")
    @Test(description = "Mapping | Merchant Solution Type For Blocked Merchants")
    public void MigratingMerchantSolutionTypeForBlockedMerchant() throws Exception {
        String testmid = "AUTOME27478030008612";
        String dbQuery = "DELETE FROM PAYTMPGDB.MERCHANT_SOLUTION_INFO where MID = '"+testmid+"';";
        queryPAYTMPGDB(dbQuery);

        String blockQuery = "UPDATE PAYTMPGDB.ENTITY_INFO SET IS_BLOCKED = 'T' where MID = '"+testmid+"';";
        queryPAYTMPGDB(blockQuery);

        EditMerchant editMerchant = new EditMerchant();
        JsonPath resp = editMerchant
                .setMID(testmid)
                .setRequestID(requestID)
                .setNoOfRetry("2")
                .execute().jsonPath();

        validateResponse(resp);


        MigrationDetails migrationDetails = new MigrationDetails(testmid);
        Awaitility.with().pollInSameThread().pollInterval(Duration.TEN_SECONDS)
                .atMost(Duration.TWO_MINUTES)
                .untilAsserted(()->migrationDetails.execute().jsonPath()
                        .get("'MERCHANT-EXTENDED-INFO'.extendedInfo.numberOfRetry")
                        .toString().equals("2"));

        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo = new GetMerchantAttributePreferenceInfo(testmid, "216820000007582184505");
        String res = getMerchantAttributePreferenceInfo.execute().jsonPath().get("merchantSolutionType");
        Assert.assertTrue(res == null);


        RemoveMerchantAttributePreferenceInfoCache removeMerchantAttributePreferenceInfoCache = new RemoveMerchantAttributePreferenceInfoCache("216820000007582184505");
        removeMerchantAttributePreferenceInfoCache.execute().toString().contains("SUCCESS");

        String insertQuery = "INSERT INTO PAYTMPGDB.MERCHANT_SOLUTION_INFO (ID, MID, SOLUTION_TYPE, OB_CHANNEL, ADDITIONAL_INFO, CREATED_DATE, STATUS, CREATED_BY, MODIFIED_DATE, MODIFIED_BY) \n" +
                "VALUES(378, '"+testmid+"', 'OFFLINE', 'UMP_WEB', NULL, '2022-01-28 15:27:30', 9376503, 8894182, '2022-01-28 15:27:30', NULL);";

        queryPAYTMPGDB(insertQuery);

        GetMerchantAttributePreferenceInfo getMerchantAttributePreferenceInfo1 = new GetMerchantAttributePreferenceInfo(testmid, "216820000007582184505");
        JsonPath res1 = getMerchantAttributePreferenceInfo1.execute().jsonPath().get("merchantSolutionType");
            Assert.assertTrue(res1.equals(SOLUTIONTYPE.online.toString()));

    }

    @Feature("PGP-29552")
    @Owner("Priyanshi")
    @Test(description = "Creating a Default merchant with PPI paymodes adding commission")
    public void CreateDefaultMerchant_PPI_Limit_0(){

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000004");
        softly.assertAll();


    }

    @Feature("PGP-34332")
    @Owner("Priyanshi")
    @Test(description = "Creating BW merchant")
    public void CreatingBW_Merchant() throws InterruptedException {
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

    }

    @Feature("PGP-34332")
    @Owner("Priyanshi")
    @Test(description = "Creating BW merchant")
    public void CreatingBW_Merchant2() throws Exception {
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("SUBSCRIBE,DEFAULT,RENEW_SUBSCRIPTION")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("MERCHANT-EXTENDED-INFO.extendedInfo.productCode")).toString().contains("51051000100000000004");
        softly.assertAll();
    }

    @Feature("PGP-25479")
    @Owner("Pragun")
    @Test(description = "Creating a merchant with settle freeze static preference ")
    public void CreateMerchantWithSFPref() throws Exception {

        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setStaticPreferences(STATICPREFERENCE.SETTLE_FREEZE.toString())
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,STATICPREFERENCE.SETTLE_FREEZE.toString());
    }

    @Feature("PGP-29552")
    @Owner("Pragun")
    @Test(description = "Creating a Default and EDC merchant AND validating product code for the same ")
    public void CreateEDCMerchant() throws Exception {

        CreateMerchantEDC createMerchant = new CreateMerchantEDC();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,EDC")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setTxnType("Payments",0)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",0)
                .setTxnType("EDC",1)
                .setPaymode("CC","simple",2.00,"FALSE",0,"",1)
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePayMethodProductCode(mid,"CREDIT_CARD","51051000100000000047");
    }



    @Feature("PGP-25479")
    @Owner("Pragun")
    @Test(description = "Creating a merchant with settle freeze static preference ")
    public void CreatePG2Merchant() throws Exception {
        ArrayList<String>staticPref=new ArrayList<>();
        staticPref.add("PG2_DATA_MIGRATION");
        staticPref.add("GST_EXEMPTED");
        CreateMerchant createMerchant = new CreateMerchant();
        JsonPath response = createMerchant.setRequestID(requestID)
                .setRequestType("DEFAULT,RETRY")
                .setMobileNumber(phoneNumber)
                .setPhoneNumber(phoneNumber)
                .setCustId(AuthHelpers.getCustomerID(phoneNumber))
                .setMultipleStaticpref(staticPref)
                .setMerchantIndustryType("SMALL")
                .setCustomName("qa8mid")
                .execute().jsonPath();

        validateResponse(response);
        mid = getMidViaBOSSAPI(phoneNumber);
        validatePrefStatus(mid,"PG2_DATA_MIGRATION");
    }


}
