package scripts;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner(Constants.Owner.PRIYANSHI)
@Feature("PGP-33730")
public class BankOfferOnPCF extends PGPBaseTest {

    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verifying Promo and Convenience Charge label on Credit Card")
    public void TC_01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyPromoText().assertVisible();
        cashierPage.convinenceCharge().assertVisible();

    }

    @Parameters({"theme"})
    @Test(description = "Verifying Promo and Convenience Charge label on Saved Credit Card")
    public void TC_02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.applyPromoText().assertVisible();
        cashierPage.convinenceCharge().assertVisible();

    }

    @Parameters({"theme"})
    @Test(description = "Verifying Promo and Convenience Charge label on Net Banking")
    public void TC_03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.applyPromoText().assertVisible();
        cashierPage.convinenceCharge().assertVisible();

    }

    @Parameters({"theme"})
    @Test(description = "Verifying Promo and Convenience Charge label on EMI")
    public void TC_04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        // SavedCardHelpers.deleteSavedCard(user);
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.PROMO_CC_CARD_HDFC);
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
       // cashierPage.applyEMIPromoText().assertVisible();
        cashierPage.convinenceCharge().assertVisible();

    }

    // @Parameters({"theme"})
    // @Test(description = "Verifying Convenience Charge label on Paytm Wallet and Promo Label should not be visible", enabled = false)
    public void TC_05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        WalletHelpers.modifyBalance(user,20.00);
        SavedCardHelpers.deleteSavedCard(user);
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //Assert user is logged in on cashier page
        cashierPage.checkBoxPPI().isElementPresent();
        cashierPage.applyPromoText().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verifying Convenience Charge label on Paytm Wallet and Promo Label should come on Credit Card Frame in case of ADD and PAY")
    public void TC_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        WalletHelpers.modifyBalance(user,2.00);
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.applyPromoText().assertVisible();
        cashierPage.convinenceCharge().assertVisible();

    }

}
