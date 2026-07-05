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
import static com.paytm.appconstants.Constants.MappingService.GET_FORMATTER
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetFormatterAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_BANK_CODE = 'bankCode'
    private static final String URL_PATH_PARAM_PAY_METHOD = 'payMethod'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_FORMATTER)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-formatter-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String ID = 'ID'
        String BANK_CODE = 'BANK_CODE'
        String PAY_METHOD = 'PAY_METHOD'
        String FORMATTER_NAME = 'FORMATTER_NAME'
        String STATUS = 'STATUS'
        String PARAMS = 'params'
    }

    @Test
    void testSuccess() {
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB("SELECT * from FORMATTER_DETAILS LIMIT 1")
        if (rows.empty) throw new SkipException('no DB entry found for the query')
        def row = rows[0]
        req()
                .pathParam(URL_PATH_PARAM_BANK_CODE, row[Column.BANK_CODE])
                .pathParam(URL_PATH_PARAM_PAY_METHOD, row[Column.PAY_METHOD])
                .get().then()
                .body(
                        'id', equalTo(row[Column.ID] as Integer),
                        'bankCode', equalTo(row[Column.BANK_CODE]),
                        'payMethod', equalTo(row[Column.PAY_METHOD]),
                        'formatterName', equalTo(row[Column.FORMATTER_NAME]),
                        'status', equalTo(row[Column.STATUS]),
                        'params', equalTo(row[Column.PARAMS])
                )
    }
}