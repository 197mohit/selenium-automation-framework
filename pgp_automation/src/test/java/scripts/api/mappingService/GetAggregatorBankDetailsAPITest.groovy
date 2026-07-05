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
import static com.paytm.appconstants.Constants.MappingService.GET_AGGREGATOR_BANK_DETAILS
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetAggregatorBankDetailsAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_BANK_ID = 'bankId'
    private static final String URL_PATH_PARAM_ENTITY_ID = 'entityId'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_AGGREGATOR_BANK_DETAILS)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-aggregator-bank-details-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String ID = 'ID'
        String AGGREGATOR_ID = 'AGGREGATOR_ID'
        String BANK_ID = 'BANK_ID'
        String BANK_CODE = 'BANK_CODE'
        String ACTIVE = 'ACTIVE'
        String ISATM = 'ISATM'
        String ENTITY_ID = 'ENTITY_ID'
    }

    @Test
    void testSuccess() {
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB("SELECT * FROM AGGREGATOR_BANK_MAP WHERE ACTIVE = 1 LIMIT 1;")
        if (rows.empty) throw new SkipException('no DB entry found for active bank')
        def row = rows[0]
        req()
                .pathParam(URL_PATH_PARAM_BANK_ID, row[Column.BANK_ID])
                .pathParam(URL_PATH_PARAM_ENTITY_ID, row[Column.ENTITY_ID])
                .get().then()
                .body(
                        'id', equalTo(row[Column.ID] as Integer),
                        'bankId', equalTo(row[Column.BANK_ID] as Integer),
                        'aggregatorId', equalTo(row[Column.AGGREGATOR_ID] as Integer),
                        'bankCode', equalTo(row[Column.BANK_CODE]),
                        'entityId', equalTo(row[Column.ENTITY_ID] as String),
                        'isActive', equalTo(row[Column.ACTIVE] as Integer),
                        'isAtm', equalTo(row[Column.ISATM] as Integer)
                )
    }
}