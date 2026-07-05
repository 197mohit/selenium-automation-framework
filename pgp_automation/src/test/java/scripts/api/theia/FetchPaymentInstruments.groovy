package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.appconstants.Constants
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.OfflineTxn.FETCH_PAYMENT_INSTRUMENT
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchPaymentInstruments extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PAYMENT_INSTRUMENT)
                .addQueryParam('mid', m()?.id ?: new Random().nextLong().abs() as String)
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        requestId: new Random().nextLong().abs() as String,
                        clientId : new Random().nextLong().abs() as String,
                        tokenType: 'SSO',
                        token    : user().tokens['sso'].id,
                        mid      : m().id,
                        version  : 'v2',
                ],
                body: [
                        orderAmount                : [
                                currency: 'INR',
                                value   : '1.0'
                        ],
                        deviceId                   : new Random().nextLong().abs() as String,
                        channelId                  : 'WEB',
                        industryTypeId             : 'retail',
                        instrumentTypes            : ['ALL'],
                        savedInstrumentsTypes      : ['ALL'],
                        extendInfo                 : null,
                        signature                  : null,
                        postpaidOnboardingSupported: null,
                ]
        ]
    }

    @Merchant({it.id == Constants.MerchantType.FLAT_PCF.getId()})
    @AUser
    @Test
    void testSuccess() {
        def root = root()
        req().body(root).post().then()
                .body('head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body('mid', equalTo(root.head.mid),
                'clientId', equalTo(root.head.clientId),
                'version', equalTo(root.head.version),
                'requestId', equalTo(root.head.requestId),
                'responseTimestamp', not(isEmptyOrNullString()))
                .root('body')
                .body('extendInfo', isA(Object.class),
                'resultInfo', isA(Object.class),
                'orderId', not(isEmptyOrNullString()),
                'postConvenienceFee', isA(Object.class),
                'enabledFlows', isA(List.class),
                'payMethodViews', isA(Object.class),
                'merchantDetails', anyOf(isA(Object.class), nullValue()))
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                'resultCodeId', equalTo('0000'),
                'resultCode', equalTo('SUCCESS'),
                'resultMsg', equalTo('Success'))
                .root('body.postConvenienceFee')
                .body('enabled', isIn('true', 'false'))
                .root('body.payMethodViews')
                .body('merchantSavedInstruments', isA(Object.class),
                'addMoneySavedInstruments', isA(Object.class),
                'merchantPayMethods', isA(List.class),
                'addMoneyPayMethods', nullValue())
                .root('body.payMethodViews.merchantSavedInstruments')
                .body('savedCards', isA(List.class),
                'savedVPAs', isA(List.class),
                'savedVPAs', isA(List.class),
                'sarvatraVpa', isA(List.class),
                'sarvatraUserProfile', isA(Object.class),
                'sarvatraVpaPriority', isIn(0))
                .root('body.payMethodViews.addMoneySavedInstruments')
                .body('savedCards', isA(List.class),
                'savedVPAs', isA(List.class),
                'sarvatraVpa', anyOf(not(isEmptyOrNullString()), nullValue()),
                'sarvatraUserProfile', anyOf(not(isEmptyOrNullString()), nullValue()),
                'sarvatraVpaPriority', isIn(0))
                .root('body.payMethodViews.merchantPayMethods')
                .body('payMethod', everyItem(isIn('CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'UPI', 'NET_BANKING_PPBL', 'BALANCE')),
                'displayName', everyItem(isIn('Credit Card', 'Debit Card', 'Net Banking', 'BHIM UPI', 'Paytm Bank Account', 'Wallet')),
                'isDisabled', everyItem(allOf(hasKey('status'), hasKey('msg'))),
                'payChannelOptions', everyItem(isA(List.class)),
                'priority', everyItem(isA(Integer.class)),
                'onboarding', everyItem(isA(Boolean.class)))
                .root('body.merchantDetails')
                .body('merchantVpa', anyOf(not(isEmptyOrNullString()), nullValue()),
                'merchantLogo', anyOf(not(isEmptyOrNullString()), nullValue()),
                'isAppInvokeAllowed', anyOf(isA(Boolean.class), nullValue()),
                'mcc', anyOf(not(isEmptyOrNullString()), nullValue()),
                'merchantName', not(isEmptyOrNullString()))
    }
}
