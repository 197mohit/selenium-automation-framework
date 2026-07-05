package scripts.flows

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
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
import scripts.api.theia.InitiateTransaction

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTION_V1
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.INIT_TXN
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.PROCESS_TXN
import static com.paytm.appconstants.Constants.Owner.GAGANDEEP
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner(GAGANDEEP)
class DeferredNativeTest extends TestSetUp implements ProcessTxnEventLinkIdTest {

    private static final ThreadLocal<String> ORDER_ID = new ThreadLocal<>()
    private static final ThreadLocal<String> TXN_TOKEN = new ThreadLocal<>()

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
                        .addQueryParam('orderId', ORDER_ID.get())
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        txnToken : TXN_TOKEN.get(),
                ],
                body: [
                        mid        : m().id,
                        orderId    : ORDER_ID.get(),
                        paymentMode: 'BALANCE',
                        website    : "retail",
                        extendInfo : [:],
                ]
        ]
    }

    @BeforeMethod
    void executeFetchPayOptionsAPI() {
        def root = [
                head: [
                        version         : 'v1',
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
                .basePath(FETCH_PAYMENT_OPTION_V1)
                .queryParam('mid', m().id)
                .body(root)
                .post()
                .path('body.orderId')
                .with {
                    ORDER_ID.set(it)
                }
            }

    class ExecuteInitiateTxnAPI {
        def root = [
                head: [
                        version  : 'v1',
                        channelId: 'WEB',
                        signature: null,
                ],
                body: [
                        mid          : m()?.id ?: UUID.randomUUID().toString(),
                        orderId      : ORDER_ID.get(),
                        requestType  : 'Payment',
                        websiteName  : 'retail',
                        txnAmount    : [
                                currency: 'INR',
                                value   : '1'
                        ],
                        userInfo     : [
                                custId: UUID.randomUUID().toString(),
                        ],
                        paytmSsoToken: user()?.tokens?.find { it.name == 'sso' }?.id,
                ]
        ]

        void executeAPI() {
            root.head.signature = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
            given()
                    .spec(reqSpec())
                    .contentType(ContentType.JSON)
                    .baseUri(PGP_HOST)
                    .basePath(INIT_TXN)
                    .queryParam('mid', m().id)
                    .queryParam('orderId', ORDER_ID.get())
                    .body(root)
                    .post()
                    .path('body.txnToken')
                    .with {
                        TXN_TOKEN.set(it)
                    }
        }
    }


    @Override
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for CC pay mode in case of risk pass'() {

        ExecuteInitiateTxnAPI executeInitiateTxnAPI = new ExecuteInitiateTxnAPI();
        executeInitiateTxnAPI.executeAPI();
        def root = root()
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        root.body.paymentMode = 'CREDIT_CARD'
        root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", not(isEmptyOrNullString()))
    }

    @Override
    @Merchant(edit = true, value={it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test eventLinkId is coming in response when order is initiated for DC pay mode in case of risk pass'() {

        ExecuteInitiateTxnAPI executeInitiateTxnAPI = new ExecuteInitiateTxnAPI()
        executeInitiateTxnAPI.executeAPI();
        def root = root()
        def card = cards.find { it.type == 'debit' && !it.prepaid }.tap { assert it }
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

        ExecuteInitiateTxnAPI executeInitiateTxnAPI = new ExecuteInitiateTxnAPI();
        executeInitiateTxnAPI.executeAPI();
        def root = root()
        root.body.paymentMode = 'NET_BANKING'
        root.body.channelCode = 'ICICI'
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId",  not(isEmptyOrNullString()))
    }

    @Override
    @Merchant(edit = true, value={it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void "test eventLinkId is not coming in response in case of risk reject"() {

        ExecuteInitiateTxnAPI executeInitiateTxnAPI = new ExecuteInitiateTxnAPI();
        executeInitiateTxnAPI.root.body.txnAmount.value = '1.2'
        executeInitiateTxnAPI.executeAPI();
        def root = root()
        root.body.paymentMode = 'NET_BANKING'
        root.body.channelCode = 'ICICI'
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('F'))
                .body("body.riskContent.eventLinkId", isEmptyOrNullString())
    }


    @Override
    @Merchant(edit = true, value={it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void "test eventLinkId is not coming in response in case of risk verify"() {

        ExecuteInitiateTxnAPI executeInitiateTxnAPI = new ExecuteInitiateTxnAPI();
        executeInitiateTxnAPI.root.body.txnAmount.value = '1.4'
        executeInitiateTxnAPI.executeAPI();
        def root = root()
        def card = cards.find { it.type == 'debit' && !it.prepaid }.tap { assert it }
        root.body.paymentMode = 'DEBIT_CARD'
        root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", isEmptyOrNullString())
    }
}
