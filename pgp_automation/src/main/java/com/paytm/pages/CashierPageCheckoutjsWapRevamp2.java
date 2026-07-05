package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Cashier page for checkoutjs_web_revamp_2 theme.
 * Overrides locators that changed in the new checkout UI (v2).
 */
public class CashierPageCheckoutjsWapRevamp2 extends CashierPageCheckoutjsWap {

    public CashierPageCheckoutjsWapRevamp2() {
        super();
        setPageName("Checkout-js-web-revamp-2");
    }

    @Override
    public UIElement loginStrip() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-login-card')]"), getPageName(), "login-strip");
    }

    public UIElement loginText() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-login-text')]"), getPageName(), "login-text");
    }

    public UIElement loginActionButton() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-login-action-text')]"), getPageName(), "login-action-button");
    }

    public UIElement loginChevron() {
        return new UIElement(By.xpath("//img[contains(@class,'ptm-login-chevron')]"), getPageName(), "login-chevron");
    }

    @Override
    public Link tabCreditCard() {
        return new Link(By.xpath("//div[@id='checkout-card']//p[contains(@class,'ptm-paymode-name')]"), getPageName(), super.tabCreditCard().getElementName()) {
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//div[@id='checkout-card']//input[contains(@name,'verify')]/following-sibling::span[contains(@class,'ptm-checkmark-new')]"),
                        getPageName(), "checked-paymode");
                return checked.isElementPresent();
            }
        };
    }

    @Override
    public Link tabDebitCard() {
        return new Link(By.xpath("//div[@id='checkout-card']//p[contains(@class,'ptm-paymode-name')]"), getPageName(), super.tabCreditCard().getElementName());
    }

    @Override
    public Link tabUPICollect() {
        return new Link(By.xpath("//div[contains(text(),'Pay using UPI ID')]"), getPageName(), "upi-collect-tab") {
            @Override
            public boolean isSelected() {
                return upiCollectTabTick().isElementPresent();
            }
        };
    }

    @Override
    public RadioButton upiCollectTabTick() {
        return new RadioButton(By.xpath("//div[contains(text(),'Pay using UPI ID')]/parent::label/input"),
                getPageName(), "upi-collect-tab-tick");
    }

    @Override
    public UIElement tabUPIId() {
        return new UIElement(By.xpath("//*[contains(text(),'Pay using UPI ID')]"), getPageName(), "select-upi-id");
    }

    @Override
    public TextBox textBoxVPA() {
        return new TextBox(By.xpath("//input[contains(@placeholder,'Enter UPI ID')] | //input[contains(@id,'ptm-upi-input')]"), getPageName(), "vpa-field");
    }

    @Override
    public UIElement verifyVPALinkText() {
        return new UIElement(By.xpath("//*[text()='Verify'] | //*[text()='Verify VPA']"), getPageName(), "verify-vpa-linktext");
    }

    @Override
    public Link tabNetBanking() {
        return new Link(By.xpath("//p[text()='Net Banking'] | //div[@id='checkout-nb']//p[contains(@class,'ptm-paymode-name')]"), getPageName(), super.tabNetBanking().getElementName()) {
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//div[@id='ptm-nb-inner'] | //div[@id='checkout-nb']//input[contains(@name,'verify')]/following-sibling::span[contains(@class,'ptm-checkmark-new')]"),
                        getPageName(), "checked-paymode");
                return checked.isElementPresent();
            }
        };
    }

    @Override
    public Link tabEMI() {
        return new Link(By.xpath("//div[@id='checkout-emi']//p[contains(@class,'ptm-paymode-name')]"), getPageName(), "emi-tab");
    }

    @Override
    public CheckBox checkBoxPPI() {
        return new CheckBox(By.xpath("//span[contains(@class,'ptm-checkmark-new')]"), getPageName(), "checkbox-ppi");
    }

    @Override
    public Link tabSavedCard() {
        return new Link(By.xpath("//div[contains(@class,'ptm-sc-main')]//span[contains(@class,'ptm-checkmark-new')]"), getPageName(), "saved-cards-tab") {
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//div[@id='ptm-checkout-sc']//div[@id='ptm-checkout-iframe']"),
                        getPageName(), "checked-paymode");
                return checked.isElementPresent();
            }
        };
    }

    @Override
    public CheckBox rememberMeCheckbox() {
        return new CheckBox(By.xpath("//label[contains(@class,'ptm-container')][contains(.,'Remember Me')]/span[contains(@class,'ptm-checkmark-new')]"),
                getPageName(), "Remember-this-mobile-number-for-future-login_checkbox") {
            @Override
            public boolean isChecked() {
                return this.isElementPresent();
            }
        };
    }

    public UIElement paymentTitle() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-payment-title')]"), getPageName(), "payment-title");
    }

    public UIElement paymentSummaryContainer() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-payment-summary-container')]"), getPageName(), "payment-summary-container");
    }

    public UIElement selectPaymentOptionHeader() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-lightening-header')]"), getPageName(), "select-payment-option-header");
    }

    public UIElement upiPaymodeSecondaryText() {
        return new UIElement(By.xpath("//div[@id='checkout-upi']//*[contains(@class,'ptm-paymode-secondary-text')]"), getPageName(), "upi-secondary-text");
    }

    public UIElement cardPaymodeSecondaryText() {
        return new UIElement(By.xpath("//div[@id='checkout-card']//*[contains(@class,'ptm-paymode-secondary-text')]"), getPageName(), "card-secondary-text");
    }

    public UIElement nbPaymodeSecondaryText() {
        return new UIElement(By.xpath("//div[@id='checkout-nb']//*[contains(@class,'ptm-paymode-secondary-text')]"), getPageName(), "nb-secondary-text");
    }

    public UIElement emiPaymodeSecondaryText() {
        return new UIElement(By.xpath("//div[@id='checkout-emi']//*[contains(@class,'ptm-paymode-secondary-text')]"), getPageName(), "emi-secondary-text");
    }

    public UIElement upiVpaDescription() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-upi-vpa-desc')]"), getPageName(), "upi-vpa-description");
    }

    public UIElement upiFooterPoweredBy() {
        return new UIElement(By.xpath("//img[contains(@class,'upi-footer')]"), getPageName(), "upi-footer-powered-by");
    }

    public UIElement promoCodeModal() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-promocode-modal-overlay')]"), getPageName(), "promo-code-modal");
    }

    public UIElement promoAppliedText() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-promocode-applied-text')]"), getPageName(), "promo-applied-text");
    }

    public UIElement promoSavedText() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-promocode-saved-text')]"), getPageName(), "promo-saved-text");
    }

    public Button promoOkButton() {
        return new Button(By.xpath("//*[contains(@class,'ptm-promocode-ok-button')]"), getPageName(), "promo-ok-button");
    }

    public Button promoModalCloseButton() {
        return new Button(By.xpath("//*[contains(@class,'ptm-promocode-modal-close')]"), getPageName(), "promo-modal-close-button");
    }

    public UIElement cartIllustration() {
        return new UIElement(By.xpath("//img[contains(@class,'ptm-cart-svg')]"), getPageName(), "cart-illustration");
    }

    @Override
    public UIElement errorTextsInUPIFlow() {
        return new UIElement(By.xpath("//span[contains(@class,'ptm-nid-error')]"), getPageName(), "Error Msg Paragraph");
    }

    @Override
    public Button closeCcDcDetailBtn() {
        return new Button(By.xpath("//*[@id='checkout-card']//*[contains(@class,'ptm-close-wrap')]//img | //*[@id='checkout-card']//span[contains(@class,'ptm-cross')]"), getPageName(), "close-button");
    }

    public UIElement overlayCloseIcon() {
        return new UIElement(By.xpath("//img[contains(@src,'cross-overlay-icon')]"), getPageName(), "overlay-close-icon");
    }

    // ========== View Details (Payment Summary) ==========

    public UIElement viewDetailsLink() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-view-details-text')]"), getPageName(), "view-details-link");
    }

    public UIElement viewDetailsArrowIcon() {
        return new UIElement(By.xpath("//img[contains(@class,'ptm-arrow-icon')]"), getPageName(), "view-details-arrow-icon");
    }

    // ========== Overlay Headings ==========

    public UIElement overlayHeading() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-overlay-maintxt')]"), getPageName(), "overlay-heading");
    }

    public UIElement cardOverlayHeading() {
        return new UIElement(By.xpath("//div[@id='checkout-card']//*[contains(@class,'ptm-overlay-maintxt')]"), getPageName(), "card-overlay-heading");
    }

    public UIElement nbOverlayHeading() {
        return new UIElement(By.xpath("//div[@id='checkout-nb']//*[contains(@class,'ptm-overlay-maintxt')]"), getPageName(), "nb-overlay-heading");
    }

    public UIElement emiOverlayHeading() {
        return new UIElement(By.xpath("//div[@id='checkout-emi']//*[contains(@class,'ptm-overlay-maintxt')]"), getPageName(), "emi-overlay-heading");
    }

    // ========== Net Banking Revamp ==========

    public UIElement nbPopularBanksHeading() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-nb-popular-banks-heading')]"), getPageName(), "nb-popular-banks-heading");
    }

    public UIElement nbOtherBanksHeading() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-nb-other-banks-heading')]"), getPageName(), "nb-other-banks-heading");
    }

    public UIElement nbPopularBankItem(String bankName) {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-nb-bank-list-container')]//p[contains(@class,'ptm-bank-name') and text()='" + bankName + "']"), getPageName(), "nb-popular-bank-" + bankName);
    }

    public UIElement nbBankItem(String bankName) {
        return new UIElement(By.xpath("//*[@id='checkout-nb-banks-list']//*[contains(@class,'ptm-nb-bank-name') and contains(text(),'" + bankName + "')]"), getPageName(), "nb-bank-" + bankName);
    }

    public UIElement nbActiveBankTick() {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-activeBank')]//div[contains(@class,'ptm-upi-tick')]"), getPageName(), "nb-active-bank-tick");
    }

    @Override
    public UIElement searchBox() {
        return new UIElement(By.xpath("//input[@id='searchBar'] | //input[contains(@placeholder,'Search your bank')]"), getPageName(), "nb-search-box");
    }

    @Override
    public Select dropdownNB() {
        return new Select(By.xpath("//div[@id='checkout-nb']//p[contains(@class,'ptm-paymode-name')]"), getPageName(), "select-nb-banks-dropdown") {
            @Override
            public void selectByValue(String value) {
                this.waitUntilClickable();
                this.click();
                DriverManager.getWebDriverElementWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id='checkout-nb-banks-list']")));
                List<UIElement> banks = UIElements.getMultiple(By.xpath("//*[@id='checkout-nb-banks-list']//*[contains(@class,'ptm-nb-bank-name')]"), getPageName(), "nb-banks-list");
                for (UIElement bank : banks) {
                    Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> !bank.getText().isEmpty());
                    if (bank.getText().toLowerCase().contains(value.toLowerCase())) {
                        bank.waitUntilClickable();
                        bank.click();
                        break;
                    }
                }
            }
        };
    }

    // ========== EMI Revamp ==========

    public UIElement emiEnterCardNumberRadio() {
        return new UIElement(By.xpath("//*[@id='use-another-card-radio']"), getPageName(), "emi-enter-card-radio");
    }

    public UIElement emiEnterCardNumberText() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-other-paymode')]"), getPageName(), "emi-enter-card-text");
    }

    public UIElement emiBankNbfcSelectionText() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-scemi-selectxt')]"), getPageName(), "emi-bank-nbfc-selection");
    }

    public UIElement emiBankNbfcSecondaryText() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-scemi-selectxt')]//*[contains(@class,'ptm-secondary')]"), getPageName(), "emi-bank-nbfc-secondary");
    }

    public UIElement emiSelectHeading() {
        return new UIElement(By.xpath("//*[contains(@class,'xs-emi-heading')]"), getPageName(), "emi-select-heading");
    }

    public UIElement emiBankListItem(String bankName) {
        return new UIElement(By.xpath("//div[@id='checkout-emi']//*[contains(@class,'ptm-nb-list-item')]//img[@alt='" + bankName + "' or contains(@src,'" + bankName + "')]"), getPageName(), "emi-bank-" + bankName);
    }

    public UIElement emiViewAllBanks() {
        return new UIElement(By.xpath("//div[@id='checkout-emi']//*[contains(@class,'ptm-bank-name') and text()='View All']"), getPageName(), "emi-view-all-banks");
    }

    // ========== CC/DC Overlay ==========

    public UIElement cardMobileHeading() {
        return new UIElement(By.xpath("//div[@id='checkout-card']//*[contains(@class,'xs-ptm-heading')]"), getPageName(), "card-mobile-heading");
    }

    // ========== Pay Button ==========

    @Override
    public Button buttonPGPayNow() {
        return new Button(By.xpath("//*[@id='checkout-button']/button | //div[@id='checkout-button']/button"), getPageName(), "pay-button");
    }

    public UIElement payButtonLockImg() {
        return new UIElement(By.xpath("//*[@id='checkout-button']//img[contains(@class,'ptm-lock-img')]"), getPageName(), "pay-button-lock-img");
    }

    // ========== Progress Bar (Promo Modal) ==========

    public UIElement promoProgressBar() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-progress-bar')]"), getPageName(), "promo-progress-bar");
    }
}
