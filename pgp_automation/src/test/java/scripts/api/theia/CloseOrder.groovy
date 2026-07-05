package scripts.api.theia

import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.FilterableRequestSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class CloseOrder extends TestSetUp {
    final def reqBldr = {
        new RequestSpecBuilder()
                .addFilter(setSignatureFilter)
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(Constants.PGPAPIResourcePath.CLOSE_ORDER_V1)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: m()?.orders?.last()?.id ?: new Random().nextLong().abs() as String])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        signature: '?',
                ],
                body: [
                        mid    : m().id,
                        orderId: m()?.orders?.last()?.id ?: new Random().nextLong().abs() as String,
                ]
        ]
    }

    def setSignatureFilter = [filter: { FilterableRequestSpecification req, res, ctx ->
        def root = new JsonSlurper().parseText(req.getBody())
        root?.head?.with {
            if (it?.signature == '?') it.signature = getChecksum(m().key, toJson(root.body))
        }
        req.body(root)
        ctx.next(req, res)
    }] as Filter

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddMoney.getId()})
    @Test
    void testSuccess() {
        assert m().orders.add(new OrderV2(1, null, m().users.find().id))
        def root = root()
        req().body(root).post().then()
                .body(
                'head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body('responseTimestamp', not(isEmptyOrNullString()))
                .root('body')
                .body('resultInfo', isA(Object.class))
                .root('body.resultInfo')
                .body(
                'resultStatus', equalTo('S'),
                'resultCode', equalTo('01'),
                'resultMsg', equalTo('SUCCESS'))
    }
}
