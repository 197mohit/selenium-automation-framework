package scripts.api.mappingService

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.util.DbQueriesUtil
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import org.testng.SkipException
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_CARD_NETWORK_DETAILS
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize

class GetCardNetworkDetailsAPITest extends TestSetUp {

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_CARD_NETWORK_DETAILS)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-card-network-details-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String CARD_NETWORK = 'CARD_NETWORK'
        String CARD_NETWORK_NAME = 'CARD_NETWORK_NAME'
        String LOGO_URL = 'LOGO_URL'
    }

    @Test
    void testSuccess() {
        String query = 'SELECT * from PGPDB.CARD_NETWORK_DETAILS WHERE STATUS = 1;'
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().get().then()
                .root('response')
                .body(
                        'resultCode', equalTo('00000'),
                        'resultStatus', equalTo('S'),
                        'messaage', equalTo('Success')
                )
                .root('cardNetworkDetailsList')
                .body(
                        'cardNetwork', equalTo((rows.collect { it[Column.CARD_NETWORK] })),
                        'logoUrl', equalTo((rows.collect { it[Column.LOGO_URL] })),
                        'displayName', equalTo((rows.collect { it[Column.CARD_NETWORK_NAME] })),
                )
    }
}