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
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import scripts.api.theia.InitiateTransaction
import com.paytm.appconstants.Constants
import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_EMI_DETAIL
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class TxnTokenFetchEmiDetailV1Test extends TestSetUp implements FetchEmiDetailTest {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    final ThreadLocal<String> orderId = new ThreadLocal<>()
    final ThreadLocal<String> token = new ThreadLocal<>()

    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilter(setQueryParamMidFilter)
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_EMI_DETAIL)
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
                        requestTimestamp: System.currentTimeMillis() as String,
                        channelId       : 'WEB',
                        txnToken        : token.get(),
                ],
                body: [
                        channelCode: 'HDFC',
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

    ResponseSpecification resSpec = new ResponseSpecBuilder()
            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-emi-detail-v1-schema.json'))
            .build()

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
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void testSuccess() {
        def root = root()
        root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        req().body(root).post().then()
                .spec(resSpec)
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
       // root.body.channelCode = m().emis.find().bank.code.toUpperCase()
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
        //root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMI_DC_CC.getId()})
    @AUser(emidc = 'true')
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        //root.body.channelCode = m().emis.find().bank.code.toUpperCase()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', hasKey('resultMsgRegional'))
    }
}
