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
import static com.paytm.appconstants.Constants.MappingService.GET_CUSTOM_PAYLOAD_DATA
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class GetCustomPayloadDataAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_MERCHANT_ID = 'merchantId'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_CUSTOM_PAYLOAD_DATA)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-custom-payload-data-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String ID = 'id'
        String MERCHANT_ID = 'merchantId'
        String CONFIGURATION = 'configuration'
        String STATUS = 'status'
    }

    @Test
    void testSuccess() {
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB('SELECT * FROM custom_payload_contract WHERE status = 9376503 limit 1')
        if (rows.empty) throw new SkipException('no DB entry found for active mid')
        def row = rows[0]
        req().pathParam(URL_PATH_PARAM_MERCHANT_ID, row[Column.MERCHANT_ID]).get().then()
                .body('customPayloadList', not(emptyIterable()))
                .root('customPayloadList[0]')
                .body(
                        'id', equalTo(row[Column.ID]),
                        'merchantId', equalTo(row[Column.MERCHANT_ID]),
                        'configuration', equalTo(row[Column.CONFIGURATION]),
                        'status', equalTo(row[Column.STATUS])
                )
    }

    @Test
    void testFailure() {
        req().pathParam(URL_PATH_PARAM_MERCHANT_ID, UUID.randomUUID().toString()).get().then()
                .body('customPayloadList', emptyIterable())
    }
}