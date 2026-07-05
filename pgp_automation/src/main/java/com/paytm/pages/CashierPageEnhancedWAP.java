package com.paytm.pages;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import com.paytm.utils.merchant.util.AuthUtil;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CashierPageEnhancedWAP extends CashierPage {
    public final String INVALID_CARD_NUMBER = "Please enter a valid card number";
    public final String COD_PAY = "Please pay ₹{amount} to the courier agent at the time of delivery.";
    public final String POSTPAID_INCORRECT_PASSCODE = "Incorrect Passcode";
    public final String POSTPAID_EMPTY_PASSCODE = "Please Enter Passcode";
    public final String EMPTY_CREDIT_CARD_NUMBER = "Please enter your card Number";
    public final String INVALID_EXPIRY_DATE = "Invalid Expiry Date";
    public final String INVALID_PAYMENT_DETAILS = "This card is not supported for subscription payments";
    private final BajajFinservBankPage bajajFinservBankPage = new BajajFinservBankPage();
    private static final String VALID_OTP = "123456";
    public CashierPageEnhancedWAP() {
        super("cashier-page-enhanced-wap-theme");
    }

    public UIElement notificationContainer() {
        return new UIElement(By.xpath("//div[contains(@class,'payment-type-methods')]"), getPageName(), super.notificationContainer().getElementName());
    }

    public TextBox textBoxExpiryMonth() {
        return new TextBox(
                By.id("mm"),
                getPageName(), "expiry-month-field");
    }

    public TextBox textBoxExpiryYear() {
        return new TextBox(
                By.id("yy"),
                getPageName(), "expiry-year-field");
    }

    @Override
    public UIElement WalletTitle() {
        return new UIElement(By.xpath("//span[contains(text(),'Paytm Balance')]"),
                getPageName(), "Paytm-Balance_checkbox");
    }

    @Override
    public UIElement promoOffersList(){
        return new UIElement(By.id("checkout-offers"),getPageName(),"promoList");
    }

    @Override
    public UIElement offerAppliedMessage(){
        return new UIElement(By.xpath("//img[@alt='offer-applied-img']//parent::span"),getPageName(),"appliedOffers");
    }

    @Override
    public UIElement promoInvalidMessage(){
        return new UIElement(By.xpath("//*[@class= ' _1lTe error fs12'][contains(text(),'This transaction requires a valid promocode to proceed.')] "),getPageName(),
                "invalidPromoMsg");
    }

    @Override
    public UIElement promoOfferNotAllowedAddNPayMessage(){
        return new UIElement(By.xpath("//p[contains(text(),'Payment Offers not allow for add and pay transactions')]"),getPageName(),
                "invalidPromoMsg");
    }

    @Override
    public Select dropdownExpiryYear() {
        return new Select(
                By.name("expiryYear"),
                getPageName(), super.dropdownExpiryYear().getElementName());
    }


    @Override
    public UIElement verifyVPALinkText() {
        return new UIElement(By.xpath("//*[text()='Verify VPA']"), getPageName(), "verify-vpa-linktext");
    }

    @Override
    public Button bankMandateConfirmPay(){
        return new Button(By.xpath("//button[@class='btn btn-primary w100 pos-r _2fNU  '][contains(.,'Confirm')]"), getPageName(), "bankmandate-pay-confirm");
    }

    @Override
    public TextBox textBoxSavedCardCVV() {
        return new TextBox(By.xpath("//input[@id='cv1']"), getPageName(), "saved-card-cvv-field");
    }

    public Button buttonNBSumbit() {
        return new Button(By.cssSelector(".btn.btn-primary"), getPageName(), "pay-button") {
            @Override
            public void waitUntilClickable() {
                super.waitUntilClickable();
                pause(1);
            }
        };
    }

    @Override
    public Button buttonCODPayNow() {
        return this.buttonPGPayNow();
    }

    @Override
    public Button buttonWalletPayNow() {
        return this.buttonPGPayNow();
    }

    @Override
    public UIElement lblCardNo() {
        return null;
    }

    @Override
    public TextBox lblSavedCardCVV() {
        return null;
    }

    @Override
    public Button buttonPpblSumbit() {
        return new Button(By.cssSelector(".btn.btn-primary"), getPageName(), super.buttonPostPaidPayNow().getElementName());
    }

    @Override
    public UIElement promoCode() {
        return null;
    }

    @Override
    public UIElement applyOfferText() {
        return null;
    }

    @Override
    public void switchToLoginFrame() {
        //Do Nothing
    }

    @Override
    public Button BtnRemoveSavedCard() {
        return null;
    }

    @Override
    public Button btnRemoveConfirmYes() {
        return null;
    }

    @Override
    public RadioButton radioButtonWalletChecked() {
        return new RadioButton(By.xpath("//span[text()='Paytm Balance']/../../preceding-sibling::div/span"), getPageName(),
                super.radioButtonWalletChecked().getElementName());
    }

    @Override
    public CheckBox ppblBalanceCheck() {
        return null;
    }


    @Override
    public CheckBox checkboxPaytmCC() {
        return new CheckBox(By.xpath("//input[@value='pdc']"), getPageName(), "postpaid-checkbox");
    }

    @Override
    public UIElement insufficientBalanceIcon() {
        return null;
    }

    @Override
    public UIElement lblCVV() {
        return null;
    }

    @Override
    public UIElement lblExpMonth() {
        return null;
    }

    public UIElement insufficientBalanceIconMsg() {
        return null;
    }

    @Override
    public UIElement imgDefaultCVVIcon() {
        return null;
    }

    @Override
    public UIElement imgAmexCVVIcon() {
        return null;
    }

    public Button btnCheckBalancePostpaid() {
        return new Button(By.xpath("//div[text()='Paytm Postpaid']/following-sibling::label/span/a[text()='Check balance']"), getPageName(), "postpaid-check-balance-button");
    }

    public UIElement lblInsufficientBalancePostpaid() {
        return new UIElement(By.xpath("//div[text()='Paytm Postpaid']/following-sibling::label/div[text()='You do not have enough balance for this payment']"), getPageName(), "postpaid-insufficient-balance-msg");
    }

    @Override
    public TextBox textBoxPPBLPassCode() {
        return new TextBox(By.cssSelector("input.form-ctrl.mask"), getPageName(), super.textBoxPPBLPassCode().getElementName());
    }

    @Override
    public TextBox textBoxPostpaidPassCode() {
        return new TextBox(By.xpath("//input[@placeholder='Enter Passcode']"), getPageName(), super.textBoxPostpaidPassCode().getElementName());
    }



    @Override
    public Link tabUPIIntent() {
        return new Link(By.xpath("//*[text()='Pay using UPI App']"), getPageName(), "upi-intent-tab");
    }



    @Override
    public RadioButton rdbtnATMPin() {
        return new RadioButton(By.xpath("//span[text()='Use ATM PIN']/parent::span/parent::span/preceding-sibling::label/input[@name='idebitOption']"), getPageName(), super.rdbtnATMPin().getElementName());
    }


    @Override
    public RadioButton rdbtn3DSecurePin() {
        return new RadioButton(By.xpath("//span[text()='Use 3D Secure PIN or OTP']/parent::span/parent::span/preceding-sibling::label/input[@name='idebitOption']"), getPageName(), super.rdbtnATMPin().getElementName());
    }

    @Override
    public RadioButton radioBtnNetBankingOrATM(String bankName) {
        return new RadioButton(
                By.id(bankName),
                getPageName(), "radioBtnNetBankingOrATM");
    }

    public UIElement netBankingOtherBank() {
        return new UIElement(By.xpath("//span[text()='Select from all other banks']"), getPageName(), "select-from-all-other-banks-button") {
            @Override
            public void click() {
                this.waitUntilClickable();
                pause(1);
                super.click();
            }
        };
    }




    public UIElement selectEMIPlan() {
        return new UIElement(By.cssSelector(".has-emi-available:not(.hide) .icon-chevron_down_dark"), getPageName(), "emi-bank-dropdown") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }

    @Override
    public List<WebElement> lblUPIpush() {
        return null;
    }

    @Override
    public UIElement rdbtnSavedCardATMPin() {
        return null;
    }

    @Override
    public UIElement radioBtnSavedCard(String cardId) {
        return null;
    }

    @Override
    public Button savedCardPayNow() {
        return null;
    }

    @Override
    public UIElement lblErrMsg() {
        return null;
    }

    @Override
    public UIElement imgScanPayQRCode() {
        return null;
    }


    @Override
    public UIElement imgUpiQRSymbol() {
        return null;
    }

    @Override
    public List<UIElement> textBoxPRNNumber() {
        return null;
    }

    @Override
    public UIElement buttonPRNVerify() {
        return null;
    }

    @Override
    public UIElement PayButtonWithWallet(){
        return new UIElement( By.xpath("//section[contains(@class, 'paytm-wallet')]"),getPageName(),"Pay-Button-Wallet");
    }

    @Override
    public UIElement PayButtonWithPPBL(){
        return new UIElement( By.xpath("//section[@id = 'ptm-ppb']//button"),getPageName(),"Pay-Button-PPBL");
    }

    @Override
    public UIElement PayButtonWithSC(){
        return new UIElement( By.xpath("//section[@id = 'ptm-sc']/div//button"),getPageName(),"Pay-Button-SC");
    }

    @Override
    public UIElement PayButtonWithPostPaid(){
        return new UIElement( By.xpath("//section[@id = 'ptm-pdc']//button"),getPageName(),"Pay-Button-PostPaid");
    }

    @Override
    public boolean isPPIChecked() {
        try {
            radioButtonWalletChecked().assertVisible();
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    @Override
    public boolean isPPBLChecked() {
        return false;
    }

    @Override
    public void clickSavedCardTab() {
        //Do nothing
    }

    public TextBox textBoxOtp() {
        return new TextBox(By.name("otp"), getPageName(), "OTP box");
    }

    public Button buttonOtpSubmit() {
        return new Button(By.name("Login"), getPageName(), "OTP Login");
    }


    public UIElement loginStitch() {
        return new UIElement(By.id("login-stitch"), getPageName(), "loginModal");
    }

    @Override
    public UIElement loginFrame() {
        return new UIElement(By.cssSelector(".login-wrapper.ng-scope"), getPageName(), super.loginFrame().getElementName());
    }

    @Override
    public void clickPgOverlay() {
        //Do Nothing, because it does't exist for enhancedCashier Flow.
    }

    @Override
    public Link btnLogin() {
        return new Link(By.cssSelector(".login-button.ml16.login-button-style"), getPageName(), super.btnLogin().getElementName());
    }

    public Link linkEmiPlans() {
        return new Link(By.cssSelector("section[data-key='sc'].active a"), getPageName(), "emi-plans-link") {
            @Override
            public void click() {
                super.waitUntilClickable();
                super.click();
            }
        };
    }

    @Override
    public void fillExpiryMonth(String expiryMonth) {
        textBoxExpiryMonth().waitUntilEditable();
        textBoxExpiryMonth().clearAndType(expiryMonth);
    }

    @Override
    public void fillExpiryYear(String expiryYear) {
        textBoxExpiryYear().waitUntilEditable();
        textBoxExpiryYear().clearAndType(expiryYear);
    }

    @Override
    public void select3DSecurePinOptionIfDisplayed() {
        if (rdbtn3DSecurePinEnhanced().isDisplayed()) {
            rdbtn3DSecurePinEnhanced().click();
        }
    }

    @Override
    public RadioButton radioBtnSaveCard() {
        return new RadioButton(By.xpath("//section[contains(@class,'card-section')]//label[contains(@class,'custom-check')]/input[@type='checkbox']"),
                getPageName(), super.radioBtnSaveCard().getElementName());
    }

/*    @Override
    public CheckBox checkBoxSaveCard() {
        return new CheckBox(By.xpath("//div[@class='pu-title']/parent::div//input[@type='checkbox']"),
                getPageName(), "prompt-save-card-checkbox");
    }*/

    @Override
    public void selectATMPinOptionIfDisplayed() {
        if (rdbtnATMPinEnhanced().isDisplayed()) {
            rdbtnATMPinEnhanced().click();
        }
    }

    @Override
    public void assertSavedCardVisibility() {
        Assertions.assertThat(savedCardNumbers().size()).withFailMessage("saved-cards are expected to be visible but are not").isNotEqualTo(0);
    }

    @Override
    public void assertSavedCardNotVisible() {
        Assertions.assertThat(savedCardNumbers().size()).withFailMessage("saved-cards are expected to be not visible but are").isEqualTo(0);
    }

    @Override
    public int savedUpiListSize(){
        List<WebElement> links;
        links = DriverManager.getCurrentWebDriver().findElements(By.xpath("(//section[@data-key='upipush']/div)"));
        return links.size();
    }

    @Override
    public UIElement getUPILogo(){
        return new UIElement(By.xpath("//*[local-name()='svg']"),getPageName(),"upi-logo");
    }

    @Override
    public void verifyCardDisplayed(String... cardNumbers) {
        int count = 0;
        for (String cardNumber : cardNumbers) {
            for (WebElement wl : this.savedCardNumbers()) {
                if (cardNumber.endsWith(wl.getText().split(" ")[1].replaceAll(".", ""))) {
                    count++;
                    break;
                }
            }
        }
        Assertions.assertThat(count).withFailMessage("Expected " + cardNumbers.length + " cards but found " + count + " cards.").isEqualTo(cardNumbers.length);
    }

    public RadioButton rdbtn3DSecurePinEnhanced() {
        return new RadioButton(By.xpath("//span[text()='Use 3D Secure PIN or OTP']/parent::span/parent::span/parent::div/span/label/input[@name='idebitOption']"),
                getPageName(), super.rdbtn3DSecurePin().getElementName());
    }

    public RadioButton rdbtnATMPinEnhanced() {
        return new RadioButton(By.xpath(
                "//span[text()='Use ATM PIN (Recommended)']/parent::span/parent::span/parent::div//input[@name='idebitOption']"),
                getPageName(), super.rdbtnATMPin().getElementName());
    }

    @Override
    public TextBox textBoxPhoneNumber() {
        return new TextBox(By.xpath("//span[text()='+91']/following-sibling::input[@type='tel']"), getPageName(), super.textBoxPhoneNumber().getElementName());
    }

    public Button loginProceedButton() {
        return new Button(By.cssSelector(".btn.btn-primary"), getPageName(), "login-proceed-button");
    }


    public TextBox loginOtpBox() {//before By.id("inp")
        return new TextBox(By.id("inp"), getPageName(), "otp-field");
    }


    @Override
    public CheckBox checkboxPPBL() {
        return new CheckBox(By.cssSelector("input[value='ppb']"), getPageName(), "ppbl-checkbox") {
            @Override
            public boolean isChecked() {
                UIElement uiElement = new UIElement(By.cssSelector("[data-key='ppb']"), getPageName(), "");
                return uiElement.getAttribute("class").contains("active");
            }
        };
        
    }


    public Button failedTxnGotItButton() {
        return new Button(By.cssSelector(".popup-global .btn-primary"), getPageName(), "failed-txn-got-it-button");
    }

    @Override
    public void clickInvalidOTPEnteredButtonIfDisplayed() {
        if (failedOTPEnteredButton().isDisplayed()) {
            failedOTPEnteredButton().click();
        }
    }

    public Button failedOTPEnteredButton(){
        return new Button(By.xpath("//p[contains(.,'OTP')]//following-sibling::section/button"),getPageName(), "failed-txn-got-it-button");
    }

    @Override
    public void directLogin(User user) {
        textBoxPhoneNumber().clearAndType(user.mobNo());
        textBoxPhoneNumber().click();
        loginProceedButton().click();
        waitUntilContainsText("OTP");
        if (loginOtpBox().isDisplayed()) {
            String otp;
            try {
                otp = AuthUtil.getOtp(user.mobNo());
            } catch (Exception e) {
//
                throw e;//done to save user OTP limits expiry
            }
            fillLoginOtp(otp);
        }

        otpVerifyButton().click();
        waitUntilContainsText("SELECT AN OPTION TO PAY");

    }


    @Override
    public UIElement paymentContainer() {
        return new UIElement(By.xpath("//div[contains(@class,'payment-type-methods')]"), getPageName(), super.paymentContainer().getElementName());
    }

    public UIElement convinenceFeeCCAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Credit Card']/following-sibling::div"), getPageName(), "Convenience fee CC");
    }

    public UIElement convinenceFeeDCAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Debit Card']/following-sibling::div"), getPageName(), "Convenience fee DC");
    }

    public UIElement convinenceFeeNBAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Net Banking']/following-sibling::div"), getPageName(), "Convenience fee NB");
    }

    public UIElement convinenceFeeUPIAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='BHIM UPI']/following-sibling::div"), getPageName(), "Convenience fee BHIM UPI");
    }

    public UIElement convinenceFeeWalletAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Paytm Balance']/following-sibling::div"), getPageName(), "Convenience fee PAYTM WALLET");
    }


    public List<UIElement> ListOfPayModsOnCashier() {
        return UIElements.getMultiple(By.cssSelector(".p-option:not([data-key='sc'])"), getPageName(), "paymodes on cashierPage");
    }

    @Override
    public CheckBox rememberMeCheckbox() {
        return new CheckBox(By.cssSelector("[type='checkbox']"), getPageName(), "remember-mobile-number-checkbox") {
            @Override
            public boolean isChecked() {
                return this.isSelected();
            }
        };
    }

    @Override
    public void submitPostPaidOnboarding() {
        if (!isPaymodeModeSelected(Constants.PayMode.PAYTM_DIGITAL_CARD)) {
            radioButtonPaytmPostpaid().click();
        }
        postpaidOnboard_TNC().assertVisible();
        postpaidOnboard_Accept_PayButton().waitUntilClickable();
        postpaidOnboard_Accept_PayButton().click();
    }

    @Override
    public UIElement postpaidOnboard_TNC() {
        return new UIElement(By.xpath("//div[contains(@class,'custom-check')]/following-sibling::span/a"),
                getPageName(), "postpaid-onboard-tnc");
    }

    @Override
    public UIElement postpaidOnboard_Accept_PayButton() {
        return new UIElement(By.className("btn-primary"),
                getPageName(), "postpaid-onboard-accept-pay-button");
    }

    public UIElement insufficientPPBLBalanceIconMsg() {
        return new UIElement(By.cssSelector("section.pc-wrap div div p"), getPageName(), "ppbl-insufficient" +
                "Balance");
    }

    @Override
    public UIElement ppblNotificationMsg() {
        return new UIElement(By.cssSelector("p._1HbL"), getPageName(), "ppbl-notification-msg");
    }

    @Override
    public Link tabSavedCard() {
        return new Link(By.xpath("//section[@data-key='sc']//*[contains(@class,'type-option-list')]//input"), getPageName(), "saved-card-tab") {
            @Override
            public void click() {
//                this.waitUntilClickable();
                super.click();
            }
        };
    }


    @Override
    public void RequestloginOTP(String mobileNumber) {
        loginStrip().click();
        textBoxPhoneNumber().clearAndType(mobileNumber);
        pause(3);
        loginProceedButton().click();
    }


    @Override
    public UIElement overlay() {
        return new UIElement(By.xpath("//div[contains(@class,'spinner')]"), getPageName(), "overlay") {
            @Override
            public void waitUntilClickable() {
                if (super.isDisplayed()) {
                    DriverManager.getWebDriverElementWait().withMessage("waiting for overlay to be finished").until(ExpectedConditions.elementToBeClickable(this.getBy()));
                } else {

                }
            }

        };
    }

    public UIElement closeIcon() {
        return new UIElement(By.cssSelector(".pos-r .pos-a .icon-ic_close"), getPageName(), "close-btn");
    }

    @Override
    public String saveCardPosition(int cardPosition) {
        return DriverManager.getDriver().findElements(By.cssSelector(".small-text.d-block")).get(cardPosition - 1).getText();
    }

    @Override
    public boolean wallet_dropdown() {

        return true;
    }

    public UIElement EMIsavedCard(String cardNumber) {
        return new UIElement(By.xpath("(//label[text()='" + cardNumber.substring(cardNumber.length() - 4, cardNumber.length()) + "']//ancestor::span//preceding-sibling::label//input[@value='sc'])[2]"), getPageName(), "saved-card-radio-btn");
    }

    @Override
    public UIElement mobileNumber() {
        return new UIElement(By.xpath("//section/p[2]"), getPageName(), "Mobile Number");
    }

    @Override
    public UIElement getErrorCC_EMI_NOTSET() {
        return new UIElement(By.xpath("//*[@id='cardNotSupportedElement']"),getPageName(),"error-message");

    }

    @Override
    public UIElement save_card_visible() {
        return new UIElement(By.xpath("//h4[@class='ptm-select-txt ptm-uppercase xs-ptm-hidden']"),getPageName(),"error-message");
    }



    @Override
    public UIElement applyPromoText() {
        return new UIElement(By.xpath("//*[contains(text(),'applicable')]"), getPageName(), "applyPromoText" );
    }

    @Override
    public UIElement tpvAccountAlert() {
        return new UIElement(By.xpath("//div[@class='account-alert']/section"), getPageName(), "accountAlert" );
    }

    @Override
    public UIElement tpvAccountInfo() {
        return new UIElement(By.xpath("//div[@class = 'account-alert']//img[@alt = 'i']"), getPageName(), "accountInfo-i");
    }

    @Override
    public UIElement tpvAccountAlertInfo() {
        return new UIElement(By.xpath("//div[@class='account-alert']//div[contains(text(),'creating this payment request')]/.."), getPageName(), "accountInfo");
    }

    @Override
    public void clickRetryIncorrectOTPBtn(){
    }

    @Override
    public UIElement getBankOfferDiscountMsg(){
        return new UIElement(By.cssSelector(".ptm-message"),"cashier-page","discount-msg");
    }

    @Override
    public Button closeSubsDetailsTab() {
        return new Button(By.xpath("//*[contains(@class,'popup-global')]//img"), getPageName(), "close-subs-details-tab");
    }







//    <------------------------------------------------------------------------------------------------>


    @Override
    public Link tabCreditCard() {
        Link ccTab = new Link(By.xpath("//section[@data-key='card']//span[contains(text(),'Credit')]"), getPageName(), super.tabCreditCard().getElementName()){
         
        @Override
            public boolean isSelected(){
                return new Link(By.xpath("//section[@data-key='card'][contains(@class,'active')]//span[contains(.,'Credit')]"), getPageName(), "selected-option").isElementPresent();
            }

            @Override
            public void click() {
                try{
                    setPageNElementTimeout(Duration.ofSeconds(1));
                    if(!isSelected()){      // if not already selected then click
                        super.waitUntilClickable();
                        pause(1);
                        super.click();
                    }
                }finally {
                    resetPageNElementTimeout();
                }
            }
        };
        this.scrollToElement(ccTab);
        return ccTab;
    }

    @Override
    public Link tabDebitCard() {
        return new Link(By.xpath("//section[@data-key='card']//span[contains(.,'Debit')]"), getPageName(), super.tabDebitCard().getElementName()) {
            @Override
            public void click() {
                try{
                    setPageNElementTimeout(Duration.ofSeconds(1));
                    if(!isSelected()){
                        super.waitUntilClickable();
                        pause(1);
                        super.click();
                    }
                }finally {
                    resetPageNElementTimeout();
                }
            }
            @Override
            public boolean isSelected(){
                return new Link(By.xpath("//section[@data-key='card'][contains(@class,'active')]//span[contains(.,'Debit')]"), getPageName(), super.getElementName()).isElementPresent();
            }
        };
    }


    @Override
    public Link tabNetBanking() {
        return new Link(By.xpath("//section[@data-key='nb']//span[contains(.,'Net')]"), getPageName(), super.tabNetBanking().getElementName()) {
            @Override
            public void click() {
                try{
                    setPageNElementTimeout(Duration.ofSeconds(1));
                    if(!isSelected()){
                        super.waitUntilClickable();
                        pause(1);
                        super.click();
                    }
                }
                finally {
                    resetPageNElementTimeout();
                }

            }
            @Override
            public boolean isSelected(){
                return new Link(By.xpath("//section[@data-key='nb'][contains(@class,'active')]"), getPageName(), "selected-option").isElementPresent();
            }
        };
    }

    public Select dropdownNB() {
        return new Select(By.xpath("//section//div[contains(@class,'view-all')]"), getPageName(), "select-nb-banks-dropdown") {
            @Override
            public void selectByValue(String value) {
                this.waitUntilClickable();
                this.click();
                DriverManager.getWebDriverElementWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("span.fs14")));
                List<UIElement> banks = UIElements.getMultiple(By.cssSelector("span.fs14"), getPageName(), "nb-banks-list");
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

    @Override
    public Link tabEMI() {
        return new Link(By.xpath("//span[text()='EMI']"), getPageName(), super.tabEMI().getElementName());
    }

    @Override
    public Link tabUPI() {
        return new Link(By.xpath("//section[@data-key='upi']//div[contains(@class,'bank-d')] | //*[contains(@class,'upi-main')]//p"), getPageName(), super.tabUPI().getElementName()){
            @Override
            public boolean isSelected(){
                return new Link(By.xpath("//section[@data-key='upi'][contains(@class,'active')]"), getPageName(), "selected-option").isElementPresent();
            }
        };
    }

    @Override
    public TextBox textBoxVPA() {
        return new TextBox(By.xpath("//div[contains(@class,'passcode')]//input"), getPageName(), "upi-address-field");
    }

    @Override
    public TextBox textBoxCVVNumber() {
        return new TextBox(By.cssSelector("input[placeholder='CVV']"), getPageName(), "cvv-field") {
            @Override
            public void clear() {
                int maxLength = Integer.parseInt(textBoxCardNumber().getAttribute("maxlength"));
                String backspace = Keys.BACK_SPACE.toString();
                sendKeys(String.format("%0" + maxLength + "d", 0).replace("0", "" + backspace + ""));

            }
        };
    }

    public List<UIElement> loginOtpBoxes() {
        return UIElements.getMultiple(By.xpath("//div[contains(text(), 'Verify One Time Password Sent to')]/following-sibling::div//input"),
                getPageName(), "otp text boxes");
    }

    @Override
    public Button buttonPGPayNow() {
        return new Button(By.xpath("//button[contains(@class,'btn')]//i | //button[contains(@class,'btn')]//img "), getPageName(), "pay-button") {
            @Override
            public String getText() {
                return new Button(By.xpath("//button[contains(@class,'btn')]//i/../.."), getPageName(), "pay-button").getWrappedElement().getText().replace("Rs", "Rs ");
            }
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }

    public Select dropdownEmiBanks() {
        return new Select(By.xpath("//input[@placeholder='Search Bank']"), getPageName(), "emi-bank-dropdown") {
            @Override
            public void selectByVisibleText(String value) {
                this.waitUntilClickable();
                this.click();
                DriverManager.getWebDriverElementWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".searchlist li.pos-r")));
                List<UIElement> banks = UIElements.getMultiple(By.cssSelector(".searchlist li.pos-r"), getPageName(), "emi-banks-list");
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

    @Override
    public Link UseAnotherCard() {
        return new Link(By.xpath("//div[contains(text(),'+ Add New Card')]"), getPageName(),"Use-another-card");
    }

    @Override
    public Link viewAllArrow() {
        return new Link(By.xpath("//div[@class='view-all']/img[@class='ib d-arrow']"), getPageName(),"View-All-Arrow");
    }

    public Link selectEmiPlanButton() {
        return new Link(By.xpath("//span[text()='Select EMI Plan']"), getPageName(),"select emi button");
    }

    public Link proceedAfterEmiSelect() {
        return new Link(By.xpath("//*[contains(@class,'has-emi-available')]//button"), getPageName(),"proceed button after Emi Selection");
    }

    @Override
    public UIElement emiDurationOption(int emiPlanMonth) {
//        return new UIElement(By.xpath("//*[@class='fs14']"), getPageName(), super.emiDurationOption(emiPlanMonth).getElementName()){
        return new UIElement(By.xpath("//*[contains(@class,'has-emi-available')]//tr//td[2][contains(text(),'" + emiPlanMonth + "')]"), getPageName(), "emi-duration-option") {
            @Override
            public void click() {
                this.waitUntilClickable();
                pause(1);
                super.click();
            }
        };
    }

    public void fillAndSubmitEMIDetails (PaymentDTO emiDetails) {
        scrollToElement(tabEMI());
        tabEMI().click();
        pause(1);
        dropdownEmiBanks().selectByVisibleText(emiDetails.getBankName());
        if (UseAnotherCard().isElementPresent()) {
            UseAnotherCard().click();
        }
        if (emiDetails.getEmiCard() != null && !emiDetails.getEmiCard().equals("")) {
            textBoxCardNumber().clearAndType(emiDetails.getEmiCard());
            selectEmiPlanButton().click();
        }

        emiDurationOption(emiDetails.getMonth()).click();
        proceedAfterEmiSelect().click();

//        if (emiDetails.getCreditCardNumber() != null && !emiDetails.getCreditCardNumber().equals("")) {
//            textBoxCardNumber().clearAndType(emiDetails.getCreditCardNumber());
//            System.out.println();
//        }

//        if (GetAllTextDom().getText().contains("Use another card")) {
//            UseAnotherCard().click();
//        }

        if (emiDetails.getExpMonth() != null && !emiDetails.getExpMonth().equals("")) {
            fillExpiryMonth(emiDetails.getExpMonth());
        }
        if (emiDetails.getExpYear() != null && !emiDetails.getExpYear().equals("")) {
            fillExpiryYear(emiDetails.getExpYear().substring(2, 4));
        }
        if (emiDetails.getCvvNumber() != null && !emiDetails.getCvvNumber().equals("") && !emiDetails.getBankName().equals("Bajaj Finserv EMI Card") ) {
            textBoxCVVNumber().clearAndType(emiDetails.getCvvNumber());
        }

        if (!emiDetails.getPromoCode().equals("")) {
            try {
                Assertions.assertThat(txtPromoCode().getText().equalsIgnoreCase(emiDetails.getPromoDesc()));
            } catch (NullPointerException e) {
                throw new RuntimeException("Promo code is not visible");
            }
        }
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
        if(emiDetails.getBankName().equals("Bajaj Finserv EMI Card")){
            bajajFinservBankPage.inputOtp(VALID_OTP);
            bajajFinservBankPage.clickSubmit();
        }
    }

    public void fillAndSubmitEMIDCDetails(PaymentDTO emiDetails) {
        tabEMI().click();
        pause(1);
        //  dropdownEmiBanks().selectByVisibleText(emiDetails.getBankName());
        if (UseAnotherCard().isElementPresent()) {
            UseAnotherCard().click();
        }
        if (emiDetails.getEmiCard() != null && !emiDetails.getEmiCard().equals("")) {
            textBoxCardNumber().clearAndType(emiDetails.getEmiCard());
            selectEmiPlanButton().click();
        }

        emiDurationOption(emiDetails.getMonth()).click();
        //proceedAfterEmiSelect().click();

//        if (emiDetails.getCreditCardNumber() != null && !emiDetails.getCreditCardNumber().equals("")) {
//            textBoxCardNumber().clearAndType(emiDetails.getCreditCardNumber());
//            System.out.println();
//        }

//        if (GetAllTextDom().getText().contains("Use another card")) {
//            UseAnotherCard().click();
//        }

        if (emiDetails.getExpMonth() != null && !emiDetails.getExpMonth().equals("")) {
            fillExpiryMonth(emiDetails.getExpMonth());
        }
        if (emiDetails.getExpYear() != null && !emiDetails.getExpYear().equals("")) {
            fillExpiryYear(emiDetails.getExpYear().substring(2, 4));
        }
        if (emiDetails.getCvvNumber() != null && !emiDetails.getCvvNumber().equals("")) {
            textBoxCVVNumber().clearAndType(emiDetails.getCvvNumber());
        }

        if (!emiDetails.getPromoCode().equals("")) {
            try {
                Assertions.assertThat(txtPromoCode().getText().equalsIgnoreCase(emiDetails.getPromoDesc()));
            } catch (NullPointerException e) {
                throw new RuntimeException("Promo code is not visible");
            }
        }
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    //div[@class='view-all']/img[@class='ib d-arrow']

    @Override
    protected void fillAndSubmitCODDetails() {
        if (viewAllArrow().isElementPresent()) {
            viewAllArrow().click();
        }
        tabCOD().waitUntilClickable();
        tabCOD().click();
        buttonCODPayNow().waitUntilClickable();
        buttonCODPayNow().click();
    }

    protected void fillAndSubmitSavedCardDetails(PaymentDTO savedCardDetails) {
        if (viewAllArrow().isElementPresent()) {
            viewAllArrow().click();
        }
//        tabSavedCard().waitUntilClickable();
        tabSavedCard().click();

        if (rdbtn3DSecurePin().isElementPresent()) {
            rdbtn3DSecurePin().click();
        }
        if (!(savedCardDetails.getCvvNumber() == null || savedCardDetails.getCvvNumber().isEmpty())) {
            textBoxSavedCardCVV().waitUntilVisible();
            textBoxSavedCardCVV().clearAndType(savedCardDetails.getCvvNumber());
        }
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public Link tabCOD() {
        return new Link(By.xpath("//div[text()='Cash on Delivery (COD)']"), getPageName(), super.tabCOD().getElementName());
    }

    @Override
    public boolean isPaytmCCChecked() {
        UIElement postPaidSection = new UIElement(By.xpath("//input[@value='pdc']/parent::label/parent::div/ancestor-or-self::section"), getPageName(), "postpaid-paymode-section");
        return postPaidSection.getAttribute("class").contains("active");
    }

    public RadioButton radioButtonPaytmPostpaid() {
        return new RadioButton(By.xpath("//*[@data-key='pdc']//input"), getPageName(), "paytm-digital-card-radiobutton");
    }

    @Override
    public Button buttonPostPaidPayNow() {
        return new Button(By.className("btn-primary"), getPageName(),
                super.buttonPostPaidPayNow().getElementName());
    }

    protected void fillAndSubmitDigitalCardDetails(PaymentDTO digitalCardDetails) {
        if (!isPaymodeModeSelected(Constants.PayMode.PAYTM_DIGITAL_CARD)) {
            radioButtonPaytmPostpaid().click();
        }
        //textBoxPostpaidPassCode().clearAndType(digitalCardDetails.getPasscode());    //REMOVED PASSCODE ON POSTPAID
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public Button buttonSecureSignIn() {
        return new Button(By.xpath("//*[text()='Proceed'] | //button[contains(@class,'ptm-login-btn')]"), getPageName(), "sign-in-button");
    }

    @Override
    public UIElement totalAmtPG() {
        return new UIElement(By.xpath("//div[contains(text(),'Select an option to pay')]/following-sibling::div"), getPageName(), "pg-charge-fee-amount") {
            @Override
            public String getText() {
                return super.getText().replace("Rs", "").trim();
            }
        };
    }

    @Override
    public UIElement prnVerifySection() {
        return new UIElement(By.xpath("//div[contains(@class,'popup-global pos-a')]"), getPageName(), "prnVerifySection");
    }

    @Override
    public UIElement chargeFeeAmtPG() {
        return new UIElement(By.xpath("//div[contains(text(),'is applicable.')]/span"), getPageName(), "pg-charge-fee-amount") {
            @Override
            public String getText() {
                pause(1);
                return super.getText().replace("Rs", "").replace("convenience fee", "").trim();
            }
        };
    }

    @Override
    public void signin(String mobileNumber, String password) {
//        loginStrip().click();
        String otp;
        textBoxPhoneNumber().clearAndType(mobileNumber);

        buttonSecureSignIn().click();

        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        if(password.equals("888888"))
        {
            otp = password;
        }
        else
            otp = "123456";
        /*try {
            otp = AuthUtil.getOtp(mobileNumber);
        } catch (Exception e) {
//            otp = "888888";//default OTP
            throw e;//done to save user OTP limits expiry
        } */
        pause(2);
        loginOtpBox().sendKeys(otp);
//        fillLoginOtp(otp);
        otpVerifyButton().click();
        waitUntilLoads();
    }

    @Override
    public Button otpVerifyButton() {
        return new Button(By.cssSelector(".btn.btn-primary"), getPageName(), "otp-verify-button");
    }

    @Override
    public UIElement labelPaymodeInfoMsg() {
        return new UIElement(By.xpath("//*[contains(@class,'warning')]/span"), getPageName(), "pay-mode-info-msg");
    }

    @Override
    public UIElement postpaidSignUpStrip() {
        return new UIElement(By.xpath(("//*[@class='_2w4Y']")),getPageName(),"");
    }

    @Override
    public CheckBox checkBoxPPI() {
        return new CheckBox(By.xpath("//*[contains(text(),'Paytm Balance')]/ancestor::div[1]/..//input[@type='checkbox']"), getPageName(), "ppi-checkbox") {
            //section[contains(@class,'paytm-wallet')]//input
            @Override
            public boolean isChecked() {            // paytm-wallet p-option active pos-r
                UIElement uiElement = new UIElement(By.xpath("//*[contains(text(),'Paytm Balance')]/ancestor::div[1]"), getPageName(), "");
                return uiElement.getAttribute("class").contains("fw500");
            }


            @Override
            public void assertChecked() {
                UIElement uiElement = new UIElement(By.xpath("//span[contains(text(),'Paytm Balance')]/ancestor::div[1]"), getPageName(), "");
                if  (uiElement.getAttribute("class").contains("fw500")){

                }
                else{
                    Assertions.fail("Expecting " + this.getElementName() + " to be checked but is not");
                }

            }
        };
    }

    public List<UIElement> listOfNbchannel() {

        tabNetBanking().click();
        return UIElements.getMultiple(By.xpath("//div[contains(@class,'_3YQQ')]"), getPageName(), "listOfNbchannel");

    }
    public List<UIElement> listOfPayModes()
    {
        return UIElements.getMultiple(By.xpath("//div[contains(@class,'bank-d')]"),getPageName(),"list-payMode");
    }

    @Override
    public UIElement imgPaytmQRSymbol() {
        return new UIElement(By.xpath("//img[@data-key='qr-code']"), getPageName(), "imgPaytmQRSymbol");
    }

    @Override
    public UIElement tabAdvanceDeposit() {
        return new UIElement(By.xpath("//div[contains(text(), 'Paytm Advance Account')]"), getPageName(), "paytm-advancedeposit-tab");
    }

    @Override
    public CheckBox walletBalanceCheck() {
        return new CheckBox(By.xpath("(//span[contains(text(),'Paytm Balance')]/parent::label/following-sibling::span)[1]"), getPageName(), "ppi-balance");
    }

    @Override
    public UIElement upiPushSection() {
        return new UIElement(By.xpath("//*[@id='ptm-upipush']"), getPageName(), "paytm-upiPush-section");

    }

    @Override
    public List<WebElement> savedCardNumbers() {
        return DriverManager.getDriver()
                .findElements(By.xpath("//span[@class='bank-d']/following-sibling::label"));
    }

    public UIElement BankMandateRadioButton(){
        return new Button(By.xpath("//section[@data-key='bm']//img"), getPageName(),
                "bankMandate-button");
    }

    public void fillAndSubmitBankMandateDetails(PaymentDTO mandateDetails) {

        BankMandateRadioButton().click();
        bankmandateAuthMode(mandateDetails.getMandateAuthMode()).click();
        proceedBtn().click();
//        buttonPGPayNow().click();
    }

    public Button okBtnFailedTrans(){
        return new Button(By.xpath("//*[contains(text(),'Your transaction has failed')]/following-sibling::div//button"), getPageName(), "failed-ok-btn");
    }

    @Override
    public UIElement txtPromoCode() {
        return new UIElement(By.xpath("(//div[@class='pu-title']/following-sibling::div//p)[4]"), getPageName(), "emi-promo-code");
    }

    public UIElement RequestOTP() {
        waitUntilLoads();
        return new Button(By.xpath("//a[contains(text(),'Resend')]"), getPageName(), "request-another-otp");
    }

    public UIElement GetHeaderError () {
        return new Button(By.xpath("//header//section/div//span/span"), getPageName(),
                "saved-card-atm-pin-checkbox");
    }

    @Override
    public Link tabSavedUPI(int index) {
        return new Link(By.xpath("(//section[@data-key='upipush']//input)[" + index +"]/following-sibling::span"), getPageName(), "saved-upi-tab") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };


    }



    @Override
    public UIElement getTextCODMessage() {
        return new UIElement(By.xpath("//*[@value='cod']/../../following-sibling::div[1]//span"), getPageName(), super.getTextCODMessage().getElementName()) {
            @Override
            public String getText() {
                return super.getWrappedElement().getText().replace("Rs", "₹");
            }
        };
    }

    @Override
    public Link tabBankMandate(){
        return new Link(By.xpath("//*[@data-key='bm']//img"), getPageName(), "tab-bank-mandate");
    }

    @Override
    public Link selectBankMandateBank(String bankname){
        return new Link(By.xpath("//div[text()='" + bankname +"']/preceding-sibling::div[contains(@class,'circle')]/img"), getPageName(), "bank-mandate-bank");
    }

    @Override
    public Link tabSavedEmi() {
        return new Link(By.xpath("//*[contains(text(),'EMI Available')]"),getPageName(),"tab-saved-emi");
    }

    @Override
    protected void fillAndSubmitEMI_SavedCard(Constants.PayMode payMode, PaymentDTO emiDetails) {
        savedCard(emiDetails.getCreditCardNumber()).click();
        linkEmiPlans().click();
//        tabSavedEmi().waitUntilClickable();
//        tabSavedEmi().click();

        //linkEmiPlans().waitUntilClickable();
        //linkEmiPlans().click();
//        emiDurationOption(emiDetails.getMonth()).waitUntilClickable();
//        emiDurationOption(emiDetails.getMonth()).click();
        if(proceedBtn().isElementPresent())
            proceedBtn().click();
        textBoxSavedCardCVV().waitUntilVisible();
        textBoxSavedCardCVV().clearAndType(emiDetails.getCvvNumber());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public UIElement savedCard(String cardNumber) {
        return new UIElement(By.xpath("//*[text()='" + cardNumber.substring(cardNumber.length() - 4, cardNumber.length()) + "']//ancestor::section//input/following-sibling::span"), getPageName(), "saved-card-radio-btn");

    }
    @Override
    public UIElement MerchantName() {
        return new UIElement(By.xpath("//div[contains(@class,'fw700')]"), getPageName(), "Merchant Name");
    }

    @Override
    public UIElement loginSection() {
        return new UIElement(By.xpath("//*[text()='SELECT AN OPTION TO PAY']/following-sibling::div[1]"), getPageName(), "login-section") {
            @Override
            public boolean isEnabled() {
                return !Arrays.asList(this.getWrappedElement().getAttribute("class").split(" ")).contains("_3o7b");
            }
        };
    }
    @Override
    public List<WebElement> getBankMandateList() {
        return DriverManager.getDriver().findElements(By.xpath("//div[contains(@class,'circle')]/img"));
    }

    @Override
    public List<WebElement> getBankMandateListNew() {
        return DriverManager.getDriver().findElements(By.xpath("//*[contains(@class,'ptm-nb-list-item')]"));
    }


    @Override
    public TextBox textBoxCardNumber() {
        return new TextBox(By.xpath("//input[contains(@class,'cardNo')]"),
                getPageName(), "card-number-field") {


            @Override
            public void type(CharSequence... keysToSend) {
                String cardNo = "";
                for (CharSequence charSequence : keysToSend) {
                    cardNo += charSequence.toString();
                }
                this.waitUntilEditable();
                pause(1);
                //10 Retries
                for (int i = 0; i < 10; i++) {
                    if (i != 0) {
                        closeIcon().click();
                        pause(1);
                    }
                    super.type(cardNo.substring(0, 6));
                    pause(1);
                    super.type(cardNo.substring(6));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (textBoxCardNumber().getAttribute("value").replaceAll(" ", "").equals(cardNo))
                        break;
                }
            }
        };
    }

    @Override
    public UIElement subscriptionTray(){
//            DriverManager.getDriver().findElement(By.cssSelector(".icon-chevron_down_dark._3trB")).click();
        return new UIElement(By.xpath("//*[text()='Subscription Details']/following::div"), getPageName(), "subscription-tray");
    }

    public void validateSubscriptionTrayText(){
        //  Your next payment  will be deducted on 29 June 2021
        Assertions.assertThat(subscriptionTray().getText()).startsWith("Your next payment will be deducted");
    }



    @Override
    public UIElementV3 backBtn() {
        return new UIElementV3(By.xpath("//div[contains(@class,'pos-r')]/section"), "go-back-btn");
    }

    public UIElement cancelPaymentYes() {
        return new UIElement(By.xpath("//*[text()='Yes']"), getPageName(), "cancelYesButton");
    }



    public String bankFormErrorMsg()
    {
        return new UIElement(By.xpath("//div[@class='pu-title']/following-sibling::div//div[contains(@class,'xs-l')]/div"),getPageName(),"failedbankForm-error-nessage").getText();
    }

    @Override
    public void clickChangeNumber() {
        waitUntilLoads();
        DriverManager.getDriver().findElement(By.linkText("Change")).click();
    }

    @Override
    public String ResponseMsgRequestOtp() {
        return DriverManager.getDriver().findElement(By.xpath("//p[contains(@class,'xsd-wrap')]")).getText();
    }

    @Override
    public PopUpV2 modalRetryPayment() {
        return new PopUpV2(By.xpath("//div[contains(@class,'popup-global')]"), "retry-payment-modal") {
            @Override
            public void accept() {
                Button button = new Button(By.xpath("//div[contains(@class,'popup-global')]//button"), getPageName(), "retry-payment-modal-accept-button");
                button.waitUntilClickable();
                button.click();
            }

            @Override
            public void close() {
                throw new UnsupportedOperationException();
            }
        };
    }


    public UIElement subsLabelDueInfo(){
        // return new UIElement(By.xpath("//*[contains(@class,'sub-info-strip fs12')]"),getPageName(),"Due-info-label");
        return new UIElement(By.xpath("//*[contains(@class,'ib')]//img[contains(@class,'info-icon')]"),getPageName(),"Due-info-label");
    }
    public UIElement subsDetailsRecurringAmount(){
        return new UIElement(By.xpath("//*[contains(text(),'Recurring Bill Amount*')]"),getPageName(),"Recurrring-info-label");
    }
    public UIElement subscriptionDetails() {
        return new UIElement(By.xpath("//*[contains(@class,'info-icon xs-c-pointer')]"), getPageName(), "view-subscription_details");
    }
    public UIElement toBePaidTab() {
    //    return new UIElement(By.xpath("(//*[text() ='To be paid now'])[2]"),getPageName(),"to-be-paid");
        return new UIElement(By.xpath("//*[text() ='Amount to be Paid Now']"),getPageName(),"to-be-paid");

    }

    public UIElement savedBankMandate(){
        return new UIElement(By.xpath("//*[@id='ptm-smb']"),getPageName(),"saved-bank-mandate");
    }
    public UIElement savedBankMandateName(){
        return new UIElement(By.xpath("//*[@id='ptm-smb']/div[1]/div/span"),getPageName(),"saved-bank-mandate-name");
    }

    @Override
    public UIElement invalidVpaText(){
        return new UIElement(By.xpath("//*[contains(@class,'passcode')]//span"), getPageName(), "invalid-vpa");
    }

    @Override
    public UIElement linkedFDBalance(){
        return new UIElement(By.xpath("//*[contains(text(), ' Linked FD balance ')]"), getPageName(), "Linked-FD-balance");
    }
    @Override
    public UIElement redemptionTextLabel(){
        return new UIElement(By.xpath("//*[contains(text(), ' from your Fixed Deposit ')]"),getPageName(),"redemption-text");
    }

    @Override
    public UIElement enterPPBLPasscodeMessage(){
        return new UIElement(By.xpath("//*[contains(@class,'_1uX1')]"),getPageName(),"enter-ppbl-passcode-message");
    }
    @Override
    public UIElement getEMILogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='ptm-emi']//img"), getPageName(), "EMI Logo Url");
    }

    @Override
    public UIElement getCardLogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='ptm-card']//img"), getPageName(), "CARD Logo Url");
    }

    @Override
    public UIElement getUpiLogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='ptm-upi']//img"), getPageName(), "UPI Logo Url");
    }

    @Override
    public UIElement getNBLogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='ptm-nb']//img"), getPageName(), "NB Logo Url");
    }


    @Override
    public TextboxV3 emiAnotherCard() {
        return new TextboxV3(By.xpath("//div[contains(@class,'choosed-emi')]/preceding-sibling::div[1]//div"), "emi-another-card-error-msg");
    }

    @Override
    public String getErrorMessageAfterEnteringCard() {
        return DriverManager.getDriver().findElement(By.xpath("(//div[@class='pu-title']/following-sibling::div/div)[2]/div")).getText();
    }

    @Override
    public UIElement loginStrip() {
        return new UIElement(By.xpath("//div[text()='Enter Mobile Number']"), getPageName(), "login-strip"){

            @Override
            public void assertDisabled(){
                UIElement uiElement = new UIElement(By.xpath("//div[@class='card-list']/div[1]"), getPageName(), "");
                if(!(uiElement.getAttribute("class").contains("_25TI"))){
                    throw new AssertionError("LoginStrip is not Disabled");
                }
            }
            @Override
            public void click() {
                super.waitUntilClickable();
                super.click();
            }
        };
    }

    public void fillLoginOtp(String sixDigitOtp) {
        loginOtpBox().clearAndType(sixDigitOtp);
    }


    @Override
    public void logout(User user) {
        waitUntilLoads();
        UIElement profileArrow = new UIElement(By.xpath("//header//*[contains(text(), '" + user.mobNo() + "')]/../..//img"),
                getPageName(), "user-profile-arrow");


        if (isLogoutVisible()) {
            profileArrow.click();
        }

        profileArrow.click();
        UIElement logoutLink = new UIElement(By.xpath("//*[contains(text(), 'Logout from Paytm')]"),
                getPageName(), "Login-with-Another-Paytm-Account-link");


        logoutLink.click();
        waitUntilLoads();
    }

    @Override
    public void clickFailedTxnGotItButtonIfDisplayed() {
        //do nothing
    }

    @Override
    public UIElement UserBankName()
    {
        return new UIElement(By.xpath("//label[contains(text(),'Account Holder')]//following-sibling::input"),getPageName(),"selected-bank");
    }


    @Override
    public UIElement IfscDetails()
    {
        return new UIElement(By.xpath("//label[text()='IFSC']//following-sibling::input"),getPageName(),"ifsc-details-bank");
    }
    @Override
    public UIElement BankDetails()
    {
        return new UIElement(By.xpath("//label[text()='Bank Account Number']//following-sibling::input"),getPageName(),"bank-Account-No");
    }

    @Override
    public UIElement validateOTPLimitBreachOnTop()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Oops ! You have reached OTP limit')]"),getPageName(),"otp-limit-msg-top");
    }

    @Override
    public void fillInvalidCCDetails(PaymentDTO ccDetails) {
        scrollToElement(tabCreditCard());
        tabCreditCard().waitUntilClickable();
        tabCreditCard().click();
        if (!(ccDetails.getCreditCardNumber() == null || ccDetails.getCreditCardNumber().isEmpty()))
            textBoxCardNumber().clearAndType(ccDetails.getCreditCardNumber());
        if (!(ccDetails.getExpMonth() == null || ccDetails.getExpMonth().isEmpty()))
            fillExpiryMonth(ccDetails.getExpMonth());
        if (!(ccDetails.getExpYear() == null || ccDetails.getExpYear().isEmpty()))
            fillExpiryYear(ccDetails.getExpYear().substring(2, 4));
        if (!(ccDetails.getCvvNumber() == null || ccDetails.getCvvNumber().isEmpty())) {
            textBoxCVVNumber().waitUntilVisible();
            textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        }
    }

    @Override
    public UIElement error_invalidExpiryDate(){
        return new UIElement(By.xpath("//p[contains(text(),'Expiry')]"),getPageName(),"invalid-expirydate-msg");
    }

    @Override
    public UIElement getError_invalidCVV(){
        return new UIElement(By.xpath("//p[contains(text(),'CVV')]"),getPageName(),"invalid-cvv-msg");
    }

    @Override
    public UIElement getPaytmLogoBlue() {
        return new Link(By.xpath("//*[contains(@class,'top-headerbox')]//*[contains(@src,'pg-blue')]"), getPageName(),"paytmLogoBlue");
    }

    @Override
    public UIElement footerLogoBlue() {
        return new UIElement(By.xpath("//footer//*[contains(@src,'pg-logo-blue')]"), getPageName(), "ptm-footer-logo-blue");
    }

    @Override
    public UIElement knowMoreLink(){
        return new UIElement(By.xpath("//*[@class='know-more']"), getPageName(), "ptm-know_more");
    }

    @Override
    public UIElement getKnowMoreLinkText(){
        return new UIElement(By.xpath("//*[contains(@class, 'popup')]//*[@class='_3TQT']"), getPageName(), "ptm-wallet-kn-msg");
    }

    @Override
    public UIElement knowMoreLinkPopup(){
        // "Okay. Got it" will be clicked present in know more popup
        return new UIElement(By.xpath("//*[contains(@class, 'popup')]//*[contains(@class, 'btn-primary')]"), getPageName(), "ptm-pref-btn");
    }

    @Override
    public UIElement getUserDeactivatedErrorMessage(){
        return new UIElement(By.xpath("//*[contains(@class, 'paytm-wallet')]//*[@class='error']"), getPageName(), "ptm-wallet-inactive");
    }

    @Override
    public Boolean isWalletDisabled(){
        UIElement walletDisabled = new UIElement(By.xpath("//*[contains(@class, 'paytm-wallet')]//input[@id='checkbox']"), getPageName(), "ptm-wallet-disabled");
        return !walletDisabled.isEnabled();
    }

    @Override
    public UIElement convinenceCharge() {
        return new UIElement(By.xpath("//div[contains(@class,'ib')]//img[1]"), getPageName(), "convenience-fee-dropdown");
    }

    @Override
    public String getKnowMoreText(){
        this.knowMoreLink().click();
        this.pause(1);
        String guidelines = this.getKnowMoreLinkText().getText();
        this.pause(1);
        this.knowMoreLinkPopup().click();
        return guidelines;
    }
    @Override
    public UIElement qrCodeCheckoutJSText(){
        return  new UIElement(By.xpath("//div[@id='ptm-qr']//*[contains(@class,'_9PPQ fw700 ')]"), getPageName(), "QR Text");
    }
    @Override
    public UIElement enabledPaymodes(){
        return  new UIElement(By.xpath("//div[@id='ptm-qr']//*[contains(@class,'_2NFB sub-label mt5')]"), getPageName(), "Enabled Paymodes");
    }
    @Override
    public UIElement infoStripPaymodes(){
        return new UIElement(By.xpath("//p[contains(text(), 'Payment Options')]//following-sibling::p"), getPageName(), "Disabled Paymodes");
    }

    @Override
    public UIElement upiTitle(){
        return new UIElement(By.xpath("//div[@class='pu-title']"), getPageName(), "UPI Title");
    }
    @Override
    public UIElement getEnhancedUPIText(){
        //return new UIElement(By.xpath("//*[text()='Enter VPA ID']/preceding::div[2]"), getPageName(), "UPI Text");
        return new UIElement(By.xpath(".//div[contains(@class,'upi-select')]"), getPageName(), "UPI Text");
    }



//    <=======================================================================================>

    public List<String> upiHandlers()
    {
        List<UIElement> upiHandlers = UIElements.getMultiple(By.cssSelector("._KGPE"),getPageName(),"upi-handlers");
        List<String> list = new ArrayList<>();
        for(UIElement upi : upiHandlers)
        {
            list.add(upi.getText());
        }

        return list;
    }

    @Override
    public UIElement otpLimitReachedMsg(){
        return new UIElement(By.xpath("//span[contains(.,'You have reached OTP validation limit for this order." +
                " Please use any other paymode to continue.')]"), getPageName(), "otp-limit-message"){
            @Override
            public void assertVisible(){
                UIElement uiElement = new UIElement(By.xpath("//span[contains(.,'You have reached OTP validation limit for this order." +
                        " Please use any other paymode to continue.')]"), getPageName(), "otp-limit-message");
                Assertions.assertThat(uiElement.getText()).isNotEmpty();
            }
        };
    }

    @Override
    public Link bankmandateAuthMode(String authmode){
        return new Link(By.xpath("//*[@name='authenticatemode']//following::span[contains(.,'"+authmode+"')]"), getPageName(), "bank-mandate-authmode");
    }

    @Override
    public UIElement upiIntentPayButton(){
        return new UIElement(By.xpath("//span[text()='Select a UPI APP']"), getPageName(), "upi-intent-pay-button");
    }

    @Override
    public UIElement noQRInfoPaymodes(){
        return new UIElement(By.xpath("//div[@id='ptm-login']//*[@class='_jEw_ pos-a text-center']"), getPageName(),"Info Strip Disabled Paymode");
    }

    @Override
    public UIElement noQRAvailableText(){
        return new UIElement(By.xpath("//div[@id='ptm-login']//*[@class='box-title']"), getPageName(), "No QR Text");
    }

    @Override
    public UIElement noQRPaymodesPresent(){
        return new UIElement(By.xpath("//div[@id='ptm-login']//*[@class='_2Xch sub-label mt5']"), getPageName(), "No QR Enabled Paymodes");
    }

    private Button selectBank(String bankName) {
        return new Button(By.xpath("//div[text()='"+bankName+"']"),getPageName(),"bank-icon");
    }

    private void searchBank(String bankName)
    {
        new TextBox(By.xpath("//input[@placeholder='Search Bank']"),getPageName(),"search-bank-nb-txt").clearAndType(bankName);
        new TextBox(By.xpath("//span[text()='"+bankName+"']"),getPageName(),bankName + " nb").click();
    }

    @Override
    public CashierPage selectOtherNetBanking(String bankName) {
        selectBank("View All Banks").waitUntilClickable();
        selectBank("View All Banks").click();
        searchBank(bankName);
        return this;
    }

    @Override
    public UIElement bankMandateSubscribe() {
        return new UIElement(By.xpath("//span[contains(text(),'to Subscribe')]"), getPageName(), "BankMandate- Pay - and- Subscribe - button");
    }

    public List<String> preLoginScreenElements()
    {
        List<UIElement> uiElements = UIElements.getMultiple(By.cssSelector("main-inner pos-r body-bg-global"),getPageName(),"pre-login-validation");
        List<String> list = new ArrayList<>();
        for(UIElement ui : uiElements)
        {
            list.add(ui.getText());
        }

        return list;
    }

    @Override
    public int getNBIconCount() {
        return  UIElements.getMultiple(By.xpath("//img[@class='centerlized'][contains(@src,'native/bank')]"),getPageName(),"All NB icon count").size();
    }

    public UIElement selectedBank()
    {
        return new UIElement(By.xpath("//*[@class='fs14']"),getPageName(),"Select your bank");
    }

    public CashierPage assertSelectedBank(String bankName)
    {
        bankName = bankName.replace("Bank","").trim();
        selectedBank().assertContainsText(bankName);
        return this;
    }

    @Override
    public UIElement paytmHeaderLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'images/logo-blue.png')])[1]"),getPageName(),"paytm-logo");
    }
    @Override
    public UIElement paytmFooterLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'assets/logo.png')])[3]"),getPageName(),"paytm-logo");
    }

    @Override
    public UIElement verifyUpiNumericID() {
        return new UIElement(By.xpath("//a[text()='Verify']"),getPageName(),"UPI NUMBER");
    }

    @Override
    public UIElement UpiNumericId() {
        return new UIElement(By.xpath("//input[@placeholder='Enter Mobile No./UPI No.']"),getPageName(),"UPI NUMBER");
    }

    @Override
    public UIElement upiPollingPageInfoText(){
        return new UIElement(By.xpath("//*[contains(text(), 'Open Paytm App to Complete the Payment of ')]"), getPageName(), "upi polling page info text");
    }

    @Override
    public UIElement upiPollingPageTxnAmount(){
        return new UIElement(By.xpath("//p[contains(text(), 'Open Paytm App to Complete the Payment of ')]/span"), getPageName(), "upi polling page txn amount");
    }

    @Override
    public UIElement upiPollingPageWarningText(){
        return new UIElement(By.xpath("//*[contains(text(), 'Do not close or tap the back button until payment is completed')]"), getPageName(), "upi polling page warning message");
    }
    @Override
    public UIElement upiPollingPageMobileLogo(){
        return new UIElement(By.xpath("//img[contains(@src, 'web/assets/upi_paytm.png')]"), getPageName(), "upi polling page warning message");
    }

    @Override
    public TextBox textBox2FAPassCode() {
        return new TextBox(By.xpath("//input[contains(@placeholder,'Enter Passcode')]"), getPageName(), "ppbl-passcode-field");
    }

    @Override
    public TextBox textBox2FAIncorrectPasscode() {
        return new TextBox(By.xpath("//div[contains(text(),'You have entered incorrect passcode. Kindly retry.')]"), getPageName(), "you have entered incorrect passcode");
    }

    @Override
    public TextBox textBox2FAIncompletePasscode() {
        return new TextBox(By.xpath("//p[contains(text(),'Enter the valid passcode')]"), getPageName(), "Enter the valid passcode");
    }

    @Override
    public Link tabUPISavedVPA(){
        return null;
    }
    @Override
    public UIElement OfferStripSavedPaymode(){
        return null;
    }
    @Override
    public UIElement applyOfferTextSavedInstruments(){
        return null;
    }
    @Override
    public UIElement payWithOtherUpiAppsPreLogin()
    {
        return new UIElement(By.xpath("//div[contains(text(),' Pay using UPI Apps')]"), getPageName(),"pay with other upi apps");
    }
    @Override
    public UIElement verified2FAPasscodeErrorMsg() {
        return null;
    }

    @Override
    public UIElement verified2FALinkForgotPasscode() {
        return null;
    }

    @Override
    public UIElement verified2FATextForgotPasscode() {
        return null;
    }


    public List<String> UPIIntentApps()
    {
        List<UIElement> uiElements = UIElements.getMultiple(By.xpath("//*[contains(@class, 'ptm-upi-bank-name ptm-ellipsis ptm-body-color')]"),getPageName(),"upi-apps");
        List<String> list = new ArrayList<>();
        for(UIElement ui : uiElements)
        {
            list.add(ui.getText());
        }

        return list;
    }


    public UIElement payViaNotificationTab()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Pay on Paytm App')]"),getPageName(),"pay via notification heading");
    }

    @Override
    public UIElement qrImg(){
        return new UIElement(By.xpath("//*[@class='ptm-pay-with-upi']"),getPageName(),"qr-upi-images");
    }

    public  TextBox cardShortcut(String lastFourDigit)
    {
        return new TextBox(By.xpath("//p[contains(text(),' Card - "+lastFourDigit+"')]"),
                    getPageName(), "card-number-field");

    }
    public  TextBox savedToken(String lastFourDigit)
    {
        return new TextBox(By.xpath("//p[contains(text(),' Card - "+lastFourDigit+"')]"),
                getPageName(), "card-number-field");
    }
}
