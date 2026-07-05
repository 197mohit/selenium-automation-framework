package scripts.api.theia.fetchPayOptions

import com.paytm.apphelpers.PGPHelpers
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.appconstants.Constants
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.GUEST_FETCH_PAYMENT_OPTION_V1
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GuestTokenFetchPayOptionsV1Test extends FetchPayOptionsV1Test {

    @Override
    RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(GUEST_FETCH_PAYMENT_OPTION_V1)
                .addQueryParam('mid', m().id)
                .addQueryParam('orderId', UUID.randomUUID().toString())
    }

    @Override
    Map<String, Object> root() {
        [
                "head": [
                        "requestId"       : UUID.randomUUID().toString(),
                        "requestTimestamp": System.currentTimeMillis() as String,
                        "channelId"       : 'WEB',
                        "version"         : 'v1',
                        "tokenType"       : 'GUEST',
                        "workFlow"        : "checkout",
                ],
                "body": [
                        "mid": m().id,
                ]
        ]
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
                .expectBody('', not(hasKey('userDetails')))
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
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void "test unable to fetch pay options details when mid is not provided in query params"() {
        def root = root()
        given(reqBldr().removeQueryParam('mid').build()).body(root).post().then()
                .spec(results.mIdMandatoryInQueryParams as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when head token is not provided'() {
        def root = root()
        root.head.remove('token')
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when head token = null'() {
        def root = root()
        root.head.token = null
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when head token = \'\''() {
        def root = root()
        root.head.token = ''
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when head token equals random value'() {
        def root = root()
        root.head.token = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when body mid is not provided'() {
        def root = root()
        root.body.remove('mid')
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when body mid = null'() {
        def root = root()
        root.body.mid = null
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when body mid = \'\''() {
        def root = root()
        root.body.mid = ''
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void 'test unable to fetch pay options details when body mid equals random value'() {
        def root = root()
        root.body.mid = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId()})
    @Test
    void "test unable to fetch pay options details when mid in query params is different from mid in body"() {
        def root = root()
        root.body.mid = new Random().nextLong().abs() as String
        req().body(root).post().then()
                .spec(results.mIdInQueryParamNotMatchingWithmIdInRequest as ResponseSpecification)
    }

    @Override
    @Merchant({it.id == Constants.MerchantType.Redirectional_Native.getId()})
    @Test
    void 'test body addMoneyPayOption when merchant has addnpay preference enabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.addMoneyPayOption')
                .body(
                        'paymentModes', nullValue(),
                        'savedInstruments', nullValue(),
                        'userProfileSarvatra', nullValue(),
                        'activeSubscriptions', nullValue(),
                        'savedMandateBanks', nullValue())
    }

    @Override
    @Merchant({ !it.preferences.addnpay.enabled && it.id == Constants.MerchantType.PRIORITY_HYBRID_MERCHANT.getId() })
    @Test
    void 'test unable to fetch body addMoneyPayOption when merchant has addnpay preference disabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(success_schema())
                .spec(results.success as ResponseSpecification)
                .root('body.addMoneyPayOption')
                .body(
                        'paymentModes', nullValue(),
                        'savedInstruments', nullValue(),
                        'userProfileSarvatra', nullValue(),
                        'activeSubscriptions', nullValue(),
                        'savedMandateBanks', nullValue())
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
                        'paymentModes.priority', hasItems('5', '6', '4', '3', '1','7', '2','8'))
    }

//    @Override
//    @Test(enabled = false)
    void 'test able to fetch body userDetails when token has user authentication'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body loginInfo userLoggedIn == true when token has user authentication'() {
        throw new UnsupportedOperationException()
    }

 //   @Override
 //   @Test(enabled = false)
    void 'test body paymentFlow when m hybrid == true && token contains user authentication'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body paymentFlow when m addnpay == true && token contains user authentication'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test !body merchantPayOption paymentModes contains(\'pdc\') when m payModes contains(\'ppbl\') && u pdc == false'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void assertAllSavedCCPayChannelOptionsReturnedWhenOnlyModeIsProvided() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test able to fetch body addMoneyMerchantDetails when merchant has addnpay preference enabled'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test able to fetch body addMoneyMerchantDetails merchantName'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body merchantPayOption paymentModes isDisabled for paytm instruments'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body merchantPayOption paymentModes isDisabled for pdc when merchant and user both have pdc'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body merchantPayOption paymentModes isDisabled for ppbl when merchant and user both have ppbl'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body merchantPayOption savedInstruments equals iterable with size greater than 0 when user has saved cards'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body userDetails kyc = false when user\'s kyc is not completed'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body userDetails mobile equals user\'s mobile no'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test body userDetails paytmCCEnabled = true when pdc is not configured on merchant but is configured on user'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        throw new UnsupportedOperationException()
    }

//    @Override
//    @Test(enabled = false)
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        throw new UnsupportedOperationException()
    }
}
