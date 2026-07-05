
package scripts.Native.checkoutjs;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;



@Owner(Constants.Owner.PRIYANSHI)
@Feature("PGP-29620 MasteroCard Validation on Blink CheckoutJS")

public class MaestroCardCheckout extends PGPBaseTest {

    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Validate Transaction by entering Expiry Date and CVV of MaestroCard")
    public void verifyTxnViaEnteringExpdateAndCvv(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
       // cashierPage.checkBoxPPI().unCheck();
        //Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).isFalse();
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MAESTROCARD_2));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Transaction witthout entering Expiry Date and CVV")
    public void verifyTxnWithoutEnteringExpandCvv(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

            User user = userManager.getForRead(Label.BASIC);
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI)
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
            config.data.setToken(txnToken);
            checkoutPage.createCheckoutJsOrder(config);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
           // cashierPage.checkBoxPPI().unCheck();
           // Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).isFalse();
            cashierPage.tabCreditCard().click();
            DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
            cashierPage.textBoxCardNumber().sendKeys(PaymentDTO.MAESTROCARD_1);
            cashierPage.cvvNotReq().waitUntilVisible();
            Assertions.assertThat(cashierPage.cvvNotReq().getText().equals("Expiry & CVV are not required if not mentioned on your card"));
            DriverManager.getDriver().switchTo().defaultContent();

//            cashierPage.pause(1);
            cashierPage.buttonPGPayNow().click();
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_SUCCESS")
                    .validateRespMsg("Txn Success")
                    .validateRespCode("01")
                    .validatePaymentMode("CC")
                    .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate For EMI paymode")
    public void verifyCardOnEMiPayMode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMISubvention)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.MAESTROCARD_2);
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Validate Saved Maestro For EMI PayMode")
    public void verifySavedMaestroCardOnEMiPayMode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.MAESTRO_CARD)
                .build();
        WalletHelpers.modifyBalance(user, 0.0);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.MAESTROCARD_1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabPPI().click();
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD);


        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    //logout state

    @Parameters({"theme"})
    @Test(description = "Make txn using Maestro card number without using CVV and Expiry without SSO token")
    public void verifyTxnWithoutCvvAndExpiryinLogoutState(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().sendKeys(PaymentDTO.MAESTROCARD_1);
        cashierPage.cvvNotReq().waitUntilVisible();
        Assertions.assertThat(cashierPage.cvvNotReq().getText().equals("Expiry & CVV are not required if not mentioned on your card"));
        DriverManager.getDriver().switchTo().defaultContent();
//        cashierPage.pause(1);
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validatePaymentMode("CC")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }


    //ADD N Pay flow


    @Parameters({"theme"})
    @Test(description = "Validate Maestro card for Add N pay merchant")
    public void verifyMaestroCardinAddnPayFlow(@Optional("checkoutjs_web") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddnPay)
                .setTxnValue("3")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO.setCreditCardNumber(PaymentDTO.MAESTROCARD_1));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validating txn via Maestro Saved card with add n pay merchant")
    public void verifyMaestroCardinAddnPayViaSavedcard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.MAESTRO_DEBIT_CARD_NUMBER);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddnPay)
                .setTxnValue("3")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
//        cashierPage.pause(1);
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }


//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Validate txn via Maestro card with Hybrid merchant")
    public void verifyMaestroCardinHybviaSavedCard(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .setTxnValue("3")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO.setCreditCardNumber(PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("Hybrid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Validate txn via Maestro Saved card for Hybrid merchant")
    public void verifyMaestroCardinHyb(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.MAESTRO_DEBIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .setTxnValue("3")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabSavedCard().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("Hybrid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

}
