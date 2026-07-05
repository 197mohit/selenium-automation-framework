package scripts.api.theia.fetchNbPaymentChannels

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
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
import scripts.api.theia.InitiateTransaction
import scripts.api.theia.TxnTokenAuthenticationTest
import com.paytm.appconstants.Constants
import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_NB_PAYMENT_CHANNELS
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class TxnTokenFetchNBPaymentChannelsV1Test extends TestSetUp implements FetchNBPaymentChannelsTest, TxnTokenAuthenticationTest {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    final ThreadLocal<String> orderId = new ThreadLocal<>()
    final ThreadLocal<String> token = new ThreadLocal<>()
    private final InitiateTransaction initTxn = new InitiateTransaction()

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_NB_PAYMENT_CHANNELS)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', orderId.get() ?: UUID.randomUUID().toString())
    }

    RequestSpecification req() {
        given(reqBldr().build())
    }

    Map root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        token           : token.get(),
                        tokenType       : 'TXN_TOKEN',
                ],
                body: [
                        mid : m()?.id,
                        type: 'MERCHANT',
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

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-nb-payment-channels-v1-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static final class ResultInfo {
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
        given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build()).post().then()
                .spec(results.mIdAndOrderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when mid in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).post().then()
                .spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when orderId in query params is not provided'() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(results.orderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type is not provided'() {
        def root = root()
        root.body.remove('type')
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type = null'() {
        def root = root()
        root.body.type = null
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type = \'\''() {
        def root = root()
        root.body.type = ''
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when body type = MERCHANT'() {
        def root = root()
        root.body.type = 'MERCHANT'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void 'test when body type = ADD_MONEY'() {
        user().wallets.each { it.balance = 0 }
        def root = root()
        root.body.type = 'ADD_MONEY'
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when body type equals random value'() {
        def root = root()
        root.body.type = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(ResultInfo.INVALID_REQUEST_PARAMS)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test when merchant has neither nb nor ppbl configured'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', nullValue())
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMI_DC_CC.getId()})
    @AUser
    @Test
    void "test when merchant has nb configured but not ppbl"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void "test when merchant has both nb and ppbl configured"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void "test when merchant had ppbl configured but not nb"() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', emptyIterable())
    }

//    @Override
//    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
//    @Test(enabled = false)
    void "test when merchant has nb configured but is disabled"() {
        throw new UnsupportedOperationException('need to create merchant having the required configuration')
    }

//    @Override
//    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
//    @Test(enabled = false)
    void 'test when one or more nb channels is disabled'() {
        throw new UnsupportedOperationException('need to create merchant having the required configuration')
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when one or more nb channels is enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.nbPayOption.isDisabled.status', equalTo('false'))
                .body("body.nbPayOption.payChannelOptions.findAll { it.isDisabled.status == 'false' }", hasSize(greaterThan(0)))
    }

//    @Override
//    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
//    @Test(enabled = false)
    void 'test when one or more nb channels has low success rate'() {
        throw new UnsupportedOperationException('need to create merchant having the required configuration')
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when one or more nb channels has high success rate'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.nbPayOption.payChannelOptions.findAll { it.hasLowSuccess.status == 'false' }", hasSize(greaterThan(0)))
                .body("body.nbPayOption.payChannelOptions.findAll { it.hasLowSuccess.status == 'false' }.hasLowSuccess.msg", everyItem(isEmptyString()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType is not provided'() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head tokenType = null'() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void 'test when head tokenType equals random value'() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true)
    ])
    @Test
    void 'test when head token equals txn token generated using different mid'() {
        def root = root()
        root.body.mid = m(1).id
        req().body(root).post().then()
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
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void 'test when head token equals txn token generated for OFFUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test when head token equals txn token generated for ONUS user'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', isA(Object.class))
                .body('body.nbPayOption.payChannelOptions', not(emptyIterable()))
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @Test
    void "test when nb pay method is disabled using Initiate Txn API"() {
        def root = root(), iniTxnRoot = initTxn.root()
        iniTxnRoot.body.disablePaymentMode = [[mode: 'NET_BANKING']]
        iniTxnRoot.head.signature = PGPUtil.getChecksum(m().key, JsonOutput.toJson(iniTxnRoot.body))
        initTxn.req(iniTxnRoot.body.orderId).body(iniTxnRoot).post().then().spec(initTxn.success).extract().path('body.txnToken').with {
            root.head.txnToken = it
        }
        given(reqBldr().removeQueryParam('orderId').build()).queryParam('orderId', iniTxnRoot.body.orderId).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('nbPayOption'))
                .body('body.nbPayOption', nullValue())
    }

    @Merchant({it.id == Constants.MerchantType.MIGRATIONDETAIL.getId()})
    @Test
    void "test when multiple nb channels are disabled using Initiate Txn API"() {
        def root = root(), iniTxnRoot = initTxn.root()
        def banksToBeDisabled = m().acquirings.findAll { it.payMode == 'nb' && it.bank != 'ppbl' }[0..1].bank
        iniTxnRoot.body.disablePaymentMode = [[mode: 'NET_BANKING', channels: banksToBeDisabled*.toUpperCase()]]
        iniTxnRoot.head.signature = getChecksum(m().key, toJson(iniTxnRoot.body))
        initTxn.req(iniTxnRoot.body.orderId).body(iniTxnRoot).post().then().spec(initTxn.success).extract().path('body.txnToken').with {
            root.head.txnToken = it
        }
        given(reqBldr().removeQueryParam('orderId').build()).queryParam('orderId', iniTxnRoot.body.orderId).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.nbPayOption.payChannelOptions.findAll { it.channelCode in ${banksToBeDisabled*.toUpperCase().collect { "'$it'" }} }.isDisabled")
                .body('status', everyItem(equalTo('true')),
                        'msg', everyItem(not(isEmptyString())))
                .root("body.nbPayOption.payChannelOptions.findAll { !(it.channelCode in ${banksToBeDisabled*.toUpperCase().collect { "'$it'" }}) }.isDisabled")
                .body('status', everyItem(equalTo('false')),
                        'msg', everyItem(nullValue()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void "test when one and only nb channel is disabled using Initiate Txn API"() {
        def root = root(), iniTxnRoot = initTxn.root()
        String bankToBeDisabled = m().acquirings.find { it.payMode == 'nb' && it.bank != 'ppbl' }.bank
        iniTxnRoot.body.disablePaymentMode = [[mode: 'NET_BANKING', channels: [bankToBeDisabled.toUpperCase()]]]
        iniTxnRoot.head.signature = getChecksum(m().key, toJson(iniTxnRoot.body))
        initTxn.req(iniTxnRoot.body.orderId).body(iniTxnRoot).post().then().spec(initTxn.success).extract().path('body.txnToken').with {
            root.head.txnToken = it
        }
        given(reqBldr().removeQueryParam('orderId').build()).queryParam('orderId', iniTxnRoot.body.orderId).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.nbPayOption.payChannelOptions", emptyIterable())
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant
    ])
    @Test
    void "test when mid provided in query params is different from mid provided in request body"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.nbPayOption', not(hasKey('displayNameRegional')),
                        'body.nbPayOption.isDisabled', not(hasKey('msgRegional')),
                        'body.nbPayOption.payChannelOptions', everyItem(not(hasKey('channelNameRegional'))),
                        'body.nbPayOption.payChannelOptions.hasLowSuccess', everyItem(not(hasKey('msgRegional'))),
                        'body.nbPayOption.payChannelOptions.isDisabled', everyItem(not(hasKey('msgRegional'))))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMI_DC_CC.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.nbPayOption', not(hasKey('displayNameRegional')),
                        'body.nbPayOption.isDisabled', not(hasKey('msgRegional')),
                        'body.nbPayOption.payChannelOptions', everyItem(not(hasKey('channelNameRegional'))),
                        'body.nbPayOption.payChannelOptions.hasLowSuccess', everyItem(not(hasKey('msgRegional'))),
                        'body.nbPayOption.payChannelOptions.isDisabled', everyItem(not(hasKey('msgRegional'))))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMI_DC_CC.getId()})
    @AUser
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', (hasKey('resultMsgRegional')),
                        'body.nbPayOption', (hasKey('displayNameRegional')),
                        'body.nbPayOption.isDisabled', (hasKey('msgRegional')),
                        'body.nbPayOption.payChannelOptions', everyItem((hasKey('channelNameRegional'))),
                        'body.nbPayOption.payChannelOptions.hasLowSuccess', everyItem((hasKey('msgRegional'))),
                        'body.nbPayOption.payChannelOptions.isDisabled', everyItem(not(hasKey('msgRegional'))))
    }
}
