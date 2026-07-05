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
import static com.paytm.appconstants.Constants.MappingService.GET_LOOK_UP
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetLookUpAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_CATEGORY = 'category'
    private static final String URL_PATH_PARAM_CHANNEL_NAME = 'channelName'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_LOOK_UP)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-look-up-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String ID = 'ID'
        String NAME = 'NAME'
        String CATEGORY = 'CATEGORY'
        String ALIPAY_CODE = 'ALIPAY_CODE'
    }

    @Test
    void testSuccess() {
        String query = 'SELECT * FROM PGPDB.LOOKUP_DATA LIMIT 1;'
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        def row = rows[0]
        req()
                .pathParam(URL_PATH_PARAM_CATEGORY, row[Column.CATEGORY])
                .pathParam(URL_PATH_PARAM_CHANNEL_NAME, row[Column.NAME])
                .get().then()
                .body(
                        'id', equalTo(row[Column.ID] as Integer),
                        'name', equalTo(row[Column.NAME]),
                        'category', equalTo(row[Column.CATEGORY]),
                        'alipayCode', equalTo(row[Column.ALIPAY_CODE]),
                )
    }
}