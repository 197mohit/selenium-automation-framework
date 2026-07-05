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
import static com.paytm.appconstants.Constants.MappingService.GET_MERCHANT_API_URL_INFO
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetMerchantApiURLInfoAPITest extends TestSetUp {
    private static final String URL_PATH_PARAM_MID = 'MID'
    private static final String URL_PATH_PARAM_PLATFORM = 'PLATFORM'
    public static final String PAYTM = "paytm"
    public static final String ALIPAY = "alipay"
    public static final String QUERY_TO_FETCH_MID = "(SELECT MM.MID FROM MIGRATION_MID AS MM INNER JOIN MERCHANT_API_URL_INFO AS MAUI ON MM.ENTITY_ID=MAUI.ENTITY_ID) LIMIT 1;"
    public static final String QUERY_TO_FETCH_ALIPAY_MID = "SELECT PAYTMPGDB.MIGRATION_MID.MID, PAYTMPGDB.MERCHANT_API_URL_INFO.URL, PAYTMPGDB.MERCHANT_API_URL_INFO.URL_TYPE, PGPDB.alipay_paytm_merchant.alipay_merchant_id FROM ((PAYTMPGDB.MIGRATION_MID INNER JOIN \n" +
            "PAYTMPGDB.MERCHANT_API_URL_INFO ON MIGRATION_MID.ENTITY_ID=MERCHANT_API_URL_INFO.ENTITY_ID) INNER JOIN PGPDB.alipay_paytm_merchant ON PAYTMPGDB.MIGRATION_MID.MID=PGPDB.alipay_paytm_merchant.paytm_merchant_id) LIMIT 1;"

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_MERCHANT_API_URL_INFO)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }
    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-merchant-api-url-info.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String URL_TYPE = 'URL_TYPE'
        String URL = 'URL'
        String ALIPAY_MERCHANT_ID = 'alipay_merchant_id'
        String MID = 'MID'
    }

    @Test
    void testThatWhenPaytmMidIsProvidedInRequestAndPaytmInPathParam() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        String MID = rows.get(0).get(Column.MID);
        query = "SELECT * FROM MERCHANT_API_URL_INFO WHERE ENTITY_ID = (SELECT ENTITY_ID FROM MIGRATION_MID WHERE MID='${MID}' LIMIT 1);"
       System.out.println("query--->"+query);
        rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, PAYTM).get().then()
                .body("merchantId", equalTo(MID))
                .root('merchantApiUrlInfoList')
                .body("urlType", equalTo(rows.collect { it[Column.URL_TYPE] as String }))
                .body("url", equalTo(rows.collect { it[Column.URL] as String }))
    }

    @Test
    void testThatWhenAlipayMidIsProvidedInRequestAndAlipayInPathParam() {
        String query = QUERY_TO_FETCH_ALIPAY_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        String MID = rows.get(0).get(Column.ALIPAY_MERCHANT_ID);
        String PaytmMid = rows.get(0).get(Column.MID)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        query = "SELECT * FROM MERCHANT_API_URL_INFO WHERE ENTITY_ID = (SELECT ENTITY_ID FROM MIGRATION_MID WHERE MID='${PaytmMid}' LIMIT 1);"
        rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, ALIPAY).get().then()
                .body("merchantId", equalTo(MID))
                .root('merchantApiUrlInfoList')
                .body("urlType", equalTo(rows.collect { it[Column.URL_TYPE] as String }))
                .body("url", equalTo(rows.collect { it[Column.URL] as String }))
    }

    @Test
    void testThatWhenPaytmMidIsProvidedInRequestAndAlipayInPathParam() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        String MID = rows.get(0).get(Column.MID);
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, ALIPAY).get().then().
                body("merchantId", equalTo(null)).
                body("merchantApiUrlInfoList", equalTo(null))

    }

    @Test
    void testThatWhenAlipayMidIsProvidedInRequestAndPaytmInPathParam() {
        String query = QUERY_TO_FETCH_ALIPAY_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        String MID = rows.get(0).get(Column.ALIPAY_MERCHANT_ID);
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, PAYTM).get().then().
                body("merchantId", equalTo(null)).
                body("merchantApiUrlInfoList", equalTo(null))

    }

    @Test
    void testThatWhenInvalidMidIsProvidedInRequestAndPaytmInPathParam() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        String MID = UUID.randomUUID().toString()
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, PAYTM).get().then().
                body("merchantId", equalTo(null)).
                body("merchantApiUrlInfoList", equalTo(null))

    }

    @Test
    void testThatWhenInvalidMidIsProvidedInRequestAndAlipayInPathParam() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        String MID = UUID.randomUUID().toString()
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, ALIPAY).get().then().
                body("merchantId", equalTo(null)).
                body("merchantApiUrlInfoList", equalTo(null))

    }

    @Test
    void testThatWhenMidIsProvidedInRequestAndInvalidPlatformInPathParam() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        String MID = rows.get(0).get(Column.MID);
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, "WWW").get().then().
                body("merchantId", equalTo(null)).
                body("merchantApiUrlInfoList", equalTo(null))

    }

    @Test
    void testThatWhenMIdIsProvidedInRequestAndalipayInPathParamReadDataFromDB() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        String MID = rows.get(0).get(Column.MID);
        REDIS:
        {
            String redisKey = 'M_API_URL_INFO_paytm' + MID
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
        }
        query = "SELECT * FROM MERCHANT_API_URL_INFO WHERE ENTITY_ID = (SELECT ENTITY_ID FROM MIGRATION_MID WHERE MID='${MID}' LIMIT 1);"
        rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, PAYTM).get().then()
                .body("merchantId", equalTo(MID))
                .root('merchantApiUrlInfoList')
                .body("urlType", equalTo(rows.collect { it[Column.URL_TYPE] as String }))
                .body("url", equalTo(rows.collect { it[Column.URL] as String }))
    }

    @Test
    void testThatWhenMIdIsProvidedInRequestAndalipayInPathParamReadDataFromRedis() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        String MID = rows.get(0).get(Column.MID);
        REDIS:
        {
            String redisKey = 'M_API_URL_INFO_paytm_' + MID
            if (STATIC_REDIS_CLUSTER().get(redisKey) == null) {
                req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, PAYTM)
                        .get()

                assert STATIC_REDIS_CLUSTER().get(redisKey) != null

            }
            query = "SELECT * FROM MERCHANT_API_URL_INFO WHERE ENTITY_ID = (SELECT ENTITY_ID FROM MIGRATION_MID WHERE MID='${MID}'  LIMIT 1 );"
            rows = DbQueriesUtil.selectFromPaytmPGDB(query)
            if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
            req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, PAYTM).get().then()
                    .body("merchantId", equalTo(MID))
                    .root('merchantApiUrlInfoList')
                    .body("urlType", equalTo(rows.collect { it[Column.URL_TYPE] as String }))
                    .body("url", equalTo(rows.collect { it[Column.URL] as String }))
        }
    }

    @Test
    void testThatWhenMIdIsProvidedInRequestAndAlipayInPathParamReadDataFromDBWhenCorruptedStringIsSetInRedis() {
        String query = QUERY_TO_FETCH_MID
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        String MID = rows.get(0).get(Column.MID);
        REDIS:
        {
            String redisKey = 'M_API_URL_INFO_paytm_' + MID
            if (STATIC_REDIS_CLUSTER().get(redisKey) != null)
                STATIC_REDIS_CLUSTER().del(redisKey)
            STATIC_REDIS_CLUSTER().set('M_API_URL_INFO_paytm_' + MID, UUID.randomUUID().toString())
        }
        query = "SELECT * FROM MERCHANT_API_URL_INFO WHERE ENTITY_ID = (SELECT ENTITY_ID FROM MIGRATION_MID WHERE MID='${MID}' LIMIT 1);"
        rows = DbQueriesUtil.selectFromPaytmPGDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().pathParam(URL_PATH_PARAM_MID, MID).pathParam(URL_PATH_PARAM_PLATFORM, PAYTM).get().then()
                .body("merchantId", equalTo(MID))
                .root('merchantApiUrlInfoList')
                .body("urlType", equalTo(rows.collect { it[Column.URL_TYPE] as String }))
                .body("url", equalTo(rows.collect { it[Column.URL] as String }))
    }
}