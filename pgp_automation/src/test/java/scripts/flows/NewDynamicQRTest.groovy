package scripts.flows

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Link
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

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.PROCESS_TXN
import static com.paytm.appconstants.Constants.Owner.GAGANDEEP
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.PAYMENT_SERVICE
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*
@Owner(GAGANDEEP)
@Link("https://developer.paytm.com/docs/apis/create-qr-code-api/")
class NewDynamicQRTest extends TestSetUp implements ProcessTxnEventLinkIdTest {

    private static final ThreadLocal<String> QR_CODE_ID = new ThreadLocal<>()
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
    void executeQRCreateAPI() {
        def root = [
                head: [
                        clientId        : 'C11',
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis().toString(),
                        channelId       : 'WEB',
                        signature       : null,
                ],
                body: [
                        mid         : m().id,
                        orderId     : UUID.randomUUID().toString(),
                        amount      : '1',
                        businessType: 'QR_ORDER',
                        posId       : 'S12_123',

                ]
        ]
        root.head.signature = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
        given()
                .spec(reqSpec())
                .contentType(ContentType.JSON)
                .baseUri(PGP_HOST)
                .basePath(PAYMENT_SERVICE)
                .body(root)
                .post()
                .path('body.qrCodeId')
                .tap {
                    assert it, "body.qrCodeId is not as expected"
                }
                .with {
                    QR_CODE_ID.set(it)
                }
    }

    @BeforeMethod(dependsOnMethods = ['executeQRCreateAPI'])
    void executeFetchQRPaymentDetailsAPI() {
        def root = [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        qrCodeId: QR_CODE_ID.get(),
                ]
        ]
        given()
                .spec(reqSpec())
                .contentType(ContentType.JSON)
                .baseUri(PGP_HOST)
                .basePath(FETCH_QR_PAYMENT_DETAILS)
                .queryParam('appVersion', '8.3.2')
                .queryParam('client', 'dd')
                .body(root)
                .post()
                .path('body.resultInfo.resultStatus')
                .tap {
                    assert it == 'S', "body.resultInfo.resultStatus is not as expected"
                }

    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('cc') && it.payModes.contains('upi') && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } })
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
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('dc') && it.payModes.contains('upi')})
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
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('nb')&& it.payModes.contains('upi') })
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
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('nb') && it.payModes.contains('upi') })
    @AUser
    @Override
    @Test
    void "test eventLinkId is not coming in response in case of risk reject"() {
        def root = root()
        root.body.paymentMode = 'NET_BANKING'
        root.body.channelCode = 'ICICI'
        root.body.txnAmount.value = '1.2'
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", isEmptyOrNullString())
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.nativeJsonRequest.enabled && it.payModes.contains('dc') && it.payModes.contains('upi') })
    @AUser
    @Test
    void "test eventLinkId is not coming in response in case of risk verify"() {
        def root = root()
        def card = cards.find { it.type == 'debit' }.tap { assert it }
        root.body.paymentMode = 'DEBIT_CARD'
        root.body.txnAmount.value = '1.2'
        root.body.cardInfo = "|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String
        req().body(root).post().then()
                .body('body.resultInfo.resultStatus', equalTo('S'))
                .body("body.riskContent.eventLinkId", isEmptyOrNullString())
    }
}
