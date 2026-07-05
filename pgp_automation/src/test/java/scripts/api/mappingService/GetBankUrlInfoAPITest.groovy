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
import static com.paytm.appconstants.Constants.MappingService.GET_BANK_URL_INFO
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetBankUrlInfoAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_BANK_ID = 'bankId'
    private static final String URL_PATH_PARAM_PAY_METHOD_ID = 'payMethodId'
    private static final String URL_PATH_PARAM_CHANNEL_ID = 'channelId'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_BANK_URL_INFO)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-bank-url-info-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String ID = 'ID'
        String BANK_ID = 'BANK_ID'
        String PAY_METHOD_ID = 'PAY_METHOD_ID'
        String CHANNEL_ID = 'CHANNEL_ID'
        String WEB_PAY_URL = 'WEB_PAY_URL'
        String S2S_PAY_URL = 'S2S_PAY_URL'
        String STATUS_QRY_URL = 'STATUS_QRY_URL'
        String REFUND_URL = 'REFUND_URL'
        String REFUND_STATUS_URL = 'REFUND_STATUS_URL'
        String WEB_RESPONSE_URL = 'WEB_RESPONSE_URL'
        String URL = 'URL'
        String URL_TYPE = 'URL_TYPE'
    }

    @Test
    void testSuccess() {
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB("SELECT * from BANK_URL_INFO LIMIT 1")
        if (rows.empty) throw new SkipException('no DB entry found for the query')
        def row = rows[0]
        req()
                .pathParam(URL_PATH_PARAM_BANK_ID, row[Column.BANK_ID])
                .pathParam(URL_PATH_PARAM_PAY_METHOD_ID, row[Column.PAY_METHOD_ID])
                .pathParam(URL_PATH_PARAM_CHANNEL_ID, row[Column.CHANNEL_ID])
                .get().then()
                .body(
                        'id', equalTo(row[Column.ID] as Integer),
                        'bankId', equalTo(row[Column.BANK_ID] as Integer),
                        'payMethodId', equalTo(row[Column.PAY_METHOD_ID] as Integer),
                        'channelId', equalTo(row[Column.CHANNEL_ID] as Integer),
                        'webPayUrl', equalTo(row[Column.WEB_PAY_URL]),
                        's2sPayUrl', equalTo(row[Column.S2S_PAY_URL]),
                        'statusQueryUrl', equalTo(row[Column.STATUS_QRY_URL]),
                        'refundUrl', equalTo(row[Column.REFUND_URL]),
                        'refundStatusUrl', equalTo(row[Column.REFUND_STATUS_URL]),
                        'webResponseUrl', equalTo(row[Column.WEB_RESPONSE_URL]),
                        'urlType', equalTo(row[Column.URL_TYPE]),
                        'url', equalTo(row[Column.URL]),
                )
    }
}