package scripts.api.merchantStatus

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.conditions.SoftAssertion
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PGPUtil
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import scripts.api.theia.InitiateTransaction
import scripts.api.theia.ProcessTransactionAPI

import java.text.DecimalFormat

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.TXNSTATUS_CHECKSUM
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasKey

@Owner("Deepak")
class GetTxnStatus extends TestSetUp {

    final def req = { mid, orderId, checksum ->
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(TXNSTATUS_CHECKSUM)
                        .addParam('JsonData', "{\"MID\":\"$mid\",\"ORDERID\":\"$orderId\",\"CHECKSUMHASH\":\"$checksum\"}")
                        .build()
        )
    }

    def success = new ResponseSpecBuilder()
            .expectBody('STATUS', equalTo('TXN_SUCCESS'))
            .build()

    def initTxn = new InitiateTransaction()
    def pt = new ProcessTransactionAPI()

    @Merchants([
            @Merchant(value = {
                it.preferences.mlv.enabled == true && it.aggregator == false && it.payModes.contains('ppi') && it.peonEnabled
            }, edit = true),
            @Merchant({ it.aggregator == true })
    ])
    @AUser(edit = true)
    @Test(description = 'test able to access details using child merchant')
    void testAbleToAccessDetailsUsingChildMerchant() {
        def txnToken
        def orderId
        def totalTxnAmt = 10D
        def txnAmt = 2D
        def mlvTxnAmt = totalTxnAmt - txnAmt
        InitTxn:
        {
            def root = initTxn.root(), req = initTxn.req
            orderId = root.body.orderId
            root.body.txnAmount.value = txnAmt
            root.body.offlineFlow = true
            root.body.orderPricingInfo = [
                    orderTotalAmount: [value: totalTxnAmt as String, currency: 'INR'],
                    amountInfoList  : [
                            [
                                    amountType: 'MLV_AMOUNT',
                                    amount    : [value: mlvTxnAmt as String, currency: 'INR'],
                            ]
                    ]
            ]
            root.body.aggMid = m(1).id
            root.body.aggType = 'ORDER_CREATOR'
            root.head.signature = getChecksum(m(1).key, toJson(root.body))
            txnToken = req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        }
        PTC:
        {
            def root = pt.root(), req = pt.req
            user().wallets['main'].balance = txnAmt
            root.head.txnToken = txnToken
            root.body.orderId = orderId
            root.body.paymentMode = 'BALANCE'
            root.body.websiteName = 'retail'
            root.body.website = 'retail'
            req().body(root).post().then().spec(results.success as ResponseSpecification)
        }
        req(m(0).id, orderId, PGPUtil.getChecksum(m(0).key, [MID: m(0).id, ORDERID: orderId] as TreeMap<String, String>)).get().then().spec(success)
                .body('TXNAMOUNT', equalTo(txnAmt.toBigDecimal().round(2) as String))
                .body('', hasKey('ADDITIONAL_PARAM'))
        assertion.apply(peonWait.apply({ peons[orderId] != null }))
        def peon = peons[orderId]
        SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                peon.txnAmt().equals(new DecimalFormat('0.00').format(txnAmt)),
        )
        sAssert.eval()
    }
}
