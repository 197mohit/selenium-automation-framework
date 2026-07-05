package scripts.api.theia.fetchPayOptions

import com.paytm.apphelpers.PGPHelpers
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import scripts.api.theia.CreateToken

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTION_V1
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

@Owner('Deepak')
class AccessTokenFetchPayOptionsV1Test extends FetchPayOptionsV1Test {

    @Override
    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PAYMENT_OPTION_V1)
                .addQueryParam('mid', '?')
                .addQueryParam('referenceId', referenceId.get())
    }

    final Filter setQueryParamMidFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            if (requestSpec.getQueryParams()['mid'] == '?') {
                requestSpec.removeQueryParam('mid').queryParam('mid', new JsonSlurper().parseText(requestSpec.getBody())?.body?.mid ?: m()?.id ?: UUID.randomUUID().toString())
            }
            ctx.next(requestSpec, responseSpec)
        }
    }

    final ThreadLocal<String> token = new ThreadLocal<>()
    final ThreadLocal<String> referenceId = new ThreadLocal<>()

    @Override
    Map<String, Object> root() {
        [
                "head": [
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis() as String,
                        requestId       : UUID.randomUUID().toString(),
                        tokenType       : 'ACCESS',
                        token           : token.get(),
                ],
                "body": [
                        mid: m()?.id,
                ]
        ]
    }

    @BeforeMethod
    void setAccessToken(Method method, ITestResult testResult) {
        try {
            CreateToken api = new CreateToken()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            referenceId.set(root.body.referenceId)
            api.req().body(root).post().then()
                    .spec(results.success as ResponseSpecification)
                    .extract().path('body.accessToken').tap { if (!it) throw new SkipException('unable to generate access token') }.with { token.set(it as String) }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void "test unable to fetch pay options details when mid is not provided in query params"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(results.midAndReferenceIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('1007'),
                        'resultMsg', equalTo('Missing mandatory element.'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('1007'),
                        'resultMsg', equalTo('Missing mandatory element.'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token = \'\''() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('1007'),
                        'resultMsg', equalTo('Missing mandatory element.'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when head token equals random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(results.midPassedInQueryParamsAndRequestBodyDoesNotMatch as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(results.orderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(results.midPassedInQueryParamsAndRequestBodyDoesNotMatch as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .root('body.resultInfo')
                .body(
                        'resultStatus', equalTo('F'),
                        'resultCode', equalTo('1002'),
                        'resultMsg', equalTo('TOKEN_VALIDATION_FAILED'))
    }

    @Override
    @Merchants([
            @Merchant(edit = true),
            @Merchant(edit = true)
    ])
    @AUser
    @Test
    void "test unable to fetch pay options details when mid in query params is different from mid in body"() {
        def root = root()
        root.body.mid = m(0).id
        given(reqBldr().removeQueryParam('mid').addQueryParam('mid', m(1).id).build()).body(root).post().then()
                .spec(results.midPassedInQueryParamsAndRequestBodyDoesNotMatch as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test able to fetch body userDetails when token has user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(body_userDetails_schema)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body loginInfo userLoggedIn == true when token has user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.loginInfo.userLoggedIn', equalTo(true))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser(edit = true)
    @Test
    void 'test body paymentFlow when m hybrid == true && token contains user authentication'() {
        def root = root()
        user().wallets['main'].balance = 1D//any balance > 0
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentFlow', equalTo('HYBRID'))
    }

    @Override
    @Merchant({ it.preferences.addnpay.enabled  && it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @AUser
    @Test
    void 'test body paymentFlow when m addnpay == true && token contains user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentFlow', equalTo('ADDANDPAY'))
    }

//    @Override
//    @Issue('PGP-28870')
//    @Test(enabled = false)
    void assertAllSavedCCPayChannelOptionsReturnedWhenOnlyModeIsProvided() {
        super.assertAllSavedCCPayChannelOptionsReturnedWhenOnlyModeIsProvided()
    }
//    @Override
//    @Issue('PGP-28870')
//    @Test(enabled = false)
    void 'test body merchantPayOption savedInstruments equals iterable with size greater than 0 when user has saved cards'() {
        super.'test body merchantPayOption savedInstruments equals iterable with size greater than 0 when user has saved cards'()
    }
}
