package scripts.api.theia.fetchBinDetail

import com.paytm.apphelpers.PGPHelpers
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV2Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_BIN_DETAIL
import static com.paytm.appconstants.Constants.Owner.PULKIT
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
class SSOTokenFetchBinDetailV1Test extends FetchBinDetailTest {

    @Override
    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_BIN_DETAIL)
                .addQueryParams([mid: '?' ?: m()?.id])
    }

    @Override
    RequestSpecification req() {
        given(reqBldr().build())
    }

    @Override
    Map root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID() as String,
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        mid        : m()?.id,
                        bin        : cards.find { it.type == 'debit' && !it.prepaid }.no[0..5],
                        paymentMode: 'DC'
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

    @Owner(PULKIT)
    @Merchant({ it.payModes.containsAll(['emi', 'emidc']) })
    @AUser(emidc = 'true')
    @Test
    void 'fetch BIN when DC_EMI is enabled'() {
        V2_FPO:
        {
            def api = new SSOTokenFetchPayOptionsV2Test()
            def root = api.root()
            root.body.enablePaymentMode = [[mode: 'EMI', emiType: 'DEBIT_CARD']]
            root.body.generateOrderId = 'true'
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
        FETCH_BIN_CC:
        {
            def root = root()
            root.body.bin = cards.find { it.type == 'credit' && !it.prepaid }.no[0..5]
            root.body.paymentMode = 'CC'
            req().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo("F"))
                    .body('body.resultInfo.resultCode', equalTo("2011"))
                    .body('body.resultInfo.resultMsg', equalTo("MASTER Credit card is not allowed for CC payment. Please try paying using other cards/options."))
        }
        FETCH_BIN_DC:
        {
            def root = root()
            root.body.bin = cards.find { it.type == 'debit' && !it.prepaid }.no[0..5]
            root.body.paymentMode = 'DC'
            req().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo("S"))
                    .body('body.resultInfo.resultCode', equalTo("0000"))
                    .body('body.resultInfo.resultMsg', equalTo("Success"))
                    .body('body.emiDetail', notNullValue())
        }
    }

    @Owner(PULKIT)
    @Merchant({ it.payModes.containsAll(['emi', 'emidc']) })
    @AUser(emidc = 'true')
    @Test
    void 'fetch BIN when CC_EMI is enabled'() {
        V2_FPO:
        {
            def api = new SSOTokenFetchPayOptionsV2Test()
            def root = api.root()
            root.body.enablePaymentMode = [[mode: 'EMI', emiType: 'CREDIT_CARD']]
            root.body.generateOrderId = 'true'
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
        FETCH_BIN_DC:
        {
            def root = root()
            root.body.bin = cards.find { it.type == 'debit' && !it.prepaid }.no[0..5]
            root.body.paymentMode = 'DC'
            req().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo("F"))
                    .body('body.resultInfo.resultCode', equalTo("2011"))
                    .body('body.resultInfo.resultMsg', containsString("MAESTRO Debit card is not allowed for DC payment. Please try paying using other cards/options."))
        }
        FETCH_BIN_CC:
        {
            def root = root()
            root.body.bin = cards.find { it.type == 'credit' && !it.prepaid }.no[0..5]
            root.body.paymentMode = 'CC'
            req().body(root).post().then()
                    .body('body.resultInfo.resultStatus', equalTo("S"))
                    .body('body.resultInfo.resultCode', equalTo("0000"))
                    .body('body.resultInfo.resultMsg', equalTo("Success"))
                    .body('body.emiDetail', notNullValue())
        }
    }
}
