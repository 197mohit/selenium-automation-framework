package scripts.api.merchantStatus

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.FilterableRequestSpecification

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.GET_PAYMENT_STATUS
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given

@Owner("Deepak")
class GetPaymentStatus extends TestSetUp {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addFilter(setSignatureFilter)
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GET_PAYMENT_STATUS)
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        signature: '?',
                ],
                body: [
                        mid    : m()?.id,
                        orderId: new Random().nextLong().abs() as String,
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

}
