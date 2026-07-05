package scripts.api.theia

import com.paytm.LocalConfig
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import io.qameta.allure.Owner
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.FETCH_PAYMENT_PROMOTION_ATTRIBUTES
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@Owner("Deepak")
class FetchPaymentPromotionAttributes extends TestSetUp {

    final def reqBldr = {
        new RequestSpecBuilder()
                .addRequestSpecification(reqSpec())
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PAYMENT_PROMOTION_ATTRIBUTES)
    }

    final def req = { given(reqBldr().build()) }

    final def root = {
        [
                head: [
                        requestId       : UUID.randomUUID().toString(),
                        requestTimestamp: System.currentTimeMillis() as String,
                        clientId        : UUID.randomUUID().toString(),
                        version         : 'v1',
                        tokenType       : 'SSO',
                        token           : PGPHelpers.createJsonWebToken([mid: m().id], PGPHelpers.ISSUER.ts, LocalConfig.JWT_KEY)
                ],
                body: [:]
        ]
    }

    @Merchant
    @Test
    void testSuccess() {
        def root = root()
        req().body(root).post().then()
                .body('head', isA(Object.class),
                'body', isA(Object.class))
                .root('head')
                .body('requestId', nullValue(),
                'responseTimestamp', not(isEmptyOrNullString()),
                'version', equalTo(root.head.version))
                .root('body')
                .body('paymethodInfo', isA(List.class),
                'cardNetworkInfo', isA(List.class),
                'bankDetails', isA(List.class),
                'resultInfo', isA(Object.class))
                .root('body.resultInfo')
                .body('resultStatus', equalTo('S'),
                'resultCodeId', nullValue(),
                'resultCode', equalTo('SUCCESS'),
                'resultMsg', equalTo('Success'))
                .root('body.paymethodInfo')
                .body('payMethod', everyItem(isIn('BALANCE', 'NET_BANKING', 'CREDIT_CARD', 'DEBIT_CARD', 'IMPS', 'ATM', 'EMI', 'UPI', 'PAYTM_DIGITAL_CREDIT', 'PREPAID_CARD', 'PPBL', 'LOYALTY_POINT', 'EMI_DC', 'WALLET', 'ADVANCE_DEPOSIT_ACCOUNT', 'EMI_CARDLESS')),
                'payMethodName', everyItem(isIn('BALANCE', 'NET_BANKING', 'CREDIT_CARD', 'DEBIT_CARD', 'IMPS', 'ATM', 'EMI', 'UPI', 'PAYTM_DIGITAL_CREDIT', 'PREPAID_CARD', 'PPBL', 'LOYALTY_POINT', 'EMI_DC', 'WALLET', 'ADVANCE_DEPOSIT_ACCOUNT', 'EMI_CARDLESS')),
                'type', everyItem(isIn('Paytm', 'PG', 'AOA')))
                .body('findAll { it.payMethod != "UPI" }.subType', everyItem(nullValue()))
                .body('find { it.payMethod == "UPI" }.subType', containsInAnyOrder('UPI_CC', 'UPI_LITE'))
                .root('body.cardNetworkInfo')
                .body(
                'cardNetwork', everyItem(isIn('VISA', 'MASTER', 'AMEX', 'MAESTRO', 'DINERS', 'RUPAY', 'DISCOVER', 'BAJAJFN', 'BAJAJ')),
                'logoUrl', everyItem(not(isEmptyOrNullString())),
                'displayName', everyItem(isIn('VISA', 'MASTER', 'AMEX', 'MAESTRO', 'DINERS', 'RUPAY', 'DISCOVER', 'BAJAJFN', 'BAJAJ')))
                .root('body.bankDetails')
                .body('bankName', everyItem(isIn('ICICI', 'CITI', 'SBI', 'BOB', 'ANDHRA', 'AXIS', 'BBK', 'BOI', 'BOM', 'BOR', 'CANARA', 'CITIUB', 'CORP', 'DEUTS', 'FDEB', 'ALH', 'IDBI', 'IOB', 'INDS', 'ING', 'JKB', 'KTKB', 'KVB', 'Kotak Bank', 'LVB', 'OBPRF', 'PNB', 'RBS', 'SIB', 'SCB', 'SBH', 'SBM', 'SBT', 'SYNBK', 'TNMB', 'UNI', 'VJYA', 'YES', 'UBI', 'CSB', 'SBOP', 'SBJ', 'SBS', 'SBOI', 'DCB', 'RATN', 'CBI', 'INDB', 'DBS', 'DHAN', 'COSMOS', 'SVB', 'HSBC', 'UCO', 'STB', 'PSB', 'DENA', 'HDFC', 'BAJAJFN', 'PEDC', 'HDUS', 'HFDC', 'KreditBee', 'DIDS', 'DINS', 'NKMU', 'SAPS', 'RBAX', 'RRDC', 'IRAX')),
                'bankCode', everyItem(isIn('ICICI', 'CITI', 'SBI', 'BOB', 'ANDHRA', 'AXIS', 'BBK', 'BOI', 'BOM', 'BOR', 'CANARA', 'CITIUB', 'CORP', 'DEUTS', 'FDEB', 'ALH', 'IDBI', 'IOB', 'INDS', 'ING', 'JKB', 'KTKB', 'KVB', 'NKMB', 'LVB', 'OBPRF', 'PNB', 'RBS', 'SIB', 'SCB', 'SBH', 'SBM', 'SBT', 'SYNBK', 'TNMB', 'UNI', 'VJYA', 'YES', 'UBI', 'CSB', 'SBOP', 'SBJ', 'SBS', 'SBOI', 'DCB', 'RATN', 'CBI', 'INDB', 'DBS', 'DHAN', 'COSMOS', 'SVB', 'HSBC', 'UCO', 'STB', 'PSB', 'DENA', 'HDFC', 'BAJAJFN', 'PEDC', 'HDUS', 'HFDC', 'KBEE', 'DIDS', 'DINS', 'NIMU', 'SAPS', 'RBAX', 'RRDC', 'IRAX')),
                'logoUrl', everyItem(not(isEmptyOrNullString())),
                'displayName', everyItem(isIn('ICICI Bank', 'Citibank', 'State Bank of India', 'Bank of Baroda', 'Andhra Bank', 'Axis Bank', 'Bank of Bahrain and Kuwait', 'Bank of India', 'Bank of Maharashtra', 'Bank of Rajasthan', 'Canara Bank', 'City Union Bank', 'Corporation Bank', 'Deutsche Bank', 'Federal Bank', 'Allahabad Bank', 'IDBI Bank', 'Indian Overseas Bank', 'IndusInd Bank', 'ING Vysya Bank (now Kotak)', 'Jammu and Kashmir Bank', 'Karnataka Bank', 'Karur Vysya Bank', 'Kotak Bank', 'Lakshmi Vilas Bank', 'Oriental Bank of Commerce', 'Punjab National Bank', 'Royal Bank of Scotland', 'South Indian Bank', 'Standard Chartered Bank', 'State Bank Of Hyderabad', 'State Bank of Mysore', 'State Bank of Travancore', 'Syndicate Bank', 'Tamilnad Mercantile Bank', 'Union Bank of India', 'Vijaya Bank', 'YES BANK', 'United Bank of India', 'Catholic Syrian Bank', 'State Bank of Patiala', 'State Bank of Bikaner and Jaipur', 'State Bank of Shaurashtra', 'State Bank of Indore', 'DCB Bank Limited', 'RBL Bank', 'Central Bank of India', 'Indian Bank', 'Development Bank of Singapore', 'Dhanlaxmi Bank', 'Cosmos Bank', 'Shamrao Vithal Bank', 'HSBC', 'UCO Bank', 'Saraswat Bank', 'Punjab & Sind Bank', 'Dena Bank', 'HDFC Bank', 'Bajaj Finserv EMI Card', 'PEDC', 'HDUS', 'HFDC', 'KreditBee', 'Digio Bank Direct Settlement', 'Digio Bank Normal Settlement', 'Kotak Bank', 'RAPS SBI Bank', 'RBL Amex', 'RBL RUPAY', 'RBL Amex')))
    }
}
