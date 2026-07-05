package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.User
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_VPA_DETAIL
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchVpaDetails extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_VPA_DETAIL)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: m()?.orders?.with {
            !it.empty ? it : null
        }?.last()?.id ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        tokenType: 'SSO',
                        token    : user().tokens['sso'].id,
                ],
                body: [
                        mid                      : m().id,
                        userId                   : null,
                        isMultiAccForVpaSupported: false,
                ]
        ]
    }

    @Merchant
    @AUser
    @Test
//TODO need to replace hard coding once would find a way to know if user has saved VPA's
    void testSuccess() {
        def root = root()
        com.paytm.base.test.User userDetails = userManager.getForRead(Label.SAVEDVPA);
        def user = new User(userDetails.mobNo(),userDetails.password())
    //  def user = new User('8684922965', 'paytm@197')
        root.head.token = user.tokens['sso'].id
        req().body(root).post().then()
                .body(
                'head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body(
                'responseTimestamp', not(isEmptyOrNullString()),
                'version', equalTo('v1'))
                .root('body')
                .body(
                'resultInfo', isA(Object.class),
                'sarvatraVpa', everyItem(not(isEmptyOrNullString())),
                'sarvatraUserProfile', isA(Object.class))
                .root('body.resultInfo')
                .body(
                'resultStatus', equalTo('S'),
                'resultCode', equalTo('0000'),
                'resultMsg', equalTo('Success'))
                .root('body.sarvatraUserProfile')
                .body(
                'status', equalTo('success'),
                'response', isA(Object.class))
                .root('body.sarvatraUserProfile.response')
                .body(
                'vpaDetails', not(emptyIterable()),
                'bankAccounts', not(emptyIterable()))
                .root('body.sarvatraUserProfile.response.vpaDetails')
                .body(
                'name', everyItem(not(isEmptyOrNullString())),
                'defaultCredit', everyItem(isA(Object.class)),
                'defaultDebit', everyItem(isA(Object.class)),
                'primary', everyItem(isA(Boolean.class)))
                .root('body.sarvatraUserProfile.response.vpaDetails.defaultCredit')
                .body(
                'bank', everyItem(not(isEmptyOrNullString())),
                'ifsc', everyItem(not(isEmptyOrNullString())),
                'account', everyItem(not(isEmptyOrNullString())),
                'accRefNumber', everyItem(not(isEmptyOrNullString())),
                'accountType', everyItem(not(isEmptyOrNullString())),
                'credsAllowed', everyItem(not(emptyIterable())),
                'name', everyItem(not(isEmptyOrNullString())),
                'mbeba', everyItem(not(isEmptyOrNullString())),
                'aeba', everyItem(not(isEmptyOrNullString())),
                'accRefId', everyItem(not(isEmptyOrNullString())),
                'maskedAccountNumber', everyItem(not(isEmptyOrNullString())),
                'invalidVpa', everyItem(isA(Boolean.class)))
                .root('body.sarvatraUserProfile.response.vpaDetails.defaultCredit.credsAllowed')
                .body(
                'dLength', everyItem(everyItem(isIn('4', '6'))),
                'credsAllowedDLength', everyItem(everyItem(isIn('4', '6'))),
                'credsAllowedDType', everyItem(everyItem(equalTo('Numeric'))),
                'credsAllowedSubType', everyItem(everyItem(isIn('SMS', 'MPIN', 'ATMPIN'))),
                'credsAllowedType', everyItem(everyItem(isIn('OTP', 'PIN'))))
                .root('body.sarvatraUserProfile.response.vpaDetails.defaultDebit')
                .body(
                'bank', everyItem(not(isEmptyOrNullString())),
                'ifsc', everyItem(not(isEmptyOrNullString())),
                'account', everyItem(not(isEmptyOrNullString())),
                'accRefNumber', everyItem(not(isEmptyOrNullString())),
                'accountType', everyItem(not(isEmptyOrNullString())),
                'credsAllowed', everyItem(not(emptyIterable())),
                'name', everyItem(not(isEmptyOrNullString())),
                'mbeba', everyItem(not(isEmptyOrNullString())),
                'aeba', everyItem(not(isEmptyOrNullString())),
                'accRefId', everyItem(not(isEmptyOrNullString())),
                'maskedAccountNumber', everyItem(not(isEmptyOrNullString())),
                'invalidVpa', everyItem(isA(Boolean.class)))
                .root('body.sarvatraUserProfile.response.vpaDetails.defaultDebit.credsAllowed')
                .body(
                'dLength', everyItem(everyItem(isIn('4', '6'))),
                'credsAllowedDLength', everyItem(everyItem(isIn('4', '6'))),
                'credsAllowedDType', everyItem(everyItem(equalTo('Numeric'))),
                'credsAllowedSubType', everyItem(everyItem(isIn('SMS', 'MPIN', 'ATMPIN'))),
                'credsAllowedType', everyItem(everyItem(isIn('OTP', 'PIN'))))
                .root('body.sarvatraUserProfile.response.bankAccounts')
                .body(
                'bank', everyItem(not(isEmptyOrNullString())),
                'ifsc', everyItem(not(isEmptyOrNullString())),
                'account', everyItem(not(isEmptyOrNullString())),
                'accRefNumber', everyItem(not(isEmptyOrNullString())),
                'accountType', everyItem(not(isEmptyOrNullString())),
                'credsAllowed', everyItem(not(emptyIterable())),
                'name', everyItem(not(isEmptyOrNullString())),
                'mbeba', everyItem(not(isEmptyOrNullString())),
                'aeba', everyItem(not(isEmptyOrNullString())),
                'accRefId', everyItem(not(isEmptyOrNullString())),
                'maskedAccountNumber', everyItem(not(isEmptyOrNullString())),
                'invalidVpa', everyItem(isA(Boolean.class)))
                .root('body.sarvatraUserProfile.response.bankAccounts.credsAllowed')
                .body(
                'dLength', everyItem(everyItem(isIn('4', '6'))),
                'credsAllowedDLength', everyItem(everyItem(isIn('4', '6'))),
                'credsAllowedDType', everyItem(everyItem(equalTo('Numeric'))),
                'credsAllowedSubType', everyItem(everyItem(isIn('SMS', 'MPIN', 'ATMPIN'))),
                'credsAllowedType', everyItem(everyItem(isIn('OTP', 'PIN'))))
    }
}
