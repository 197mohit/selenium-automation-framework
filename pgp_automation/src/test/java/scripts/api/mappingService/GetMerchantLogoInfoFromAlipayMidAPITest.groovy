package scripts.api.mappingService

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_MERCHANT_LOGO_INFO_FROM_ALIPAY_MID
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetMerchantLogoInfoFromAlipayMidAPITest extends TestSetUp {

    private static final String ALIPAY_MID = "alipayMid"

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_MERCHANT_LOGO_INFO_FROM_ALIPAY_MID)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-merchant-logo-info-from-alipay-mid-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    @Merchant
    @Test
    void testSuccess() {
        req().pathParam(ALIPAY_MID, m().alipayId).get().then()
                .body('paytmMid', equalTo(m().id))
    }
}