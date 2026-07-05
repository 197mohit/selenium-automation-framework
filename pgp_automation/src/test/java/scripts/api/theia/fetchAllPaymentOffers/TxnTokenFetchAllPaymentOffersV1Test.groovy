package scripts.api.theia.fetchAllPaymentOffers

import com.paytm.LocalConfig
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.utils.RedisUtil
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.junit.Assert
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import redis.clients.jedis.Jedis
import scripts.api.theia.InitiateTransaction
import scripts.api.theia.TxnTokenAuthenticationTest

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_ALL_PAYMENT_OFFERS
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class TxnTokenFetchAllPaymentOffersV1Test extends TestSetUp implements FetchAllPaymentOffersTest, TxnTokenAuthenticationTest {

    private final static PROMOS_NOT_ADDED = "unable to add promo(s) successfully"

    private final ThreadLocal<String> orderId = new ThreadLocal<>()
    private final ThreadLocal<String> token = new ThreadLocal<>()
    private final ThreadLocal<String> txnAmount = new ThreadLocal<>()

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, setQueryParamOrderIdFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_ALL_PAYMENT_OFFERS)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', '?' ?: UUID.randomUUID().toString())
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Map root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : new Random().nextLong().abs() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'TXN_TOKEN',
                        token           : token.get(),
                ],
                body: [
                        mid    : m().id,
                        orderId: orderId.get(),
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

    Filter setQueryParamOrderIdFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['orderId'] == '?') {
                requestSpec.removeQueryParam('orderId').queryParam('orderId', new JsonSlurper().parseText(requestSpec.getBody())?.body?.orderId ?: orderId?.get() ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    ResponseSpecification resSpec = new ResponseSpecBuilder()
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-all-payment-offers-v1-schema.json'))
            .build()

    private final static class ResultInfo {
        static ResponseSpecification SUCCESS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('S'))
                .expectBody('', hasKey('resultCodeId'))
                .expectBody('resultCodeId', nullValue())
                .expectBody('resultCode', equalTo('00000000'))
                .expectBody('resultMsg', equalTo('Success'))
                .build()
        static ResponseSpecification MID_OR_ORDERID_DOES_NOT_MATCH = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("Mid or OrderId doesn't match"))
                .build()
        static ResponseSpecification ORDERID_CANNOT_BE_BLANK = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("OrderId can't be blank"))
                .build()
        static ResponseSpecification SIMPLIFIED_PAYMENT_OFFERS_CANNOT_BE_NULL = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('9999'))
                .expectBody('resultMsg', equalTo("SimlifiedPaymentOffers can't be null for tokenType TXN_TOKEN"))
                .build()
        static ResponseSpecification INVALID_REQUEST_PARAMS = new ResponseSpecBuilder()
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', equalTo('F'))
                .expectBody('resultCode', equalTo('1001'))
                .expectBody('resultMsg', equalTo("Request parameters are not valid"))
                .build()
    }

    @BeforeMethod
    void setTxnToken(Method method, ITestResult testResult) {
        try {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString()
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() }),
            @Merchant
    ])
    @Test
    void 'test when head token equals txn token generated using different mid'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = m(1).id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MID_OR_ORDERID_DOES_NOT_MATCH)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head token equals txn token generated using different orderId'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.orderId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MID_OR_ORDERID_DOES_NOT_MATCH)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head token equals txn token generated for OFFUS user'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @AUser
    @Test
    void 'test when head token equals txn token generated for ONUS user'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body mid = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MID_OR_ORDERID_DOES_NOT_MATCH)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body orderId is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('orderId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.ORDERID_CANNOT_BE_BLANK)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body orderId = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.orderId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.ORDERID_CANNOT_BE_BLANK)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body orderId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.orderId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.ORDERID_CANNOT_BE_BLANK)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when body orderId equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.orderId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MID_OR_ORDERID_DOES_NOT_MATCH)
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() }),
            @Merchant
    ])
    @Test
    void "test when mid provided in request body is different from mid provided in query params"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.somethingWentWrong as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void "test when orderId provided in request body is different from orderId provided in query params"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.orderId = orderId.get()
        given(reqBldr().removeQueryParam('orderId').addQueryParam('orderId', UUID.randomUUID().toString()).build()).body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MID_OR_ORDERID_DOES_NOT_MATCH)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void "test when mid in query params is not provided"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.somethingWentWrong as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void "test when orderId in query params is not provided"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.MID_OR_ORDERID_DOES_NOT_MATCH)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void "test when merchant has promos configured"() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void "test when merchant does have not promos configured"() {
        def root = root()
        m().promos.clear()
        Response r=req().body(root).post().then().extract().response();
                //.spec(resSpec)
               // .spec(ResultInfo.SUCCESS)
        JsonPath res=r.jsonPath();
        Assert.assertEquals(res.getString("body.resultInfo.resultMsg"),"Something went wrong");
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void "test when simplifiedPaymentOffers object was not provided in Initate Txn API while creating txn token"() {
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString()
            root.body.simplifiedPaymentOffers = null
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SIMPLIFIED_PAYMENT_OFFERS_CANNOT_BE_NULL)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.SUCCESS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.channelId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head tokenType is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head tokenType = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head tokenType equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head token is not provided'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.remove('token')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head token = null'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head token = \'\''() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head token equals random value'() {
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @Test
    void 'test when head token equals expired token'() {
        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_URI)
        jedis.del("NativeTxnInitiateRequest${m().id}_${orderId.get()}")
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }
}