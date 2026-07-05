package scripts.api.instantrefund

import com.paytm.apphelpers.PGPHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.dto.PaymentDTO
import com.paytm.framework.reporting.Owners
import com.paytm.utils.merchant.merchant.util.OrderV2
import com.paytm.utils.merchant.merchant.util.RefundV2
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.Account
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.lang.reflect.Method

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.Refund.ASYNC_REFUND
import static com.paytm.utils.merchant.util.PGPUtil.getChecksum
import static groovy.json.JsonOutput.toJson
import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Pulkit")
class AsyncRefund extends TestSetUp {

    final def req = {
        given(
                new RequestSpecBuilder()
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
                        signature: null
                ],
                body: [
                        refId               : new Random().nextLong().abs() as String,
                        txnType             : 'REFUND',
                        comments            : 'Instant Refund',
                        orderId             : m().orders.last().id,
                        txnId               : m().orders.last().transaction.id,
                        mid                 : m().id,
                        refundAmount        : m().orders.last().amt,
                        preferredDestination: 'TO_INSTANT',
                        token               : null
                ]
        ]
    }

    @BeforeMethod
    void orderSetUp(Method method, ITestResult testResult) {
        try {
            assert m().orders.add(new OrderV2(1))
            m().orders.last().payUsingNB()
            assertion.apply(txnStatusWait.apply({ m().orders.last().transaction.status == 'TXN_SUCCESS' }))
        }
     catch(Throwable e) {
        testResult.setStatus(ITestResult.SKIP);
        testResult.setThrowable(new SkipException(method.getName(), e));
    }
    }

    @Merchant(edit = true, value = { it.payModes.contains('nb') })
    @AUser
    @Test
    void testSuccessfulInstantRefundWithAccount() {
        def root = root()
        root.body.token = new Account('003100100030871', 'BACB0000003', 'HDFC').token.tap { assert it }
        root.head.signature = getChecksum(m().key, toJson(root.body))
        req().body(root).post()
                .then()
                .root("body.resultInfo")
                .body("resultMsg", equalTo("Refund request was raised for this transaction. But it is pending state"),
                "resultCode", equalTo('601'))
        assert m().orders.last().refunds.add(new RefundV2(root.body.refId))
        assertion.apply(refundStatusWait.apply({ m().orders.last().refunds.last().status == 'TXN_SUCCESS' }))
    }

    @Merchant(edit = true, value = { it.payModes.contains('nb') })
    @AUser
    @Test
    void testSuccessfulInstantRefundWithVPA() {
        def root = root()
        root.body.token = new Account(new PaymentDTO().getVpa()).token.tap { assert it }
        root.head.signature = getChecksum(m().key, toJson(root.body))
        req().body(root).post()
                .then()
                .root("body.resultInfo")
                .body("resultMsg", equalTo("Refund request was raised for this transaction. But it is pending state"),
                "resultCode", equalTo('601'))
        assert m().orders.last().refunds.add(new RefundV2(root.body.refId))
        assertion.apply(refundStatusWait.apply({ m().orders.last().refunds.last().status == 'TXN_SUCCESS' }))
    }
}
