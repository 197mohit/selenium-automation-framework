package scripts.api.mappingService


import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.util.DbQueriesUtil
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_FETCH_ENTITY_IGNORE_PARAMS
import static com.paytm.appconstants.Constants.Owner.GAGANDEEP
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath

@Owner(GAGANDEEP)
class GetFetchEntityIgnoreParamTest extends TestSetUp {

    private static final String ENTITY_ID = 'entityId'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_FETCH_ENTITY_IGNORE_PARAMS)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/fetch-entity-ignore-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static List<Map<String, Object>> getDataFromDB() {

        String entityId =
                DbQueriesUtil.selectFromPaytmPGDB("SELECT ENTITY_ID from ENTITY_IGNORE_PARAMS").get(0).get("ENTITY_ID")

        DbQueriesUtil.selectFromPaytmPGDB("SELECT FIELD_NAME,ENTITY_ID FROM ENTITY_IGNORE_PARAMS WHERE ENTITY_ID = $entityId;")
    }


    @Test
    void 'successful api response from db'() {
        def dataFromDB = getDataFromDB();
        String entityId = dataFromDB.get(0).get("ENTITY_ID")
        String redisKey = "EIP_" + entityId
        List EntityIgnoreParamDB = dataFromDB.collect { it -> it.FIELD_NAME }

        REDIS:
        {
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        API:
        def EntityIgnoreParamAPI = req()
                .pathParam(ENTITY_ID, entityId)
                .get()
                .then()
                .extract()
                .jsonPath()
        VALIDATION:
        Assertions.assertThat(EntityIgnoreParamAPI.get('paramsList.fieldName') == EntityIgnoreParamDB)
        Assertions.assertThat(STATIC_REDIS_CLUSTER().get(redisKey) != null);
    }

    @Test
    void 'successful api response from redis'() {
        def dataFromDB = getDataFromDB();
        String entityId = dataFromDB.get(0).get("ENTITY_ID")
        String redisKey = "EIP_" + entityId
        List EntityIgnoreParamDB = dataFromDB.collect { it -> it.FIELD_NAME }

        REDIS:
        {
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam(ENTITY_ID, entityId).get()
                assert STATIC_REDIS_CLUSTER().get(redisKey) != null
            }
        }
        API:
        def EntityIgnoreParamAPI = req()
                .pathParam(ENTITY_ID, entityId)
                .get()
                .then()
                .extract()
                .jsonPath()
        VALIDATION:
        Assertions.assertThat(EntityIgnoreParamAPI.get('paramsList.fieldName') == EntityIgnoreParamDB)
        Assertions.assertThat(STATIC_REDIS_CLUSTER().get(redisKey) != null);
    }


    @Test
    void 'empty response from api to have Dummy Redis Key with ttl equal to 1 hour'() {
        String entityId = new Random().nextInt(6)   //random entity id which is not present in db
        String redisKey = "DUMMY_EIP_" + entityId

        REDIS:
        {
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }

        API:
        given()
                .baseUri(PGP_HOST)
                .basePath(GET_FETCH_ENTITY_IGNORE_PARAMS)
                .pathParam(ENTITY_ID, entityId)
                .get()

        VALIDATION:
        Assertions.assertThat(STATIC_REDIS_CLUSTER().get(redisKey) != null);
        Assertions.assertThat(STATIC_REDIS_CLUSTER().ttl(redisKey) <= 3600);  //ttl should be equal to or less than 1 hour
        Assertions.assertThat(STATIC_REDIS_CLUSTER().ttl(redisKey) >= 3550);  //ttl could not be less than 3500 having delay of 50 sec
    }

}
