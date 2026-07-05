package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.UPDATE_TXN_DETAIL
import static io.restassured.RestAssured.given

@Owner("Deepak")
class UpdateTransactionDetail extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addFilter(setSignatureFilter)
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(UPDATE_TXN_DETAIL)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: m()?.orders?.last()?.id ?: new Random().nextLong().abs() as String])
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
                        signature: '?',
                ],
                body: [
                        txnAmount: [
                                value   : m().orders.last().amt + 1,
                                currency: 'INR'
                        ]
                ]
        ]
    }

    private def setSignatureFilter = [filter: { req, res, ctx ->
        def root = new JsonSlurper().parseText(req.getBody())
        root?.head?.with {
            if (it?.signature == '?') it.signature = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
        }
        req.body(root)
        ctx.next(req, res)
    }] as Filter

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.EDC_EMI_MERCH.getId() })
    @AUser
    @Test
    void testSuccess() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }
}
