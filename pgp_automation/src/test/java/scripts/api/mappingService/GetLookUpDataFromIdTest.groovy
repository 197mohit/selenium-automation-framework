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
import static com.paytm.appconstants.Constants.MappingService.GET_LOOKUP_DATA_FROM_ID
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetLookUpDataFromIdTest extends TestSetUp {

    private static final String URL_PATH_PARAM_ID = 'id'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_LOOKUP_DATA_FROM_ID)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-look-up-data-from-id-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String ID = 'LOOKUP_ID'
        String CATEGORY = 'CATEGORY'
        String SUB_CATEGORY = 'SUB_CATEGORY'
        String NAME = 'NAME'
        String VALUE = 'VALUE'
        String STATUS = 'STATUS'
        String DESCRIPTION = 'DESCRIPTION'
        String CREATED_BY = 'CREATED_BY'
        String CREATED_DATE = 'CREATED_DATE'
        String MODIFIED_BY = 'MODIFIED_BY'
        String MODIFIED_DATE = 'MODIFIED_DATE'
    }

    @Test
    void testSuccess() {
        String query = 'SELECT * FROM PAYTMPGDB.LOOKUP_DATA LIMIT 1;'
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        def row = rows[0]
        req()
                .pathParam(URL_PATH_PARAM_ID, row[Column.ID])
                .get().then()
                .body(
                        'id', equalTo(row[Column.ID] as Integer),
                        'category', equalTo(row[Column.CATEGORY]),
                        'subCategory', equalTo(row[Column.SUB_CATEGORY]),
                        'name', equalTo(row[Column.NAME]),
                        'value', equalTo(row[Column.VALUE]),
                        'status', equalTo(row[Column.STATUS] as Integer),
                        'description', equalTo(row[Column.DESCRIPTION]),
                        'createdBy', equalTo(row[Column.CREATED_BY] as Integer),
                        'createdDate', equalTo(row[Column.CREATED_DATE] as String),
                        'modifiedBy', equalTo(row[Column.MODIFIED_BY] as Integer),
                        'modifiedDate', equalTo(row[Column.MODIFIED_DATE] as String),
                )
    }
}