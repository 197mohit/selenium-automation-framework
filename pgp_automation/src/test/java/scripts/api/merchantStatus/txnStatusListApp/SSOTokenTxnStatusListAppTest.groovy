package scripts.api.merchantStatus.txnStatusListApp

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
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
import org.awaitility.Duration
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import scripts.api.merchantStatus.txnStatusApp.TxnStatusAppTest

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.PROCESS_TXN
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.TXN_STATUS_LIST_APP
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.awaitility.Awaitility.await
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.everyItem

class SSOTokenTxnStatusListAppTest extends TestSetUp implements TxnStatusAppTest {

    private final static ThreadLocal<String> REF_ID = new ThreadLocal<>()

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            Response res = ctx.next(requestSpec, responseSpec)
            if (res.body().asString()) {
                responseSpec.spec(
                        new ResponseSpecBuilder()
                                .expectBody(matchesJsonSchemaInClasspath('json-schemas/merchant-status/txn-status-list-app-schema.json'))
                                .build()
                )
            }
            return res
        }
    }

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(TXN_STATUS_LIST_APP)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Map root() {
        [
                head: [
                        mid      : m().id,
                        tokenType: 'SSO',
                        token    : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        refId: REF_ID.get()
                ]
        ]
    }

    private final ptcReq() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
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
    @Merchant({ it.payModes.contains('ppi') })
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
        await()
                .atMost(Duration.ONE_MINUTE)
                .pollInterval(Duration.FIVE_SECONDS)
                .pollDelay(Duration.TWO_SECONDS)
                .until({ req().body(root).post().path('body.TXN_LIST.find().STATUS') }, equalTo('TXN_SUCCESS'))
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                        'resultCodeId', equalTo('00023424'),
                        'resultCode', equalTo('Success'),
                        'resultMsg', equalTo('Success'))
    }

    @Override
    @Merchant({ it.payModes.contains('ppi') })
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
        await()
                .atMost(Duration.ONE_MINUTE)
                .pollInterval(Duration.FIVE_SECONDS)
                .pollDelay(Duration.TWO_SECONDS)
                .until({ req().body(root).post().path('body.TXN_LIST.find().STATUS') }, equalTo('TXN_FAILURE'))
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                        'resultCodeId', equalTo('00023424'),
                        'resultCode', equalTo('Success'),
                        'resultMsg', equalTo('Success'))
    }

    @Override
    @Merchant({ it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
    @AUser(edit = true)
    @Test
    void "test when order is pending"() {
        String orderId = null
        PTC:
        {
            def root = ptcRoot()
            root.body.paymentMode = 'CREDIT_CARD'
            root.body.cardInfo = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }.with {
                "|$it.no|$it.cvv|${it.with { expMo + expYr }}" as String
            }
            orderId = ptcReq().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo('S'))
                    .extract()
                    .path('body.txnInfo.ORDERID')
        }
        Thread.sleep(5000)// wait till the final txn status is attained (any decent time interval would work)
        def root = root()
        req().body(root).post().then()
                .root('body.TXN_LIST')
                .body('STATUS', everyItem(equalTo('PENDING')))
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                        'resultCodeId', equalTo('00023424'),
                        'resultCode', equalTo('Success'),
                        'resultMsg', equalTo('Success'))
    }
}
