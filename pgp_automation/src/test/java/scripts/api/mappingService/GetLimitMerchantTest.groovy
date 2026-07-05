package scripts.api.mappingService

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.util.DbQueriesUtil
import groovy.json.JsonSlurper
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
import static com.paytm.appconstants.Constants.MappingService.GET_LIMIT_MERCHANT
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

public class GetLimitMerchantTest extends TestSetUp {

    private static final String key = 'key'


    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_LIMIT_MERCHANT)
    }


    private final RequestSpecification req() {
        given(reqBldr().build())
    }


    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-limit-merchant-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }


    private static interface Column {
        String ID = 'id'
        String KEY = 'key'
        String VALUE = 'value'
        String STATUS = 'status'
    }

    @Test
    void 'testSuccessWithDB'() {
        String query = 'SELECT * FROM global_configuration LIMIT 1;'
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        def row = rows[0]
        String redisKey = "GLOBAL_CONFIG_" + row[Column.KEY]
        if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
            STATIC_REDIS_CLUSTER().del(redisKey)
        req()
                .pathParam(key, row[Column.KEY])
                .get().then()
                .body('', equalTo(new JsonSlurper().parseText(row[Column.VALUE])))
    }


    @Test
    void 'testSuccessWithRedis'() {
        String query = 'SELECT * FROM global_configuration LIMIT 1;'
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        def row = rows[0]
        String redisKey = "GLOBAL_CONFIG_" + row[Column.KEY]
        if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
            req().pathParam(key, row[Column.KEY])
                    .get().then()
            assert STATIC_REDIS_CLUSTER().get(redisKey) != null}
        req()
                .pathParam(key, row[Column.KEY])
                .get().then()
                .body('', equalTo(new JsonSlurper().parseText(row[Column.VALUE])))
    }
}
