package scripts.api.theia.vpaValidate

import com.paytm.LocalConfig
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
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
import redis.clients.jedis.Jedis
import scripts.api.theia.AccessAuthenticationTest
import scripts.api.theia.CreateToken

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.VPA_VALIDATE
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class AccessTokenVPAValidateV1Test extends TestSetUp implements VPAValidateTest, AccessAuthenticationTest {

    private final ThreadLocal<String> token = new ThreadLocal<>()

    private final ThreadLocal<String> referenceId = new ThreadLocal<>()

    private Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    private RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VPA_VALIDATE)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', UUID.randomUUID().toString())
                .addQueryParam('referenceId', referenceId.get())
    }

    private RequestSpecification req() {
        given(reqBldr().build())
    }

    private Map root() {
        [
                head: [
                        version         : "v1",
                        requestTimestamp: System.currentTimeMillis().toString(),
                        requestId       : UUID.randomUUID().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'ACCESS',
                        token           : token.get(),
                ],
                body: [
                        vpa: '9999661503@paytm',
                        mid: m()?.id,
                ]
        ]
    }

    ResponseSpecification resSpec = new ResponseSpecBuilder()
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/vpa-validate-v1-schema.json'))
            .build()

    private static final class ResultInfo {
        static ResponseSpecification TOKEN_VALIDATION_FAILED = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1002'))
                .expectBody('resultMsg', equalTo('TOKEN_VALIDATION_FAILED'))
                .build()
        static ResponseSpecification REFERENCE_ID_NOT_VALID = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo('Request parameters are not valid. ReferenceId should fulfill: Minimum length of 10 and maximum length of 20.'))
                .build()
        static ResponseSpecification MISSING_MANDATORY_ELEMENT = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1007'))
                .expectBody('resultMsg', equalTo('Missing mandatory element.'))
                .build()
    }

    @BeforeMethod
    void setAccessToken(Method method, ITestResult testResult) {
        try {
            CreateToken api = new CreateToken()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            referenceId.set(root.body.referenceId)
            api.req().body(root).post().then()
                    .spec(results.success as ResponseSpecification)
                    .extract().path('body.accessToken').tap { if (!it) throw new SkipException('unable to generate access token') }.with { token.set(it as String) }
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
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test unable to validate vpa when body vpa = null'() {
        def root = root()
        root.body.vpa = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.systemErrorInvalidParam as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test unable to validate vpa when body vpa = \'\''() {
        def root = root()
        root.body.vpa = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.systemErrorInvalidParam as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('1002'),
                        'resultMsg', equalTo('TOKEN_VALIDATION_FAILED'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
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

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MISSING_MANDATORY_ELEMENT)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MISSING_MANDATORY_ELEMENT)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head token = \'\''() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MISSING_MANDATORY_ELEMENT)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head token equals random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() })
    @Test
    void 'test when head token equals expired token'() {
        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_URI)
        String key = jedis.keys("AccessTokenRequest_${m().id}_*")[0]
        jedis.del(key)
        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head token equals access token generated for OFFUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test when head token equals access token generated for ONUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when referenceId in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('referenceId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.TOKEN_VALIDATION_FAILED)
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when referenceId provided while creating access token is different from referenceId provided in query params'() {
        def root = root()
        given(reqBldr().removeQueryParam('referenceId').addQueryParam('referenceId', UUID.randomUUID().toString()[0..19]).build()).body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.TOKEN_VALIDATION_FAILED)
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() }),
            @Merchant
    ])
    @Test
    void "test when mid provided in query params is different from mid provided in request body"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() }),
            @Merchant
    ])
    @Test
    void 'test when head token equals access token generated by different merchant'() {
        def root = root()
        root.body.mid = m(1).id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.TOKEN_VALIDATION_FAILED)
    }
}