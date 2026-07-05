package scripts;

import com.paytm.api.CreateLink;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.SATWIK_SHARMA;

@Owner("Gagandeep")
public class LinkBasePayment extends PGPBaseTest {

    @Parameters({"theme"})
    @Test(description = "Verify the PG only transaction for link based payments", groups = {"smoke"})
    public void PGPM_856_successfulLinkBasedPaymentThroughCC(@Optional("enhancedwap") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        String mid = MerchantType.PGOnly.getId();
        String txnAmount = "2";
        CreateLink createLink = new CreateLink(mid, "linkDescripion", 2.00);
        createLink.execute();
        String paymentLink = createLink.getPaymentLink();

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTP(user, paymentLink);

        CashierPage cashierPage = CashierPageFactory.getCashierPage("merchant");
        String orderId = linkPaymentLoginPage.textOrderId().getText();
        cashierPage.radioButtonWalletChecked().click();
        cashierPage.payBy(PayMode.CC);

        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(mid)
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the wallet only transaction for link based payments")
    public void PGPM_855_successfulLinkBasedPaymentThroughWalletOnly(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        String mid = MerchantType.WalletOnly.getId();
        String txnAmount = "2";
        CreateLink createLink = new CreateLink(mid, "linkDesccription", 2.00);
        createLink.execute();
        String paymentLink = createLink.getPaymentLink();

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTP(user, paymentLink);

        CashierPage cashierPage = CashierPageFactory.getCashierPage("merchant");
        String orderId = linkPaymentLoginPage.textOrderId().getText();
        cashierPage.radioButtonWalletChecked().click();
        cashierPage.payBy(PayMode.WALLET);

        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(mid)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the Hybrid transaction flow for link based payments")
    public void PGPM_857_successfulLinkBasedPaymentThroughHybridMerchant(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        WalletHelpers.modifyBalance(user, 2.00);
        String mid = MerchantType.Hybrid.getId();
        String txnAmount = "3";
        CreateLink createLink = new CreateLink(mid, "linkDescription", 3.00);
        createLink.execute();
        String paymentLink = createLink.getPaymentLink();

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTP(user, paymentLink);

        CashierPage cashierPage = CashierPageFactory.getCashierPage("merchant");
        String orderId = linkPaymentLoginPage.textOrderId().getText();
        cashierPage.payBy(PayMode.CC);

        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(mid)
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(txnAmount) - 2.00))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(2.00))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the add and pay flow for link based payments")
    public void PGPM_858_successfulLinkBasedPaymentThroughAddNPayMerchant(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        String mid = MerchantType.AddnPay.getId();
        String txnAmount = "3";

        CreateLink createLink = new CreateLink(mid, "linkDescription", 3.00);
        createLink.execute();
        String paymentLink = createLink.getPaymentLink();

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTP(user, paymentLink);

        CashierPage cashierPage = CashierPageFactory.getCashierPage("merchant");
        String orderId = linkPaymentLoginPage.textOrderId().getText();
        cashierPage.payBy(PayMode.CC);

        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(mid)
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }

    @Parameters({"theme"})
    @Test(description = "Verify the final transaction screen for sucessfull transaction")
    public void PGPM_862_uiValidationForsuccessfulLinkBasedPaymentThroughCC(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String mid = MerchantType.PGOnly.getId();
        String txnAmount = "2";
        CreateLink createLink = new CreateLink(mid, "linkDescription", 2.00);
        createLink.execute();
        String paymentLink = createLink.getPaymentLink();

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTP(user, paymentLink);

        CashierPage cashierPage = CashierPageFactory.getCashierPage("merchant");
        String orderId = linkPaymentLoginPage.textOrderId().getText();
        cashierPage.radioButtonWalletChecked().click();
        cashierPage.payBy(PayMode.CC);

        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(mid)
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String uiTxnAmount = responsePage.textTxnAmount().getText();
        String uiSuccessMessage = responsePage.textSuccessMessage().getText();
        String uiMerchantName = responsePage.textMerchantName().getText();
        String uiTxnId = responsePage.getTransactionId();

        Assertions.assertThat(uiTxnAmount).isEqualToIgnoringCase(txnAmount);
        Assertions.assertThat(uiSuccessMessage).isEqualToIgnoringCase("Paid Successfully To");
        // Assertions.assertThat(uiMerchantName).isEqualToIgnoringCase(PGPHelpers.getMerchantName(mid));
        Assertions.assertThat(uiTxnId).isEqualToIgnoringCase(txnStatus.getResponse().getTXNID());
    }

    @Parameters({"theme"})
    @Test(description = "Verify final transaction screen for failed transaction")
    public void PGPM_863_uiValidationForFailedLinkBasedPaymentThroughCC(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 0.00);
        String mid = MerchantType.PGOnly.getId();
        String txnAmount = "99.98";
        CreateLink createLink = new CreateLink(mid, "linkDescription", 99.98);
        createLink.execute();
        String paymentLink = createLink.getPaymentLink();

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTP(user, paymentLink);

        CashierPage cashierPage = CashierPageFactory.getCashierPage("merchant");
        String orderId = linkPaymentLoginPage.textOrderId().getText();
        cashierPage.radioButtonWalletChecked().click();
        cashierPage.payBy(PayMode.CC);

        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        responsePage.imageFail().waitUntilVisible();
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(mid)
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();

        String uiFailureMessage = responsePage.textFailureMessage().getText();
        String uiTxnId = responsePage.getTransactionId();

        Assertions.assertThat(uiFailureMessage).isEqualToIgnoringCase("Your payment has been declined by your bank. If money is deducted from your account, your bank should refund the same in 7 days. Please contact your bank in case of any delay or further queries.");
        Assertions.assertThat(uiTxnId).isEqualToIgnoringCase(txnStatus.getResponse().getTXNID());
    }

    @Parameters({"theme"})
    @Test(description = "Verify final transaction screen for pending transaction")
    public void PGPM_864_uiValidationForPendingLinkBasedPaymentThroughCC(@Optional("merchant") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 0.00);
        String mid = MerchantType.PGOnly.getId();
        String txnAmount = "99.84";

        CreateLink createLink = new CreateLink(mid, "linkDescription", 99.84);
        createLink.execute();
        String paymentLink = createLink.getPaymentLink();

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
        linkPaymentLoginPage.openLinkAndSubmitOTP(user, paymentLink);

        CashierPage cashierPage = CashierPageFactory.getCashierPage("merchant");
        String orderId = linkPaymentLoginPage.textOrderId().getText();
        cashierPage.radioButtonWalletChecked().click();
        cashierPage.payBy(PayMode.CC);

        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        responsePage.imageFail().waitUntilVisible();
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();

        String uiFailureMessage = responsePage.textPendingMessage().getText();
        String uiTxnId = responsePage.getTransactionId();
        String uiTxnAmount = responsePage.textTxnAmount().getText();

        Assertions.assertThat(uiFailureMessage).isEqualToIgnoringCase("Your bank systems are overloaded. Please check back in 4 hours for your payment status.");
        Assertions.assertThat(uiTxnId).isEqualToIgnoringCase(txnStatus.getResponse().getTXNID());
        Assertions.assertThat(uiTxnAmount).isEqualToIgnoringCase(txnAmount);
    }

}
