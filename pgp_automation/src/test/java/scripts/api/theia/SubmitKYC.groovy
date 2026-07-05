package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.OrderV2
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.SUBMIT_KYC
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class SubmitKYC extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(SUBMIT_KYC)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: m()?.orders.with {
            !it.empty ? it : null
        }?.last()?.id ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        txnToken : m()?.orders?.with {
                            assert it.add(new OrderV2(1, user()?.tokens?.getAt('sso')?.id, null))
                            it.last()?.transaction?.token
                        },
                ],
                body: [
                        kycNameOnDoc: 'kycNameOnDoc',
                        kycDocCode  : 'kycDocCode',
                        kycDocValue : 'kycDocValue'
                ]
        ]
    }

    @Merchant(edit = true)
    @AUser
    @Test
    void testSuccess() {
        def root = root()
        req().body(root).post().then()
                .root('body')
                .body('kycSuccessful', equalTo(false),
                'kycErrorMsg', equalTo('We could not validate your ID. Please try again with a different document ID'),
                'kycRetryCount', equalTo(2))
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('0001'),
                'resultMsg', equalTo('We could not validate your ID. Please try again with a different document ID'))

    }
}
