package scripts.api.instantrefund

import com.paytm.appconstants.Constants
import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.Group
import com.paytm.base.test.TestSetUp
import com.paytm.dto.OrderDTO
import com.paytm.dto.OrderFactory
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.Merchant
import com.paytm.utils.merchant.merchant.util.Order
import com.paytm.utils.merchant.merchant.util.Transaction
import com.paytm.utils.merchant.user.Account
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Link
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.assertj.core.api.Assertions
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static io.restassured.RestAssured.given
import static com.paytm.appconstants.Constants.Refund.REFUND_VIEW_CONSULT
import static org.hamcrest.Matchers.*

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
@Test(groups = ['refund', Group.PUBLIC_API])
@Link(name = 'doc', value = 'https://wiki.mypaytm.com/display/PGP/IMPS+Refund+Integration+Solution')
class RefundConsultView extends TestSetUp {

    final def req = {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                        .setBaseUri(PGP_HOST)
                        .setBasePath(REFUND_VIEW_CONSULT)
                        .build()
        )
    }

    final def root = {
        [
                "head": [
                        "mid"      : null,
                        "requestId": new Random().nextLong().abs() as String,
                        "tokenType": "SSO",
                        "token"    : user()?.tokens?.getAt('sso')?.id,
                ],
                "body": [
                        "refundAmount": [
                                "currency": "INR",
                                "value"   : null
                        ],
                        "orderId"     : null,
                        "channelId"   : "WEB"
                ]
        ]
    }

    private final Account validUpiAcc = new Account('valid upiAccountId')
    private final Account validAcc = new Account('003100100030871', 'BACB0000003', 'HDFC', 'pulkit', 'aggarwal')

    private final static ThreadLocal<Merchant> m = new ThreadLocal<>()
    private final static ThreadLocal<Order> o = new ThreadLocal<>()
    private final static ThreadLocal<Transaction> t = new ThreadLocal<>()

    private final static ResponseSpecification rootSchema = new ResponseSpecBuilder()
            .expectStatusCode(isIn(200))
            .expectContentType(ContentType.JSON)
            .expectBody('head', instanceOf(Object.class))
            .expectBody('body', instanceOf(Object.class))
            .build()

    private final static ResponseSpecification headSchema = new ResponseSpecBuilder()
            .rootPath('head')
            .expectBody('responseTimestamp', instanceOf(String.class))
            .build()

    private final static ResponseSpecification bodySchema = new ResponseSpecBuilder()
            .rootPath('body')
            .expectBody('resultInfo', instanceOf(Object.class))
            .expectBody('refundConsultViews', instanceOf(Object.class))
            .build()

    private final static ResponseSpecification consultViewSchema = new ResponseSpecBuilder()
            .rootPath('body.refundConsultViews')
            .expectBody('', hasSize(3))
            .expectBody('destination[0]', equalTo('TO_SOURCE'))
            .expectBody('destination[1]', equalTo('TO_VPA'))
            .expectBody('destination[2]', equalTo('TO_INSTANT'))
            .build()

    private final static ResponseSpecification success = new ResponseSpecBuilder()
            .rootPath('body.resultInfo')
            .expectBody('resultStatus', equalTo('S'))
            .expectBody('resultCode', equalTo('SUCCESS'))
            .expectBody('resultCodeId', equalTo('0000'))
            .expectBody('resultMsg', equalTo('Success'))
            .build()

    private final static ResponseSpecification instantRefundApplicable = new ResponseSpecBuilder()
            .rootPath('body.refundConsultViews')
            .expectBody('enableStatus[0]', equalTo(true))
            .expectBody('enableStatus[2]', equalTo(true))
            .build()

    @BeforeMethod
    void orderSetUp(Method method, ITestResult testResult) {
        try {
        if (method.name in ["testWhenRefundAccountIsAddedOnTheUserAfterCCTxn"]) {

                Constants.MerchantType merchantType = Constants.MerchantType.PGOnly
                OrderDTO orderDTO = new OrderFactory.OffLineTxn(merchantType, 'merchant4', user().tokens['sso'].id)
                        .setPAYMENT_DETAILS('4718650100010336|882|052026')
                        .setPAYMENT_TYPE_ID("CC")
                        .build()
                Response response = PGPHelpers.executeProcessTransaction(orderDTO)
                String finalResponse = PGPHelpers.completeHDFCCardTxn(response.asString())
                Assertions.assertThat(finalResponse).contains("Processing")
                def order = new Order(orderDTO.getORDER_ID(), merchantType.id, merchantType.key)
                assertion.apply(pageWait.apply({ order.isSuccess() as Boolean }))
                Transaction txn = order.subTransactions[0]
                m.set(new Merchant(merchantType.id, merchantType.key, false))
                o.set(order)
                t.set(txn)
            } else if (method.name in ["testWhenRefundAccountIsAddedOnTheUserAfterNBTxn"]) {
                Constants.MerchantType merchantType = Constants.MerchantType.PGOnly
                OrderDTO orderDTO = new OrderFactory.OffLineTxn(merchantType, 'merchant4', user().tokens['sso'].id)
                        .setBANK_CODE("ICICI")
                        .setPAYMENT_TYPE_ID("NB")
                        .build()
                Response response = PGPHelpers.executeProcessTransaction(orderDTO)
                String finalResponse = PGPHelpers.completeICICINBTxn(response.asString())
                Assertions.assertThat(finalResponse).contains("Processing")
                def order = new Order(orderDTO.getORDER_ID(), merchantType.id, merchantType.key)
                assertion.apply(pageWait.apply({ order.isSuccess() as Boolean }))
                Transaction txn = order.subTransactions[0]
                m.set(new Merchant(merchantType.id, merchantType.key, false))
                o.set(order)
                t.set(txn)

            } else if (method.name in ["testWhenRefundAccountIsAddedOnTheUserAfterHybridTxnWithDC"]) {
                Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID
                def txnAmount = "2.00"
                user().wallets['main'].balance = 1
                OrderDTO orderDTO = new OrderFactory.OffLineTxn(merchantType, 'merchant4', user().tokens['sso'].id)
                        .setPAYMENT_DETAILS('5103720068758083|882|052026')
                        .setPAYMENT_TYPE_ID("DC")
                        .setTXN_AMOUNT(txnAmount)
                        .setAddMoney("0")
                        .build()
                Response response = PGPHelpers.executeProcessTransaction(orderDTO)
                String finalResponse = PGPHelpers.completeHDFCCardTxn(response.asString())
                Assertions.assertThat(finalResponse).contains("Processing")
                def order = new Order(orderDTO.getORDER_ID(), merchantType.id, merchantType.key)
                assertion.apply(pageWait.apply({ order.isSuccess() as Boolean }))
                Transaction txn = order.subTransactions[0]
                m.set(new Merchant(merchantType.id, merchantType.key, false))
                o.set(order)
                t.set(txn)

            } else {
                Constants.MerchantType merchantType = Constants.MerchantType.PGOnly
                OrderDTO orderDTO = new OrderFactory.OffLineTxn(merchantType, 'enhancedweb', user().tokens['sso'].id)
                        .setPAYMENT_DETAILS('5103720068758083|882|052026')
                        .setPAYMENT_TYPE_ID("DC")
                        .build()
                Response response = PGPHelpers.executeProcessTransaction(orderDTO)
                String finalResponse = PGPHelpers.completeHDFCCardTxn(response.asString())
                Assertions.assertThat(finalResponse).contains("Processing")
                def order = new Order(orderDTO.getORDER_ID(), merchantType.id, merchantType.key)
                assertion.apply(pageWait.apply({ order.isSuccess() as Boolean }))
                Transaction txn = order.subTransactions[0]

                m.set(new Merchant(merchantType.id, merchantType.key, false))
                o.set(order)
                t.set(txn)
            }
        } catch (Throwable e ) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @com.paytm.utils.merchant.merchant.util.annotations.Merchant({ it.preferences.impsInstantRefund.enabled == true })
    @AUser(edit = true)
    @Test
    void testWhenRefundAccountIsAddedOnTheUserAfterDCTxn() {
        def m = m.get(), o = o.get(), t = t.get()
        user().accounts.clear()
        assert user().accounts.add(validAcc)
        def root = root()
        root.body.orderId = o.id
        root.body.refundAmount.value = t.amt
        root.head << [mid: m.id]
        req().body(root).post()
                .then().spec(rootSchema).spec(bodySchema).spec(headSchema).spec(success).spec(consultViewSchema).spec(instantRefundApplicable)
                .body("body.refundConsultViews.find { it.destination == 'TO_INSTANT' }.refundAllocatedInfos[0].assetId", equalTo(user().accounts[0].id))
    }

    @com.paytm.utils.merchant.merchant.util.annotations.Merchant({ it.preferences.impsInstantRefund.enabled == true })
    @AUser(edit = true)
    @Test
    void testWhenRefundAccountIsAddedOnTheUserAfterNBTxn() {
        def m = m.get(), o = o.get(), t = t.get()
        user().accounts.clear()
        assert user().accounts.add(validAcc)
        def root = root()
        root.body.orderId = o.id
        root.body.refundAmount.value = t.amt
        root.head << [mid: m.id]
        req().body(root).post()
                .then().spec(rootSchema).spec(bodySchema).spec(headSchema).spec(success).spec(consultViewSchema).spec(instantRefundApplicable)
                .body("body.refundConsultViews.find { it.destination == 'TO_INSTANT' }.refundAllocatedInfos[0].assetId", equalTo(user().accounts[0].id))
    }

    @com.paytm.utils.merchant.merchant.util.annotations.Merchant({ it.preferences.impsInstantRefund.enabled == true })
    @AUser(edit = true)
    @Test
    void testWhenRefundAccountIsAddedOnTheUserAfterCCTxn() {
        def m = m.get(), o = o.get(), t = t.get()
        user().accounts.clear()
        assert user().accounts.add(validAcc)
        def root = root()
        root.body.orderId = o.id
        root.body.refundAmount.value = t.amt
        root.head << [mid: m.id]
        req().body(root).post()
                .then().spec(rootSchema).spec(bodySchema).spec(headSchema).spec(success).spec(consultViewSchema)
                .root('body.refundConsultViews')
                .body('enableStatus[0]', equalTo(true))
                .body('enableStatus[2]', equalTo(false))
                .body('disableReason[2]', equalTo("NOT_SUPPORT_INSTANT_REFUND"))
    }

    //TODO cannot be tested per Pulkit
//    @com.paytm.utils.merchant.merchant.util.annotations.Merchant({ it.preferences.impsInstantRefund.enabled == true })
//    @AUser(edit = true)
//    @Test(enabled = false)
    void testWhenRefundAccountIsAddedOnTheUserAfterHybridTxnWithDC() {
        def m = m.get(), o = o.get(), t = t.get()
        user().accounts.clear()
        assert user().accounts.add(validAcc)
        def root = root()
        root.body.orderId = o.id
        root.body.refundAmount.value = t.amt
        root.head << [mid: m.id]
        req().body(root).post()
                .then().spec(rootSchema).spec(bodySchema).spec(headSchema).spec(success).spec(consultViewSchema).spec(instantRefundApplicable)
                .root('body.refundConsultViews')
                .body('refundAllocatedInfos[0]', hasSize(2))
                .body('refundAllocatedInfos[0].targetPayMethod[1]', equalTo('BALANCE'))
                .body('refundAllocatedInfos[2]', hasSize(2))
                .body('refundAllocatedInfos[2].targetPayMethod[1]', equalTo('BALANCE'))
                .body('refundAllocatedInfos[2].assetId', equalTo(user().accounts[0]))
    }

    @com.paytm.utils.merchant.merchant.util.annotations.Merchant({ it.preferences.impsInstantRefund.enabled == true })
    @AUser(edit = true)
    @Test
    void testWhenRefundAccountIsNotAddedOnTheUser() {
        def m = m.get(), o = o.get(), t = t.get()
        user().accounts.clear()
        def root = root()
        root.body.orderId = o.id
        root.body.refundAmount.value = t.amt
        root.head << [mid: m.id]
        req().body(root).post()
                .then().spec(rootSchema).spec(bodySchema).spec(headSchema).spec(success).spec(consultViewSchema)
                .root('body.refundConsultViews')
                .body('enableStatus[0]', equalTo(true))
                .body('enableStatus[2]', equalTo(false))
    }

    @com.paytm.utils.merchant.merchant.util.annotations.Merchant({ it.preferences.impsInstantRefund.enabled == true })
    @AUser(edit = true)
    @Test
    void testWhenRefundAmountIsGreaterThanTxnAmount() {
        def m = m.get(), o = o.get(), t = t.get()
        user().accounts.clear()
        assert user().accounts.add(validAcc)
        def root = root()
        root.body.orderId = o.id
        root.body.refundAmount.value = (t.amt as double) + 1
        root.head << [mid: m.id]
        req().body(root).post()
                .then().spec(rootSchema).spec(headSchema)
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'))
                .body('resultCodeId', equalTo('2008'))
                .body('resultCode', equalTo("REFUND_AMOUNT_EXCEED_PAY_AMOUNT"))
    }
}