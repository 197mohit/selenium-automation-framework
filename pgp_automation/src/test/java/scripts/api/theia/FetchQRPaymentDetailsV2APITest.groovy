package scripts.api.theia

import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.QRCode
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_QR_PAYMENT_DETAILS_V2
import static com.paytm.appconstants.Constants.Owner.*
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

class FetchQRPaymentDetailsV2APITest extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addFilters([setQueryParamOrderIdFilter])
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_QR_PAYMENT_DETAILS_V2)
                .addQueryParams([appVersion: '8.3.2', client: 'dd', orderId: '?'])
    }

    final RequestSpecification req() { given(reqBldr().build()) }

    final Map<String, Object> root() {
        [
                head: [
                        version         : 'v2',
                        requestId       : UUID.randomUUID() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        qrCodeId       : m().with {
                            it ? it.qrCodes?.add(QRCode.Merchant())?.id.tap { assert it } : null
                        },
                        mlvSupported   : null,//O
                        generateOrderId: false,
                        orderId        : new Random().nextLong().abs() as String,
                ]
        ]
    }

    def setQueryParamOrderIdFilter = [filter: { req, res, ctx ->
        if (req.getQueryParams()['orderId'] == '?') {
            req.removeQueryParam('orderId').queryParam('orderId', new JsonSlurper().parseText(req.getBody())?.body?.orderId ?: new Random().nextLong().abs() as String)
        }
        ctx.next(req, res)
    }] as Filter

    @Owner(PULKIT)
    @Merchant(value = { it.limit == 1 }, edit = true)
    @AUser
    @Test
    void 'Verify that merchant limit list is returned in the response for merchantLimit is 1'() {
        def root = root()
        root.body.generateOrderId = 'true'
        root.orderId = null
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .body('body.paymentOptions.merchantLimitInfo.merchantPaymodesLimits', notNullValue())
    }

    @Owner(PULKIT)
    @Merchant(value = { it.limit == 2 }, edit = true)
    @AUser
    @Test
    void 'Verify that merchant limit list is not returned in the response for merchantLimit is 2'() {
        def root = root()
        root.body.generateOrderId = 'true'
        root.orderId = null
        req().body(root).post().then().spec(results.success as ResponseSpecification)
                .body('body.paymentOptions.merchantLimitInfo', not(hasKey("merchantPaymodesLimits")))
    }

    @Merchant(value = { it.payModes.containsAll(['ppi', 'nb', 'cc', 'dc', 'emi', 'upi', 'ppbl']) }, edit = true)
    @AUser(edit = true)
    @Test(description = 'test fetchQR priority')
    void testFetchQR() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.paymentOptions.merchantPayOption.paymentModes')
                .body('paymentMode', hasItems('BALANCE', 'NET_BANKING', 'CREDIT_CARD', 'DEBIT_CARD', 'EMI', 'UPI', 'PPBL'),
                        'priority', hasItems('1', '6','9','3','7','5','2'))
    }
}
