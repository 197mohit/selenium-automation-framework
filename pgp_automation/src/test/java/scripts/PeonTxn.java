package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.base.test.Group.Status.*;


@Owner("Deepak")
public class PeonTxn extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for successful PGOnly Txn.")
    //@Severity(SeverityLevel.BLOCKER)
    public void PGP_397_PeonPGonlyPass(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HYBRID, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
        //        peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()

        );
        sAssert.eval();
    }

    @Issue("PGP-18466")
    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for failed PGOnly Txn.")
    public void PGP_398_PeonPGonlyFail(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HYBRID, theme).setTXN_AMOUNT("99.95").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.respMsg().equals("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank"),
                peon.bankName().equals("HDFC Bank"),
                peon.payMode().equals("CC"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
             //   peon.respCode().equals("227"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_FAILURE"),
                peon.bankTxnId().equals(""),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for Successful WalletOnly Txn.")
    public void PGP_399_PeonWalletOnlyPass(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.NATIVE_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("5.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("WALLET"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals("WALLET"),
                peon.payMode().equals("PPI"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for failed WalletOnly Txn.")
    public void PGP_400_PeonWalletOnlyFail(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.NATIVE_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00").build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        WalletHelpers.modifyBalance(user, 1.00);
        WalletHelpers.validateBalance(user, 1.00);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("WALLET"),
                peon.respMsg().equals("Wallet balance Insufficient"),
                peon.bankName().equals("WALLET"),
                peon.payMode().equals("PPI"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("235"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_FAILURE"),
                peon.bankTxnId().equals(""),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Peon sent successfully for Hybrid transaction.")
    public void PGP_401_PeonHybridPass(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.NATIVE_HYBRID, theme, user).build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.hybridMoneyAmount().assertText(String.valueOf("1.00"));
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHILDTXNLIST", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals("HDFC Bank"),
                peon.payMode().equals("HYBRID"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.childTxnList().equals("").not(),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not()
        );
        sAssert.eval();
    }

    @Issue("PGP-18466")
    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for Failed hybrid Txn")
    public void PGP_402_PeonHybridFail(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setTXN_AMOUNT("100.99").build();
        double amountToBeRetainedInWallet = Double.valueOf("1.00");
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHILDTXNLIST", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("HDFC"),
                peon.respMsg().equals("Looks like OTP entered was incorrect. Please try again."),
                peon.bankName().equals("HDFC Bank"),
                peon.payMode().equals("HYBRID"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("227"),
                peon.childTxnList().equals("").not(),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_FAILURE"),
                peon.bankTxnId().equals(""),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not()
        );
        sAssert.eval();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for successful  add & pay Txn ")
    public void PGP_403_PeonAddNPayPass(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDNPAYPEON, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("WALLET"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals("WALLET"),
                peon.payMode().equals("PPI"),
                peon.custId().equals(orderDTO.getCUST_ID()),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for successful add money Txn ")
    public void PGP_404_PeonAddMoneyPass(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_SUCCESS")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("PPBL"),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals(""),
                peon.payMode().equals("NB"),
                peon.custId().equals("").not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("01"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_SUCCESS"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    @Issue("PGP-18547")
    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for failed add & pay Txn ")
    public void PGP_405_PeonAddNPayFail(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDNPAYPEON, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("100.98").build();
        WalletHelpers.modifyBalance(user, 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals("WALLET"),
                peon.respMsg().equals("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"),
                peon.bankName().equals("WALLET"),
                peon.payMode().equals("PPI"),
                peon.custId().equals(orderDTO.getCUST_ID()),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("227"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_FAILURE"),
                peon.bankTxnId().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }


    @Issue("PGP-18547")
    @Parameters({"theme"})
    @Test(description = "Validate Peon sent for failed add money Txn")
    public void PGP_406_PeonAddMoneyFail(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.NATIVE_HYBRID, theme, user)
                .setTXN_AMOUNT("60.10").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_FAILURE")
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals(""),
                peon.respMsg().equals("Payment failed due to a technical error. Please try after some time."),
                peon.bankName().equals(""),
                peon.payMode().equals(""),
                peon.custId().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.mercUnqRef().equals(""),
                peon.respCode().equals("810"),
                peon.txnId().equals("").not(),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.status().equals("TXN_FAILURE"),
                peon.bankTxnId().equals(""),
                peon.txnDateTime().equals("").not(),
                peon.txnDate().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    @Owner(Constants.Owner.GAURAV)
    @Parameters({"theme"})
    @Test(description = "Validate if peon is successful after transaction is successful")
    public void isPeonSuccessfulAfterTransaction(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {

        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.HIGH_PRIORITY_SMS, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.validateRespCode("01");
        responsePage.validateRespMsg("Txn Success");

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
        peon.status().equals("TXN_SUCCESS"),
        peon.respCode().equals("01"),
        peon.respMsg().equals("Txn Success"),
        peon.riskInfo().isNull(),
        peon.mId().equals(Constants.MerchantType.HIGH_PRIORITY_SMS.getId())
        );
        sAssert.eval();


        String grepcmd = "grep \"Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :\" '\"orderDTO.getORDER_ID()\"' /paytm/logs/notificationQueueHandler.log";
        String notificationQHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd);
        Assertions.assertThat(notificationQHandlerLogs).contains("Successfully produced SmsInfo in Kafka topic HIGH_PRIORITY_SMS for PhoneNo :");

        String grepcmds = "grep Peon Sent successfully to '\"MID\":\"orderDTO.getMID()\"' for '\"OrderID\":\"orderDTO.getORDER_ID()\"' /paytm/logs/communicationGateway.log";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmds);
        Assertions.assertThat(commLogs).contains("Peon Sent successfully to");
    }

    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-33443")
    @Parameters({"theme"})
    @Test(description = "Validate peon after risk reject transaction")
    public void validateRiskRejectMessage(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {

        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PEON_ENABLED, theme)
                .setTXN_AMOUNT("1.4")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        for (int i = 0; i < 5; i++) {
            cashierPage.buttonPGPayNow().click();
            cashierPage.waitUntilLoads();
        }

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("501");
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validateRespMsg("System Error.");
        responsePage.validateTxnAmount("1.40");

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
        peon.status().equals("TXN_FAILURE"),
        peon.respCode().equals("810"),
        peon.respMsg().equals("Payment failed due to a technical error. Please try after some time."),
        peon.riskInfo().isNull(),
        peon.mId().equals(Constants.MerchantType.PEON_ENABLED.getId()));
        sAssert.eval();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log ";
        String notificationQHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd);
        Assertions.assertThat(notificationQHandlerLogs).contains("Payment failed due to a technical error");

        String grepCommand = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log ";
        String communicationGatewayLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepCommand);
        Assertions.assertThat(communicationGatewayLogs).contains("Type=PeonSentSerivce");
        Assertions.assertThat(communicationGatewayLogs).contains("Payment failed due to a technical error");

    }


    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-33443")
    @Parameters({"theme"})
    @Test(description = "Validate peon after failure transaction")
    public void validatePeonForFailedTransaction(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {

        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PEON_ENABLED, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validateRespCode("227");
        responsePage.validateRespMsg("Payment failed due to a technical error. Please try after some time.");
        responsePage.validateTxnAmount("99.98");

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
        peon.status().equals("TXN_FAILURE"),
        peon.respCode().equals("227"),
        peon.respMsg().equals("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"),
        peon.riskInfo().isNull(),
        peon.mId().equals(Constants.MerchantType.PEON_ENABLED.getId()));
        sAssert.eval();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log ";
        String notificationQHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd);
        Assertions.assertThat(notificationQHandlerLogs).contains("Your payment has been declined by your bank.");

        String grepCommand = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log ";
        String communicationGatewayLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepCommand);
        Assertions.assertThat(communicationGatewayLogs).contains("Type=PeonSentSerivce");
        Assertions.assertThat(communicationGatewayLogs).contains("Your payment has been declined by your bank.");

    }

    @Owner(Constants.Owner.GAURAV)
    @Feature("PGP-33443")
    @Parameters({"theme"})
    @Test(description = "Validate peon after limit breach transaction")
    public void validatePeonForLimitBreach(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {

        CheckoutPage checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PEON_ENABLED, theme)
                .setTXN_AMOUNT("200001.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.validatedailybreachMsg();
        for (int i = 0; i < 5; i++) {
            cashierPage.buttonPGPayNow().click();
            cashierPage.waitUntilLoads();
        }

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validateRespCode("501");
        responsePage.validateRespMsg("Payment failed due to a technical error. Please try after some time.");
        responsePage.validateTxnAmount("200001.00");

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.status().equals("TXN_FAILURE"),
                peon.respCode().equals("810"),
                peon.respMsg().equals("Payment failed due to a technical error. Please try after some time."),
                peon.riskInfo().isNull(),
                peon.mId().equals(Constants.MerchantType.PEON_ENABLED.getId()));
        sAssert.eval();

        String grepcmdNotify = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log ";
        String notificationQHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmdNotify);
        Assertions.assertThat(notificationQHandlerLogs).contains("Payment failed due to a technical error.");

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log ";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commLogs).contains("Type=PeonSentSerivce");
        Assertions.assertThat(commLogs).contains("Payment failed due to a technical error. Please try after some time.");

    }

    @Owner(Constants.Owner.ROUNAK)
    @Feature("PGP-37891")
    @Parameters({"theme"})
    @Test(description = "Detailing out error msgs for merchant with RESULT_CODE_ENABLED")
    public void PGP_37891_DetailingOutErrorMsgsForMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Result_Code_Enable, theme)
                .setTXN_AMOUNT("99.95").build();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(paymentDTO.DEBIT_CARD_NUMBER);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("750")
                .validateRespMsg("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank")
                .validatePaymentMode("DC")
                .AssertAll();

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "TXNDATETIME", "TXNDATE", "RESULTCODE", "BANKTXNID"),   //CHECKSUMHASH BANKTXNID
                peon.bankName().equals(Constants.Bank.HDFCBANK.toString()),
                peon.currency().equals("INR"),
                peon.custId().equals(orderDTO.getCUST_ID()),
                peon.gatewayName().equals(Constants.Gateway.HDFC.toString()),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("750"),
                peon.respMsg().equals("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank"),
                peon.status().equals("TXN_FAILURE"),
                peon.resultCode().equals("FGW_USER_CANCELLED_OR_INCORRECT_OTP"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Owner(Constants.Owner.ROUNAK)
    @Feature("PGP-37891")
    @Parameters({"theme"})
    @Test(description = "Detailing out error msgs for merchant with disabled RESULT_CODE_ENABLED preference")
    public void PGP_37891_DetailingOutErrorResCodeDisable(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Result_Code_Disabled, theme)
                .setTXN_AMOUNT("99.95").build();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(paymentDTO.DEBIT_CARD_NUMBER);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("750")
                .validateRespMsg("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank")
                .validatePaymentMode("DC")
                .AssertAll();

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "RESULTCODE"),
                peon.bankName().equals(Constants.Bank.HDFCBANK.toString()),
                peon.currency().equals("INR"),
                peon.custId().equals(orderDTO.getCUST_ID()),
                peon.gatewayName().equals(Constants.Gateway.HDFC.toString()),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("750"),
                peon.respMsg().equals("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank"),
                peon.status().equals("TXN_FAILURE"),
                peon.resultCode().equals(""),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }

    @Owner(Constants.Owner.ROUNAK)
    @Feature("PGP-37891")
    @Parameters({"theme"})
    @Test(description = "Detailing out error msgs for merchant with RESULT_CODE_ENABLED not configured")
    public void PGP_37891_DetailingOutErrorResCodeNotConfig(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_HYBRID, theme)
                .setTXN_AMOUNT("99.95").build();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(paymentDTO.DEBIT_CARD_NUMBER);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("750")
                .validateRespMsg("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank")
                .validatePaymentMode("DC")
                .AssertAll();

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals(Constants.Bank.HDFCBANK.toString()),
                peon.currency().equals("INR"),
                peon.custId().equals(orderDTO.getCUST_ID()),
                peon.gatewayName().equals(Constants.Gateway.HDFC.toString()),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("750"),
                peon.respMsg().equals("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not()
        );
        sAssert.eval();
    }
    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-43016")
    @Parameters({"theme"})
    @Test(description = "Verify the UDF parameters in PEON body when REQUEST_TYPE=NATIVE and IS_UDF_ENABLED = ON")
    public void UDFParams_PEON_RequestType_NATIVE_FF4J_True(@Optional("true") Boolean isNativePlus) throws Exception{
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        User user = userManager.getForRead(Label.BASIC);
   //     WalletHelpers.modifyBalance(user, 10.00);
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1("test1");
        extendInfo.setUdf2("test2");
        extendInfo.setUdf3("test3");
        extendInfo.setMercUnqRef("");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("5.00")
                .setExtendInfo(extendInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .AssertAll();

        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log ");
        Assertions.assertThat(logs).contains("BUSINESS_FLOW_NAME=PeonSentServiceImp");
        Assertions.assertThat(logs).contains("udf_1=[test1]");
        Assertions.assertThat(logs).contains("udf_2=[test2]");
        Assertions.assertThat(logs).contains("udf_3=[test3]");
              
    }                   

    @Owner(Constants.Owner.VIDHI)
    @Feature("PGP-43016")
    @Test(description = "Verify the UDF parameters in PEON body when REQUEST_TYPE=NATIVE_MF and IS_UDF_ENABLED = ON")
    public void UDFParams_PEON_RequestType_NATIVE_MF_FF4J_True(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.MUTUAL_FUND;
        User user = userManager.getForWrite(Label.BASIC);
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1("test1");
        extendInfo.setUdf2("test2");
        extendInfo.setUdf3("test3");
        extendInfo.setMercUnqRef("");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setRequestType("NATIVE_MF")
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("2")
                .setExtendInfo(extendInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.MUTUAL_FUND, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "udf_1", "udf_2", "udf_3"),
                peon.currency().equals("INR"),
                peon.gatewayName().equals(Constants.Gateway.HDFC.toString()),
                peon.respMsg().equals("Txn Success"),
                peon.bankName().equals(Constants.Bank.HDFCBANK.toString()),
                peon.payMode().equals("DC"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.custId().isNull().not(),
                peon.mercUnqRef().equals(""),
                peon.txnId().isNull().not(),
                peon.bankTxnId().isNull().not(),
                peon.txnDateTime().isNull().not(),
                peon.txnDate().isNull().not(),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.udf1().equals("test1"),
                peon.udf2().equals("test2"),
                peon.udf3().equals("test3")
        );
        sAssert.eval();
    }
}
