package scripts.api.theia.fetchPayOptions

import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.User
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test

import static com.paytm.base.test.Group.Status.BUG
import static org.hamcrest.Matchers.*

@Owner('Deepak')
abstract class FetchPayOptionsV2Test extends FetchPayOptionsV1Test {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    @Override
    ResponseSpecification success_schema() {
        new ResponseSpecBuilder()
                .expectStatusCode(200)
             //   .addResponseSpecification(headersResSpec)
                .expectBody('head', isA(Object.class))
                .expectBody('body', isA(Object.class))
                .rootPath('head')
                .expectBody('requestId', nullValue())
                .expectBody('responseTimestamp', not(isEmptyOrNullString()))
                .expectBody('version', equalTo('v2'))
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
                .expectBody('', not(hasKey('isDisabled')))
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
//                .expectBody('', everyItem(everyItem(not(hasKey('isDisabled')))))
//                .expectBody('', everyItem(everyItem(not(hasKey('hasLowSuccess')))))
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
    ResponseSpecification body_merchantPayOption_paymentMode_schema(String paymentMode) {
        new ResponseSpecBuilder()
                .expectStatusCode(200)
                .addResponseSpecification(results.success as ResponseSpecification)
                .rootPath("body.merchantPayOption.paymentModes.find { it.paymentMode == '$paymentMode' }")
                .expectBody('', not(nullValue()))
                .expectBody('displayName', isA(String.class))
                .expectBody('', not(hasKey('isDisabled')))
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
    }

    @Merchant({it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes isDisabled'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root('body.merchantPayOption.paymentModes')
                .body("findAll { !(it.paymentMode in ['BALANCE', 'PPBL', 'PAYTM_DIGITAL_CREDIT']) }", everyItem(not(hasKey('isDisabled'))))
                .body("findAll { it.paymentMode in ['BALANCE', 'PPBL', 'PAYTM_DIGITAL_CREDIT'] }.isDisabled", everyItem(anyOf(nullValue(), isA(Object.class))))
    }

    @Merchant({it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body iconbaseurl contain(\'host native bank\')'() {
        def root = root()
        def url = req().body(root).post().then()
                .spec(results.success as ResponseSpecification).extract().path('body.iconBaseUrl')
        assert url ==~ /https:\/\/(.*).paytm.in\/native\/bank\//
    }

    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes payChannelOptions iconurl contain(null)'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.findAll {it.paymentMode in ['COD', 'BALANCE']}.payChannelOptions.iconUrl", everyItem(everyItem(nullValue())))
    }

    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes nb payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'NET_BANKING'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes dc payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'DEBIT_CARD'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes cc payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'CREDIT_CARD'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes ppi payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'BALANCE'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }


    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes ppbl payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({  it.id == Constants.MerchantType.EMI.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes emi payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'EMI'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes upi payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }


    @Merchant({  it.id == Constants.MerchantType.PG2_COD.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes cod payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'COD'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes upip payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'UPI'}.payChannelOptions.findAll { it.channelCode == 'UPIPUSH' }.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser(paytmcc = 'true')
    @Test
    void 'test body merchantPayOption paymentModes pdc payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @Test
    void 'test body merchantPayOption upiProfile vpa'() {
        //user hard coding is temporary and will be removed soon once new user implementation is done
        def user = new User("7259493013", "paytm@123")
        def root = root()
        root.head.token = user.tokens['sso'].id
        def vpas = req().body(root).post().then()
                .spec(results.success as ResponseSpecification).extract().path("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails.name")
        vpas.each { vpa ->
            assert vpa ==~ /[a-z0-9]+@[a-z]+/
        //    assert vpa ==~ "anjali.chhabra@paytm"  //Hardcoding it as we dont have any other saved vpa user
        }
    }

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @Test
    void 'test body merchantPayOption upiProfile defaultCreditAccRefId'() {
        //user hard coding is temporary and will be removed soon once new user implementation is done
        def user = new User("7259493013", "paytm@123")
        def root = root()
        root.head.token = user.tokens['sso'].id
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails.defaultCreditAccRefId", hasSize(greaterThan(0)))

    }

    @Override
    @Merchant({  it.id == Constants.MerchantType.PG2_COD.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'cod\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'COD' }.payChannelOptions")
                .body('', hasSize(1))
                .body(
                        '', everyItem(not(hasKey('isDisabled'))),
                        'minAmount', everyItem(isA(Object.class)),
                        'codMessage', everyItem(isA(String.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)),
                        'iconUrl', everyItem(nullValue()),
                        'codHybridErrMsg', everyItem(isA(String.class)))
    }

    @Override
    @Merchant({  it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'emi\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'EMI' }.payChannelOptions")
                .body('', not(emptyIterable()))
                .body(
                        '', everyItem(not(hasKey('isDisabled'))),
                        'iconUrl', everyItem(isA(String.class)),
                        'minAmount', everyItem(isA(Object.class)),
                        'maxAmount', everyItem(isA(Object.class)),
                        'emiType', everyItem(isA(String.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)),
                        'channelCode', everyItem(isA(String.class)),
                        'channelName', everyItem(isA(String.class)))
    }

    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    @Override
    void 'test body merchantPayOption paymentModes find(\'nb\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'NET_BANKING' }.payChannelOptions")
                .body('', not(emptyIterable()))
                .body(
                        '', everyItem(not(hasKey('isDisabled'))),
                        'iconUrl', everyItem(isA(String.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)),
                        'channelCode', everyItem(isA(String.class)),
                        'channelName', everyItem(isA(String.class)))
    }

    @Override
    @Issue('PGP-23213')
    @Merchant({   it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test(groups = [BUG])
    void 'test body merchantPayOption paymentModes find(\'ppbl\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL' }.payChannelOptions")
                .body('', hasSize(1))
                .body(
                        '', everyItem(not(hasKey('isDisabled'))),
                        'iconUrl', everyItem(isA(String.class)),
                        'balanceInfo', everyItem(isA(Object.class)),
                        'isHybridDisabled', everyItem(isA(Boolean.class)))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'upi\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['UPI'] }.payChannelOptions.find { it.channelCode == 'UPI' }")
                .body('', not(nullValue()))
                .body(
                        '', not(hasKey('isDisabled')),
                        'iconUrl', isA(String.class),
                        'isHybridDisabled', isA(Boolean.class),
                        'channelCode', isA(String.class),
                        'channelName', isA(String.class))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'upip\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['UPI'] }.payChannelOptions.find { it.channelCode == 'UPIPUSH' }")
                .body('', not(nullValue()))
                .body(
                        '', not(hasKey('isDisabled')),
                        'iconUrl', isA(String.class),
                        'isHybridDisabled', isA(Boolean.class),
                        'channelCode', isA(String.class),
                        'channelName', isA(String.class))
    }

    @Override
    @Merchant({  it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes find(\'upipe\') payChannelOptions'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .root("body.merchantPayOption.paymentModes.find { it.paymentMode in ['UPI'] }.payChannelOptions.find { it.channelCode == 'UPIPUSHEXPRESS' }")
                .body('', not(nullValue()))
                .body(
                        '', not(hasKey('isDisabled')),
                        'iconUrl', isA(String.class),
                        'isHybridDisabled', isA(Boolean.class),
                        'channelCode', isA(String.class),
                        'channelName', isA(String.class))
    }

    @Merchant({it.id == Constants.MerchantType.AddMoney.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes isDisabled for non-paytm instruments'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.findAll { it.paymentMode in ['UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'COD', 'EMI', 'NET_BANKING'] }", not(hasKey('isDisabled')))
    }

    @Merchant({ it.payModes.containsAll(['ppi', 'ppbl', 'pdc']) })
    @AUser(ppbl = 'true', paytmcc = 'true')
    void 'test body merchantPayOption paymentModes isDisabled for paytm instruments'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.findAll { it.paymentMode in ['BALANCE', 'PPBL', 'PAYTM_DIGITAL_CREDIT'] }", not(hasKey('isDisabled')))
    }

//    @Override
//    @Test(enabled = false)
//isDisabled field removed from response
    void 'test body merchantPayOption paymentModes isDisabled for pdc when merchant and user both have pdc'() {}

//    @Override
//    @Test(enabled = false)
//isDisabled field removed from response
    void 'test body merchantPayOption paymentModes isDisabled for pdc when merchant has pdc but not user'() {}

//    @Override
//    @Test(enabled = false)
//isDisabled field removed from response
    void 'test body merchantPayOption paymentModes isDisabled for ppbl when merchant and user both have ppbl'() {}

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.merchantPayOption.paymentModes', (everyItem(not(hasKey('displayNameRegional')))),
                        'body.merchantPayOption.paymentModes.find { it.paymentMode == \'NET_BANKING\' }.payChannelOptions.findAll { it.channelName }', (everyItem(not(hasKey('channelNameRegional')))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.hasLowSuccess', (everyItem(everyItem(not(hasKey('msgRegional'))))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.isDisabled', (everyItem(everyItem(not(hasKey('msgRegional'))))))
    }

    @Merchant({  it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.merchantPayOption.paymentModes', (everyItem(not(hasKey('displayNameRegional')))),
                        'body.merchantPayOption.paymentModes.find { it.paymentMode == \'NET_BANKING\' }.payChannelOptions.findAll { it.channelName }', (everyItem(not(hasKey('channelNameRegional')))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.hasLowSuccess', (everyItem(everyItem(not(hasKey('msgRegional'))))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.isDisabled', (everyItem(everyItem(not(hasKey('msgRegional'))))))
    }

    @Merchant({  it.id == Constants.MerchantType.EMISubvention.getId()})
    @AUser
    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', (hasKey('resultMsgRegional')),
                        'body.merchantPayOption.paymentModes', everyItem(hasKey('displayNameRegional')),
                        'body.merchantPayOption.paymentModes.find { it.paymentMode == \'NET_BANKING\' }.payChannelOptions.findAll { it.channelName }', (everyItem(hasKey('channelNameRegional'))),
                        'body.merchantPayOption.paymentModes.find { it.paymentMode == \'NET_BANKING\' }.payChannelOptions.findAll { it.hasLowSuccess }.hasLowSuccess', (everyItem(hasKey('msgRegional'))),
                        'body.merchantPayOption.paymentModes.payChannelOptions.isDisabled', everyItem(everyItem(not(hasKey('msgRegional')))))
    }
}
