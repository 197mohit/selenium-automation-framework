package scripts.flows

import com.paytm.api.TxnStatus
import com.paytm.base.test.TestSetUp
import com.paytm.dto.OrderDTO
import com.paytm.framework.conditions.SoftAssertion
import com.paytm.pages.CashierPage
import com.paytm.pages.CashierPageEnhancedWAP
import com.paytm.pages.CheckoutPage
import com.paytm.pages.responsePage.ResponsePage
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.user.annotations.AUsers
import org.testng.annotations.Test

import static com.paytm.appconstants.Constants.ACCOUNT_NO_RETURNED_IN_FETCH_PPBL_BALANCE_API_MOCK
import static com.paytm.appconstants.Constants.PayMode
import static com.paytm.pages.responsePage.ResponsePage.Attribute

class EnhancedWAPDefaultMFFlowTest extends TestSetUp implements DefaultMFFlowTest {

    OrderDTO.Builder orderBuilder() {
        new OrderDTO.Builder()
                .setTHEME('enhancedwap')
                .setCHANNEL_ID("WAP")
                .setORDER_ID(UUID.randomUUID().toString())
                .setCUST_ID(UUID.randomUUID().toString())
                .setMID(m().id)
                .setMerchantKey(m().key)
                .setREQUEST_TYPE('DEFAULT_MF')
                .setTXN_AMOUNT("1.00")
                .setINDUSTRY_TYPE_ID("retail")
                .setWEBSITE("retail")
                .setAUTH_MODE("3D")
                .setAccountNumber(ACCOUNT_NO_RETURNED_IN_FETCH_PPBL_BALANCE_API_MOCK)//this should be same as the account no. returned from fetch ppbl balance API mock which is matched against account no. passed in order request; otherwise order will fail.
                .setBANK_CODE('PPBL')
    }

    CashierPage cashierPage = new CashierPageEnhancedWAP()
    ResponsePage responsePage = new ResponsePage()

    private static class Status {
        static final String SUCCESS = 'TXN_SUCCESS'
        static final String FAILURE = 'TXN_FAILURE'
        static final String PENDING = 'PENDING'
    }

    @Override
    @Merchant({ it.payModes.every { it == 'ppbl' } })
    @AUser(ppbl = "true")
    @Test
    void 'test order is successful when payment is done by ppbl'() {
        OrderDTO order = orderBuilder().build()
        new CheckoutPage().createOrder(order)
        cashierPage.login(user().mobile, user().password)
        cashierPage.payBy(PayMode.PPBL)
        pageWait.apply(responsePage.hasLoaded())
        SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals(Status.SUCCESS),
                responsePage.get(Attribute.RESPCODE).equals('01')
        )
        sAssert.eval()
        new TxnStatus(order.getMID(), order.getORDER_ID())
                .executeUntilNotPending()
                .validateStatus(Status.SUCCESS)
                .AssertAll()
    }

    @Override
    @Merchant({ it.payModes.every { it == 'ppbl' } })
    @AUsers([
            @AUser(ppbl = "true", edit = true),
            @AUser(ppbl = "true"),
    ])
    @Test
    void 'test order is unsuccessful when account number passed in the request does not match with account number associated with mobile no used to login'() {
        OrderDTO order = orderBuilder()
                .setAccountNumber('91' + user(0).mobile)
                .build()
        new CheckoutPage().createOrder(order)
        cashierPage.login(user(1).mobile, user(1).password)
        cashierPage.payBy(PayMode.PPBL)
        pageWait.apply(responsePage.hasLoaded())
        SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals(Status.FAILURE),
                responsePage.get(Attribute.RESPCODE).equals('507')
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.execute()
        txnStatus.validateStatus(Status.PENDING)
                .AssertAll()
    }

    @Override
    @Merchant({ it.payModes.every { it == 'ppbl' } })
    @AUser(ppbl = "true")
    @Test
    void 'test order is unsuccessful when account number is not passed in request'() {
        OrderDTO order = orderBuilder()
                .setAccountNumber('')
                .build()
        new CheckoutPage().createOrder(order)
        SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals(Status.FAILURE),
                responsePage.get(Attribute.RESPCODE).equals('283')
        )
        sAssert.eval()
        new TxnStatus(order.getMID(), order.getORDER_ID())
                .executeUntilNotPending()
                .validateStatus(Status.FAILURE)
                .AssertAll()
    }
}
