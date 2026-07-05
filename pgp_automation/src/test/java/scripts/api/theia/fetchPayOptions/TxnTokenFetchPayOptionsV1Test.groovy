package scripts.api.theia.fetchPayOptions

import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.utils.merchant.merchant.util.Promo
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
import io.restassured.specification.ResponseSpecification
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import scripts.api.theia.InitiateTransaction
import scripts.api.theia.fetchMerchantInfo.TxnTokenFetchMerchantInfoV1Test

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PAYMENT_OPTION_V1
import static io.restassured.RestAssured.given

import static org.hamcrest.Matchers.*

@Owner('Deepak')
class TxnTokenFetchPayOptionsV1Test extends FetchPayOptionsV1Test implements TxnTokenFetchPayOptionsTest {

    private final static PROMOS_NOT_ADDED = "unable to add promo(s) successfully"
    private final static PROMO_OFFERS_REDIS_KEY_PREFIX = 'com.paytm.pgplus.facade.paymentpromotion.models.response.SearchPaymentOffersServiceResponse_'

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
                .addQueryParam('orderId', orderId.get() ?: UUID.randomUUID().toString())
    }

    @Override
    Map root() {
        [
                "head": [
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis() as String,
                        requestId       : UUID.randomUUID().toString(),
                        tokenType       : 'TXN_TOKEN',
                        token           : token.get(),
                ],
                "body": [
                        mid                  : m()?.id,
                        fetchAllPaymentOffers: null,
                        applyPaymentOffer    : null,
                        addNPayOnUPIPushSupported   : false,
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

    @Override
    ResponseSpecification success_schema() {
        new ResponseSpecBuilder()
                .expectStatusCode(200)
//                .addResponseSpecification(headersResSpec)
                .expectBody('head', isA(Object.class))
                .expectBody('body', isA(Object.class))
                .rootPath('head')
                .expectBody('requestId', nullValue())
                .expectBody('responseTimestamp', not(isEmptyOrNullString()))
                .expectBody('version', equalTo('v1'))
                .rootPath('body')
                .expectBody('resultInfo', isA(Object.class))
                .expectBody('merchantDetails', isA(Object.class))
                .expectBody('walletOnly', isA(Boolean.class))
                .expectBody('zeroCostEmi', isA(Boolean.class))
                .expectBody('pcfEnabled', isA(Boolean.class))
                .expectBody('nativeJsonRequestSupported', isA(Boolean.class))
                .expectBody('orderId', anyOf(nullValue(), not(isEmptyOrNullString())))
                .expectBody('activeMerchant', isA(Boolean.class))
                .expectBody('oneClickMaxAmount', not(isEmptyOrNullString()))
                .expectBody('userDetails', isA(Object.class))
                .expectBody('loginInfo', isA(Object.class))
                .expectBody('addDescriptionMandatory', isA(Boolean.class))
                .expectBody('descriptionTextFormat', anyOf(nullValue(), isA(String.class)))
                .expectBody('onTheFlyKYCRequired', isA(Boolean.class))
                .expectBody('paymentFlow', isA(String.class))
                .expectBody('merchantPayOption', isA(Object.class))
                .rootPath('body.resultInfo')
                .expectBody('resultStatus', not(isEmptyOrNullString()))
                .expectBody('resultCode', not(isEmptyOrNullString()))
                .expectBody('resultMsg', not(isEmptyOrNullString()))
                .rootPath('body.merchantDetails')
                .expectBody('mcc', anyOf(equalTo(null), not(isEmptyOrNullString())))
                .expectBody('merchantVpa', anyOf(equalTo(null), not(isEmptyOrNullString())))
                .expectBody('merchantName', not(isEmptyOrNullString()))
                .expectBody('merchantLogo', anyOf(equalTo(null), not(isEmptyOrNullString())))
                .rootPath('body.userDetails')
                .expectBody('paytmCCEnabled', isA(Boolean.class))
                .expectBody('kyc', isA(Boolean.class))
                .expectBody('username', not(isEmptyOrNullString()))
                .expectBody('mobile', not(isEmptyOrNullString()))
                .rootPath('body.loginInfo')
                .expectBody('userLoggedIn', isA(Boolean.class))
                .expectBody('pgAutoLoginEnabled', isA(Boolean.class))
                .expectBody('mobileNumberNonEditable', isA(Boolean.class))
                .rootPath('body.merchantPayOption')
                .expectBody('', hasKey('savedMandateBanks'))
                .expectBody('paymentModes', isA(List.class))
                .rootPath('body.merchantPayOption.paymentModes')
                .expectBody('displayName', everyItem(not(isEmptyOrNullString())))
                .expectBody('isDisabled', everyItem(isA(Object.class)))
                .expectBody('payChannelOptions', everyItem(isA(List.class)))
//                .expectBody('', everyItem(hasKey('feeAmount')))       assertion not valid in case of actual UPI system
//                .expectBody('', everyItem(hasKey('taxAmount')))
//                .expectBody('', everyItem(hasKey('totalTransactionAmount')))
//                .expectBody('priority', everyItem(not(isEmptyOrNullString())))
                .expectBody('prepaidCardSupported', everyItem(anyOf(nullValue(), isA(Boolean.class))))
                .expectBody('paymentMode', everyItem(not(isEmptyOrNullString())))
                .expectBody('isHybridDisabled', everyItem(isA(Boolean.class)))
                .rootPath('body.merchantPayOption.paymentModes.isDisabled')
                .expectBody('status', everyItem(isIn('false', 'true')))
                .expectBody('msg', everyItem(anyOf(isEmptyOrNullString(), not(isEmptyOrNullString()))))
                .rootPath('body.merchantPayOption.paymentModes.payChannelOptions')
                .expectBody('isDisabled', everyItem(everyItem(isA(Object.class))))
                .expectBody('hasLowSuccess', everyItem(everyItem(isA(Object.class))))
                .expectBody('', everyItem(everyItem(hasKey('iconUrl'))))
                .expectBody('minAmount', everyItem(everyItem(anyOf(nullValue(), isA(Object.class)))))
                .expectBody('maxAmount', everyItem(everyItem(anyOf(nullValue(), isA(Object.class)))))
                .expectBody('codHybridErrMsg', everyItem(everyItem(anyOf(isA(String.class), nullValue()))))
                .expectBody('codMessage', everyItem(everyItem(anyOf(isA(String.class), nullValue()))))
                .expectBody('isHybridDisabled', everyItem(everyItem(isA(Boolean.class))))
                .rootPath('body.merchantPayOption.paymentModes.payChannelOptions.isDisabled')
                .expectBody('status', everyItem(everyItem(isIn('false', 'true'))))
                .expectBody('msg', everyItem(everyItem(anyOf(nullValue(), not(isEmptyOrNullString())))))
                .rootPath('body.merchantPayOption.paymentModes.payChannelOptions.hasLowSuccess')
                .expectBody('status', everyItem(everyItem(isIn('false', 'true'))))
                .expectBody('msg', everyItem(everyItem(anyOf(isEmptyString(), not(isEmptyOrNullString())))))
                .rootPath('body.merchantPayOption.paymentModes.payChannelOptions.minAmount')
                .expectBody('currency', everyItem(everyItem(equalTo('INR'))))
                .expectBody('value', everyItem(everyItem(not(isEmptyOrNullString()))))
                .rootPath('body.merchantPayOption.paymentModes.payChannelOptions.maxAmount')
                .expectBody('currency', everyItem(everyItem(equalTo('INR'))))
                .expectBody('value', everyItem(everyItem(not(isEmptyOrNullString()))))
                .build()
    }

    @Override
    ResponseSpecification body_merchantPayOption_savedInstruments_schema() {
        new ResponseSpecBuilder()
                .expectStatusCode(200)
                .addResponseSpecification(results.success as ResponseSpecification)
                .rootPath('body.merchantPayOption.savedInstruments')
                .expectBody('isDisabled', everyItem(isA(Object.class)))
                .expectBody('hasLowSuccess', everyItem(isA(Object.class)))
                .expectBody('iconUrl', everyItem(isA(String.class)))
                .expectBody('oneClickSupported', everyItem(isA(Boolean.class)))
                .expectBody('cardDetails', everyItem(isA(Object.class)))
                .expectBody('issuingBank', everyItem(isA(String.class)))
                .expectBody('isEmiAvailable', everyItem(isA(Boolean.class)))
                .expectBody('authModes', everyItem(isA(List.class)))
                .expectBody('displayName', everyItem(isA(String.class)))
//                .expectBody('priority', everyItem(isA(String.class)))
                .expectBody('paymentOfferDetails', everyItem(anyOf(nullValue(), isA(Object.class))))
                .expectBody('savedCardEmisubventionDetail', everyItem(anyOf(nullValue(), isA(Object.class))))
                .expectBody('isHybridDisabled', everyItem(isA(Boolean.class)))
                .expectBody('channelCode', everyItem(isA(String.class)))
                .expectBody('channelName', everyItem(isA(String.class)))
                .expectBody('isEmiHybridDisabled', everyItem(isA(Boolean.class)))
                .build()
    }

    final ThreadLocal<String> orderId = new ThreadLocal<>()
    final ThreadLocal<String> token = new ThreadLocal<>()

    @BeforeMethod
    void setTxnToken(Method method, ITestResult testResult) {
        try {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = 10
            root.body.orderId.with { orderId.set(it) }
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void "test unable to fetch pay options details when mid is not provided in query params"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void 'test unable to fetch pay options details when head token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void 'test unable to fetch pay options details when head token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void 'test unable to fetch pay options details when head token = \'\''() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void 'test unable to fetch pay options details when head token equals random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.sessionIsExpired as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test unable to fetch pay options details when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant
    @Test
    void 'test unable to fetch pay options details when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void "test unable to fetch pay options details when mid in query params is different from mid in body"() {
        def root = root()
        root.body.mid = new Random().nextLong().abs() as String
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
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

    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @Test
    void 'test unable to fetch body userDetails when token does not have user authentication'() {
        def root = root()
        req().body(root).post().then()
                .body('body', not(hasKey('userDetails')))
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
    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
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
    @Merchant({  it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void 'test body paymentFlow when m addnpay == true && token contains user authentication'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.paymentFlow', equalTo('ADDANDPAY'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PG2_COD.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'cod\') when m payModes contains(\'cod\')'() {
        INITIATE_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = 10
            root.body.orderId.with { orderId.set(it) }
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('COD'))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PG2_COD.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'cod\') displayName'() {
        INITIATE_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = 10
            root.body.orderId.with { orderId.set(it) }
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'COD'}.displayName", equalTo('Cash on Delivery (COD)'))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PG2_COD.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'cod\') payChannelOptions'() {
        INITIATE_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = 10
            root.body.orderId.with { orderId.set(it) }
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'COD' }.payChannelOptions")
                .body('', hasSize(1))
                .body('minAmount', everyItem(isA(Object.class)),
                        'hasLowSuccess', everyItem(isA(Object.class)),
                        'codMessage', everyItem(isA(String.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)),
                        'isDisabled', everyItem(isA(Object.class)),
                        'iconUrl', everyItem(nullValue()),
                        'codHybridErrMsg', everyItem(isA(String.class)))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test body simplifiedPaymentOffers is returned in response when body simplifiedPaymentOffers is passed in Init Txn API'() {
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.simplifiedPaymentOffers')
                .body('', hasKey('promoCode'))
                .body('promoCode', nullValue())
                .body('applyAvailablePromo', equalTo(true))
                .body('validatePromo', equalTo(true))
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void 'test body simplifiedPaymentOffers is not returned in response when body simplifiedPaymentOffers is not passed in Init Txn API'() {
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = null
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body')
                .body('', not(hasKey('simplifiedPaymentOffers')))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ_HDFC.getId() })
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers is not provided'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('fetchAllPaymentOffers')
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('paymentOffers')))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.BAJAJFINEMI.getId() })
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers = null'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.fetchAllPaymentOffers = null
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('paymentOffers')))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.EMI_DC.getId() })
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers = \'\''() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.fetchAllPaymentOffers = ''
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('paymentOffers')))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.MGV_AGGREGATOR_CHILD.getId() })
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers equals random value'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.fetchAllPaymentOffers = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('paymentOffers')))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.NATIVE_PROMO_HYBRID.getId()})
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant does not have promos configured'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        m().promos.clear()
        root.body.fetchAllPaymentOffers = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(0))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId()})
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant has promos configured and body simplifiedPaymentOffers promoCode is not passed in Initiate Txn API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        m().promos.clear()
        if (!m().promos.addAll((0..9).collect { new Promo() })) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.fetchAllPaymentOffers = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(m().promos.size()))
    }

    @Override
    @Merchant(edit = true)
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant has promos configured and body simplifiedPaymentOffers promoCode equals valid promo in Initiate Txn API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        if (!m().promos.addAll((0..9).collect { new Promo() })) throw new SkipException(PROMOS_NOT_ADDED)
        String promoCode = m().promos.find().name
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : promoCode,
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        root.body.fetchAllPaymentOffers = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(1))
                .body('body.paymentOffers[0].promocode', equalTo(promoCode))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.PPBLC_ONLY.getId() })
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant has promos configured and body simplifiedPaymentOffers promoCode equals invalid promo in Initiate Txn API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        if (!m().promos.addAll((0..9).collect { new Promo() })) throw new SkipException(PROMOS_NOT_ADDED)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : UUID.randomUUID().toString(),
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        root.body.fetchAllPaymentOffers = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(0))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.AMEX_PCF.getId() })
    @AUser
    @Test
    void 'test when body fetchAllPaymentOffers = false given merchant has promos configured'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.fetchAllPaymentOffers = 'false'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('paymentOffers')))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.PGOnly_Retry.getId() })
    @AUser
    @Test
    void 'test when body applyPaymentOffer is not provided'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.remove('applyPaymentOffer')
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId() })
    @AUser
    @Test
    void 'test when body applyPaymentOffer = null'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.applyPaymentOffer = null
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId() })
    @AUser
    @Test
    void 'test when body applyPaymentOffer = \'\''() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.applyPaymentOffer = ''
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.EMI.getId() })
    @AUser
    @Test
    void 'test when body applyPaymentOffer equals random value'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        root.body.applyPaymentOffer = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.ADD_MONEY_PCF.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = true in Initiate Txn API and body applyPaymentOffer = true in FetchPayOptions API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        user().savedCards.add(cards.find())
        root.body.applyPaymentOffer = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(isA(Object.class)))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.PGOnly_Pcf.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = false in Initiate Txn API and body applyPaymentOffer = true in FetchPayOptions API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : m().promos.find().name,
                    applyAvailablePromo: 'false',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        user().savedCards.add(cards.find())
        root.body.applyPaymentOffer = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(nullValue()))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.WALLETOnly_PCF.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = true in Initiate Txn API and body applyPaymentOffer = false in FetchPayOptions API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        user().savedCards.add(cards.find())
        root.body.applyPaymentOffer = 'false'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(nullValue()))
    }

    @Override
    @Merchant(edit = true)
    @AUser(edit = true)
    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = false in Initiate Txn API and body applyPaymentOffer = false in FetchPayOptions API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : m().promos.find().name,
                    applyAvailablePromo: 'false',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        user().savedCards.add(cards.find())
        root.body.applyPaymentOffer = 'false'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(nullValue()))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.EMISubvention.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body simplifiedPaymentOffers promoCode is not passed in Initiate Txn API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        user().savedCards.add(cards.find())
        root.body.fetchAllPaymentOffers = 'true'
        root.body.applyPaymentOffer = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(greaterThan(0)))
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(isA(Object.class)))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId()})
    @AUser(edit = true)
    @Test
    void 'test when body simplifiedPaymentOffers promoCode equals valid promo code in Initiate Txn API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        m().promos.clear()
        if (!m().promos.addAll((0..9).collect { new Promo() })) throw new SkipException(PROMOS_NOT_ADDED)
        String promoCode = m().promos.find().name
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : promoCode,
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        user().savedCards.add(cards.find())
        root.body.fetchAllPaymentOffers = 'true'
        root.body.applyPaymentOffer = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(equalTo(1)))
                .body('body.paymentOffers[0].promocode', equalTo(promoCode))
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(nullValue()))
    }

    @Override
    @Merchant(edit = true,value = {it.id == Constants.MerchantType.Hybrid.getId() })
    @AUser(edit = true)
    @Test
    void 'test when body simplifiedPaymentOffers promoCode equals invalid promo code in Initiate Txn API'() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : 'NOT_VALID',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        user().savedCards.add(cards.find())
        root.body.fetchAllPaymentOffers = 'true'
        root.body.applyPaymentOffer = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(equalTo(0)))
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(nullValue()))
    }

//    @Override
//    @Merchant(edit = true)
//    @AUser(edit = true)
//    @Test(enabled = false)
    void "test that Bulk Promo API is applied successfully when user has saved cards"() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = UUID.randomUUID().toString().replace("-","")
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        user().savedCards.add(cards.find())
        root.body.applyPaymentOffer = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(isA(Object.class)))
    }

    @Override
    @Merchant(edit = true)
    @AUser
    @Test
    void "test that Bulk Promo API is applied successfully when merchant has saved cards"() {
        RedisAPI.deleteKey(PROMO_OFFERS_REDIS_KEY_PREFIX + m().id)
        INIT_TXN:
        {
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = '2'
            root.body.orderId.with { orderId.set(it) }
            root.body.userInfo.custId = m().users[0].id
            root.body.simplifiedPaymentOffers = [
                    promoCode          : '',
                    applyAvailablePromo: 'true',
                    validatePromo      : 'true',
            ]
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        def root = root()
        if (!m().promos.add(new Promo())) throw new SkipException(PROMOS_NOT_ADDED)
        m().users[0].savedCards.add(cards.find())
        root.body.applyPaymentOffer = 'true'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments.paymentOfferDetails', everyItem(isA(Object.class)))
    }

    @Owner('Pulkit')
    @Merchant({ it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
    void "Verify UPIPUSHEXPRESS present in add and pay paymodes for app invoke flow and addNPayOnUPIPushSupported is true"() {
        INITIATE_TXN:
        {
            user().wallets['main'].balance = 2
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = ""
            root.body.txnAmount.value = 10
            root.body.orderId.with { orderId.set(it) }
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }

        FETCH_MERCHANT_INFO_V1:
        {
            def api = new TxnTokenFetchMerchantInfoV1Test()
            def root = api.root()
            root.head.ssoToken = user()?.tokens?.getAt('sso')?.id
            root.head.txnToken= token.get()
            root.body.mid= m()?.id
            root.body.orderId = orderId.get()
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
        FETCH_PAYMENT_OPTION_V1:
        {
            def root = root()
            root.body.addNPayOnUPIPushSupported = true
            req().body(root).post().then()
                    .spec(success_schema())
                    .spec(results.success as ResponseSpecification)
                    .body("body.paymentFlow", equalTo("ADDANDPAY"))
                    .body("body.addMoneyPayOption.paymentModes.find { it.paymentMode == 'UPI'}.payChannelOptions.channelCode", hasItem("UPIPUSHEXPRESS"))
        }
    }

    @Owner('Pulkit')
    @Merchant({ it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test
    void "Verify UPIPUSHEXPRESS not present in add and pay paymodes for app invoke flow and addNPayOnUPIPushSupported is false"() {
        INITIATE_TXN:
        {
            user().wallets['main'].balance = 2
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = ""
            root.body.txnAmount.value = 10
            root.body.orderId.with { orderId.set(it) }
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }

        FETCH_MERCHANT_INFO_V1:
        {
            def api = new TxnTokenFetchMerchantInfoV1Test()
            def root = api.root()
            root.head.ssoToken = user()?.tokens?.getAt('sso')?.id
            root.head.txnToken= token.get()
            root.body.mid= m()?.id
            root.body.orderId = orderId.get()
            api.req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
        FETCH_PAYMENT_OPTION_V1:
        {
            def root = root()
            root.body.addNPayOnUPIPushSupported = false
            req().body(root).post().then()
                    .spec(success_schema())
                    .spec(results.success as ResponseSpecification)
                    .body("body.paymentFlow", equalTo("ADDANDPAY"))
                    .body("body.addMoneyPayOption.paymentModes.find { it.paymentMode == 'UPI'}.payChannelOptions.channelCode", not(hasItem("UPIPUSHEXPRESS")))
        }
    }

    @Owner('Pulkit')
    @Merchant({ it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser(edit = true)
    @Test
    void "Verify UPIPUSHEXPRESS not present in add and pay paymodes for non app invoke flow even when addNPayOnUPIPushSupported is true"() {
        INITIATE_TXN:
        {
            user().wallets['main'].balance = 2
            def api = new InitiateTransaction()
            def root = api.root()
            root.body.paytmSsoToken = user()?.tokens?.getAt('sso')?.id
            root.body.txnAmount.value = 10
            root.body.orderId.with { orderId.set(it) }
            api.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken').with { token.set(it) }
        }
        FETCH_PAYMENT_OPTION_V1:
        {
            def root = root()
            root.body.addNPayOnUPIPushSupported = false
            req().body(root).post().then()
                    .spec(success_schema())
                    .spec(results.success as ResponseSpecification)
                    .body("body.paymentFlow", equalTo("ADDANDPAY"))
                    .body("body.addMoneyPayOption.paymentModes.find { it.paymentMode == 'UPI'}.payChannelOptions.channelCode", not(hasItem("UPIPUSHEXPRESS")))
        }
    }
}
