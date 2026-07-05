package scripts.api.mappingService.merchantInfo

import com.paytm.appconstants.Constants
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.DbQueriesUtil
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_MERCHANT_REQUEST_TYPES
import static com.paytm.appconstants.Constants.MerchantType.WITHOUT_REQ_TYPE
import static com.paytm.appconstants.Constants.Owner.GAGANDEEP
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

@Owner(GAGANDEEP)
class GetMerchantRequestTypesTest extends TestSetUp {

    private static final String MID_TYPE = 'midType'
    private static final String PAYTM_MID = 'paytmMid'
    private static final String midType = 'paytm'
    private static final String invalidPaytmMid = 'RJSSS232d91233124'

    private def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .addFilters([schemaFilter])
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GET_MERCHANT_REQUEST_TYPES)
                        .build()
        )
    }


    private static def getExpectedRequestTypes(String merchantId) {
        DbQueriesUtil.selectFromPaytmPGDB('SELECT LD.value AS REQ_TYPE_ID FROM ENTITY_REQ_TYPE ERQ join LOOKUP_DATA LD ' +
                'on ERQ.REQ_TYPE_ID = LD.LOOKUP_ID WHERE ERQ.ENTITY_ID = (select id from ENTITY_INFO where mid ="' + merchantId + '") ')
    }


    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/merchant-request-type.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    @Merchant(edit = true)
    @Test
    void 'test Merchant V1 api for paytm user with Database'() {
        List expectedRequestTypes
        REDIS:
        {
            String redisKey = 'M_REQ_TYPES_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        QUERY:
        {
            expectedRequestTypes = getExpectedRequestTypes(m().id)*.values().flatten()
        }
        req().pathParam(MID_TYPE, midType)
                .pathParam(PAYTM_MID, m().id).get().then()
                .statusCode(200)
                .body('requestTypes', equalTo(expectedRequestTypes))
    }

    @Merchant(edit = true)
    @Test
    void 'test Merchant V1 api for paytm user with Redis'() {
        List expectedRequestTypes
        REDIS:
        {
            String redisKey = 'M_REQ_TYPES_' + m().id
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam(MID_TYPE, midType)
                        .pathParam(PAYTM_MID, m().id).get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }
        }
        QUERY:
        {
            expectedRequestTypes = getExpectedRequestTypes(m().id)*.values().flatten()
        }
        req().pathParam(MID_TYPE, midType)
                .pathParam(PAYTM_MID, m().id).get().then()
                .statusCode(200)
                .body('requestTypes', equalTo(expectedRequestTypes))
    }


    @Merchant
    @Test
    void 'Validate response when paytm id is blank'() {
        req().pathParam(MID_TYPE, '')
                .pathParam(PAYTM_MID, m().id).get().then()
                .statusCode(404)
                .body('code', equalTo(404),
                        'message', equalTo('HTTP 404 Not Found'))
    }


    @Test
    void 'Validate response when mid id is blank'() {
        req().pathParam(MID_TYPE, midType)
                .pathParam(PAYTM_MID, '').get().then()
                .statusCode(404)
                .body('code', equalTo(404),
                        'message', equalTo('HTTP 404 Not Found'))
    }


    @Test
    void 'Validate response when paytm id is invalid'() {
        req().pathParam(MID_TYPE, midType)
                .pathParam(PAYTM_MID, invalidPaytmMid).get().then()
                .body('code', equalTo(500),
                        'message', equalTo('Mid does not exist'))
    }


    @Merchant(edit = true, value = { it.id == WITHOUT_REQ_TYPE.id })
    @Test//TODO Gagandeep, you have to fetch the merchant from DB directly as this merchant is getting picked by other TC's and so failing them
    void 'Validate response of the API when no request type present on the MID'() {
        req().pathParam(MID_TYPE, midType)
                .pathParam(PAYTM_MID, Constants.MerchantType.NO_REQUEST_TYPE_MID.getId()).get().then()
                .body('code', equalTo(500),
                        'message', equalTo('No request types available for this mid'))
    }
}
