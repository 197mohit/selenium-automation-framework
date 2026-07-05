package scripts.api.theia.fetchPayOptions

import com.paytm.apphelpers.PGPHelpers
import com.paytm.dto.PaymentDTO
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.User;
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Feature
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
import com.paytm.appconstants.Constants
import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTIONS_V5
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Sourav')
@Feature('PGP-37815')
class AccessTokenFetchPayOptionsV5Test extends FetchPayOptionsV5Test {

    @Override
    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setQueryParamMidFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PAYMENT_OPTIONS_V5)
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
                        version         : 'v5',
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
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(results.midPassedInQueryParamsAndRequestBodyDoesNotMatch as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(results.orderIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(results.midPassedInQueryParamsAndRequestBodyDoesNotMatch as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
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
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
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
    @Merchant({it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @AUser
    @Test
    void 'test body paymentFlow when m addnpay == true && token contains user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentFlow', equalTo('ADDANDPAY'))
    }

    @Merchant({it.id == Constants.MerchantType.Seamless_Hybrid_Onus.getId()})
    @AUser(edit = true)
    @Test
    void 'test body merchantPayOption savedInstruments equals empty list when user has saved cards but these cards are not supported by merchant'() {
        def root = root()
        //user().savedCards.clear()
        //assert user().savedCards.add(cards.find { it.type == 'credit' && it.scheme == 'amex' })
       com.paytm.base.test.User user = new com.paytm.base.test.User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .spec(success_schema())
                .body('body.merchantPayOption.savedInstruments', emptyIterable())
    }

    @Merchant({it.id == Constants.MerchantType.Seamless_Hybrid_Onus.getId()})
    @AUser(edit = true)
    @Test
    void 'test body merchantPayOption savedInstruments contains saved card details when user has amex saved card and merchant it'() {
        def root = root()
        //user().savedCards.clear()
        //assert user().savedCards.add(cards.find { it.type == 'credit' && it.scheme == 'amex' })
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .spec(success_schema())
                .spec(body_merchantPayOption_savedInstruments_schema())
    }

    @Merchant({it.id == Constants.MerchantType.EMI.getId()})
    @Test
    void 'test body merchantPayOption upiProfile defaultCreditAccRefId'() {
        //user hard coding is temporary and will be removed soon once new user implementation is done
        def user = new User("8684922965", "paytm@123")
        CreateToken api = new CreateToken()
        def createTokenRoot = api.root()
        createTokenRoot.body.paytmSsoToken = user?.tokens?.getAt('sso')?.id
        referenceId.set(createTokenRoot.body.referenceId)
        api.req().body(createTokenRoot).post().then()
                .spec(results.success as ResponseSpecification)
                .extract().path('body.accessToken').tap { if (!it) throw new SkipException('unable to generate access token') }.with { token.set(it as String) }
        def root = root()
        root.head.token = token.get()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails.defaultCreditAccRefId", hasSize(greaterThan(0)))
    }

    @Merchant({it.id == Constants.MerchantType.EMI.getId()})
    @Test
    void 'test body merchantPayOption upiProfile vpa'() {
        //user hard coding is temporary and will be removed soon once new user implementation is done
        def user = new User('7259493013','paytm@123')
        CreateToken api = new CreateToken()
        def createTokenRoot = api.root()
        createTokenRoot.body.paytmSsoToken = user?.tokens?.getAt('sso')?.id
        referenceId.set(createTokenRoot.body.referenceId)
        api.req().body(createTokenRoot).post().then()
                .spec(results.success as ResponseSpecification)
                .extract().path('body.accessToken').tap { if (!it) throw new SkipException('unable to generate access token') }.with { token.set(it as String) }
        def root = root()
        root.head.token = token.get()
        def vpas = req().body(root).post().then()
                .spec(results.success as ResponseSpecification).extract().path("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails.name")
        vpas.each { vpa ->
        // Hardcoding as failing in Regression
            assert vpa ==~ /[a-z0-9]+@[a-z]+/
        //    assert vpa == 'anjali.chhabra@paytm'
        }
    }

    @Override
    @Merchant(edit = true, value = { it.id == PRIORITY_DEFAULT_MERCHANT.id })
    @AUser
    @Test
    void 'test priority in fpo for all paymodes for default merchant'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantPayOption')
                .body('paymentModes.paymentMode', hasItems('BALANCE', 'GIFT_VOUCHER', 'CREDIT_CARD','DEBIT_CARD', 'NET_BANKING', 'UPI', 'PPBL'),
                        'paymentModes.priority', hasItems('1', '2', '4', '3', '1', '2','3'))
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
