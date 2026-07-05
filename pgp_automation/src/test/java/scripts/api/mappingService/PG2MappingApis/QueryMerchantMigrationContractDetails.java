package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class QueryMerchantMigrationContractDetails extends PGPBaseTest {

    @Test(groups = "QueryMerchantMigrationContractDetailsApi", description = "Verify QueryMerchantMigrationContractDetails API Success response ")
    void verifyQueryMerchantMigrationContractDetailsApiSuccessResponse() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
            mappingApisPG2.Query_merchant_migration_contract_detail("qa14pg55055186160241");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();

        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"), "Success");

    }
    @Test(dependsOnGroups = "QueryMerchantMigrationContractDetailsApi",description = "Verify QueryMerchantMigrationContractDetails API  response ")
    void verifyQueryMerchantMigrationContractDetailsApiContractBasicSuccessResp() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail("qa14pg55055186160241");

        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].resultInfo.resultStatus"), "S");
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].resultInfo.resultCodeId"), "00000000");
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].resultInfo.resultCode"), "SUCCESS");
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].resultInfo.resultMsg"), "SUCCESS");
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].resultInfo.retryable"), "false");

    }

    @Test(dependsOnGroups = "QueryMerchantMigrationContractDetailsApi",description = "Verify QueryMerchantMigrationContractDetails API  response ")
    void verifyQueryMerchantMigrationContractDetailsApiContractBasic() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        MappingApisPG2 mappingApisPG=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail("qa14pg55055186160241");
        mappingApisPG.Query_merchant_migration_contract_detail_PG2("qa14pg55055186160241");

        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        JsonPath expectedResponse=mappingApisPG.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].contractBasic.contractId"), expectedResponse.getString("body.contractDetails[0].contractBasic.contractId"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].contractBasic.merchantId"), expectedResponse.getString("body.contractDetails[0].contractBasic.merchantId"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].contractBasic.productVersion"), expectedResponse.getString("body.contractDetails[0].contractBasic.productVersion"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].contractBasic.productCode"), expectedResponse.getString("body.contractDetails[0].contractBasic.productCode"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].contractBasic.contractStatus"), expectedResponse.getString("body.contractDetails[0].contractBasic.contractStatus"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].contractBasic.effectType"), expectedResponse.getString("body.contractDetails[0].contractBasic.effectType"));

    }

    @Test(dependsOnGroups = "QueryMerchantMigrationContractDetailsApi",description = "Verify QueryMerchantMigrationContractDetails API  response ")
    void verifyQueryMerchantMigrationContractDetailsApiProductCondition() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        MappingApisPG2 mappingApisPG=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail("qa14pg55055186160241");
        mappingApisPG.Query_merchant_migration_contract_detail_PG2("qa14pg55055186160241");

        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        JsonPath expectedResponse=mappingApisPG.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.maxAmount.currency"),expectedResponse.getString("body.contractDetails[0].productCondition.maxAmount.currency"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.maxAmount.value"),expectedResponse.getString("body.contractDetails[0].productCondition.maxAmount.value"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.payIntegrationTypes"),expectedResponse.getString("body.contractDetails[0].productCondition.payIntegrationTypes"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.orderTimeout"),expectedResponse.getString("body.contractDetails[0].productCondition.orderTimeout"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.supportMergeOrder"),expectedResponse.getString("body.contractDetails[0].productCondition.supportMergeOrder"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.needLogin"),expectedResponse.getString("body.contractDetails[0].productCondition.needLogin"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.payMethods"),expectedResponse.getString("body.contractDetails[0].productCondition.payMethods"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.settleStrategy"),expectedResponse.getString("body.contractDetails[0].productCondition.settleStrategy"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.settleCycle"),expectedResponse.getString("body.contractDetails[0].productCondition.settleCycle"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.settleMethod"),expectedResponse.getString("body.contractDetails[0].productCondition.settleMethod"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.settleAccountType"),expectedResponse.getString("body.contractDetails[0].productCondition.settleAccountType"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.bankAccountType"),expectedResponse.getString("body.contractDetails[0].productCondition.bankAccountType"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.settleAfterClearingResult"),expectedResponse.getString("body.contractDetails[0].productCondition.settleAfterClearingResult"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.chargebackPayoutAccountSource"),expectedResponse.getString("body.contractDetails[0].productCondition.chargebackPayoutAccountSource"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.supportRefund"),expectedResponse.getString("body.contractDetails[0].productCondition.supportRefund"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.supportMultiRefund"),expectedResponse.getString("body.contractDetails[0].productCondition.supportMultiRefund"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.refundExpiryTime"),expectedResponse.getString("body.contractDetails[0].productCondition.refundExpiryTime"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.pendingOrderRefundSource"),expectedResponse.getString("body.contractDetails[0].productCondition.pendingOrderRefundSource"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.completedOrderRefundSource"),expectedResponse.getString("body.contractDetails[0].productCondition.completedOrderRefundSource"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.extendInfo"),expectedResponse.getString("body.contractDetails[0].productCondition.extendInfo"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.supportPreCreateOrder"),expectedResponse.getString("body.contractDetails[0].productCondition.supportPreCreateOrder"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.currency"),expectedResponse.getString("body.contractDetails[0].productCondition.currency"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.timeoutForInactiveOrder"),expectedResponse.getString("body.contractDetails[0].productCondition.timeoutForInactiveOrder"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.refundOptions"),expectedResponse.getString("body.contractDetails[0].productCondition.refundOptions"));
        Assert.assertEquals(actualResponse.getString("contractDetailList[0].productCondition.supportMultipleConfirm"),expectedResponse.getString("body.contractDetails[0].productCondition.supportMultipleConfirm"));

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midBlank() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_migration_contract_detail("");

        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("code"),"404");
        Assert.assertEquals(actualResponse.getString("message"),"HTTP 404 Not Found");
        Assert.assertEquals(actualResponse.getString("offendingPath"),"ServerRuntime.javaorg.glassfish.jersey.server.ServerRuntime$2");
        Assert.assertEquals(actualResponse.getString("offendingMethod"), "run");
        Assert.assertEquals(actualResponse.getString("offendingLine"),"323");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midNull() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Query_merchant_migration_contract_detail("null");
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultStatus"),"F");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.messaage"),"Entry is not available ");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midSpace() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Query_merchant_migration_contract_detail(" ");
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();

        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultStatus"),"F");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.messaage"),"Entry is not available ");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midRandom() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Query_merchant_migration_contract_detail("Random1234");
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultStatus"),"F");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.messaage"),"Entry is not available ");

    }
}
