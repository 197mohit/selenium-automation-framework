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
import org.testng.SkipException
import org.testng.annotations.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.not

class LinkBasedPaymentNativeSTTest extends TestSetUp implements LinkPaymentsFlowTest {
    //old link cases comment out
/*
    private static final String RESP_CODE = "RESPCODE"
    private static final String LINK_TYPE = "LINK_TYPE"
    private static final String SUCCESS = "01"
    private static final String FAILURE = "227"
    private static final String THEME = "enhancedweb"
    private static final String MID_PLACEHOLDER = "{mid}"
    private static final String ORDER_ID_PLACEHOLDER = "{orderId}"
    private static final String TXN_TOKEN_PLACEHOLDER = "{txnToken}"
    private static final String CORRECT_ACCOUNT_NO = "7777777777"
    private static final String WRONG_ACCOUNT_NO = "7777777770"
    private static final String LINK_ID_NOT_GENERATED = "Link Id not generated properly"

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is successful when payment is done by nb"() {
        String linkId = new LinkBasedService(m().id, m().key, "2").execute().jsonPath().getString("body.linkId")
        if (linkId == null) throw new SkipException(LINK_ID_NOT_GENERATED)
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentNativeST(m().id, m().key, orderId, linkId, CORRECT_ACCOUNT_NO).execute().jsonPath().getString("body.txnToken")
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
                .body('', not(hasKey(LINK_TYPE)))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is successful when payment is done by dc"() {
        String linkId = new LinkBasedService(m().id, m().key, "2").execute().jsonPath().getString("body.linkId")
        if (linkId == null) throw new SkipException(LINK_ID_NOT_GENERATED)
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentNativeST(m().id, m().key, orderId, linkId, CORRECT_ACCOUNT_NO).execute().jsonPath().getString("body.txnToken")
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
                .body('', not(hasKey(LINK_TYPE)))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is successful when payment is done by upi"() {
        String linkId = new LinkBasedService(m().id, m().key, "2").execute().jsonPath().getString("body.linkId")
        if (linkId == null) throw new SkipException(LINK_ID_NOT_GENERATED)
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentNativeST(m().id, m().key, orderId, linkId, CORRECT_ACCOUNT_NO).execute().jsonPath().getString("body.txnToken")
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
                .body('', not(hasKey(LINK_TYPE)))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId() })
    @Test
    void "test order is successful when wrong account no is passed and validateAccountNumber = true and allowUnverifiedAccount = true"() {
        String linkId = new LinkBasedService(m().id, m().key, "2").execute().jsonPath().getString("body.linkId")
        if (linkId == null) throw new SkipException(LINK_ID_NOT_GENERATED)
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentNativeST(m().id, m().key, orderId, linkId, WRONG_ACCOUNT_NO, "true", "true").execute().jsonPath().getString("body.txnToken")
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
                .body('', not(hasKey(LINK_TYPE)))
    }

    @Override
    @Merchant({ it.id == Constants.MerchantType.PGOnly.getId() })
    @Test
    void "test order is unsuccessful when wrong account no is passed and validateAccountNumber = true and allowUnverifiedAccount = false"() {
        String linkId = new LinkBasedService(m().id, m().key, "2").execute().jsonPath().getString("body.linkId")
        if (linkId == null) throw new SkipException(LINK_ID_NOT_GENERATED)
        def orderId = UUID.randomUUID().toString()
        String txnToken = InitTxn.LinkBasedPaymentNativeST(m().id, m().key, orderId, linkId, WRONG_ACCOUNT_NO, "true", "false").execute().jsonPath().getString("body.txnToken")
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
                .body('', not(hasKey(LINK_TYPE)))
    }*/
}
