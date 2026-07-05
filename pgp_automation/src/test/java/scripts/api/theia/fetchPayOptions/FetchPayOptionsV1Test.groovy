package scripts.api.theia.fetchPayOptions

import com.paytm.LocalConfig
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.dto.PaymentDTO
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.merchant.util.Promo
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.DataProvider
import org.testng.annotations.Optional
import org.testng.annotations.Parameters
import org.testng.annotations.Test

import static com.paytm.appconstants.Constants.MerchantType.*
import static com.paytm.appconstants.Constants.Owner.*
import static com.paytm.base.test.Group.Status.BUG
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
abstract class FetchPayOptionsV1Test extends TestSetUp {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    abstract RequestSpecBuilder reqBldr()

    final RequestSpecification req() { given(reqBldr().build()) }

    abstract Map<String, Object> root()

    final ResponseSpecification headersResSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectHeader('Date', not(isEmptyOrNullString()))
            .expectHeaders([
                    "Server"                      : "Apache-Coyote/1.1",
                    'Content-Type'                : 'application/json;charset=UTF-8',
                    'Transfer-Encoding'           : 'chunked',
//                    "Content-Security-Policy"     : "default-src 'self' https://*.paytm.com https://*.paytm.in; connect-src 'self' https://*.paytm.com https://*.paytm.in wss://*.paytm.in ; frame-src 'self' https://*.paytm.com https://*.paytm.in; img-src 'self' data: https://*.paytm.in; script-src 'unsafe-eval' 'unsafe-inline' https://*.paytm.in ; style-src 'unsafe-inline' https://*.paytm.in https://maxcdn.bootstrapcdn.com ;font-src 'self' data: https://*.paytm.in https://themes.googleusercontent.com ;",
                    "Access-Control-Allow-Origin" : "*",
                    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
                    "Access-Control-Allow-Headers": "DNT, X-CustomHeader, Keep-Alive, User-Agent, X-Requested-With, If-Modified-Since, Cache-Control, Content-Type"
            ])
            .build()

    ResponseSpecification success_schema() {
        new ResponseSpecBuilder()
                .expectStatusCode(200)
        //        .addResponseSpecification(headersResSpec)
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
                .expectBody('merchantDisplayName', anyOf(nullValue(), not(isEmptyOrNullString())))
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
                .expectBody('priority', everyItem(not(isEmptyOrNullString())))
                .expectBody('prepaidCardSupported', everyItem(anyOf(nullValue(), isA(Boolean.class))))
                .expectBody('paymentMode', everyItem(not(isEmptyOrNullString())))
                .expectBody('isHybridDisabled', everyItem(isA(Boolean.class)))
                .rootPath('body.merchantPayOption.paymentModes.isDisabled')
                .expectBody('status', everyItem(isIn('false', 'true')))
                .expectBody('msg', everyItem(anyOf(isEmptyOrNullString(), not(isEmptyOrNullString()))))
                .rootPath('body.merchantPayOption.paymentModes.payChannelOptions')
                .expectBody('isDisabled', everyItem(everyItem(anyOf(nullValue(), isA(Object.class)))))
                .expectBody('hasLowSuccess', everyItem(everyItem(anyOf(nullValue(), isA(Object.class)))))
                .expectBody('', everyItem(everyItem(hasKey('iconUrl'))))
                .expectBody('minAmount', everyItem(everyItem(anyOf(nullValue(), isA(Object.class)))))
                .expectBody('maxAmount', everyItem(everyItem(anyOf(nullValue(), isA(Object.class)))))
                .expectBody('codHybridErrMsg', everyItem(everyItem(anyOf(isA(String.class), nullValue()))))
                .expectBody('codMessage', everyItem(everyItem(anyOf(isA(String.class), nullValue()))))
                .expectBody('isHybridDisabled', everyItem(everyItem(isA(Boolean.class))))
                .rootPath('body.merchantPayOption.paymentModes.payChannelOptions.isDisabled')
                .expectBody('status', everyItem(everyItem(isIn('false', 'true'))))
                .expectBody('msg', everyItem(everyItem(anyOf(nullValue(), isA(String.class)))))
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

    ResponseSpecification body_userDetails_schema = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .addResponseSpecification(results.success as ResponseSpecification)
            .rootPath('body.userDetails')
            .expectBody('paytmCCEnabled', isA(Boolean.class))
            .expectBody('kyc', isA(Boolean.class))
            .expectBody('username', isA(String.class))
            .expectBody('mobile', isA(String.class))
            .build()

    ResponseSpecification body_merchantPayOption_paymentMode_schema(String paymentMode) {
        new ResponseSpecBuilder()
                .expectStatusCode(200)
                .addResponseSpecification(results.success as ResponseSpecification)
                .rootPath("body.merchantPayOption.paymentModes.find { it.paymentMode == '$paymentMode' }")
                .expectBody('', not(nullValue()))
                .expectBody('displayName', isA(String.class))
                .expectBody('isDisabled', isA(Object.class))
                .expectBody('payChannelOptions', isA(List.class))
                .expectBody('feeAmount', nullValue())
                .expectBody('taxAmount', nullValue())
                .expectBody('totalTransactionAmount', nullValue())
                .expectBody('priority', anyOf(isA(String.class), nullValue()))
                .expectBody('onboarding', isA(Boolean.class))
                .expectBody('paymentMode', isA(String.class))
                .expectBody('isHybridDisabled', isA(Boolean.class))
                .build()
    }



    ResponseSpecification body_merchantPayOption_savedInstruments_schema() {
        new ResponseSpecBuilder()
                .expectStatusCode(200)
                .addResponseSpecification(results.success as ResponseSpecification)
                .rootPath('body.merchantPayOption.savedInstruments')
                .expectBody('', everyItem(not(hasKey('isDisabled'))))
                .expectBody('', everyItem(not(hasKey('hasLowSuccess'))))
                .expectBody('tokenStatus', everyItem(anyOf(nullValue(), isEmptyString())))
                .expectBody('isEligibleForCoft', everyItem(isA(Boolean.class)))
                .expectBody('isHybridDisabled', everyItem(isA(Boolean.class)))
                .expectBody('displayName', everyItem(isA(String.class)))
                .expectBody('isCardCoft', everyItem(isA(Boolean.class)))
                .expectBody('bankName', everyItem(isA(String.class)))
                .expectBody('isCoftPaymentSupported', everyItem(isA(Boolean.class)))
                .expectBody('prepaidCard', everyItem(isA(Boolean.class)))
                .expectBody('issuingBank', everyItem(isA(String.class)))
                .expectBody('paymentOfferDetails', everyItem(anyOf(nullValue(), isA(Object.class))))
                .expectBody('paymentOfferDetailsV2', everyItem(anyOf(nullValue(), isA(Object.class))))
                .expectBody('cardDetails', everyItem(isA(Object.class)))
                .expectBody('iconUrl', everyItem(isA(String.class)))
                .expectBody('channelCode', everyItem(isA(String.class)))
                .expectBody('bankLogoUrl', everyItem(isA(String.class)))
                .expectBody('par', everyItem(anyOf(nullValue(), isA(Object.class))))
                .expectBody('cardType', everyItem(isA(String.class)))
                .expectBody('priority', everyItem(isA(String.class)))
                .expectBody('corporateCard', everyItem(isA(Boolean.class)))
                .expectBody('oneClickSupported', everyItem(isA(Boolean.class)))
                .expectBody('isEmiAvailable', everyItem(isA(Boolean.class)))
                .expectBody('channelName', everyItem(isA(String.class)))
                .expectBody('authModes', isA(Object.class))
                .expectBody('isEmiHybridDisabled', everyItem(isA(Boolean.class)))
                .expectBody('savedCardEmisubventionDetail', everyItem(anyOf(nullValue(), isA(Object.class))))
                .build()
    }

/*
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
                .expectBody('priority', everyItem(isA(String.class)))
                .expectBody('paymentOfferDetails', everyItem(anyOf(nullValue(), isA(Object.class))))
                .expectBody('savedCardEmisubventionDetail', everyItem(anyOf(nullValue(), isA(Object.class))))
                .expectBody('isHybridDisabled', everyItem(isA(Boolean.class)))
                .expectBody('channelCode', everyItem(isA(String.class)))
                .expectBody('channelName', everyItem(isA(String.class)))
                .expectBody('isEmiHybridDisabled', everyItem(isA(Boolean.class)))
                .build()
    }*/

    final com.paytm.utils.merchant.merchant.util.Merchant scwMerchant = new com.paytm.utils.merchant.merchant.util.Merchant('SCWMER90619707098260')

    @Test
    abstract void 'test unable to fetch pay options details when mid is not provided in query params'()

    @Test
    abstract void 'test unable to fetch pay options details when head token is not provided'()

    @Test
    abstract void 'test unable to fetch pay options details when head token = null'()

    @Test
    abstract void 'test unable to fetch pay options details when head token = \'\''()

    @Test
    abstract void 'test unable to fetch pay options details when head token equals random value'()

    @Test
    abstract void 'test unable to fetch pay options details when body mid is not provided'()

    @Test
    abstract void 'test unable to fetch pay options details when body mid = null'()

    @Test
    abstract void 'test unable to fetch pay options details when body mid = \'\''()

    @Test
    abstract void 'test unable to fetch pay options details when body mid equals random value'()

    @Test
    abstract void 'test unable to fetch pay options details when mid in query params is different from mid in body'()


//    @Merchant
//    @AUser
//    @Test
////TODO not required
//    void testWhenMidIsValid() {
//        def root = root()
//        req().body(root).post().then()
//                .spec(results.success as ResponseSpecification)
//                .body('body', allOf(hasKey('resultInfo'), hasKey('merchantDetails'), hasKey('walletOnly')
//                        , hasKey('zeroCostEmi'), hasKey('pcfEnabled'), hasKey('nativeJsonRequestSupported'),
//                        hasKey('activeMerchant'), hasKey('oneClickMaxAmount'), hasKey('userDetails')
//                        , hasKey('loginInfo'), hasKey('onTheFlyKYCRequired'), hasKey('paymentFlow')
//                        , hasKey('merchantPayOption'), hasKey('addMoneyPayOption'), hasKey('merchantLimitInfo')))
//    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test able to fetch body merchantDetails'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantDetails')
                .body('mcc', anyOf(isA(String.class), equalTo(null)),
                        'merchantVpa', anyOf(isA(String.class), equalTo(null)),
                        'merchantName', isA(String.class),
                        'merchantLogo', anyOf(isA(String.class), equalTo(null)))
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT.getId() })
    @AUser
    @Test
    void 'test priority in fpo for all paymodes for default merchant'() {
        def root = root()

        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantPayOption')
                .body('paymentModes.paymentMode', hasItems('BALANCE', 'GIFT_VOUCHER', 'CREDIT_CARD','DEBIT_CARD', 'NET_BANKING', 'UPI', 'PPBL'),
                    'paymentModes.priority', hasItems('7', '8', '4', '3', '1', '2','9'))
    }


    @Merchant(edit = true, value = { it.id == Constants.MerchantType.PGOnly.getId() })
    @AUser(edit = true)
    @Test
    void 'Boss Panel with default merchant'() {
        def root = root()
        user().savedCards.clear()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.merchantPayOption.paymentModes')
                .body('paymentMode', hasItems('BALANCE', 'NET_BANKING', 'CREDIT_CARD', 'DEBIT_CARD', 'EMI', 'UPI', 'PPBL'))
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser
    @Test
    void 'test able to fetch body merchantDetails merchantName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantDetails')
                .body('merchantName', isIn(m().names.display.name, m().names.business.name))
    }

 //   @Merchant
 //   @AUser
 //   @Test(enabled = false)
//TODO need to implement mcc fetching
    void 'test able to fetch body merchantDetails mcc'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantDetails')
                .body('mcc', equalTo(m().mcc.code))
    }

//    @Merchant
//    @AUser
//    @Test(enabled = false)
//TODO need to implement merchantVpa fetching
    void 'test able to fetch body merchantDetails merchantVpa'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantDetails')
                .body('merchantVpa', equalTo(m().vpa))
    }

//    @Merchant
//    @AUser
//    @Test(enabled = false)
    //TODO need to implement merchantLogo fetching
    void 'test able to fetch body merchantDetails merchantLogo'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantDetails')
                .body('merchantLogo', equalTo(m().logo))
    }

    @Merchant({it.id == Constants.MerchantType.WalletOnly.getId()})
    @AUser
    @Test
    void 'test body walletOnly = true when merchant has wallet-only preference enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.walletOnly', equalTo(true))
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body walletOnly = false when merchant has wallet-only preference disabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.walletOnly', equalTo(false))
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body nativeJsonRequestSupported = true when merchant has nativeJsonRequest preference enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.nativeJsonRequestSupported', equalTo(true))
    }

    @Merchant({  it.id == Constants.MerchantType.FLAT_PCF.getId() })
    @AUser
    @Test
    void 'test body nativeJsonRequestSupported = false when merchant has nativeJsonRequest preference disabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.nativeJsonRequestSupported', equalTo(false))
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test
    void 'test body activeMerchant = true when merchant is active'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.activeMerchant', equalTo(true))
    }


//    @Merchant({ !it.active })
//    @AUser
//    @Test(enabled = false)
//TODO Presently, merchants.find { it.active } == null
    void 'test body activeMerchant = false when merchant is not active'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.activeMerchant', equalTo(false))
    }

    @Merchant
    @AUser
    @Test
    void 'test body oneClickMaxAmount = 2000'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.oneClickMaxAmount', equalTo('2000'))
    }

    @Test
    abstract void 'test able to fetch body userDetails when token has user authentication'()

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(paytmcc = 'true')
    @Test
    void 'test body userDetails paytmCCEnabled = true when merchant and user both support pdc'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.userDetails.paytmCCEnabled', equalTo(true))
    }

    @Merchant({  it.id == Constants.MerchantType.PPBLYONLY.getId()})
    @AUser(paytmcc = 'true')
    @Test
    void 'test body userDetails paytmCCEnabled = true when pdc is not configured on merchant but is configured on user'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.userDetails.paytmCCEnabled', equalTo(true))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(paytmcc = 'false')
    @Test
    void 'test body userDetails paytmCCEnabled = false when pdc is not configured on user but is configured on merchant'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.userDetails.paytmCCEnabled', equalTo(false))
    }

    @Issue('PGP-23213')
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(kyc = 'true')
    @Test(groups = [BUG])
    void 'test body userDetails kyc = true when user\'s kyc is completed'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.userDetails.kyc', equalTo(false))       // AS PER COMMENTS MENTIONED IN JIRA: PGP-23213
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(kyc = 'false')
    @Test
    void 'test body userDetails kyc = false when user\'s kyc is not completed'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.userDetails.kyc', equalTo(false))
    }

 //   @Merchant
 //   @AUser
 //   @Test(enabled = false)
//TODO need to impl u.name
    void 'test body userDetails username'() {}

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body userDetails mobile equals user\'s mobile no'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.userDetails.mobile', equalTo(user().mobile))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body loginInfo is as expected'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.loginInfo')
                .body('userLoggedIn', isA(Boolean.class),
                        'pgAutoLoginEnabled', isA(Boolean.class),
                        'mobileNumberNonEditable', isA(Boolean.class))
    }

    @Test
    abstract void 'test body loginInfo userLoggedIn == true when token has user authentication'()

//    @Merchant
//    @AUser
//    @Test(enabled = false)
//TODO need to find when pgAutoLoginEnabled comes as true || false
    void 'test body loginInfo pgAutoLoginEnabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.loginInfo.pgAutoLoginEnabled', equalTo(true))
    }

//    @Merchant
//    @AUser
//    @Test(enabled = false)
//TODO need to find when mobileNumberNonEditable comes as true || false
    void 'test body loginInfo mobileNumberNonEditable'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.loginInfo.mobileNumberNonEditable', equalTo(true))
    }

    @Test
    abstract void 'test body paymentFlow when m hybrid == true && token contains user authentication'()

    @Test
    abstract void 'test body paymentFlow when m addnpay == true && token contains user authentication'()


    @Merchant({  it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test body paymentFlow = NONE when merchant is pg only'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentFlow', equalTo('NONE'))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test able to fetch body merchantPayOption'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantPayOption')
                .body('paymentModes', isA(List.class),
                        'savedInstruments', isA(List.class),
                        'userProfileSarvatra', anyOf(isA(String.class), nullValue()),
                        'activeSubscriptions', anyOf(isA(String.class), nullValue()))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(edit = true)
    @Test
    void 'test body merchantPayOption savedInstruments equals empty list when user has no saved cards'() {
        def root = root()
        //user().savedCards.clear()
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments', hasSize(0))
    }

    @Merchant({ it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['amex', 'bajajfn']) } && it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(edit = true)
    @Test
//TODO need to write tc's for cards saved on mid, custId
    void 'test body merchantPayOption savedInstruments equals iterable with size greater than 0 when user has saved cards'() {
        def root = root()
        //assert user().savedCards.add(cards.find { it.type == 'credit' && !(it.scheme in ['amex', 'bajajfn']) })
        User user = new User(user().getMobile(),user().getPassword());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments', hasSize(not(0)))
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId() })
    @AUser(edit = true)
    @Test
    void 'test body merchantPayOption savedInstruments equals empty list when user has saved cards but these cards are not supported by merchant'() {
        def root = root()
        //user().savedCards.clear()
        //assert user().savedCards.add(cards.find { it.type == 'credit' && it.scheme == 'amex' })
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.savedInstruments', not('Amex'))
    }

    @Merchant({it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @AUser(edit = true)
    @Test
    void 'test body merchantPayOption savedInstruments contains saved card details when user has amex saved card and merchant it'() {
        def root = root()
       // user().savedCards.clear()
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

    @Merchant({it.id == Constants.MerchantType.Seamless_Hybrid_Onus.getId()})
    @AUser(edit = true)
    @Test
    void 'test body merchantPayOption savedInstruments contains saved card details when user has bajajfn saved card and merchant supports it'() {
        def root = root()
        //user().savedCards.clear()
        //assert user().savedCards.add(cards.find { it.scheme == 'bajajfn' })
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_savedInstruments_schema())
    }

    @Merchant({it.id == Constants.MerchantType.AMEX_PCF.getId()})
    @AUser(edit = true)
    @Test
    void 'test body merchantPayOption savedInstruments contains saved card details when user has non-amex-bajajfn saved card and merchant supports it'() {
        def root = root()
        //user().savedCards.clear()
        //assert user().savedCards.add(cards.find { it.type == 'credit' && !(it.scheme in ['amex', 'bajajfn']) })
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_savedInstruments_schema())
    }

//    @Merchant({ it.payModes.empty })
//    @AUser
//    @Test(enabled = false)
//TODO need to know if merchant with this configuration can exist
    void 'test body merchantPayOption paymentModes when m payModes empty == true'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes', hasSize(0))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes equals iterable with size greater than 0 when merchant has payModes configured'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes', hasSize(greaterThan(0)))
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'ppi\') when m payModes contains(\'ppi\') && u ppi == true'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('BALANCE'))
    }


//    @Merchant({ it.payModes.contains('ppi') })
//    @AUser
//    @Test(enabled = false)
//TODO need user not having ppi
    void 'test !body merchantPayOption paymentModes contains(\'ppi\') when m payModes contains(\'ppi\') && u ppi == false'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'BALANCE'}", nullValue())
    }

    @Merchant({  it.id == Constants.MerchantType.PPBL_NB.getId() })
    @AUser(ppbl = 'true')
    @Test
    void 'test body merchantPayOption paymentModes contains(\'ppbl\') when m payModes contains(\'ppbl\') && u ppbl == true'() {
        def root = root()
        req().body(root).post().then()
                .spec(body_merchantPayOption_paymentMode_schema('PPBL'))
    }


//    @Merchant({ it.payModes.contains('ppbl') })
//    @AUser(ppbl = 'false')
//    @Test(enabled = false)
//TODO need user satisfying condition
    void 'test !body merchantPayOption paymentModes contains(\'ppbl\') when m payModes contains(\'ppbl\') && u ppbl == false'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .spec(body_merchantPayOption_paymentMode_schema('PPBL'))
    }

    @Merchant({  it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(paytmcc = 'true')
    @Test
    void 'test body merchantPayOption paymentModes contains(\'pdc\') when m payModes contains(\'pdc\') && u pdc == true'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('PAYTM_DIGITAL_CREDIT'))
    }


    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(paytmcc = 'false')
    @Test
    void 'test !body merchantPayOption paymentModes contains(\'pdc\') when m payModes contains(\'ppbl\') && u pdc == false'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}", nullValue())
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'cc\') when m payModes contains(\'cc\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('CREDIT_CARD'))
    }

    @Merchant({ it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'dc\') when m payModes contains(\'dc\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('DEBIT_CARD'))
    }

    @Merchant({  it.id == Constants.MerchantType.EMI_DC_CC.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'emi\') when m payModes contains(\'emi\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('EMI'))
    }

    @Merchant({ it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'upi\') when m payModes contains(\'upi\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('UPI'))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'nb\') when m payModes contains(\'nb\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('NET_BANKING'))
    }

    @Merchant({ it.id == Constants.MerchantType.PG2_COD.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes contains(\'cod\') when m payModes contains(\'cod\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(body_merchantPayOption_paymentMode_schema('COD'))
    }

    @Merchant({ it.id == Constants.MerchantType.FLAT_PCF.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'upi\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.displayName", equalTo('BHIM UPI'))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'cc\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'CREDIT_CARD'}.displayName", equalTo('Credit Card'))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'dc\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'DEBIT_CARD'}.displayName", equalTo('Debit Card'))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'nb\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'NET_BANKING'}.displayName", equalTo('Net Banking'))
    }

    @Merchant({ it.id == Constants.MerchantType.EMI_DC_CC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'emi\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'EMI'}.displayName", equalTo('EMI'))
    }

    @Merchant({  it.id == Constants.MerchantType.PG2_COD.getId() } )
    @AUser(paytmcc = 'true')
    @Test
    void 'test body merchantPayOption paymentModes find(\'cod\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'COD'}.displayName", equalTo('Cash on Delivery (COD)'))
    }


    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(ppbl = 'true')
    @Test
    void 'test body merchantPayOption paymentModes find(\'ppbl\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL'}.displayName", equalTo('Paytm Payments Bank'))
    }

    @Merchant({  it.id == Constants.MerchantType.AddMoney.getId()})
    @AUser(paytmcc = 'true')
    @Test
    void 'test body merchantPayOption paymentModes find(\'pdc\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.displayName", equalTo('Postpaid Loan'))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'ppi\') displayName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'BALANCE'}.displayName", equalTo('Paytm Balance'))
    }

//    @Merchant({ !it.payModes.empty && !it.preferences.hybrid.enabled })
//    @AUser
//    @Test(enabled = false)
//TODO need to fix
    void 'test body merchantPayOption paymentModes isHybridDisabled when m hybrid == false'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes.isHybridDisabled', everyItem(equalTo(false)))
    }

//    @Merchant({ !it.payModes.empty && it.preferences.hybrid.enabled })
//    @AUser
//    @Test(enabled = false)
//TODO need to fix
    void 'test body merchantPayOption paymentModes isHybridDisabled when m hybrid == true'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes.isHybridDisabled', everyItem(equalTo(true)))
    }

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes onboarding'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.merchantPayOption.paymentModes.onboarding', everyItem(equalTo(false)))
    }

    @Merchant({  it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes isDisabled for non-paytm instruments'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.findAll { it.paymentMode in ['UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'COD', 'EMI', 'NET_BANKING'] }.isDisabled")
                .body('', everyItem(allOf(hasKey('status'), hasKey('msg'), not(hasKey('userAccountExist')), not(hasKey('merchantAccept')))))
                .body('status', everyItem(equalTo('false')),
                        'msg', everyItem(isEmptyString()))
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(ppbl = 'true', paytmcc = 'true')
    @Test
    void 'test body merchantPayOption paymentModes isDisabled for paytm instruments'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.findAll { it.paymentMode in ['BALANCE', 'PPBL', 'PAYTM_DIGITAL_CREDIT'] }.isDisabled")
                .body('', everyItem(allOf(hasKey('status'), hasKey('msg'), hasKey('userAccountExist'), hasKey('merchantAccept'))))
                .body('status', everyItem(equalTo('false')),
                        'msg', everyItem(anyOf(nullValue(), not(isEmptyString()))),
                        'userAccountExist', everyItem(isIn(['true', 'false'])),
                        'merchantAccept', everyItem(isIn(['true', 'false'])))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(ppbl = 'true')
    @Test
    void 'test body merchantPayOption paymentModes isDisabled for ppbl when merchant and user both have ppbl'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['PPBL'] }.isDisabled")
                .body('', not(nullValue()))
                .body('status', equalTo('false'),
                        'msg', nullValue(),
                        'userAccountExist', equalTo('true'),
                        'merchantAccept', equalTo('true'))
    }

//    @Merchant({ it.payModes.contains('ppbl') })
//    @AUser(ppbl = 'false')
//    @Test(enabled = false)
//TODO need user satisfying condition
    void 'test body merchantPayOption paymentModes isDisabled for ppbl when merchant has ppbl but not user'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['PPBL'] }.isDisabled")
                .body('', not(nullValue()))
                .body('status', equalTo('true'),
                        'msg', equalTo('Please create account'),
                        'userAccountExist', equalTo('false'),
                        'merchantAccept', equalTo('true'))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(paytmcc = 'true')
    @Test
    void 'test body merchantPayOption paymentModes isDisabled for pdc when merchant and user both have pdc'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['PAYTM_DIGITAL_CREDIT'] }.isDisabled")
                .body('', not(nullValue()))
                .body('status', equalTo('false'),
                        'msg', nullValue(),
                        'userAccountExist', equalTo('true'),
                        'merchantAccept', equalTo('true'))
    }

    @Issue('PGP-23213')
    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser(paytmcc = 'false')
    @Test(groups = [BUG])
    void 'test body merchantPayOption paymentModes isDisabled for pdc when merchant has pdc but not user'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['PAYTM_DIGITAL_CREDIT'] }.isDisabled")
                .body('', nullValue())
//                .body('', not(nullValue()))   // AS PER COMMENT MENTIONED IN JIRA: PGP-23213 body.merchantPayOption.paymentModes for PAYTM_DIGITAL_CREDIT will be null
//                .body('status', equalTo('true'),
//                        'msg', equalTo('Please create account'),
//                        'userAccountExist', equalTo('false'),
//                        'merchantAccept', equalTo('true'))
    }

    @Merchant({ it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'upi\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['UPI'] }.payChannelOptions.find { it.channelCode == 'UPI' }")
                .body('', not(nullValue()))
                .body('isDisabled', isA(Object.class),
                        'hasLowSuccess', isA(Object.class),
                        'iconUrl', isA(String.class),
                        'isHybridDisabled', isA(Boolean.class),
                        'channelCode', isA(String.class),
                        'channelName', isA(String.class))
    }

    @Merchant({it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'upip\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['UPI'] }.payChannelOptions.find { it.channelCode == 'UPIPUSH' }")
                .body('', not(nullValue()))
                .body('isDisabled', isA(Object.class),
                        'hasLowSuccess', isA(Object.class),
                        'iconUrl', isA(String.class),
                        'isHybridDisabled', isA(Boolean.class),
                        'channelCode', isA(String.class),
                        'channelName', isA(String.class))
    }

    @Merchant({  it.id == Constants.MerchantType.PCF_ONUS.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'upipe\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['UPI'] }.payChannelOptions.find { it.channelCode == 'UPIPUSHEXPRESS' }")
                .body('', not(nullValue()))
                .body('isDisabled', isA(Object.class),
                        'hasLowSuccess', isA(Object.class),
                        'iconUrl', isA(String.class),
                        'isHybridDisabled', isA(Boolean.class),
                        'channelCode', isA(String.class),
                        'channelName', isA(String.class))
    }

    @Merchant({  it.id == Constants.MerchantType.PG2_COD.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'cod\') payChannelOptions'() {
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

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'cc\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'CREDIT_CARD' }.payChannelOptions", emptyIterable())
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'dc\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'DEBIT_CARD' }.payChannelOptions", emptyIterable())
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'nb\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'NET_BANKING' }.payChannelOptions")
                .body('', not(emptyIterable()))
                .body('isDisabled', everyItem(isA(Object.class)),
                        'hasLowSuccess', everyItem(isA(Object.class)),
                        'iconUrl', everyItem(isA(String.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)),
                        'channelCode', everyItem(isA(String.class)),
                        'channelName', everyItem(isA(String.class)))

    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'emi\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'EMI' }.payChannelOptions")
                .body('', not(emptyIterable()))
                .body('isDisabled', everyItem(isA(Object.class)),
                        'hasLowSuccess', everyItem(isA(Object.class)),
                        'iconUrl', everyItem(isA(String.class)),
                        'minAmount', everyItem(isA(Object.class)),
                        'maxAmount', everyItem(isA(Object.class)),
                        'emiType', everyItem(isA(String.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)),
                        'channelCode', everyItem(isA(String.class)),
                        'channelName', everyItem(isA(String.class)))

    }

    @Issue('PGP-23213')
    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(groups = [BUG])
    void 'test body merchantPayOption paymentModes find(\'ppbl\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL' }.payChannelOptions")
                .body('', hasSize(1))
                .body('isDisabled', everyItem(isA(Object.class)),
                        'hasLowSuccess', everyItem(isA(Object.class)),
                        'iconUrl', everyItem(isA(String.class)),
//                        'balanceInfo', everyItem(isA(Object.class)),  // AS PER DEV COMMENT IN PGP-23213 THIS CAN BE null
                        'isHybridDisabled', everyItem(isA(Boolean.class)))
    }

//    @Merchant({ it.payModes.contains('pdc') })
//    @AUser
//    @Test(enabled = false)
//TODO need to find correct res for pdc
    void 'test body merchantPayOption paymentModes find(\'pdc\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL' }.payChannelOptions")
                .body('', hasSize(1))
                .body('isDisabled', everyItem(isA(Object.class)),
                        'hasLowSuccess', everyItem(isA(Object.class)),
                        'iconUrl', everyItem(isA(String.class)),
                        'balanceInfo', everyItem(isA(Object.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)))
    }

    @Merchant( {it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test body addMoneyPayOption when merchant has addnpay preference enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.addMoneyPayOption')
                .body('paymentModes', allOf(not(nullValue()), isA(List.class)),
                        'savedInstruments', allOf(not(nullValue()), isA(List.class)),
                        'userProfileSarvatra', anyOf(nullValue(), isA(String.class)),
                        'activeSubscriptions', anyOf(nullValue(), isA(String.class)))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test
    void 'test unable to fetch body addMoneyPayOption when merchant has addnpay preference disabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.addMoneyPayOption')
                .body('paymentModes', nullValue(),
                        'savedInstruments', emptyIterable(),
                        'userProfileSarvatra', nullValue(),
                        'activeSubscriptions', nullValue())
    }

//    @Merchant({ it.preferences.addnpay.enabled })
//    @AUser
//    @Test(enabled = false)
//TODO need to discuss what all paymodes will come
    void 'test body addMoneyPayOption paymentModes'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.addMoneyPayOption.paymentModes', hasSize(scwMerchant.payModes.size()))
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test able to fetch body addMoneyMerchantDetails when merchant has addnpay preference disabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('addMoneyMerchantDetails')))
    }


    @Merchant({ it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void 'test able to fetch body addMoneyMerchantDetails when merchant has addnpay preference enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body', hasKey('addMoneyMerchantDetails'))
                .root('body.addMoneyMerchantDetails')
                .body('mcc', isA(String.class),
                        'merchantVpa', isA(String.class),
                        'merchantName', isA(String.class),
                        'merchantLogo', anyOf(nullValue(), isA(String.class)))
    }

    @Merchant({ it.id==Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test able to fetch body addMoneyMerchantDetails merchantName'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.addMoneyMerchantDetails.merchantName', is('TestBenificiary'))
    }

//    @Merchant({ it.preferences.addnpay.enabled })
//    @AUser
//    @Test(enabled = false)
//TODO need to implement mcc fetching
    void 'test body addMoneyMerchantDetails mcc'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.addMoneyMerchantDetails.mcc', equalTo(scwMerchant.mcc.code))
    }

//    @Merchant({ it.preferences.addnpay.enabled })
//    @AUser
//    @Test(enabled = false)
//TODO need to implement merchantVpa fetching
    void 'test body addMoneyMerchantDetails merchantVpa'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.addMoneyMerchantDetails.merchantVpa', equalTo(scwMerchant.vpa))
    }

//    @Merchant({ it.preferences.addnpay.enabled })
//    @AUser
//    @Test(enabled = false)
    //TODO need to implement merchantLogo fetching
    void 'test body addMoneyMerchantDetails merchantLogo'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.addMoneyMerchantDetails.merchantLogo', equalTo(scwMerchant.logo))
    }

    @Merchant({ it.pcfEnabled })
    @AUser
    void 'test body pcfEnabled when merchant has pcf enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.pcfEnabled', equalTo(true))
    }

    @Merchant({ !it.pcfEnabled })
    @AUser
    void 'test body pcfEnabled when merchant has pcf disabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.pcfEnabled', equalTo(false))
    }

 /*   @Test(enabled = false)
//TODO need to implement
    void 'test zeroCostEmi'() {}
  */

    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test body merchantLimitInfo'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantLimitInfo')
                .body('merchantRemainingLimits', isA(List.class),
                        'excludedPaymodes', isA(List.class),
                        'message', isA(String.class))
    }

    /**************MISCELLANEOUS*****************/

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert All NB Pay Channel Options Returned When Only Mode Is Provided')
    void assertAllNBPayChannelOptionsReturnedWhenOnlyModeIsProvided() {
        def root = root()
        root.body.enablePaymentMode = [[mode: 'NET_BANKING']]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes[0]")
                .body("paymentMode", equalTo("NET_BANKING"))
                .body("payChannelOptions.channelCode", not(hasSize(0)))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test(description = 'assert Filtered NB Pay Channel Options When Mode And Single Bank Is Provided')
    void assertFilteredNBPayChannelOptionsWhenModeAndSingleBankIsProvided() {
        def root = root()
        //def bank = m().acquirings.find { it.payMode == 'nb' && !(it.bank in ['ppbl', 'zest']) }.bank.toUpperCase()
        ArrayList<String>bank=new ArrayList<>()
        bank.add("ICICI");
        root.body.enablePaymentMode = [[mode: 'NET_BANKING', banks: ['ICICI']]]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes", hasSize(1))
                .root("body.merchantPayOption.paymentModes[0]")
                .body("paymentMode", equalTo("NET_BANKING"))
                .body("payChannelOptions.channelCode", containsInAnyOrder(*bank))
    }

    @Merchant({  it.id == Constants.MerchantType.PPBLC_ONLY.getId()})
    @AUser
    @Test(description = 'assert Filtered NB Pay Channel Options When Mode And Multiple Banks Are Provided')
    void assertFilteredNBPayChannelOptionsWhenModeAndMultipleBanksAreProvided() {
        def root = root()
//        def banks = m().acquirings.findAll {
//            it.payMode == 'nb' && !(it.bank in ['ppbl', 'zest'])
//        }[0..1].collect { it.bank.toUpperCase() }     commenting the code as we don't need to hit alipay api directly to check Mode. we can do it via configuring required data on mid
        ArrayList<String>banks=new ArrayList<>()
        banks.add("ICICI");
        root.body.enablePaymentMode = [[mode: 'NET_BANKING', banks: ['ICICI']]]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes", hasSize(1))
                .root("body.merchantPayOption.paymentModes[0]")
                .body("paymentMode", equalTo("NET_BANKING"))
                .body("payChannelOptions.channelCode", containsInAnyOrder(*banks))
    }

    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert All CC Pay Channel Options Returned When Only Mode Is Provided')
    void assertAllCCPayChannelOptionsReturnedWhenOnlyModeIsProvided() {
        def root = root()
        root.body.enablePaymentMode = [[mode: 'CREDIT_CARD']]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.merchantPayOption.paymentModes')
                .body('', hasSize(1))
                .body("paymentMode", contains("CREDIT_CARD"))
    }

    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser(edit = true)
    @Test(description = 'assert All Saved CC Pay Channel Options Returned When Only Mode Is Provided')
    void assertAllSavedCCPayChannelOptionsReturnedWhenOnlyModeIsProvided() {
        def root = root()
      //  user().savedCards.clear()
       // assert user().savedCards.add(cards.find { it.scheme == 'amex' && it.type == 'credit' })
        User user = new User(user().getMobile(),user().getPassword());
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        root.body.enablePaymentMode = [[mode: 'CREDIT_CARD']]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.savedInstruments")
                .body("", hasSize(1))
                .body("cardDetails.cardType", everyItem(equalTo("CREDIT_CARD")),
                        "channelCode", everyItem(equalTo("AMEX")))
    }

    @Merchant({  it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'assert Filtered CC Pay Channel Options Returned When Mode And Single Bank Is Provided')
    void assertFilteredCCPayChannelOptionsReturnedWhenModeAndSingleBankIsProvided() {
        def root = root()
//        root.body.enablePaymentMode = [[mode: 'CREDIT_CARD', banks: [m().acquirings.find {
//            it.enabled
//        }.bank.toUpperCase()]]]       commenting the code as we don't need to hit alipay api directly to check Mode. we can do it via configuring required data on mid
        root.body.enablePaymentMode = [[mode: 'CREDIT_CARD', banks:["PPBL"]]]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes", hasSize(1))
                .root("body.merchantPayOption.paymentModes[0]")
                .body("paymentMode", equalTo("CREDIT_CARD"))
    }

    @Merchant({ it.id == Constants.MerchantType.PGOnly_Retry.getId()})
    @AUser
    @Test(description = 'assert Filtered CC Pay Channel Options Returned When Mode And Multiple Banks Are Provided')
    void assertFilteredCCPayChannelOptionsReturnedWhenModeAndMultipleBanksAreProvided() {
        def root = root()
//        root.body.enablePaymentMode = [[mode: 'CREDIT_CARD', banks: m().acquirings.findAll {
//            it.payMode == 'cc'
//        }.collect { it.bank.toUpperCase() }]]         commenting the code as we don't need to hit alipay api directly to check Mode. we can do it via configuring required data on mid
        root.body.enablePaymentMode = [[mode: 'CREDIT_CARD', "banks": ["HDFC"]]]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes", hasSize(1))
                .root("body.merchantPayOption.paymentModes[0]")
                .body("paymentMode", equalTo('CREDIT_CARD'))
                .body("payChannelOptions", hasSize(0))
    }

    @Parameters(['mode', 'channel'])
    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @AUser
    @Test(description = 'assert Filtered CC Pay Channel Options Returned When Mode And Channel Is Provided')
    void assertFilteredCCPayChannelOptionsReturnedWhenModeAndChannelIsProvided(@Optional('CREDIT_CARD') String mode, @Optional('VISA') String channel) {
        def root = root()
        root.body.enablePaymentMode = [[mode: mode, channels: [channel]]]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes", hasSize(1))
                .root("body.merchantPayOption.paymentModes[0]")
                .body("paymentMode", equalTo(mode))
                .body("payChannelOptions", hasSize(0))
    }

    @DataProvider
    static Object[][] singlePaymodes() {
        [['CREDIT_CARD'], ['DEBIT_CARD'], ['NET_BANKING'], ['EMI'], ['PPBL'], ['UPI']]
    }

    @Merchant({  it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test(description = 'check Response For Single Pay Mode Provided', dataProvider = "singlePaymodes")
    void checkResponseForSinglePayModeProvided(String payMode) {
        def root = root()
        root.body.enablePaymentMode = [[mode: payMode]]
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes", hasSize(1))
                .root("body.merchantPayOption.paymentModes[0]")
                .body("paymentMode", equalTo(payMode))
    }

    @DataProvider
    static Object[][] multiplePaymodes() {
        [[["CREDIT_CARD", "NET_BANKING"]], [["DEBIT_CARD", "NET_BANKING", "EMI"]]]
    }


    
    @Merchant({ it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test(description = 'check Response For Multiple Pay Modes Provided', dataProvider = "multiplePaymodes")
    void checkResponseForMultiplePayModesProvided(payModes) {
        def root = root()
        root.body.enablePaymentMode = payModes.collect { [mode: it] }
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes", hasSize(payModes.size()))
                .root("body.merchantPayOption.paymentModes")
                .body("paymentMode", containsInAnyOrder(*payModes))
    }

    @Merchant(edit = true, value ={it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test(description = 'test paymentOffers returned when fetchAllPaymentOffers param = true')
    void testWhenFetchAllPaymentOffersPassedTrue() {
        def root = root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        root.body.fetchAllPaymentOffers = "true"
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', hasSize(greaterThan(0)))
    }

    @Merchant(edit = true, value ={it.id == Constants.MerchantType.Redirectional_Native.getId()})
    @AUser
    @Test(description = 'test paymentOffers not returned when fetchAllPaymentOffers param = false')
    void testWhenFetchAllPaymentOffersPassedFalse() {
        def root = root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        root.body.fetchAllPaymentOffers = 'false'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .body('body.paymentOffers', nullValue())
    }


//    @Merchant(edit = true)
//    @AUser(edit = true)
//    @Test(enabled = false)
//TODO TC needs to be automated
    void testWhenApplyPaymentOfferPassedTrue() {
        def root = root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.successRate == 'high' })
        root.body.applyPaymentOffer = 'true'
        root.body.orderAmount = '1'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .spec(paymentOfferDetails)
    }

//    @Merchant(edit = true)
//    @AUser(edit = true)
//    @Test(enabled = false)
//TODO TC needs to be automated
    void testWhenApplyPaymentOfferPassedFalse() {
        def root = root()
        m().promos.clear()
        assert m().promos.add(new Promo(false, false))
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.successRate == 'high' })
        root.body.applyPaymentOffer = 'false'
        root.body.orderAmount = '1'
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .spec(paymentOfferDetails)
    }

//    @Merchant(edit = true, value = { it.payModes.containsAll(['cc', 'ppi']) })
//    @AUser(edit = true)
//    @Test(enabled = false)
    //TODO TC needs to be automated
    void testAttrPaymentPromocode() {
        def root = root()
        m().promos.clear()
        assert m().promos.addAll(new Promo(false, false), new Promo(false, false))
        user().savedCards.clear()
        assert user().savedCards.add(cards.find {
            it.successRate == 'high' && it.type == 'credit' && it.scheme == 'visa'
        })
        root.body.paymentPromocode = m().promos[0].name
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .spec(paymentOfferDetails)
                .rootPath('body.merchantPayOption.savedInstruments.paymentOfferDetails[0]')
                .body('promocodeApplied', equalTo(m().promos[0].name))
    }

    @Owner(JAI)
    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test body addDescriptionMandatory when preference - OFFLINE_SNP_DESC_FLAG is enabled'(){
        def root = root()
        req().body(root).post().then()
            .spec(results.success as ResponseSpecification)
            .body('body.addDescriptionMandatory', equalTo(true))
    }

    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void 'test body descriptionTextFormat when preference - OFFLINE_SNP_DESC_TXT is enabled'(){
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.descriptionTextFormat', equalTo("Automation UBER Text - Jai1"))
    }

    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Merchant({it.id == Constants.MerchantType.Hybrid.getId()})
    @AUser
    @Test
    void 'test body addDescriptionMandatory when preference - OFFLINE_SNP_DESC_FLAG is disabled'(){
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.addDescriptionMandatory', equalTo(false))
    }

    @Epic(Constants.Sprint.SPRINT33_2)
    @Feature("PGP-21691")
    @Merchant({it.id == Constants.MerchantType.Hybrid.getId()})
    @AUser
    @Test
    void 'test body descriptionTextFormat when preference - OFFLINE_SNP_DESC_TXT is disabled'(){
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body', not(hasKey('descriptionTextFormat')))
    }


    @Owner(GAGANDEEP)
    @Feature("PGP-21691")
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.Hybrid.getId()})
    @AUser
    @Test
    void 'merchant brand name should be in merchantDisplayName field'(){

        String brandName = 'AutomationMerchant0011'


        String redisKey = 'MERC_NAME_LOGO_V2_'

        STATIC_REDIS_CLUSTER().del(redisKey+m().id)

        DatabaseUtil.getInstance()
                .executeUpdateQuery(LocalConfig.PG_DB_CONNECTION_URL,'UPDATE ENTITY_INFO set MERCHANT_NAME=\''+brandName+'\' WHERE MID=\''+m().id+'\'')

        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantDetails.merchantDisplayName',equalTo(brandName))
    }



    @Owner(GAGANDEEP)
    @Feature("PGP-21691")
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSPGONLY.getId() })
    @AUser
    @Test
    void 'merchant brand name with Special characters should be in merchantDisplayName field'(){

        String brandName = '$#^*ndhsidjo dihso2325'


        String redisKey = 'MERC_NAME_LOGO_V2_'

        STATIC_REDIS_CLUSTER().del(redisKey+m().id)


        DatabaseUtil.getInstance()
                .executeUpdateQuery(LocalConfig.PG_DB_CONNECTION_URL,'UPDATE ENTITY_INFO set MERCHANT_NAME=\''+brandName+'\' WHERE MID=\''+m().id+'\'')

        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantDetails.merchantDisplayName',equalTo(brandName))
    }




    @Owner(GAGANDEEP)
    @Feature("PGP-21691")
    @Merchant(edit = true, value = { it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId()})
    @AUser
    @Test
    void 'merchant brand name blank merchantDisplayName should not come'(){

        String brandName = ''


        String redisKey = 'MERC_NAME_LOGO_V2_'

          STATIC_REDIS_CLUSTER().del(redisKey+m().id)

        DatabaseUtil.getInstance()
                .executeUpdateQuery(LocalConfig.PG_DB_CONNECTION_URL,'UPDATE ENTITY_INFO set MERCHANT_NAME=\''+brandName+'\' WHERE MID=\''+m().id+'\'')

        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.merchantDetail' +
                        's.merchantDisplayName', not(hasProperty("merchantDisplayName")))
    }

    @Merchant({ !it.localeEnabled && it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.merchantPayOption.paymentModes', (everyItem(not(hasKey('displayNameRegional')))),
                        'body.merchantPayOption.paymentModes.isDisabled.findAll { it.msg != null }', (everyItem(not(hasKey('msgRegional')))),
                        'body.merchantPayOption.paymentModes.find { it.paymentMode == \'NET_BANKING\' }.payChannelOptions.findAll { it.channelName }', (everyItem(not(hasKey('channelNameRegional')))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.hasLowSuccess', (everyItem(everyItem(not(hasKey('msgRegional'))))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.isDisabled', (everyItem(everyItem(not(hasKey('msgRegional'))))))
    }

    @Merchant({it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.merchantPayOption.paymentModes', (everyItem(not(hasKey('displayNameRegional')))),
                        'body.merchantPayOption.paymentModes.isDisabled.findAll { it.msg != null }', (everyItem(not(hasKey('msgRegional')))),
                        'body.merchantPayOption.paymentModes.find { it.paymentMode == \'NET_BANKING\' }.payChannelOptions.findAll { it.channelName }', (everyItem(not(hasKey('channelNameRegional')))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.hasLowSuccess', (everyItem(everyItem(not(hasKey('msgRegional'))))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.isDisabled', (everyItem(everyItem(not(hasKey('msgRegional'))))))
    }

    @Merchant({ it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', (hasKey('resultMsgRegional')),
                        'body.merchantPayOption.paymentModes', everyItem(hasKey('displayNameRegional')),
                        'body.merchantPayOption.paymentModes.isDisabled.findAll { it.msg != null }', everyItem(hasKey('msgRegional')),
                        'body.merchantPayOption.paymentModes.find { it.paymentMode == \'NET_BANKING\' }.payChannelOptions.findAll { it.channelName }', (everyItem(hasKey('channelNameRegional'))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.hasLowSuccess', everyItem(everyItem(hasKey('msgRegional'))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.isDisabled', everyItem(everyItem(not(hasKey('msgRegional')))))
    }
}


