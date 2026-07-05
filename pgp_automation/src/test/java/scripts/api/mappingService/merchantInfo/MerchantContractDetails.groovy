package scripts.api.mappingService.merchantInfo

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.api.MappingService.MappingAlipayApi.getContractDetails
import static com.paytm.appconstants.Constants.MappingService.MERCHANT_CONTRACT_DETAILS
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo


class MerchantContractDetails extends TestSetUp {

    protected def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(MERCHANT_CONTRACT_DETAILS)
                        .build()
        )
    }

    //No REDIS KEY is Stored for This API

    @Merchant
    @Test
    void 'test Contract Basic for Merchant Contract Details Api'() {

        def query
        def resp
        def contractId = m().contracts[0].id


        QUERY:
        {
            query = getContractDetails(contractId)['contractBasic']
        }

        API:
        {
            resp = req().pathParam('contractId', contractId)
                    .get().then().root('response.contractBasic')

        }

        VALIDATION:
        {
            resp.body('contractStatus', equalTo(query.get('contractStatus')),
                    "productName", equalTo(query.get('productName')),
                    'merchantId', equalTo(query.get('merchantId')),
                    'contractId', equalTo(query.get('contractId')))
        }


    }


    @Merchant
    @Test
    void 'test Product Condition for Merchant Contract Details Api'() {

        def query
        def resp
        def contractId = m().contracts[0].id

        QUERY:
        {
            query = getContractDetails(contractId)['productCondition']['payMethods']
        }
        API:
        {
            resp = req().pathParam('contractId', contractId)
                    .get().then().extract().jsonPath().get('response.productCondition.payMethods')

        }
        VALIDATION:
        {
            assert resp == query
        }


    }


    @Merchant
    @Test
    void 'test refund details for Merchant api Details Api'() {

        def query
        def resp
        def contractId = m().contracts[0].id

        QUERY:
        {
            query = getContractDetails(contractId)['productCondition']['refundOptions']
        }
        API:
        {
            resp = req().pathParam('contractId', contractId)
                    .get().then().extract().jsonPath().get('response.productCondition.refundOptions')

        }
        VALIDATION:
        {
            assert resp == query
        }


    }
}