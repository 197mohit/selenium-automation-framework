package scripts.api.mappingService;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class StatusFlagApi extends PGPBaseTest {

    @Test(description = "Verify Successfull response of Old query/merchant/migration/contract/details Api")
    void verifyQueryMerchantMigrationContractDetailsApi_01(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);

        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify Successfull response of Old query/merchant/migration/contract/details Api")
    void verifyQueryMerchantMigrationContractDetailsApi_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultStatus"), "S");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultCodeId"), "00000000");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultCode"), "SUCCESS");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultMsg"), "SUCCESS");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractId"));
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productCode"));
        }
    }

    @Test(description = "Verify response of Old query/merchant/migration/contract/details Api")
    void verifyQueryMerchantMigrationContractDetailsApi_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo"));
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractTemplate"));
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractRelations"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].merchantConfiguration"));

        }
    }

    @Test(description = "Status flag should not appear in old API but appears in new v2 api")
    void verifyQueryMerchantMigrationContractDetailsApi_04() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s1=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s1;i++)
        {
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic").contains("status")).isFalse();
        }

        mappingApisPG2.New_Query_MerchantMigration_ContractDetails("qa14pg55055186160241", "ACTIVE");
        JsonPath withDrawJson2=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead1= withDrawJson2.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper1=new PG2MappingApisHelper();
        pg2MappingApisHelper1.verifyQueryMerchantMigrationContractDetails(objectHead1);
        int s2=withDrawJson2.getList("contractDetailList").size();
        for(int i=0;i<s2;i++)
        {
            Assertions.assertThat(withDrawJson2.getString("contractDetailList["+ i +"].contractBasic").contains("status")).isTrue();
        }
    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api with Query param status=ACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_05(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails("qa14pg55055186160241", "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("contractDetailList"));
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api with Query param status=acTIVe")
    void verifyQueryMerchantMigrationContractDetailsApi_06(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails("qa14pg55055186160241", "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("contractDetailList"));
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api with Query param status= empty")
    void verifyQueryMerchantMigrationContractDetailsApi_07(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails("qa14pg55055186160241", "");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("contractDetailList"));
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api with Query param status=ACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_08(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultStatus"), "S");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultCodeId"), "00000000");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultCode"), "SUCCESS");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultMsg"), "SUCCESS");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractId"));
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productCode"));
        }
    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api with Query param status=ACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_09() {
        MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1 = mappingApisPG2.execute().jsonPath();
        Map<String, String> objectHead = withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s = withDrawJson1.getList("contractDetailList").size();
        for (int i = 0; i < s; i++) {
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo"));
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractTemplate"));
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractRelations"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].merchantConfiguration"));
        }
    }

    @Test(description = "Verify status is ACTIVE in response of contractBasic.contractId.status with Query param status=ACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_10(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.status"), "ACTIVE");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        }
    }

    @Test(description = "Verify response of contractBasic with Query param status=ACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_11(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractId"), "contractId");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productCode"), "productCode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productName"), "productName");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productVersion"), "productVersion");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.signedTime"), "signedTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.effectTime"), "effectTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.expiryTime"), "expiryTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.lastModifier"), "lastModifier");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractStatus"), "EFFECTIVE");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.effectType"), "IMMEDIATE");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.expiryType"), "expiryType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.memo"), "memo");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.externalContractId"), "externalContractId");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.createdTime"), "createdTime");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.modifiedTime"), "modifiedTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.transId"), "transId");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.status"), "ACTIVE");

        }
    }

    @Test(description = "Verify response of productCondition with Query param status=ACTIVE part1")
    void verifyQueryMerchantMigrationContractDetailsApi_12(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.currency"), "INR");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.value"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.amountInRs"), "productCode");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.amount"), "productName");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("payIntegrationTypes")).isTrue();

            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.orderTimeout"), "4320");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("needLogin")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMergeOrder"), "false");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("payMethods")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleStrategy")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleCycle")).isTrue();
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleCycleStartTime"), "settleCycleStartTime");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleAccountType")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleMethod")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.bankAccountType"), "bankAccountType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardToken"), "cardToken");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleBalanceAccount"), "settleBalanceAccount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardIndexNo"), "cardIndexNo");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleAfterClearingResult"), "false");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.chargebackPayoutAccountSource"), "SETTLEMENT_BALANCE_ACCOUNT_PAYOUT");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.chargebackDefaultPayoutAccount"), "chargebackDefaultPayoutAccount");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportRefund"), "true");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMultiRefund"), "true");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundExpiryTime"), "18");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("pendingOrderRefundSource")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.defaultPendingPayoutAccount"), "defaultPendingPayoutAccount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundPayoutAccounts"), "refundPayoutAccounts");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("completedOrderRefundSource")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("defaultCompletedRefundAccount")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("extendInfo")).isTrue();

        }
    }

    @Test(description = "Verify feeItems of productCondition with Query param status=ACTIVE part2")
    void verifyQueryMerchantMigrationContractDetailsApi_13(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeCalcBasis"), "feeCalcBasis");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeTarget"), "RECEIVER or PAYER");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeCalcMethod"), "feeCalcMethod");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeSettleMode"), "REALTIME");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeRefundRule"), "UNSUPPORTED or DECIDED_BY_MERCHANT");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].payMethodFeeInfos"), "payMethodFeeInfos");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeMode"), "chargeMode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRate"), "taxRate");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRateInfos"), "taxRateInfos");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeCurrency"), "chargeCurrency");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].roundingRule"), "roundingRule");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeItemCode"), "ACQUIRING_SERVICE_FEE or WITHDRAWAL_SERVICE_FEE");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].accountRemark"), "accountRemark");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxCategory"), "taxCategory");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxPriceRelationship"), "taxPriceRelationship");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRateCode"), "taxRateCode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].pricingMethod"), "pricingMethod");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeTargetRules"), "chargeTargetRules");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].enableSubWalletCommission"), "enableSubWalletCommission");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].isPCFFeeOnHold"), "isPCFFeeOnHold");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeOnHold"), "false");


        }
    }

    @Test(description = "Verify response of productCondition with Query param status=ACTIVE part3")
    void verifyQueryMerchantMigrationContractDetailsApi_14(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportPreCreateOrder"), "true");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("orderingMode")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.currency"), "INR");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("stagePaymentType")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("acquiringMode")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleCurrency")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("refundCurrency")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("timeoutForInactiveOrder")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("refundOptions")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("supportMultipleConfirm")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("confirmTimeout")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("supportExcessConfirm")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("excessProportion")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("allowPrnValidation")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("maxPrnValidRetryAllowCount")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("maxPrnValidRetryAllowTime")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("prnExpiryTime")).isTrue();

            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isCancelAllowed"), "false");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxCancelAllowTime"), "1440 or 0");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAgreementContractNumber"), "maxAgreementContractNumber");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.merchantNotifyUrl"), "merchantNotifyUrl");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.smsNotifyUserAfterTxnFailure"), "smsNotifyUserAfterTxnFailure");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.smsNotifyUserAfterTxnSuccess"), "smsNotifyUserAfterTxnSuccess");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.unlinkNeedMerchantAgree"), "unlinkNeedMerchantAgree");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMerchantInitOTP"), "supportMerchantInitOTP");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.agreementConfirmTimeoutRule"), "agreementConfirmTimeoutRule");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.splitMode"), "splitMode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportAgreementPay"), "supportAgreementPay");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleFreeze"), "settleFreeze");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("dccSupport")).isTrue();

        }
    }

    @Test(description = "Verify response of productCondition with Query param status=ACTIVE part4")
    void verifyQueryMerchantMigrationContractDetailsApi_15(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.payConfirmFlowType"), "payConfirmFlowType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.preferredPayConfirmFlowType"), "preferredPayConfirmFlowType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxConfirmTimeout"), "maxConfirmTimeout");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isVoidFeeSupported"), "isVoidFeeSupported");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("enableSubWalletCommission")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isPCFFeeOnHold"), "isPCFFeeOnHold");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.pendingOrderTimeout"), "pendingOrderTimeout");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isChargeBackAllowed"), "isChargeBackAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.aggregationType"), "aggregationType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settlementType"), "settlementType");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("isDynamicChargeTarget")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("instantSettlement")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("supportPrepaidCard")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("cardSubTypeDC")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("cardSubTypeCC")).isTrue();
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.oneClickCardSchemes"), "oneClickCardSchemes");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("oneClickSupported")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.tokenTransactionSupport"), "tokenTransactionSupport");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundPreference"), "refundPreference");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardId"), "cardId");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.qSparcFlowsAllowed"), "qSparcFlowsAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isTransferReversalAllowed"), "isTransferReversalAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.excessAmount"), "excessAmount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportPartialCapture"), "supportPartialCapture");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.timeoutConfirmFundDisposition"), "timeoutConfirmFundDisposition");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isEDCVoidAllowed"), "isEDCVoidAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.voidFeeSupported"), "voidFeeSupported");

        }
    }

    @Test(description = "Verify status is Null in response of contractBasic.contractId.status with Query param status=ACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_16(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(), "ACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList"));
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_PG2_Attribute.getId().toString());
        }
    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api with Query param status=INACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_17(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "INACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("contractDetailList"));
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api with Query param status=InacTiVe")
    void verifyQueryMerchantMigrationContractDetailsApi_18(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "InacTiVe");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("contractDetailList"));
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify contractDetailList will be NULL when Query param with wrong status value hits")
    void verifyQueryMerchantMigrationContractDetailsApi_19(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "cTiVeaIN");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        Assert.assertNull(withDrawJson1.getString("contractDetailList"));
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify status is INACTIVE in response of contractBasic.contractId.status with Query param status=INACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_20(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "INACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.status"), "INACTIVE");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        }
    }

    @Test(description = "Verify response of contractBasic with Query param status=INACTIVE")
    void verifyQueryMerchantMigrationContractDetailsApi_21(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "INACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractId"), "contractId");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productCode"), "productCode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productName"), "productName");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productVersion"), "productVersion");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.signedTime"), "signedTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.effectTime"), "effectTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.expiryTime"), "expiryTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.lastModifier"), "lastModifier");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractStatus"), "EFFECTIVE");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.effectType"), "IMMEDIATE");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.expiryType"), "expiryType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.memo"), "memo");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.externalContractId"), "externalContractId");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.createdTime"), "createdTime");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.modifiedTime"), "modifiedTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.transId"), "transId");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.status"), "INACTIVE");

        }
    }

    @Test(description = "Verify response of productCondition with Query param status=INACTIVE part1")
    void verifyQueryMerchantMigrationContractDetailsApi_22(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "INACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.currency"), "INR");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.value"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.amountInRs"), "productCode");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.amount"), "productName");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.payIntegrationTypes"), "[CASHIER, API]");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("orderTimeout")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("needLogin")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMergeOrder"), "false");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("payMethods")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleStrategy")).isTrue();
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleCycle"), "settleCycle");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleCycleStartTime"), "settleCycleStartTime");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleAccountType")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleMethod"), "ONLINE_SETTLEMENT");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.bankAccountType"), "bankAccountType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardToken"), "cardToken");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleBalanceAccount"), "settleBalanceAccount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardIndexNo"), "cardIndexNo");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleAfterClearingResult"), "false");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.chargebackPayoutAccountSource"), "SETTLEMENT_BALANCE_ACCOUNT_PAYOUT");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.chargebackDefaultPayoutAccount"), "chargebackDefaultPayoutAccount");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportRefund"), "true");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMultiRefund"), "true");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundExpiryTime"), "18");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.pendingOrderRefundSource"), "SETTLEMENT_BALANCE_ACCOUNT_PAYOUT");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.defaultPendingPayoutAccount"), "defaultPendingPayoutAccount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundPayoutAccounts"), "refundPayoutAccounts");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.completedOrderRefundSource"), "SETTLEMENT_BALANCE_ACCOUNT_PAYOUT");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.defaultCompletedRefundAccount"), "defaultCompletedRefundAccount");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.extendInfo"), "{\"isSupportAddPay\":\"Y\"}");

        }
    }

    @Test(description = "Verify feeItems of productCondition with Query param status=INACTIVE part2")
    void verifyQueryMerchantMigrationContractDetailsApi_23(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "INACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeCalcBasis"), "feeCalcBasis");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeTarget"), "RECEIVER or PAYER");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeCalcMethod"), "feeCalcMethod");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeSettleMode"), "REALTIME");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeRefundRule"), "UNSUPPORTED or DECIDED_BY_MERCHANT");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].payMethodFeeInfos"), "payMethodFeeInfos");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeMode"), "chargeMode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRate"), "taxRate");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRateInfos"), "taxRateInfos");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeCurrency"), "chargeCurrency");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].roundingRule"), "roundingRule");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeItemCode"), "ACQUIRING_SERVICE_FEE");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].accountRemark"), "accountRemark");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxCategory"), "taxCategory");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxPriceRelationship"), "taxPriceRelationship");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRateCode"), "taxRateCode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].pricingMethod"), "pricingMethod");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeTargetRules"), "chargeTargetRules");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].enableSubWalletCommission"), "enableSubWalletCommission");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].isPCFFeeOnHold"), "isPCFFeeOnHold");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeOnHold"), "false");


        }
    }

    @Test(description = "Verify response of productCondition with Query param status=INACTIVE part3")
    void verifyQueryMerchantMigrationContractDetailsApi_24(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "INACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportPreCreateOrder"), "true");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("orderingMode")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.currency"), "INR");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("stagePaymentType")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("acquiringMode")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleCurrency")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("refundCurrency")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.timeoutForInactiveOrder"), "10");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundOptions"), "[TO_SOURCE]");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMultipleConfirm"), "supportMultipleConfirm");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.confirmTimeout"), "confirmTimeout");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportExcessConfirm"), "supportExcessConfirm");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.excessProportion"), "excessProportion");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.allowPrnValidation"), "allowPrnValidation");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxPrnValidRetryAllowCount"), "maxPrnValidRetryAllowCount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxPrnValidRetryAllowTime"), "maxPrnValidRetryAllowTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.prnExpiryTime"), "prnExpiryTime");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isCancelAllowed"), "false");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxCancelAllowTime"), "1440 or 0");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAgreementContractNumber"), "maxAgreementContractNumber");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.merchantNotifyUrl"), "merchantNotifyUrl");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.smsNotifyUserAfterTxnFailure"), "smsNotifyUserAfterTxnFailure");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.smsNotifyUserAfterTxnSuccess"), "smsNotifyUserAfterTxnSuccess");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.unlinkNeedMerchantAgree"), "unlinkNeedMerchantAgree");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMerchantInitOTP"), "supportMerchantInitOTP");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.agreementConfirmTimeoutRule"), "agreementConfirmTimeoutRule");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.splitMode"), "splitMode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportAgreementPay"), "supportAgreementPay");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleFreeze"), "settleFreeze");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.dccSupport"), "false");

        }
    }

    @Test(description = "Verify response of productCondition with Query param status=INACTIVE part4")
    void verifyQueryMerchantMigrationContractDetailsApi_25(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.New_Query_MerchantMigration_ContractDetails(Constants.MerchantType.Mapping_Status_Flag.getId().toString(), "INACTIVE");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.payConfirmFlowType"), "payConfirmFlowType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.preferredPayConfirmFlowType"), "preferredPayConfirmFlowType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxConfirmTimeout"), "maxConfirmTimeout");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isVoidFeeSupported"), "isVoidFeeSupported");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("enableSubWalletCommission")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isPCFFeeOnHold"), "isPCFFeeOnHold");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.pendingOrderTimeout"), "pendingOrderTimeout");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isChargeBackAllowed"), "isChargeBackAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.aggregationType"), "aggregationType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settlementType"), "settlementType");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("isDynamicChargeTarget")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("instantSettlement")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("supportPrepaidCard")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("cardSubTypeDC")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("cardSubTypeCC")).isTrue();
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.oneClickCardSchemes"), "oneClickCardSchemes");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("oneClickSupported")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.tokenTransactionSupport"), "tokenTransactionSupport");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundPreference"), "refundPreference");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardId"), "cardId");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.qSparcFlowsAllowed"), "qSparcFlowsAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isTransferReversalAllowed"), "isTransferReversalAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.excessAmount"), "excessAmount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportPartialCapture"), "supportPartialCapture");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.timeoutConfirmFundDisposition"), "timeoutConfirmFundDisposition");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isEDCVoidAllowed"), "isEDCVoidAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.voidFeeSupported"), "voidFeeSupported");

        }
    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api without Query param ")
    void verifyQueryMerchantMigrationContractDetailsApi_26(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        Assert.assertNotNull(withDrawJson1.getString("contractDetailList"));
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api without Query param ")
    void verifyQueryMerchantMigrationContractDetailsApi_27(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultStatus"), "S");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultCodeId"), "00000000");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultCode"), "SUCCESS");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo.resultMsg"), "SUCCESS");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractId"));
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productCode"));
        }
    }

    @Test(description = "Verify Successfull response of New v2 query/merchant/migration/contract/details Api without Query param")
    void verifyQueryMerchantMigrationContractDetailsApi_28() {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s = withDrawJson1.getList("contractDetailList").size();
        for (int i = 0; i < s; i++) {
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].resultInfo"));
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractTemplate"));
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractRelations"));
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].merchantConfiguration"));
        }
    }

    @Test(description = "Verify response of contractBasic.contractId.status without Query param")
    void verifyQueryMerchantMigrationContractDetailsApi_29(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic").contains("status")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        }
    }

    @Test(description = "Verify response of contractBasic without Query param")
    void verifyQueryMerchantMigrationContractDetailsApi_30(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractId"), "contractId");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.merchantId"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productCode"), "productCode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productName"), "productName");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.productVersion"), "productVersion");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.signedTime"), "signedTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.effectTime"), "effectTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.expiryTime"), "expiryTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.lastModifier"), "lastModifier");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.contractStatus"), "EFFECTIVE");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.effectType"), "IMMEDIATE");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.expiryType"), "expiryType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.memo"), "memo");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.externalContractId"), "externalContractId");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.createdTime"), "createdTime");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.modifiedTime"), "modifiedTime");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic.transId"), "transId");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].contractBasic").contains("status")).isTrue();

        }
    }

    @Test(description = "Verify response of productCondition without Query param part1")
    void verifyQueryMerchantMigrationContractDetailsApi_31(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.currency"), "INR");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.value"),Constants.MerchantType.Mapping_Status_Flag.getId().toString());
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.amountInRs"), "productCode");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAmount.amount"), "productName");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("payIntegrationTypes")).isTrue();

            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.orderTimeout"), "4320");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("needLogin")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMergeOrder"), "false");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("payMethods")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleStrategy")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleCycle")).isTrue();
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleCycleStartTime"), "settleCycleStartTime");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleAccountType")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleMethod")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.bankAccountType"), "bankAccountType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardToken"), "cardToken");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleBalanceAccount"), "settleBalanceAccount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardIndexNo"), "cardIndexNo");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleAfterClearingResult"), "false");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.chargebackPayoutAccountSource"), "SETTLEMENT_BALANCE_ACCOUNT_PAYOUT");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.chargebackDefaultPayoutAccount"), "chargebackDefaultPayoutAccount");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportRefund"), "true");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMultiRefund"), "true");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundExpiryTime"), "18");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("pendingOrderRefundSource")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.defaultPendingPayoutAccount"), "defaultPendingPayoutAccount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundPayoutAccounts"), "refundPayoutAccounts");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("completedOrderRefundSource")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("defaultCompletedRefundAccount")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("extendInfo")).isTrue();

        }
    }

    @Test(description = "Verify feeItems of productCondition without Query param part2")
    void verifyQueryMerchantMigrationContractDetailsApi_32(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeCalcBasis"), "feeCalcBasis");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeTarget"), "RECEIVER or PAYER");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeCalcMethod"), "feeCalcMethod");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeSettleMode"), "REALTIME");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeRefundRule"), "UNSUPPORTED or DECIDED_BY_MERCHANT");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].payMethodFeeInfos"), "payMethodFeeInfos");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeMode"), "chargeMode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRate"), "taxRate");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRateInfos"), "taxRateInfos");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeCurrency"), "chargeCurrency");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].roundingRule"), "roundingRule");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeItemCode"), "ACQUIRING_SERVICE_FEE or WITHDRAWAL_SERVICE_FEE");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].accountRemark"), "accountRemark");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxCategory"), "taxCategory");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxPriceRelationship"), "taxPriceRelationship");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].taxRateCode"), "taxRateCode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].pricingMethod"), "pricingMethod");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].chargeTargetRules"), "chargeTargetRules");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].enableSubWalletCommission"), "enableSubWalletCommission");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].isPCFFeeOnHold"), "isPCFFeeOnHold");
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.feeItems["+ 0 +"].feeOnHold"), "false");


        }
    }

    @Test(description = "Verify response of productCondition without Query param part3")
    void verifyQueryMerchantMigrationContractDetailsApi_33(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportPreCreateOrder"), "true");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("orderingMode")).isTrue();
            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.currency"), "INR");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("stagePaymentType")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("acquiringMode")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("settleCurrency")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("refundCurrency")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("timeoutForInactiveOrder")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("refundOptions")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("supportMultipleConfirm")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("confirmTimeout")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("supportExcessConfirm")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("excessProportion")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("allowPrnValidation")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("maxPrnValidRetryAllowCount")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("maxPrnValidRetryAllowTime")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("prnExpiryTime")).isTrue();

            Assert.assertEquals(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isCancelAllowed"), "false");
            Assert.assertNotNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxCancelAllowTime"), "1440 or 0");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxAgreementContractNumber"), "maxAgreementContractNumber");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.merchantNotifyUrl"), "merchantNotifyUrl");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.smsNotifyUserAfterTxnFailure"), "smsNotifyUserAfterTxnFailure");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.smsNotifyUserAfterTxnSuccess"), "smsNotifyUserAfterTxnSuccess");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.unlinkNeedMerchantAgree"), "unlinkNeedMerchantAgree");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportMerchantInitOTP"), "supportMerchantInitOTP");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.agreementConfirmTimeoutRule"), "agreementConfirmTimeoutRule");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.splitMode"), "splitMode");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportAgreementPay"), "supportAgreementPay");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settleFreeze"), "settleFreeze");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("dccSupport")).isTrue();

        }
    }

    @Test(description = "Verify response of productCondition without Query param part4")
    void verifyQueryMerchantMigrationContractDetailsApi_34(){
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_MerchantMigration_ContractDetails_V2(Constants.MerchantType.Mapping_Status_Flag.getId().toString());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyQueryMerchantMigrationContractDetails(objectHead);
        int s=withDrawJson1.getList("contractDetailList").size();
        for(int i=0;i<s;i++)
        {
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.payConfirmFlowType"), "payConfirmFlowType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.preferredPayConfirmFlowType"), "preferredPayConfirmFlowType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.maxConfirmTimeout"), "maxConfirmTimeout");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isVoidFeeSupported"), "isVoidFeeSupported");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("enableSubWalletCommission")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isPCFFeeOnHold"), "isPCFFeeOnHold");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.pendingOrderTimeout"), "pendingOrderTimeout");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isChargeBackAllowed"), "isChargeBackAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.aggregationType"), "aggregationType");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.settlementType"), "settlementType");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("isDynamicChargeTarget")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("instantSettlement")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("supportPrepaidCard")).isTrue();

            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("cardSubTypeDC")).isTrue();
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("cardSubTypeCC")).isTrue();
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.oneClickCardSchemes"), "oneClickCardSchemes");
            Assertions.assertThat(withDrawJson1.getString("contractDetailList["+ i +"].productCondition").contains("oneClickSupported")).isTrue();

            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.tokenTransactionSupport"), "tokenTransactionSupport");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.refundPreference"), "refundPreference");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.cardId"), "cardId");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.qSparcFlowsAllowed"), "qSparcFlowsAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isTransferReversalAllowed"), "isTransferReversalAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.excessAmount"), "excessAmount");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.supportPartialCapture"), "supportPartialCapture");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.timeoutConfirmFundDisposition"), "timeoutConfirmFundDisposition");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.isEDCVoidAllowed"), "isEDCVoidAllowed");
            Assert.assertNull(withDrawJson1.getString("contractDetailList["+ i +"].productCondition.voidFeeSupported"), "voidFeeSupported");

        }
    }
}
