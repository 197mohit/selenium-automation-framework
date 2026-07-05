package scripts.api.mappingService

import com.paytm.base.test.TestSetUp
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_DYNAMIC_WRAPPER_CONFIGS
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class GetDynamicWrapperConfigsAPITest extends TestSetUp {

    public static final String MERCHANT_ID = 'merchantId'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_DYNAMIC_WRAPPER_CONFIGS)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-dynamic-wrapper-configs-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    @Test
    void testSuccess() {
        given(reqBldr().removeQueryParam(MERCHANT_ID).build()).get().then()
                .body('dynamicWrapperConfigs', not(emptyIterable()))
    }

    @Test
    void testForExistingMerchantId() {
        req().queryParam(MERCHANT_ID, 'LnTConfig').get().then()
                .body('dynamicWrapperConfigs', hasSize(1))
    }

    @Test
    void testForNonExistingMerchantId() {
        req().queryParam(MERCHANT_ID, UUID.randomUUID().toString()).get().then()
                .body('dynamicWrapperConfigs', emptyIterable())
    }
}