package scripts.api.mappingService

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.util.DbQueriesUtil
import io.qameta.allure.Issue
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
import static com.paytm.appconstants.Constants.MappingService.GET_BANK_DETAILS_FROM_ID
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.equalTo

class GetBankDetailsFromIdAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_BANK_ID = 'bankId'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_BANK_DETAILS_FROM_ID)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-bank-details-from-id-schema.json'))
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
        String EXT_IFSC_CODE = 'extIfscCode'
    }

    @Issue('PGP-29168')
    @Test
    void testSuccess() {
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB("SELECT * from BANK_MASTER LIMIT 1;")
        if (rows.empty) throw new SkipException('no DB entry found for the query')
        def row = rows[0]
        req()
                .pathParam(URL_PATH_PARAM_BANK_ID, row[Column.BANK_ID])
                .get().then()
                .body(
                        'bankId', equalTo(row[Column.BANK_ID] as Integer),
                        'bankName', equalTo(row[Column.BANK_NAME]),
                        'bankCode', equalTo(row[Column.BANK_CODE]),
                        'bankDisplayName', equalTo(row[Column.BANK_DISPLAY_NAME]),
                        'bankKey', equalTo(row[Column.BANK_KEY]),
                        'alipayBankCode', equalTo(row[Column.ALIPAY_CODE]),
                        'bankWebLogo', equalTo(row[Column.BANK_WEB_LOGO]),
                        'bankWapLogo', equalTo(row[Column.BANK_WAP_LOGO]),
//                        'status', equalTo(row[Column.STATUS]),
                        'bankMandate', equalTo(row[Column.MANDATE_TYPE]),
                        'extIfscCode', equalTo(row[Column.EXT_IFSC_CODE] as String),
//                        'standardBankCode', equalTo(row[Column.STANDARD_BANK_CODE]),
//                        'mandateNetBanking', equalTo(row[Column.MANDATE_NET_BANKING]),
//                        'mandateDebitCard', equalTo(row[Column.MANDATE_DEBIT_CARD]),
//                        'payMode', equalTo(row[Column.PAY_MODE]),
//                        'displayOrder', equalTo(row[Column.DISPLAY_ORDER] as Integer),
                )
    }
}