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
import static com.paytm.appconstants.Constants.MappingService.GET_ALL_BANK_DETAILS
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize

class GetAllBankDetailsAPITest extends TestSetUp {

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_ALL_BANK_DETAILS)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-all-bank-details-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String BANK_ID = 'BANK_ID'
        String BANK_NAME = 'BANK_NAME'
        String BANK_CODE = 'BANK_CODE'
        String STATUS = 'STATUS'
        String BANK_DISPLAY_NAME = 'BANK_DISPLAY_NAME'
        String BANK_KEY = 'BANK_KEY'
        String ALIPAY_CODE = 'ALIPAY_CODE'
        String BANK_WAP_LOGO = 'BANK_WAP_LOGO'
        String BANK_WEB_LOGO = 'BANK_WEB_LOGO'
        String MANDATE_TYPE = 'MANDATE_TYPE'
        String MANDATE_NET_BANKING = 'MANDATE_NET_BANKING'
        String MANDATE_DEBIT_CARD = 'MANDATE_DEBIT_CARD'
        String STANDARD_BANK_CODE = 'STANDARD_BANK_CODE'
        String PAY_MODE = 'PAY_MODE'
        String DISPLAY_ORDER = 'DISPLAY_ORDER'
        String EXIT_IFSC_CODE = 'EXIT_IFSC_CODE'
    }

    @Test
    void testSuccess() {
        String query = 'SELECT * FROM BANK_MASTER WHERE STATUS = 1 AND BANK_NAME IS NOT NULL AND BANK_CODE IS NOT NULL ORDER BY BANK_NAME ASC'
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB(query)
        if (rows.empty) throw new SkipException("no DB entry found for the query - $query")
        req().get().then()
                .root('response')
                .body(
                        'resultCode', equalTo('00000'),
                        'resultStatus', equalTo('S'),
                        'messaage', equalTo('Success')
                )
                .root('bankMasterDetailsList')
                .body('', hasSize(rows.size()))
                .body(
                        'bankId', equalTo(rows.collect { it[Column.BANK_ID] as Integer }),
                        'bankId', equalTo((rows.collect { it[Column.BANK_ID] as Integer })),
                        'bankName', equalTo((rows.collect { it[Column.BANK_NAME] })),
                        'bankCode', equalTo((rows.collect { it[Column.BANK_CODE] })),
                        'bankDisplayName', equalTo((rows.collect { it[Column.BANK_DISPLAY_NAME] })),
                        'bankKey', equalTo((rows.collect { it[Column.BANK_KEY] })),
                        'alipayBankCode', equalTo((rows.collect { it[Column.ALIPAY_CODE] })),
                        'bankWebLogo', equalTo((rows.collect { it[Column.BANK_WEB_LOGO] })),
                        'bankWapLogo', equalTo((rows.collect { it[Column.BANK_WAP_LOGO] })),
                        'status', equalTo((rows.collect { it[Column.STATUS] })),
                        'bankMandate', equalTo((rows.collect { it[Column.MANDATE_TYPE] })),
                        'standardBankCode', equalTo((rows.collect { it[Column.STANDARD_BANK_CODE] })),
                        'mandateNetBanking', equalTo((rows.collect { it[Column.MANDATE_NET_BANKING] })),
                        'mandateDebitCard', equalTo((rows.collect { it[Column.MANDATE_DEBIT_CARD] })),
                        'payMode', equalTo((rows.collect { it[Column.PAY_MODE] })),
                        'displayOrder', equalTo((rows.collect { it[Column.DISPLAY_ORDER] as Integer }.collect { it ?: 9999 })),
                        'extIfscCode', equalTo((rows.collect {it[Column.EXIT_IFSC_CODE] })),
                )
    }
}
