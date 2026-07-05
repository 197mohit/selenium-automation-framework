package scripts.flows

import com.paytm.LocalConfig
import com.paytm.api.LinkBasedService
import com.paytm.api.nativeAPI.InitTxn
import com.paytm.appconstants.Constants
import com.paytm.base.test.TestSetUp
import com.paytm.dto.PaymentDTO
import com.paytm.framework.core.DriverManager
import com.paytm.pages.CashierPage
import com.paytm.pages.CashierPageFactory
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import org.testng.annotations.Test

import static org.hamcrest.Matchers.equalTo

class LinkBasedPaymentInvoiceNativeSTTest extends TestSetUp implements LinkPaymentsFlowTest {

  /*  private static final String RESP_CODE = "RESPCODE"
    private static final String LINK_TYPE = "LINK_TYPE"
    private static final String INVOICE = "INVOICE"
    private static final String SUCCESS = "01"
    private static final String FAILURE = "227"
    private static final String THEME = "enhancedweb"
    private static final String MID_PLACEHOLDER = "{mid}"
    private static final String ORDER_ID_PLACEHOLDER = "{orderId}"
    private static final String TXN_TOKEN_PLACEHOLDER = "{txnToken}"
    private static final String CORRECT_ACCOUNT_NO = "7777777777"
    private static final String WRONG_ACCOUNT_NO = "7777777770"

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is successful when payment is done by nb"() {
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentInvoiceNativeST(m().id, m().key, orderId, UUID.randomUUID().toString(), CORRECT_ACCOUNT_NO).execute().jsonPath().getString("body.txnToken")
        DriverManager.getDriver().get(
                LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE
                        .replace(MID_PLACEHOLDER, m().id)
                        .replace(ORDER_ID_PLACEHOLDER, orderId)
                        .replace(TXN_TOKEN_PLACEHOLDER, txnToken)
        )
        CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
        cashierPage.waitUntilLoads()
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"))
        super.merchantCallback(orderId)
                .body(RESP_CODE, equalTo(SUCCESS))
                .body(LINK_TYPE, equalTo(INVOICE))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is successful when payment is done by dc"() {
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentInvoiceNativeST(m().id, m().key, orderId, UUID.randomUUID().toString(), CORRECT_ACCOUNT_NO).execute().jsonPath().getString("body.txnToken")
        DriverManager.getDriver().get(
                LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE
                        .replace(MID_PLACEHOLDER, m().id)
                        .replace(ORDER_ID_PLACEHOLDER, orderId)
                        .replace(TXN_TOKEN_PLACEHOLDER, txnToken)
        )
        CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
        cashierPage.waitUntilLoads()
        cashierPage.payBy(Constants.PayMode.DC)
        super.merchantCallback(orderId)
                .body(RESP_CODE, equalTo(SUCCESS))
                .body(LINK_TYPE, equalTo(INVOICE))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is successful when payment is done by upi"() {
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentInvoiceNativeST(m().id, m().key, orderId, UUID.randomUUID().toString(), CORRECT_ACCOUNT_NO).execute().jsonPath().getString("body.txnToken")
        DriverManager.getDriver().get(
                LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE
                        .replace(MID_PLACEHOLDER, m().id)
                        .replace(ORDER_ID_PLACEHOLDER, orderId)
                        .replace(TXN_TOKEN_PLACEHOLDER, txnToken)
        )
        CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
        cashierPage.waitUntilLoads()
        cashierPage.payBy(Constants.PayMode.UPI)
        super.merchantCallback(orderId)
                .body(RESP_CODE, equalTo(SUCCESS))
                .body(LINK_TYPE, equalTo(INVOICE))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @Test
    void "test order is successful when wrong account no is passed and validateAccountNumber = true and allowUnverifiedAccount = true"() {
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentInvoiceNativeST(m().id, m().key, orderId, UUID.randomUUID().toString(), WRONG_ACCOUNT_NO, "true", "true").execute().jsonPath().getString("body.txnToken")
        DriverManager.getDriver().get(
                LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE
                        .replace(MID_PLACEHOLDER, m().id)
                        .replace(ORDER_ID_PLACEHOLDER, orderId)
                        .replace(TXN_TOKEN_PLACEHOLDER, txnToken)
        )
        CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
        cashierPage.waitUntilLoads()
        cashierPage.payBy(Constants.PayMode.UPI)
        super.merchantCallback(orderId)
                .body(RESP_CODE, equalTo(SUCCESS))
                .body(LINK_TYPE, equalTo(INVOICE))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is unsuccessful when wrong account no is passed and validateAccountNumber = true and allowUnverifiedAccount = false"() {
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentInvoiceNativeST(m().id, m().key, orderId, UUID.randomUUID().toString(), WRONG_ACCOUNT_NO, "true", "false").execute().jsonPath().getString("body.txnToken")
        DriverManager.getDriver().get(
                LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE
                        .replace(MID_PLACEHOLDER, m().id)
                        .replace(ORDER_ID_PLACEHOLDER, orderId)
                        .replace(TXN_TOKEN_PLACEHOLDER, txnToken)
        )
        CashierPage cashierPage = CashierPageFactory.getCashierPage(THEME)
        cashierPage.waitUntilLoads()
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"))
        super.merchantCallback(orderId)
                .body(RESP_CODE, equalTo(FAILURE))
                .body(LINK_TYPE, equalTo(INVOICE))
    }*/
}
