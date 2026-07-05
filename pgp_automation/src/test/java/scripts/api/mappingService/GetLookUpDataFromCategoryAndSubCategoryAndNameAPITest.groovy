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
import static com.paytm.appconstants.Constants.MappingService.GET_LOOK_UP_DATA_FROM_CATEGORY_AND_SUB_CATEGORY_AND_NAME
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetLookUpDataFromCategoryAndSubCategoryAndNameAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_CATEGORY = 'category'
    private static final String URL_PATH_PARAM_SUB_CATEGORY = 'subCategory'
    private static final String URL_PATH_PARAM_NAME = 'name'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_LOOK_UP_DATA_FROM_CATEGORY_AND_SUB_CATEGORY_AND_NAME)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-look-up-data-from-category-and-sub-category-and-name-schema.json'))
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
        String query = "SELECT * FROM PAYTMPGDB.LOOKUP_DATA WHERE STATUS=9376503 AND CATEGORY IS NOT NULL AND SUB_CATEGORY IS NOT NULL AND NAME IS NOT NULL AND CATEGORY != 'PREFERENCES' LIMIT 1;"
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        final CATEGORY = rows[0].get(Column.CATEGORY)
        final SUB_CATEGORY = rows[0].get(Column.SUB_CATEGORY)
        final NAME = rows[0].get(Column.NAME)
        query = "SELECT * FROM PAYTMPGDB.LOOKUP_DATA WHERE STATUS=9376503 AND CATEGORY='${CATEGORY}' AND SUB_CATEGORY='${SUB_CATEGORY}' AND NAME='${NAME}';"
        rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req()
                .pathParam(URL_PATH_PARAM_CATEGORY, CATEGORY)
                .pathParam(URL_PATH_PARAM_SUB_CATEGORY, SUB_CATEGORY)
                .pathParam(URL_PATH_PARAM_NAME, NAME)
                .get().then()
                .root('lookUpDataOldPgList')
                .body(
                        'id.collect { it as Long }', equalTo(rows.collect { it[Column.ID] as Long }),
                        'category', equalTo(rows.collect { it[Column.CATEGORY] }),
                        'subCategory', equalTo(rows.collect { it[Column.SUB_CATEGORY] }),
                        'name', equalTo(rows.collect { it[Column.NAME] }),
                        'value', equalTo(rows.collect { it[Column.VALUE] }),
                        'status', equalTo(rows.collect { it[Column.STATUS] as Integer }),
                        'description', equalTo(rows.collect { it[Column.DESCRIPTION] }),
                        'createdBy', equalTo(rows.collect { it[Column.CREATED_BY] as Integer }),
                        'createdDate', equalTo(rows.collect { it[Column.CREATED_DATE] as String }),
                        'modifiedBy', equalTo(rows.collect { it[Column.MODIFIED_BY] }),
                        'modifiedDate', equalTo(rows.collect { it[Column.MODIFIED_DATE] as String }),
                )
    }
}