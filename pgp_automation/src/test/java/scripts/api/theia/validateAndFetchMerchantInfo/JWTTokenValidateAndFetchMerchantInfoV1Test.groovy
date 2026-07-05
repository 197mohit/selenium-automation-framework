package scripts.api.theia.validateAndFetchMerchantInfo

import com.auth0.jwt.JWT
import com.paytm.appconstants.Constants
import com.auth0.jwt.algorithms.Algorithm
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.JWT_KEY
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.VALIDATE_AND_FETCH_MERCHANT_INFO
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.*
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class JWTTokenValidateAndFetchMerchantInfoV1Test extends TestSetUp implements ValidateAndFetchMerchantInfoTest {

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilter(setTokenFilter)
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VALIDATE_AND_FETCH_MERCHANT_INFO)
    }

    RequestSpecification req() {
        given(reqBldr().build())
    }

    Map root() {
        [
                head: [
                        requestId       : UUID.randomUUID().toString(),
                        requestTimeStamp: System.currentTimeMillis().toString(),
                        clientId        : UUID.randomUUID().toString(),
                        version         : 'v1',
                        tokenType       : 'JWT',
                        token           : '?'
                ],
                body: [
                        mids: [m(0).id]
                ]
        ]
    }

    Filter setTokenFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = JWT.create().withIssuer('ts').withClaim('mids', root.body.mids?.join(',') ?: '').sign(Algorithm.HMAC256(JWT_KEY))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    ResponseSpecification resSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/validate-and-fetch-merchant-info-v1-schema.json'))
            .build()

    private static class ResultInfo {
        static final ResponseSpecification INVALID_INPUT_DATA = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCodeId', equalTo(null))
                .expectBody('resultCode', equalTo('INVALID_INPUT_DATA'))
                .expectBody('resultMsg', equalTo('Invalid data entered by user'))
                .build()
        static final ResponseSpecification PROCESS_FAIL = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCodeId', equalTo(null))
                .expectBody('resultCode', equalTo('PROCESS_FAIL'))
                .expectBody('resultMsg', equalTo('PROCESS FAIL'))
                .build()
        static final ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('S'))
                .expectBody('resultCodeId', equalTo(null))
                .expectBody('resultCode', equalTo('00000'))
                .expectBody('resultMsg', equalTo('Success'))
                .build()
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids is not provided'() {
        def root = root()
        root.body.remove('mids')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_INPUT_DATA)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids = null'() {
        def root = root()
        root.body.mids = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_INPUT_DATA)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids = \'\''() {
        def root = root()
        root.body.mids = ''
        req().body(root).post().then()
                .statusCode(200)
                .body('html.body.toString()', stringContainsInOrder(['Something went wrong', 'It may be due to any of these reasons', 'Session expired due to inactivity', 'Our system encountered an obstacle']))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids equals empty list'() {
        def root = root()
        root.body.mids = []
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_INPUT_DATA)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids equals iterable containing single random value'() {
        def root = root()
        root.body.mids = [UUID.randomUUID().toString()]
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.PROCESS_FAIL)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids equals iterable containing multiple random values'() {
        def root = root()
        root.body.mids = (0..1).collect { UUID.randomUUID().toString() }
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.PROCESS_FAIL)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids equals iterable containing single valid mid'() {
        def root = root()
        root.body.mids = [m().id]
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchants([
            @Merchant({it.id == Constants.MerchantType.PGOnly.getId()}),
            @Merchant
    ])
    @Test
    void 'test when body mids equals iterable containing multiple valid mids'() {
        def root = root()
        root.body.mids = [m(0).id, m(1).id]
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    void 'test when body mids equals iterable containing both single valid mid and null'() {
        def root = root()
        root.body.mids = [m(0).id, null]
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    void 'test when body mids equals iterable containing both single valid mid and \'\''() {
        def root = root()
        root.body.mids = [m(0).id, '']
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mids equals iterable containing both single valid mid and a random value'() {
        def root = root()
        root.body.mids = [m(0).id, UUID.randomUUID().toString()]
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head clientId is not provided'() {
        def root = root()
        root.head.remove('clientId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head clientId = null'() {
        def root = root()
        root.head.clientId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head clientId = \'\''() {
        def root = root()
        root.head.clientId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head clientId equals random value'() {
        def root = root()
        root.head.clientId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(groups = "Security")
    void 'test when head tokenType is not provided'() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(groups = "Security")
    void 'test when head tokenType = null'() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(groups = "Security")
    void 'test when head tokenType = \'\''() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test(groups = "Security")
    void 'test when head tokenType equals random value'() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }
}
