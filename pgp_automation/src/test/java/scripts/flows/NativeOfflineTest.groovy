package scripts.flows

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import static com.paytm.appconstants.Constants.Owner.GAGANDEEP
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V2
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.PROCESS_TXN
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner(GAGANDEEP)
class NativeOfflineTest extends TestSetUp implements ProcessTxnEventLinkIdTest {

    private static final ThreadLocal<String> ORDER_ID = new ThreadLocal<>()

    private final Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addFilters([setQueryParamMidFilter, new RequestLoggingFilter()])
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(PROCESS_TXN)
                        .addQueryParam('mid', '?')
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        tokenType: 'SSO',
                        token    : user().tokens['sso'].id,
                ],
                body: [
                        mid        : m().id,
                        orderId    : ORDER_ID.get(),
                        paymentMode: 'BALANCE',
                        website    : "retail",
                        txnAmount  : [
                                currency: 'INR',
                                value   : 1
                        ],
                        extendInfo : [:],
                ]
        ]
    }

    @BeforeMethod
    void setUp() {
        def root = [
                head: [
                        version         : 'v2',
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user().tokens['sso'].id,
                ],
                body: [
                        generateOrderId: 'true',
                        mid            : m().id,
                ],
        ]
        given()
                .spec(reqSpec())
                .contentType(ContentType.JSON)
                .baseUri(PGP_HOST)
                .basePath(FETCH_PAYMENT_OPTIONS_V2)
                .queryParam('mid', m().id)
                .body(root)
                .post()
                .path('body.orderId')
                .with {
                    ORDER_ID.set(it)
                }
    }

    @Merchant
    @AUser(edit = true)
    @Test
    public void testName() {
        def root = root()
        user().wallets['main'].balance = 1D
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId() })
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for CC pay mode in case of risk pass'() {
        def root = root()
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        root.body.paymentMode = 'CREDIT_CARD'
        root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId() })
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for DC pay mode in case of risk pass'() {
        def root = root()
        def card = cards.find { it.type == 'debit' }.tap { assert it }
        root.body.paymentMode = 'DEBIT_CARD'
        root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId() })
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for NB pay mode in case of risk pass'() {
        def root = root()
        root.body.paymentMode = 'NET_BANKING'
        root.body.channelCode = 'ICICI'
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void "test eventLinkId is not coming in response in case of risk reject"() {

        def root = root()
        root.body.paymentMode = 'NET_BANKING'
        root.body.channelCode = 'ICICI'
        root.body.txnAmount.value = '1.2'
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('F'))
                .body("body.riskContent.eventLinkId", isEmptyOrNullString())
    }

    @Override
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId() })
    @AUser
    @Test
    void "test eventLinkId is not coming in response in case of risk verify"() {

        def root = root()
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        root.body.paymentMode = 'CREDIT_CARD'
        root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
        root.body.txnAmount.value = '1.4'
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", isEmptyOrNullString())
    }
}
