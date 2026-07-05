package scripts.api.refund

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.Order
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchants
import com.paytm.utils.merchant.user.annotations.AUser
import groovy.json.JsonSlurper
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import scripts.api.theia.InitiateTransaction
import scripts.api.theia.ProcessTransactionAPI

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.Refund.ASYNC_REFUND
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Ankit")
class AsyncRefund extends TestSetUp {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addFilter(setSignatureFilter)
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(ASYNC_REFUND)
                        .build()
        )
    }

    final def root = {
        [
                head: [
                        channelId: 'WEB',
                        signature: '?'//M
                ],
                body: [
                        mid         : m().id,
                        orderId     : new Random().nextLong().abs() as String,
                        refId       : new Random().nextLong().abs() as String,
                        txnId       : new Random().nextLong().abs() as String,
                        refundAmount: 2 as String,
                        txnType     : 'REFUND',
                        comments    : '',
                        aggMid      : m().id,
                        aggType     : 'ORDER_CREATOR',
                ]
        ]
    }

    def setSignatureFilter = [filter: { FilterableRequestSpecification req, res, ctx ->
        def root = new JsonSlurper().parseText(req.getBody())
        root?.head?.with {
            if (it?.signature == '?') it.signature = getChecksum(m().key, toJson(root.body))
        }
        req.body(root)
        ctx.next(req, res)
    }] as Filter

    final def success = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('PENDING'))
            .expectBody('resultCode', equalTo('601'))
            .expectBody('resultMsg', equalTo('Refund request was raised for this transaction. But it is pending state'))
            .build()

    def initTxn = new InitiateTransaction()
    def pt = new ProcessTransactionAPI()

    @AUser(edit = true)
    @Merchants([
            @Merchant(value = {
                it.preferences.mlv.enabled == true && it.aggregator == false && it.payModes.contains('ppi')
            }, edit = true),
            @Merchant({ it.aggregator == true })
    ])
    @Test(description = 'test able to initiate full refund of mlv order using agg merchant')
    void testAbleToInitiateFullRefundOfMlvOrderUsingAggMerchant() {
        def txnToken
        def orderId
        def totalTxnAmt = 10
        def txnAmt = 2
        def mlvTxnAmt = totalTxnAmt - txnAmt
        InitTxn:
        {
            def root = initTxn.root(), req = initTxn.req
            orderId = root.body.orderId
            root.body.offlineFlow = true
            root.body.txnAmount.value = txnAmt
            root.body.aggMid = m(1).id
            root.body.aggType = 'ORDER_CREATOR'
            root.body.orderPricingInfo = [
                    orderTotalAmount: [value: totalTxnAmt as String, currency: 'INR'],
                    amountInfoList  : [
                            [
                                    amountType: 'MLV_AMOUNT',
                                    amount    : [value: mlvTxnAmt as String, currency: 'INR'],
                            ]
                    ]
            ]
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
            req(root.body.orderId).body(root).post().then().spec(results.success as ResponseSpecification)
        }
        def root = root()
        root.body.orderId = orderId
        root.body.txnId = new Order(orderId, m().id, m().key).subTransactions[0].id
        root.body.refundAmount = txnAmt
        root.body.aggMid = m(1).id
        root.body.aggType = 'ORDER_CREATOR'
        root.head.signature = getChecksum(m(1).key, toJson(root.body))
        req().body(root).post().then()
                .spec(success)
    }

    @AUser(edit = true)
    @Merchants([
            @Merchant(value = {
                it.preferences.mlv.enabled == true && it.aggregator == false && it.payModes.contains('ppi')
            }, edit = true),
            @Merchant({ it.aggregator == true })
    ])
    @Test(description = 'test able to initiate partial refund of mlv order using agg merchant')
    void testAbleToInitiatePartialRefundOfMlvOrderUsingAggMerchant() {
        def txnToken
        def orderId
        def totalTxnAmt = 10
        def txnAmt = 2
        def mlvTxnAmt = totalTxnAmt - txnAmt
        InitTxn:
        {
            def root = initTxn.root(), req = initTxn.req
            orderId = root.body.orderId
            root.body.offlineFlow = true
            root.body.txnAmount.value = txnAmt
            root.body.aggMid = m(1).id
            root.body.aggType = 'ORDER_CREATOR'
            root.body.orderPricingInfo = [
                    orderTotalAmount: [value: totalTxnAmt as String, currency: 'INR'],
                    amountInfoList  : [
                            [
                                    amountType: 'MLV_AMOUNT',
                                    amount    : [value: mlvTxnAmt as String, currency: 'INR'],
                            ]
                    ]
            ]
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
            req(root.body.orderId).body(root).post().then().spec(results.success as ResponseSpecification)
        }
        def root = root()
        root.body.orderId = orderId
        root.body.txnId = new Order(orderId, m().id, m().key).subTransactions[0].id
        root.body.refundAmount = txnAmt - 1
        root.body.aggMid = m(1).id
        root.body.aggType = 'ORDER_CREATOR'
        root.head.signature = getChecksum(m(1).key, toJson(root.body))
        req().body(root).post().then()
                .spec(success)
    }

    @AUser(edit = true)
    @Merchants([
            @Merchant(value = {
                it.preferences.mlv?.enabled == true && it.aggregator == false && it.payModes.contains('ppi')
            }, edit = true),
            @Merchant({ it.aggregator == true })
    ])
    @Test(description = 'test not able to initiate mlv order refund using agg merchant')
    void testNotAbleToInitiateMlvOrderRefundUsingAggMerchant() {
        def txnToken
        def orderId
        def totalTxnAmt = 10
        def txnAmt = 2
        def mlvTxnAmt = totalTxnAmt - txnAmt
        InitTxn:
        {
            def root = initTxn.root(), req = initTxn.req
            orderId = root.body.orderId
            root.body.offlineFlow = true
            root.body.txnAmount.value = txnAmt
            root.body.aggMid = m(1).id
            root.body.aggType = 'ORDER_CREATOR'
            root.body.orderPricingInfo = [
                    orderTotalAmount: [value: totalTxnAmt as String, currency: 'INR'],
                    amountInfoList  : [
                            [
                                    amountType: 'MLV_AMOUNT',
                                    amount    : [value: mlvTxnAmt as String, currency: 'INR'],
                            ]
                    ]
            ]
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
            req(root.body.orderId).body(root).post().then().spec(results.success as ResponseSpecification)
        }
        def root = root()
        root.body.orderId = orderId
        root.body.txnId = new Order(orderId, m().id, m().key).subTransactions[0].id
        root.body.refundAmount = txnAmt
        root.body.remove('aggMid')
        root.body.remove('aggType')
        root.head.signature = getChecksum(m(0).key, toJson(root.body))
        req().body(root).post().then()
                .rootPath('body.resultInfo')
                .body('resultStatus', equalTo('TXN_FAILURE'),
                'resultCode', equalTo('611'),
                'resultMsg', equalTo('Refund request is in process.'))
    }


}
