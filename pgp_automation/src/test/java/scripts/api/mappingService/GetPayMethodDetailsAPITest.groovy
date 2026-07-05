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
import static com.paytm.appconstants.Constants.MappingService.GET_PAY_METHOD_DETAILS
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class GetPayMethodDetailsAPITest extends TestSetUp {

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_PAY_METHOD_DETAILS)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-pay-method-details-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String PAY_METHOD = 'PAY_METHOD'
        String PAY_METHOD_NEW_NAME = 'PAY_METHOD_NEW_NAME'
        String TYPE = 'TYPE'
    }


    @Test
    void testSuccess() {
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB('SELECT * FROM PAY_METHOD_DETAILS;')
        if (rows.empty) throw new SkipException('no DB entry found for the query')
        req().get().then()
                .root('response')
                .body(
                        'resultCode', equalTo('00000'),
                        'resultStatus', equalTo('S'),
                        'messaage', equalTo('Success')
                )
                .root('payMethodDetailsList')
                .body(
                        'payMethod', everyItem(isIn(rows.collect { it[Column.PAY_METHOD] })),
                        'payMethodName', everyItem(isIn(rows.collect { it[Column.PAY_METHOD_NEW_NAME] })),
                        'type', everyItem(isIn(rows.collect { it[Column.TYPE] })),
                )
    }
}
