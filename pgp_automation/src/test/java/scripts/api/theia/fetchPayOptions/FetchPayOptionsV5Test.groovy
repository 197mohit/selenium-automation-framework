package scripts.api.theia.fetchPayOptions

import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.User
import com.paytm.appconstants.Constants
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Feature
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.junit.Assert
import org.luaj.vm2.ast.Str
import org.testng.annotations.Test

import static com.paytm.base.test.Group.Status.BUG
import static org.hamcrest.Matchers.*

@Owner('Sourav')
@Feature('PGP-37815')
abstract class FetchPayOptionsV5Test extends FetchPayOptionsV2Test {

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
                .expectBody('responseTimestamp', not(isEmptyOrNullString()))
                .expectBody('requestId', nullValue())
                .expectBody('version', equalTo('v5'))
                .rootPath('body')
                .expectBody('iconBaseUrl', isA(String.class))
                .expectBody('zeroCostEmi', isA(Boolean.class))
                .expectBody('otpAuthorised', isA(Boolean.class))
                .expectBody('nativeJsonRequestSupported', isA(Boolean.class))
                .expectBody('merchantDetails', isA(Object.class))
                .expectBody('onTheFlyKYCRequired', isA(Boolean.class))
                .expectBody('userDetails', isA(Object.class))
                .expectBody('resultInfo', isA(Object.class))
                .expectBody('groupedAddMoneyPayOption', isA(Object.class))
                .expectBody('oneClickMaxAmount', not(isEmptyOrNullString()))
                .expectBody('addMoneyPayOption', isA(Object.class))
                .expectBody('isPostpaidEnabledOnMerchantAndDisabledOnUser', isA(Boolean.class))
                .expectBody('addDescriptionMandatory', isA(Boolean.class))
                .expectBody('merchantPayOption', isA(Object.class))
                .expectBody('walletOnly', isA(Boolean.class))
                .expectBody('pcfEnabled', isA(Boolean.class))
                .expectBody('loginInfo', isA(Object.class))
                .expectBody('merchantLimitInfo', isA(Object.class))
                .expectBody('activeMerchant', isA(Boolean.class))
                .expectBody('locationPermission', isA(Boolean.class))
                .expectBody('allowedRetryCountsForMerchant', isA(Integer.class))
                .expectBody('paymentFlow', isA(String.class))
                .expectBody('groupedMerchantPayOption', isA(Object.class))

                .rootPath('body.merchantDetails')
                .expectBody('merchantDisplayName', not(isEmptyOrNullString()))
                .expectBody('autoAppInvokeAllowed', isA(Boolean.class))
                .expectBody('verifiedMerchant', isA(Boolean.class))
                .expectBody('merchantVpa', anyOf(equalTo(null), not(isEmptyOrNullString())))
           //     .expectBody('merchantBankName', isA(isEmptyOrNullString()))
                .expectBody('isAppInvokeAllowed', isA(Boolean.class))
                .expectBody('mcc', anyOf(equalTo(null), not(isEmptyOrNullString())))
                .expectBody('merchantName', not(isEmptyOrNullString()))

                .rootPath('body.userDetails')
                .expectBody('kyc', isA(Boolean.class))
                .expectBody('mobile', not(isEmptyOrNullString()))
                .expectBody('paytmCCEnabled', isA(Boolean.class))
                .expectBody('username', not(isEmptyOrNullString()))

                .rootPath('body.resultInfo')
                .expectBody('resultStatus', not(isEmptyOrNullString()))
                .expectBody('resultCode', not(isEmptyOrNullString()))
                .expectBody('resultMsg', not(isEmptyOrNullString()))

                .rootPath('body.addMoneyPayOption')
                .expectBody('', hasKey('savedMandateBanks'))
              //  .expectBody('savedInstruments', isA(Object.class))
              //  .expectBody('savedMandateBanks', isA(Object.class))
              //  .expectBody('upiProfile', isA(Object.class))

                .rootPath('body.loginInfo')
                .expectBody('userLoggedIn', isA(Boolean.class))
                .expectBody('pgAutoLoginEnabled', isA(Boolean.class))
                .expectBody('mobileNumberNonEditable', isA(Boolean.class))
                .expectBody('disableLoginStrip', isA(Boolean.class))

                .rootPath('body.merchantLimitInfo')
                .expectBody('merchantRemainingLimits', isA(Object.class))
                .expectBody('excludedPaymodes', isA(List.class))
                .expectBody('message', isA(String.class))

        //        .rootPath('body.merchantLimitInfo.merchantRemainingLimits')
        //        .expectBody('amount', isA(Double.class))
        //        .expectBody('limitType', isA(String.class))

                .rootPath('body.merchantPayOption')
                .expectBody('', hasKey('savedMandateBanks'))
                .expectBody('paymentModes', isA(List.class))
        //        .expectBody('upiProfile', isA(Object.class))
                .expectBody('savedInstruments', isA(Object.class))

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
                .expectBody('onboarding', everyItem(isA(Boolean.class)))

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
                .expectBody('tokenStatus', everyItem(isA(Boolean.class)))
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

    @Merchant({it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test body iconbaseurl contain(\'host native bank\')'() {
        def root = root()
        def url = req().body(root).post().then()
                .spec(results.success as ResponseSpecification).extract().path('body.iconBaseUrl')
        assert url ==~ /https:\/\/(.*).paytm.in\/native\/bank\//
    }

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes payChannelOptions iconurl contain(null)'() {
        def root = root()
        Response res=req().body(root).post().then()
                .spec(results.success as ResponseSpecification).extract().response();
                //.body("body.merchantPayOption.paymentModes.findAll {it.paymentMode in ['COD', 'BALANCE']}.payChannelOptions.iconUrl", "[[WALLET.png]]")
        JsonPath response=res.jsonPath();
        Assert.assertEquals(response.getString("body.merchantPayOption.paymentModes.payChannelOptions.iconUrl"))
    }

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes nb payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'NET_BANKING'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.ZEST_MONEY.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes dc payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'DEBIT_CARD'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes cc payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'CREDIT_CARD'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes ppi payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'BALANCE'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }


    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes ppbl payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PPBL'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.ZEST_MONEY.getId() })
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


    @Merchant({ it.id == Constants.MerchantType.PG2_COD.getId() })
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

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @AUser(paytmcc = 'true')
    @Test
    void 'test body merchantPayOption paymentModes pdc payChannelOptions hasLowSuccess(\'true\')'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.find { it.paymentMode == 'PAYTM_DIGITAL_CREDIT'}.payChannelOptions.hasLowSuccess.findAll().status", anyOf(hasSize(0), everyItem(equalTo("true"))))
    }

    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId()})
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

    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void 'test body merchantPayOption upiProfile defaultCreditAccRefId'() {
        com.paytm.base.test.User userDetails = userManager.getForRead(Label.SAVEDVPA);
        def user = new User(userDetails.mobNo(),userDetails.password())
    //  def user = new User("8684922965", "paytm@197")
        def root = root()
        root.head.token = user.tokens['sso'].id
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails.defaultCreditAccRefId", hasSize(greaterThan(0)))

    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PG2_COD.getId() })
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
    @Merchant({ it.id == Constants.MerchantType.ZEST_MONEY.getId() })
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

    @Merchant({ it.id == Constants.MerchantType.ZEST_MONEY.getId() })
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
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
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
    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
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

    @Merchant({ ({it.id == Constants.MerchantType.AddMoney.getId()})})
    @AUser
    @Test
    void 'test body merchantPayOption paymentModes isDisabled for non-paytm instruments'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body("body.merchantPayOption.paymentModes.findAll { it.paymentMode in ['UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'COD', 'EMI', 'NET_BANKING'] }", not(hasKey('isDisabled')))
    }

    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
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

    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
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

    @Merchant({ it.id == Constants.MerchantType.EMISubvention.getId()})
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

    @Merchant({ it.id == Constants.MerchantType.EMISubvention.getId() })
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
