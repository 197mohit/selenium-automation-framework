package scripts.api.theia

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.util.PGPUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import com.paytm.appconstants.Constants

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.CREATE_TOKEN
import static io.restassured.RestAssured.given

@Owner("Deepak")
class CreateToken extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([setChecksumFilter])
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(CREATE_TOKEN)
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        version         : 'v1',
                        requestTimestamp: System.currentTimeMillis() as String,
                        token           : '?',
                        tokenType       : 'CHECKSUM',
                ],
                body: [
                        mid        : m().id,
                        referenceId: UUID.randomUUID().toString()[0..19],
                ],
        ]
    }

    Filter setChecksumFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            def root = new JsonSlurper().parseText(requestSpec.getBody())
            root?.head?.with {
                if (it?.token == '?') it.token = PGPUtil.getChecksum(m().key, JsonOutput.toJson(root.body))
            }
            requestSpec.body(root)
            ctx.next(requestSpec, responseSpec)
        }
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
    @Test
    void testAbleToAccessAPIUsingChecksum() {
        def root = root()
        String accessToken = req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .extract().path('body.accessToken')
        assert accessToken.length() == 45, 'body.accessToken length is not equal to 45'
    }
}
