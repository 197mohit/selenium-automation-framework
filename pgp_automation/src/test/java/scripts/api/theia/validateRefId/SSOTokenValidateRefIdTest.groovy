package scripts.api.theia.validateRefId

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.PROCESS_TXN
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.VALIDATE_REF_ID
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class SSOTokenValidateRefIdTest extends TestSetUp implements ValidateRefIdTest {

    private final static ThreadLocal<String> REF_ID = new ThreadLocal<>()

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(VALIDATE_REF_ID)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Map root() {
        [
                refId   : REF_ID.get(),
                mid     : m()?.id,
                ssoToken: user()?.tokens?.getAt('sso')?.id,
        ]
    }

    private final ptcReq() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .setBaseUri(PGP_HOST)
                        .setBasePath(PROCESS_TXN)
                        .addQueryParam('mid', m().id)
                        .build()
        )
    }

    private final ptcRoot() {
        [
                head: [
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        mid        : m().id,
                        paymentMode: 'BALANCE',
                        website    : 'retail',
                        txnAmount  : [
                                value   : 1D,
                                currency: 'INR'
                        ],
                        extendInfo : [:],
                        refId      : REF_ID.get(),
                ]
        ]
    }
    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            Response res = ctx.next(requestSpec, responseSpec)
            if (res.body().asString()) {
                responseSpec.spec(
                        new ResponseSpecBuilder()
                                .expectBody(matchesJsonSchemaInClasspath('json-schemas/validate-ref-id-schema.json'))
                                .build()
                )
            }
            return res
        }
    }

    @BeforeMethod
    void setUp(Method method, ITestResult testResult) {
        try {
            REF_ID.set(UUID.randomUUID().toString())
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void "test when refId is not provided"() {
        def root = root()
        root.remove('refId')
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void "test when refId = null"() {
        def root = root()
        root.refId = null
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void "test when refId = ''"() {
        def root = root()
        root.refId = ''
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void "test when refId equals random value"() {
        def root = root()
        root.refId = UUID.randomUUID().toString()
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when ssoToken is not provided"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.remove('ssoToken')
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when ssoToken = null"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.ssoToken = null
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when ssoToken = ''"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.ssoToken = ''
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when ssoToken equals random value"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.ssoToken = UUID.randomUUID().toString()
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when ssoToken equals SSO token"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.ssoToken = user().tokens['sso'].id
        req().body(root).post().then()
                .body('orderId', equalTo(orderId))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void 'test when ssoToken equals WALLET token given SSO token is used in PTC'() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            root.head.token = user().tokens['sso'].id
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.ssoToken = user().tokens['wallet'].id
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when ssoToken equals WALLET token given WALLET token is used in PTC"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            root.head.token = user().tokens['wallet'].id
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.ssoToken = user().tokens['wallet'].id
        req().body(root).post().then()
                .body('orderId', equalTo(orderId))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void 'test when ssoToken equals TXN token given SSO token is used in PTC'() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            root.head.token = user().tokens['sso'].id
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.ssoToken = user().tokens['txn'].id
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when ssoToken equals expired token"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        def token = user().tokens['sso'].id
        user().tokens.clear()
        root.ssoToken = token
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when mid is not provided"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.remove('mid')
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when mid = null"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.mid = null
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when mid = ''"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.mid = ''
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when mid equals random value"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchants([
            @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId() }),
            @Merchant({ it.payModes.contains('ppi') }),
    ])
    @AUser(edit = true)
    @Test
    void "test when mid passed is different from one provided in PTC"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        root.mid = m(1).id
        req().body(root).post().then()
                .statusCode(200)
                .extract().body().asString().with {
            assert it == ''
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when order is successful"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = root.body.txnAmount.value
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        req().body(root).post().then()
                .body('orderId', equalTo(orderId))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "test when order is failure"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            user().wallets['main'].balance = 0
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('F'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        def root = root()
        req().body(root).post().then()
                .body('orderId', equalTo(orderId))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(edit = true)
    @Test
    void "test when order is pending"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            root.body.txnAmount.value = 77
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.cardInfo = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }.with {
                "|$it.no|$it.cvv|${it.with { expMo + expYr }}" as String
            }
            ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
        }
        def root = root()
        req().body(root).post().then()
                .body('orderId', not(isEmptyOrNullString()))
    }
}
