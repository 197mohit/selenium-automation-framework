package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MerchantQueryContractDetail extends PGPBaseTest {

    @Test(description = "Verify Successfull response of Merchant Query Contract Detail Api With Product code")
    void verifySuccessfullResponseOfQueryContractItemApi() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Merchant_query_contract_details(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");

    }
    @Test(description = "Verify Successfull response of Merchant Query Contract Detail Api With Product code")
    void verifySuccessfullResponseOfResultInfoInApi() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        MappingApisPG2 mappingApisPG=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        mappingApisPG.Merchant_query_contract_details_PG(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        JsonPath expectedResponse=mappingApisPG.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("response.resultInfo.resultStatus"),expectedResponse.getString("resultInfo.resultStatus"));
        Assert.assertEquals(actualResponse.getString("response.resultInfo.resultCodeId"),expectedResponse.getString("resultInfo.resultCodeId"));
        Assert.assertEquals(actualResponse.getString("response.resultInfo.resultCode"),expectedResponse.getString("resultInfo.resultMsg"));
        Assert.assertEquals(actualResponse.getString("response.resultInfo.resultMsg"),expectedResponse.getString("resultInfo.resultMsg"));
        Assert.assertEquals(actualResponse.getString("response.resultInfo.retryable"),"false");

    }
    @Test(description = "Verify contractBasic Details Schema")
    void MerchantQueryContractdetail_02() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        MappingApisPG2 mappingApisPG=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        mappingApisPG.Merchant_query_contract_details_PG(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        JsonPath expectedResponse=mappingApisPG.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("response.contractBasic.contractId"),expectedResponse.getString("body.contractDetails[0].contractBasic.contractId"));
        Assert.assertEquals(actualResponse.getString("response.contractBasic.merchantId"),expectedResponse.getString("body.contractDetails[0].contractBasic.merchantId"));
        Assert.assertEquals(actualResponse.getString("response.contractBasic.productCode"),expectedResponse.getString("body.contractDetails[0].contractBasic.productCode"));
        Assert.assertEquals(actualResponse.getString("response.contractBasic.productVersion"),expectedResponse.getString("body.contractDetails[0].contractBasic.productVersion"));
        Assert.assertEquals(actualResponse.getString("response.contractBasic.contractStatus"),expectedResponse.getString("body.contractDetails[0].contractBasic.contractStatus"));
        Assert.assertEquals(actualResponse.getString("response.contractBasic.effectType"),expectedResponse.getString("body.contractDetails[0].contractBasic.effectType"));

    }
    @Test(description = "Verify productConditions Details Schema")
    void MerchantQueryContractdetail_03() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        MappingApisPG2 mappingApisPG=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        mappingApisPG.Merchant_query_contract_details_PG(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        JsonPath expectedResponse=mappingApisPG.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("response.productCondition.maxAmount.currency"),expectedResponse.getString("body.contractDetails[0].productCondition.maxAmount.currency"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.maxAmount.value"),expectedResponse.getString("body.contractDetails[0].productCondition.maxAmount.value"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.payIntegrationTypes"),expectedResponse.getString("body.contractDetails[0].productCondition.payIntegrationTypes"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.orderTimeout"),expectedResponse.getString("body.contractDetails[0].productCondition.orderTimeout"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.needLogin"),expectedResponse.getString("body.contractDetails[0].productCondition.needLogin"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.supportMergeOrder"),expectedResponse.getString("body.contractDetails[0].productCondition.supportMergeOrder"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.payMethods"),expectedResponse.getString("body.contractDetails[0].productCondition.payMethods"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.settleStrategy"),expectedResponse.getString("body.contractDetails[0].productCondition.settleStrategy"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.settleCycle"),expectedResponse.getString("body.contractDetails[0].productCondition.settleCycle"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.settleMethod"),expectedResponse.getString("body.contractDetails[0].productCondition.settleMethod"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.settleAccountType"),expectedResponse.getString("body.contractDetails[0].productCondition.settleAccountType"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.bankAccountType"),expectedResponse.getString("body.contractDetails[0].productCondition.bankAccountType"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.settleAfterClearingResult"),expectedResponse.getString("body.contractDetails[0].productCondition.settleAfterClearingResult"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.chargebackPayoutAccountSource"),expectedResponse.getString("body.contractDetails[0].productCondition.chargebackPayoutAccountSource"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.supportRefund"),expectedResponse.getString("body.contractDetails[0].productCondition.supportRefund"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.supportMultiRefund"),expectedResponse.getString("body.contractDetails[0].productCondition.supportMultiRefund"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.refundExpiryTime"),expectedResponse.getString("body.contractDetails[0].productCondition.refundExpiryTime"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.pendingOrderRefundSource"),expectedResponse.getString("body.contractDetails[0].productCondition.pendingOrderRefundSource"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.completedOrderRefundSource"),expectedResponse.getString("body.contractDetails[0].productCondition.completedOrderRefundSource"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.extendInfo"),expectedResponse.getString("body.contractDetails[0].productCondition.extendInfo"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.supportPreCreateOrder"),expectedResponse.getString("body.contractDetails[0].productCondition.supportPreCreateOrder"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.currency"),expectedResponse.getString("body.contractDetails[0].productCondition.currency"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.timeoutForInactiveOrder"),expectedResponse.getString("body.contractDetails[0].productCondition.timeoutForInactiveOrder"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.refundOptions"),expectedResponse.getString("body.contractDetails[0].productCondition.refundOptions"));
        Assert.assertEquals(actualResponse.getString("response.productCondition.supportMultipleConfirm"),expectedResponse.getString("body.contractDetails[0].productCondition.supportMultipleConfirm"));

    }

    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midBlank() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details("","EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("code"),"404");
        Assert.assertEquals(actualResponse.getString("message"),"HTTP 404 Not Found");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midNull() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details("null","EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultStatus"),"F");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.messaage"),"Entry is not available ");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midSpace() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details("null","EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();

        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultStatus"),"F");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.messaage"),"Entry is not available ");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_midRandom() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details("Random1234","EFFECTIVE",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultStatus"),"F");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.messaage"),"Entry is not available ");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_ContractStatusNull() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details("Random1234","null",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("code"),"404");
        Assert.assertEquals(actualResponse.getString("message"),"HTTP 404 Not Found");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_ContractStatusSpace() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details("Random1234","null",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("code"),"404");
        Assert.assertEquals(actualResponse.getString("message"),"HTTP 404 Not Found");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_ContractStatusRandom() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details("Random1234","Random1234",Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getProductCode());
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("code"),"404");
        Assert.assertEquals(actualResponse.getString("message"),"HTTP 404 Not Found");

    }
    @Test(description = "Verify contractBasic Details Schema Negative Cases")
    void MerchantQueryContractdetailNegativeCases_productCodeNull() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();

        mappingApisPG2.Merchant_query_contract_details(Constants.MerchantType.Mapping_PG2_AttributeWithoutPaymodes.getId().toString(),"EFFECTIVE","null");
        JsonPath actualResponse=mappingApisPG2.execute().jsonPath();
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultCode"),"00001");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.resultStatus"),"F");
        Assert.assertEquals(actualResponse.getString("paytmResultInfo.messaage"),"Entry is not available ");

    }

    @Test(description = "Verify Successfull response of Merchant Query Contract Detail Api With Product code")
    void MerchantQueryContractdetail_04() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_contract_details(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"EFFECTIVE","51051000100000000001");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.messaage"),"Success");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultStatus"),"S");
        Assert.assertEquals(withDrawJson1.getString("paytmResultInfo.resultCode"),"00000");

    }
    @Test(description = "Verify contractBasic is not null")
    void MerchantQueryContractdetail_05() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_contract_details(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"EFFECTIVE","51051000100000000001");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead= withDrawJson1.getMap("response.contractBasic");
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyContractBasicsResponseJson(objectHead);

    }
    @Test(description = "Verify productCondition is not null")
    void MerchantQueryContractdetail_06() throws InterruptedException {
        MappingApisPG2 mappingApisPG2=new MappingApisPG2();
        mappingApisPG2.Query_merchant_contract_details(Constants.MerchantType.Mapping_PG2_Attribute.getId().toString(),"EFFECTIVE","51051000100000000001");
        JsonPath withDrawJson1=mappingApisPG2.execute().jsonPath();
        PG2MappingApisHelper pg2MappingApisHelper=new PG2MappingApisHelper();
        Assert.assertNotNull(withDrawJson1.getString("response.productCondition"));

    }
}
