package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_MERCHANT_USER_INFO
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchMerchantUserInfo extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_MERCHANT_USER_INFO)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: m()?.orders.with {
            !it.empty ? it : null
        }?.last()?.id ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        tokenType: 'SSO',
                        token    : user().tokens['sso'].id,
                ],
                body: [
                        mid    : m().id,
                        orderId: null//O
                ]
        ]
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void testSuccess() {
        def root = root()
        req().body(root).post().then()
                .body(
                'head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body(
                'requestId', nullValue(),
                'responseTimestamp', not(isEmptyOrNullString()),
                'version', equalTo('v1'))
                .root('body')
                .body(
                'extraParamsMap', nullValue(),
                'resultInfo', isA(Object.class),
                'merchantInfoResp', isA(Object.class),
                'userInfoResp', isA(Object.class))
                .root('body.resultInfo')
                .body(
                'resultStatus', equalTo('S'),
                'resultCode', equalTo('0000'),
                'resultMsg', equalTo('Success'))
                .root('body.merchantInfoResp')
                .body(
                'merDispname', equalTo(m().names.display.name),
                'merBusName', equalTo(m().names.business.name),
                '', hasKey('merLogoUrl'))
                .root('body.userInfoResp')
                .body('userInfo', nullValue())
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @AUser
    @Test
    void testFailre() {
        def root = root()
        root.head.token=null;
        req().body(root).post().then()
                .body(
                        'head', isA(Object.class),
                        'body', isA(Object.class))
                .root("body.resultInfo")
        .body("resultStatus",equalTo("F"))
        .body("resultCode",equalTo("1001"))
        .body("resultMsg",equalTo("Request parameters are not valid"))
    }

}
