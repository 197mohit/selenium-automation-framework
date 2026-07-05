package scripts.api.theia.fetchEmiDetail

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
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
import org.testng.annotations.Test
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV2Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_EMI_DETAIL
import static com.paytm.appconstants.Constants.Owner.PULKIT
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class SSOTokenFetchEmiDetailV1Test extends TestSetUp implements FetchEmiDetailTest {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter, schemaFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_EMI_DETAIL)
                .addQueryParam('mid', '?')
                .addQueryParam('orderId', UUID.randomUUID().toString())
    }

    RequestSpecification req() {
        given(reqBldr().build())
    }

    Map root() {
        [
                head: [
                        version         : 'v1',
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        tokenType       : 'SSO',
                        token           : user()?.tokens?.getAt('sso')?.id,
                ],
                body: [
                        mid        : m().id,
                        channelCode: 'HDFC',
                        emiType    : null,//O
                        amount     : null,
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
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-emi-detail-v1-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(emidc = 'true')
    @Test
    void testSuccess() {
        def root = root()
        root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Owner(PULKIT)
    @Merchant({it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'fetch EMI details when only CC_EMI is enabled'() {
        def channelCodeMap = ['ICIE':'ICICI','HDFE':'HDFC']
        V2_FPO:
        {
            def api = new SSOTokenFetchPayOptionsV2Test()
            def root = api.root()
            root.body.enablePaymentMode = [[mode: 'EMI', emiType: 'CREDIT_CARD']]
            root.body.generateOrderId = 'true'
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
        FETCH_EMI_DETAILS_CC:
        {
            def root = root()
            root.body.emiType = 'CREDIT_CARD'
            req().body(root).post().then()
                    .spec(results.success as ResponseSpecification)
                .body('body.emiDetail', notNullValue())
        }
        FETCH_EMI_DETAILS_DC:
        {
            def root = root()
            root.body.channelCode = m().acquirings.findAll {it.payMode=='emidc' && it.enabled==true}*.bank*.toUpperCase()[0].with {channelCodeMap[it]}
            root.body.emiType = 'DEBIT_CARD'
            req().body(root).post().then()
                    .body('body.resultInfo.resultStatus',equalTo("S"))
                    .body('body.resultInfo.resultCode',equalTo("0000"))
                    .body('body.resultInfo.resultMsg',equalTo("Success"))
                    .body('body.emiDetail', isEmptyOrNullString())
        }
    }

    @Owner(PULKIT)
    @Merchant({it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'fetch EMI details when only DC_EMI is enabled'() {
        def channelCodeMap = ['ICIE':'ICICI','HDFE':'HDFC']
        V2_FPO:
        {
            def api = new SSOTokenFetchPayOptionsV2Test()
            def root = api.root()
            root.body.enablePaymentMode = [[mode: 'EMI', emiType: 'DEBIT_CARD']]
            root.body.generateOrderId = 'true'
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
        FETCH_EMI_DETAILS_DC:
        {
            def root = root()
            root.body.channelCode = m().acquirings.findAll {it.payMode=='emidc' && it.enabled==true}*.bank*.toUpperCase()[0].with {channelCodeMap[it]}
            root.body.emiType = 'DEBIT_CARD'
            req().body(root).post().then()
                    .spec(results.success as ResponseSpecification)
                    .body('body.emiDetail', notNullValue())
        }
        FETCH_EMI_DETAILS_CC:
        {
            def root = root()
            root.body.emiType = 'CREDIT_CARD'
            req().body(root).post().then()
                    .body('body.resultInfo.resultStatus',equalTo("S"))
                    .body('body.resultInfo.resultCode',equalTo("0000"))
                    .body('body.resultInfo.resultMsg',equalTo("Success"))
                    .body('body.emiDetail', isEmptyOrNullString())
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', hasKey('resultMsgRegional'))
    }
}
