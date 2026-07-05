package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.SHOW_PAYMENT_PAGE
import static io.restassured.RestAssured.given

@Owner("Deepak")
class ShowPaymentPage extends TestSetUp {

    def setQueryParamOrderIdFilter = [filter: { req, res, ctx ->
        if (req.getQueryParams()['orderId'] == '?') {
            req.removeQueryParam('orderId').queryParam('orderId', req.formParams['orderId'] ?: new Random().nextLong().abs() as String)
        }
        ctx.next(req, res)
    }] as Filter

    final def reqBldr = {
        new RequestSpecBuilder()
                .addFilter(setQueryParamOrderIdFilter)
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.URLENC)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(SHOW_PAYMENT_PAGE)
                .addQueryParams([mid: m()?.id ?: new Random().nextLong().abs() as String, orderId: '?'])
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                mid     : m().id,
                txnToken: m()?.orders?.with {
                    assert it.add(new OrderV2(1, null, null))
                    it.last()?.transaction?.token
                },
                orderId : m()?.orders?.with {
                    !it.empty ? it : null
                }?.last()?.id ?: new Random().nextLong().abs() as String,
        ]
    }

    @Merchant(edit = true)
    @Test
    void testSuccess() {
        def root = root()
        req().formParams(root).post().then()
                .contentType(ContentType.HTML)
    }
}
