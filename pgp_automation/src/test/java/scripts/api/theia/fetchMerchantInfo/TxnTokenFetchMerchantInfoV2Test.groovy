package scripts.api.theia.fetchMerchantInfo

import com.paytm.api.nativeAPI.SubscriptionCreate
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.user.annotations.AUsers
import groovy.json.JsonSlurper
import io.qameta.allure.Feature
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
import scripts.api.theia.HeadTest
import scripts.api.theia.InitiateTransaction

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_MERCHANT_INFO_V2
import static com.paytm.appconstants.Constants.Owner.DEEPAK
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@Owner(DEEPAK)
class TxnTokenFetchMerchantInfoV2Test extends TestSetUp implements FetchMerchantInfoTest, HeadTest {

    final ThreadLocal<String> orderId = new ThreadLocal<>()
    final ThreadLocal<String> token = new ThreadLocal<>()
    final ThreadLocal<String> txnAmount = new ThreadLocal<>()

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, setQueryParamOrderIdFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_MERCHANT_INFO_V2)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', '?')
    }

    RequestSpecification req() {
        given(reqBldr().build())
    }

    Map root() {
        [
                head: [
                        version         : 'v2',
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        ssoToken        : user()?.tokens?.getAt('sso')?.id,
                        txnToken        : token.get(),
                ],
                body: [
                        mid    : m()?.id,
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
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-merchant-info-v2-schema.json'))
            .build()

    ResponseSpecification success_merchantInfoResp(com.paytm.utils.merchant.merchant.util.Merchant m) {
        new ResponseSpecBuilder()
                .rootPath('body.merchantInfoResp')
                .expectBody('merDispname', equalTo(m.names.display.name))
                .expectBody('merBusName', equalTo(m.names.business.name))
            //    .expectBody('merLogoUrl', equalTo(m.logoUrl))
                .build()
    }

    ResponseSpecification success_txnAmount(String txnAmount) {
        new ResponseSpecBuilder()
                .rootPath('body.txnAmount')
                .expectBody('currency', equalTo('INR'))
                .expectBody('value', equalTo(txnAmount))
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
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        } catch (Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void "test when query params are not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void "test when mid in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('orderId').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void "test when orderId in query params is not provided"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant
    ])
    @AUser
    @Test
    void "test when mid provided in query params is different from mid provided in request body"() {
        def root = root()
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void "test when orderId provided in query params is different from orderId provided in request body"() {
        def root = root()
        root.body.mid = orderId.get()
        given(reqBldr().removeQueryParam('orderId').addQueryParam('orderId', UUID.randomUUID().toString()).build()).body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant
    @AUser
    @Test
    void 'test when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body orderId is not provided'() {
        def root = root()
        root.body.remove('orderId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body orderId = null'() {
        def root = root()
        root.body.orderId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body orderId = \'\''() {
        def root = root()
        root.body.orderId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body orderId equals random value'() {
        def root = root()
        root.body.orderId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.orderIdInQueryParamNotMatchingWithOrderIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head txnToken is not provided'() {
        def root = root()
        root.head.remove('txnToken')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head txnToken = null'() {
        def root = root()
        root.head.txnToken = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head txnToken = \'\''() {
        def root = root()
        root.head.txnToken = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head txnToken equals random value'() {
        def root = root()
        root.head.txnToken = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant
    ])
    @AUser
    @Test
    void 'test when head txnToken equals txn token generated using different mid'() {
        def root = root()
        root.body.mid = m(1).id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head txnToken equals txn token generated using different orderId'() {
        def root = root()
        root.body.orderId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.orderIdInQueryParamNotMatchingWithOrderIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head txnToken equals txn token generated for OFFUS user'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = null
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head ssoToken is not provided'() {
        def root = root()
        root.head.remove('ssoToken')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head ssoToken = null'() {
        def root = root()
        root.head.ssoToken = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head ssoToken = \'\''() {
        def root = root()
        root.head.ssoToken = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.missingMandatoryElement as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head ssoToken equals random value'() {
        def root = root()
        root.head.ssoToken = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head ssoToken equals SSO token'() {
        def root = root()
        root.head.ssoToken = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head ssoToken equals WALLET token'() {
        def root = root()
        root.head.ssoToken = user().tokens['wallet'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head ssoToken equals TXN token'() {
        def root = root()
        root.head.ssoToken = user().tokens['txn'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser(edit = true)
    @Test
    void 'test when head ssoToken equals expired token'() {
        def root = root()
        def token = user().tokens['sso'].id
        user().tokens.clear()
        root.head.ssoToken = token
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.ssoTokenIsInvalid as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUsers([
            @AUser(edit = true),
            @AUser
    ])
    @Test
    void 'test when SSO token used in head ssoToken is different from SSO token used to create txnToken'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user(0).tokens['sso'].id
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        root.head.ssoToken = user(1).tokens['sso'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUsers([
            @AUser(edit = true),
            @AUser
    ])
    @Test
    void 'test when WALLET token used in head ssoToken is different from WALLET token used to create txnToken'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user(0).tokens['wallet'].id
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        root.head.ssoToken = user(1).tokens['wallet'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUsers([
            @AUser(edit = true),
            @AUser
    ])
    @Test
    void 'test when TXN token used in head ssoToken is different from TXN token used to create txnToken'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user(0).tokens['txn'].id
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        root.head.ssoToken = user(1).tokens['txn'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when SSO token is used in head ssoToken but WALLET token is used to create txnToken'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user().tokens['wallet'].id
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        root.head.ssoToken = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body promoCodeApplied = true when promo is passed in InitiateTxn API while creating txnToken'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user().tokens['sso'].id
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        initTxnRoot.body.promoCode = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
                .body('body.promoCodeApplied', equalTo('true'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body promoCodeApplied = false when promo is not passed in InitiateTxn API while creating txnToken'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user().tokens['sso'].id
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        initTxnRoot.body.promoCode = null
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
                .body('body.promoCodeApplied', equalTo('false'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body txnAmount value when txnAmount having integer value'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user().tokens['sso'].id
        initTxnRoot.body.txnAmount.value = '2'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body txnAmount value when txnAmount having decimal value with one significant digit after decimal which is 0'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user().tokens['sso'].id
        initTxnRoot.body.txnAmount.value = '2.0'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body txnAmount value when txnAmount having decimal value with two significant digits after decimal with both as 0'() {
        def api = new InitiateTransaction()
        def initTxnRoot = api.root()
        initTxnRoot.body.paytmSsoToken = user().tokens['sso'].id
        initTxnRoot.body.txnAmount.value = '2.00'.tap { txnAmount.set(it) }
        initTxnRoot.body.orderId.with { orderId.set(it) }
        initTxnRoot.body.userInfo.custId = UUID.randomUUID().toString()
        api.req().body(initTxnRoot).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }

        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestId is not provided'() {
        def root = root()
        root.head.remove('requestId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestId = null'() {
        def root = root()
        root.head.requestId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestId = \'\''() {
        def root = root()
        root.head.requestId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestId equals random value'() {
        def root = root()
        root.head.requestId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp is not provided'() {
        def root = root()
        root.head.remove('requestTimestamp')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp = null'() {
        def root = root()
        root.head.requestTimestamp = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp = \'\''() {
        def root = root()
        root.head.requestTimestamp = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head requestTimestamp equals random value'() {
        def root = root()
        root.head.requestTimestamp = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head channelId is not provided'() {
        def root = root()
        root.head.remove('channelId')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head channelId = null'() {
        def root = root()
        root.head.channelId = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head channelId = \'\''() {
        def root = root()
        root.head.channelId = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head channelId equals random value'() {
        def root = root()
        root.head.channelId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head tokenType is not provided'() {
        def root = root()
        root.head.remove('tokenType')
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head tokenType = null'() {
        def root = root()
        root.head.tokenType = null
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head tokenType = \'\''() {
        def root = root()
        root.head.tokenType = ''
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when head tokenType equals random value'() {
        def root = root()
        root.head.tokenType = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.sysErr as ResponseSpecification)
    }

    @Override
    @Feature('PGP-22648')
    @Merchant({ it.id == Constants.MerchantType.Subscription_PGOnly.getId() })
    @AUser
    @Test
    void "test for a subscription order having txn amt = 0"() {
        orderId.set(UUID.randomUUID().toString())
        txnAmount.set("0")
        InitTxnResponseDTO response = SubscriptionCreate.Default(m().id, m().key, orderId.get(), user().tokens['sso'].id, txnAmount.get()).execute().as(InitTxnResponseDTO.class)
        if (!response.getBody().getTxnToken()) throw new SkipException("Txn token not generated")
        token.set(response.getBody().getTxnToken())
        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }

    @Override
    @Feature('PGP-22648')
    @Merchant({ it.id == Constants.MerchantType.Subscription_PGOnly.getId() })
    @AUser
    @Test
    void 'test for a subscription order having txn amt greater than or equal to 1'() {
        orderId.set(UUID.randomUUID().toString())
        txnAmount.set("1")
        InitTxnResponseDTO response = SubscriptionCreate.Default(m().id, m().key, orderId.get(), user().tokens['sso'].id, txnAmount.get()).execute().as(InitTxnResponseDTO.class)
        if (!response.getBody().getTxnToken()) throw new SkipException("Txn token not generated")
        token.set(response.getBody().getTxnToken())
        def root = root()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
                .spec(success_merchantInfoResp(m()))
                .spec(success_txnAmount(txnAmount.get()))
    }
}
