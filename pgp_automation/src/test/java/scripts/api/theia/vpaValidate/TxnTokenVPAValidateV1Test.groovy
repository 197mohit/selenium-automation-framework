package scripts.api.theia.vpaValidate

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
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
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import scripts.api.theia.InitiateTransaction
import scripts.api.theia.TxnTokenAuthenticationTest

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.VPA_VALIDATE
import static com.paytm.appconstants.Constants.Owner.PULKIT
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class TxnTokenVPAValidateV1Test extends TestSetUp implements VPAValidateTest, TxnTokenAuthenticationTest {

    final ThreadLocal<String> orderId = new ThreadLocal<>()
    final ThreadLocal<String> token = new ThreadLocal<>()
    private final static VALID_VPA = '9999661503@paytm'
    private final static INVALID_VPA = 'a' * 10

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilter(setQueryParamMidFilter)
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VPA_VALIDATE)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', orderId.get() ?: UUID.randomUUID().toString())
    }

    RequestSpecification req() {
        given(reqBldr().build())
    }

    Map root() {
        [
                head: [
                        version         : "v1",
                        requestTimestamp: System.currentTimeMillis().toString(),
                        requestId       : UUID.randomUUID().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'TXN_TOKEN',
                        token           : token.get(),
                ],
                body: [
                        vpa: VALID_VPA,
                        mid: m()?.id,
                ]
        ]
    }

    Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    ResponseSpecification resSpec = new ResponseSpecBuilder()
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/vpa-validate-v1-schema.json'))
            .build()

    ResponseSpecification resSpecFailure = new ResponseSpecBuilder()
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/VpaFailure.json'))
            .build()

    @BeforeMethod
    void setTxnToken(Method method, ITestResult testResult) {
        try {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = 2
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString()
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when query params are not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when mid in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when orderId in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.orderIdInQueryParamNotMatchingWithOrderIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test unable to validate vpa when body vpa is not provided'() {
        def root = root()
        root.body.remove('vpa')
        req().body(root).post().then()
                .spec(resSpec)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                        'resultCode', equalTo('0'),
                        'resultMsg', equalTo('System Error, invalid param'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test unable to validate vpa when body vpa = null'() {
        def root = root()
        root.body.vpa = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.systemErrorInvalidParam as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test unable to validate vpa when body vpa = \'\''() {
        def root = root()
        root.body.vpa = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.systemErrorInvalidParam as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @AUser
    @Test
    void 'test unable to validate vpa when body vpa equals random alphabetical value'() {
        def root = root()
        root.body.vpa = 'a' * 10
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.invalidUpiId as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test unable to validate vpa when body vpa equals random numerical value'() {
        def root = root()
        root.body.vpa = '1' * 10
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.invalidUpiId as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test unable to validate vpa when body vpa equals value having pattern equals upiHandler@psp'() {
        def root = root()
        root.body.vpa = 'a@a'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.unsuccessfulPaymentRequestTryAgain as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @Test
    void 'test unable to validate vpa when body vpa equals vpa whose psp is not registered'() {
        def root = root()
        root.body.vpa = 'test@google'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.unsuccessfulPaymentRequestTryAgain as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test able to validate vpa when body vpa equals paytm vpa'() {
        def root = root()
        root.body.vpa = 'srivastavaprateek@paytm'
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(resSpecFailure)

    }


    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType is not provided'() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType = null'() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType equals random value'() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant({it.id == Constants.MerchantType.MLV.getId()}),
            @Merchant(edit = true)
    ])
    @Test
    void 'test when head token equals txn token generated using different mid'() {
        def root = root()
        root.body.mid = m(1).id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head token equals txn token generated using different orderId'() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').addQueryParam('orderId', UUID.randomUUID().toString()).build())
                .body(root).post().then()
                .spec(results.orderIdInQueryParamNotMatchingWithOrderIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @Test
    void 'test when head token equals txn token generated for OFFUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @AUser
    @Test
    void 'test when head token equals txn token generated for ONUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Owner(PULKIT)
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void "test that the API fails after 5 hits with valid VPA"(){
        def root = root()
        root.body.vpa = VALID_VPA
        5.times {
            req().body(root).post().then()
                    .spec(resSpec)
                    .spec(results.success as ResponseSpecification)
        }
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus',equalTo("F"))
                .body('body.resultInfo.resultCode',equalTo("429"))
                .body('body.resultInfo.resultMsg',equalTo("Rate Limiting breached, not proceeding with the request"))
    }

    @Owner(PULKIT)
    @Merchant({it.id == Constants.MerchantType.UPI_CONSENT_ADD.getId()})
    @AUser
    @Test
    void "test that the API fails after 5 hits with invalid VPA"(){
        def root = root()
        root.body.vpa = INVALID_VPA
        5.times {
            req().body(root).post().then()
                    .spec(resSpec)
                    .spec(results.invalidUpiId as ResponseSpecification)
        }
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus',equalTo("F"))
                .body('body.resultInfo.resultCode',equalTo("429"))
                .body('body.resultInfo.resultMsg',equalTo("Rate Limiting breached, not proceeding with the request"))
    }

    @Override
    @Merchants([
            @Merchant({it.id == Constants.MerchantType.UPI_INTENT.getId()}),
            @Merchant
    ])
    @Test
    void "test when mid provided in query params is different from mid provided in request body"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }
}
