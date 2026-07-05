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
import static com.paytm.appconstants.Constants.MappingService.GET_BANK_DETAILS_LIST_FROM_BANK_CODES
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class GetBankDetailsListFromBankCodesAPITest extends TestSetUp {

    private static final String URL_PATH_PARAM_BANK_CODES = 'bankCodes'

    private final RequestSpecBuilder reqBldr() {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .addFilters([schemaFilter])
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_BANK_DETAILS_LIST_FROM_BANK_CODES)
    }

    private final RequestSpecification req() {
        given(reqBldr().build())
    }

    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            responseSpec.spec(
                    new ResponseSpecBuilder()
                            .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/get-bank-details-list-from-bank-codes-schema.json'))
                            .build()
            )
            return ctx.next(requestSpec, responseSpec)
        }
    }

    private static interface Column {
        String BANK_ID = 'BANK_ID'
        String BANK_NAME = 'BANK_NAME'
        String BANK_CODE = 'BANK_CODE'
        String BANK_DISPLAY_NAME = 'BANK_DISPLAY_NAME'
        String BANK_KEY = 'BANK_KEY'
        String ALIPAY_CODE = 'ALIPAY_CODE'
        String BANK_WAP_LOGO = 'BANK_WAP_LOGO'
        String BANK_WEB_LOGO = 'BANK_WEB_LOGO'
        String STATUS = 'STATUS'
        String MANDATE_TYPE = 'MANDATE_TYPE'
        String MANDATE_NET_BANKING = 'MANDATE_NET_BANKING'
        String MANDATE_DEBIT_CARD = 'MANDATE_DEBIT_CARD'
        String STANDARD_BANK_CODE = 'STANDARD_BANK_CODE'
        String PAY_MODE = 'PAY_MODE'
        String DISPLAY_ORDER = 'DISPLAY_ORDER'
        String EXT_IFSC_CODE = 'EXT_IFSC_CODE'
    }

    @Test
    void testSuccess() {
        List<Map<String, Object>> rows = DbQueriesUtil.selectFromPGPDB("SELECT * FROM BANK_MASTER WHERE STATUS = 1 limit 1;")
        if (rows.empty) throw new SkipException('no DB entry found for active bank')
        def row = rows[0]
        req().pathParam(URL_PATH_PARAM_BANK_CODES, row[Column.BANK_CODE]).get().then()
                .root('response')
                .body('resultCode', equalTo('00000'),
                        'resultStatus', equalTo('S'),
                        'messaage', equalTo('Success'))
                .root('')
                .body('bankIds', not(isEmptyOrNullString()))
                .body('bankMasterDetailsList', not(emptyIterable()))
                .body('notFound', emptyIterable())
                .root('bankMasterDetailsList[0]')
                .body(
                        'bankId', equalTo(row[Column.BANK_ID] as Integer),
                        'bankName', equalTo(row[Column.BANK_NAME] as String),
                        'bankCode', equalTo(row[Column.BANK_CODE] as String),
                        'bankDisplayName', equalTo(row[Column.BANK_DISPLAY_NAME] as String),
                        'bankKey', equalTo(row[Column.BANK_KEY] as String),
                        'alipayBankCode', equalTo(row[Column.ALIPAY_CODE] as String),
                        'bankWebLogo', equalTo(row[Column.BANK_WEB_LOGO] as String),
                        'bankWapLogo', equalTo(row[Column.BANK_WAP_LOGO] as String),
                        'status', equalTo(row[Column.STATUS] as Boolean),
                        'bankMandate', equalTo(row[Column.MANDATE_TYPE] as String),
                        'standardBankCode', equalTo(row[Column.STANDARD_BANK_CODE] as String),
                        'mandateNetBanking', equalTo(row[Column.MANDATE_NET_BANKING] as Boolean),
                        'mandateDebitCard', equalTo(row[Column.MANDATE_DEBIT_CARD] as Boolean),
                        'payMode', equalTo(row[Column.PAY_MODE] as String),
                        'extIfscCode', equalTo(row[Column.EXT_IFSC_CODE] as String),
//                        'displayOrder', equalTo(row[Column.DISPLAY_ORDER] as String),
                )
    }
}