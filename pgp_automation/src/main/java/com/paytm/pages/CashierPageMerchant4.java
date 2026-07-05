package com.paytm.pages;

import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CashierPageMerchant4 extends CashierPage {

    public CashierPageMerchant4() {
        super("cashier-page-merchant4-theme");
    }

    public Link linkRemoveSavedCard() {
        return new Link(By.linkText("Remove Card"), getPageName(), "remove-saved-card-button");
    }
    public UIElement getErrorCC_EMI_NOTSET()
    {
        return null;
    }
    public UIElement save_card_visible()
    {
        return null;
    }


    public UIElement getError_invalidCVV()
    {
        return null;
    }

    @Override
    public UIElement tabAdvanceDeposit() {
        return null;
    }

    @Override
    public UIElement WalletTitle() {
        return null;
    }

    @Override
    public Button btnRemoveConfirmYes() {
        return new Button(By.cssSelector("#delete-confirm-modal input[value='Yes']"), getPageName(), "remove-saved-card-confirmation-yes-button");
    }

    public Button btnRemoveConfirmNo() {
        return new Button(By.cssSelector("#delete-confirm-modal input[value='No']"), getPageName(), "remove-saved-card-confirmation-no-button");
    }

    @Override
    public Button BtnRemoveSavedCard() {
        return new Button(By.xpath("//a[text()='Remove Card']"), getPageName(), "remove-saved-card-button");
    }

    /**
     * use checkBoxPpbl method
     *
     * @return
     */
    @Deprecated
    @Override
    public CheckBox ppblBalanceCheck() {
        return null;
    }

    public UIElement insufficientPPBLBalanceIconMsg() {
        return new UIElement(By.xpath("//div[@id='totalPaymentBankBalance']/following-sibling::div[1]"), getPageName(), "ppbl-insufficient" +
                "Balance");
    }


    @Override
    public Button buttonPpblSumbit() {
        return new Button(By.cssSelector("input.btn-normal.blur-btn-new"), getPageName(), super.buttonPostPaidPayNow().getElementName());
    }

    @Override
    public UIElement promoCode() {
        return new UIElement(By.cssSelector(".notification .promocode-options-msg"), getPageName(), "promo-code");
    }

    @Override
    public Button buttonPGPayNow() {
        return new Button(By.cssSelector(".cards-tabs:not(.hide) .content.active button[type='submit']"), getPageName(), "pg-pay-button") {
            @Override
            public void click() {
                this.waitUntilClickable();
                pause(1);
                super.click();
            }
        };
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
    public CheckBox walletBalanceCheck() {
        return new CheckBox(By.id("walletBalanceAmt"), getPageName(), "ppi-balance");
    }


    public CheckBox checkBoxPPI() {
        return new CheckBox(By.className("btn-link"), getPageName(), "ppi-balance-checkbox") {

            @Override
            public boolean isChecked() {
                UIElement uiElement = new UIElement(By.cssSelector(".walletCheckbox .tick.cb-icon-check"), getPageName(), "");
                return uiElement.isDisplayed();
            }
        };

    }

    @Override
    public CheckBox checkboxPaytmCC() {
        return new CheckBox(By.cssSelector("#paytmDigitalCard button span.cb-icon-check"), getPageName(), "paytm-digital-card-checkbox");
    }

    @Override
    public UIElement insufficientBalanceIcon() {
        return null;
    }

    @Override
    public UIElement insufficientBalanceIconMsg() {
        return new UIElement(By.cssSelector(".paytm-Bank.PPBL .notification"), getPageName(), "insufficient-balance-icon-msg");
    }

    @Override
    public UIElement lblCVV() {
        return new UIElement(By.cssSelector("label[for='cvvNumber']"), getPageName(), "cvv-label");
    }

    @Override
    public UIElement postpaidSignUpStrip() {
        return null;
    }

    @Override
    public UIElement lblExpMonth() {
        return new UIElement(By.cssSelector("label[for='ccExpMonth']"), getPageName(), "expiry-month-label");
    }

    @Override
    public UIElement imgDefaultCVVIcon() {
        return null;
    }

    @Override
    public UIElement imgAmexCVVIcon() {
        return new UIElement(By.cssSelector("input[class*='amex']"), getPageName(), "amex-cvv-icon");
    }

    @Override
    public UIElement loginStrip() {
        return null;
    }

    @Override
    public RadioButton radioBtnNetBankingOrATM(String bankName) {
        return new RadioButton(
                By.cssSelector(
                        ".cards-tabs:not(.hide) .content.active .radio#" + bankName.toUpperCase() + " .btn-link"),
                getPageName(), "radioBtnNetBankingOrATM");
    }

    @Override
    public Button btnCheckBalancePostpaid() {
        return null;
    }

    @Override
    public UIElement lblInsufficientBalancePostpaid() {
        return null;
    }

    @Override
    public List<WebElement> lblUPIpush() {
        return null;
    }

    @Override
    public UIElement upiPushSection() {
        return null;
    }

    @Override
    public UIElement rdbtnSavedCardATMPin() {
        return new UIElement(By
                .xpath("//span[text()='Use ATM PIN']/preceding-sibling::input[@class='pcb checkbox paymentIdebit fl']"),
                getPageName(), "atm-pin-radiobutton");
    }

    @Override
    public UIElement radioBtnSavedCard(String cardId) {
        return new UIElement(By
                .xpath("//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//input[@id='" + cardId + "']/following-sibling::span//button/span"),
                getPageName(), "saved-card-radiobutton");
    }

    @Override
    public Button savedCardPayNow() {
        return new Button(By
                .xpath("//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//button[@id='scSubmit']"),
                getPageName(), "saved-card-pay-button");
    }

    @Override
    public UIElement lblErrMsg() {
        return new UIElement(By.xpath("//div[contains(@class,'card content active')]//div[@id='errorMsg']"),
                getPageName(), "error-msg");
    }

    @Override
    public UIElement imgScanPayQRCode() {
        return null;
    }

    @Override
    public UIElement imgPaytmQRSymbol() {
        return null;
    }

    @Override
    public UIElement imgUpiQRSymbol() {
        return null;
    }

    @Override
    public UIElement prnVerifySection() {
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
    public boolean isPPIChecked() {
        WebElement checkedWalletCheckbox = DriverManager.getDriver().findElement(By.cssSelector("#paytmWalletCheckButton button span.cb-icon-check"));
        return checkedWalletCheckbox.isDisplayed();
    }

    @Override
    public boolean isPPBLChecked() {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        Object obj = js.executeScript("return document.getElementById('paytm-paymentBank-checkbox').checked.toString()");
        return Boolean.parseBoolean((String) obj);
    }


    @Override
    public boolean isPaytmCCChecked() {
        return checkboxPaytmCC().isDisplayed();
    }

    @Override
    public UIElement error_invalidExpiryDate() {
        return null;
    }

    @Override
    public Link tabSavedEmi() {
        return null;
    }

    public UIElement convinenceCharge() {
        return new UIElement(By.xpath("//span[text()='Convenience Charge + GST ']"), getPageName(), "Convenience fee dropdown");
    }

    public UIElement convinenceFeeCCAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Credit Card']/following-sibling::div"), getPageName(), "Convenience fee dropdown");
    }

    public UIElement convinenceFeeDCAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Debit Card']/following-sibling::div"), getPageName(), "Convenience fee dropdown");
    }

    public UIElement convinenceFeeNBAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Debit Card']/following-sibling::div"), getPageName(), "Convenience fee dropdown");
    }

    public UIElement convinenceFeeUPIAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Debit Card']/following-sibling::div"), getPageName(), "Convenience fee dropdown");
    }

    public UIElement convinenceFeeWalletAmount() {
        return new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Paytm Balance']/following-sibling::div"), getPageName(), "Convenience fee PAYTM WALLET");
    }

    public List<UIElement> ListOfPayModsOnCashier() {
        return UIElements.getMultiple(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Paytm Balance']/following-sibling::div"), getPageName(), "Convenience fee PAYTM WALLET");//UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Paytm Balance']/following-sibling::div"), getPageName(), "Convenience fee PAYTM WALLET");
    }

    @Override
    public CheckBox rememberMeCheckbox() {
        throw new NoSuchElementException("Element is not available on current theme");
    }

    @Override
    public PopUpV2 modalRetryPayment() {
        throw new NoSuchElementException("Element is not available on current theme");
    }

    @Override
    public Select dropdownEmiBanks() {
        return new Select(By.cssSelector(".cards-tabs:not(.hide) .content.active .emi-bank-map:not(.hide) .emiBankSelect"), getPageName(),
                "emi-bank-dropdown");
    }

    @Override
    public UIElement emiDurationOption(int EMIMonth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIElement selectEMIPlan() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIElement labelPaymodeInfoMsg() {
        throw new UnsupportedOperationException();
    }

    public Select dropdownNB() {
        return new Select(By.cssSelector(".cards-tabs:not(.hide) .content.active .nbSelect"), getPageName(),"select-nb-banks-dropdown");
    }

    @Override
    public UIElement postpaidOnboard_TNC() {
        return null;
    }

    @Override
    public void submitPostPaidOnboarding() {

    }

    @Override
    public UIElement postpaidOnboard_Accept_PayButton() {
        return null;
    }


    public CheckBox checkboxPPBL() {
        return new CheckBox(By.cssSelector("#paytmPaymentBank button"), getPageName(), "ppbl-checkbox") {
            @Override
            public boolean isChecked() {
                UIElement uiElement = new UIElement(By.cssSelector("#paytmPaymentBank .tick.cb-icon-check"), getPageName(), "");
                return uiElement.isDisplayed();
            }
        };
    }

    @Override
    public UIElement ppblNotificationMsg() {
        return new UIElement(By.cssSelector("#paymentBank .notification"), getPageName(), "ppbl-notification-msg");
    }

    public TextBox textBoxPPBLPassCode() {
        return new TextBox(By.id("banktxtPassCode"), getPageName(), super.textBoxPPBLPassCode().getElementName());
    }

    public TextBox textBoxPostpaidPassCode() {
        return new TextBox(By.id("txtPassCode"), getPageName(), super.textBoxPostpaidPassCode().getElementName());
    }

    @Override
    public UIElement savedCard(String cardNumber)
    {

        return new UIElement(By.xpath("//input[starts-with(@value,'"+cardNumber.substring(0,4)+"') and contains(@value,'"+cardNumber.substring(cardNumber.length()-4,cardNumber.length())+"')]//parent::div//preceding-sibling::div//button[@class='btn-link']"),getPageName(),"saved-card-btn");
    }
    @Override
    public UIElement EMIsavedCard(String cardNumber)
    {

        return new UIElement(By.xpath("(//input[starts-with(@value,'"+cardNumber.substring(0,4)+"') and contains(@value,'"+cardNumber.substring(cardNumber.length()-4,cardNumber.length())+"')]//parent::div//preceding-sibling::div//button[@class='btn-link'])[2]"),getPageName(),"saved-card-btn");
    }

    @Override
    public void RequestloginOTP(String mobileNumber) {

    }

    @Override
    public String ResponseMsgRequestOtp() {
        return null;
    }

    public TextBox textBoxSavedCardCVV() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active div:not([style='display: none;'])[class='saveCardCvvDiv fr'] .scCvvInput"), getPageName(), "saved-card-cvv-field");
    }

    public void logout(User user) {
        UIElement profileArrow = new UIElement(By.xpath("//header//div[contains(text(), '"+user.mobNo()+"')]/following-sibling::i"),
                getPageName(), "user-profile-arrow");
        profileArrow.click();
        UIElement logoutLink = new UIElement(By.xpath("//div[contains(text(), 'Login with Another Paytm Account')]"),
                getPageName(), "Login-with-Another-Paytm-Account-link");
        logoutLink.click();
    }

    @Override
    public void fillAndSubmitEMIDetails(PaymentDTO emiDetails) {
        tabEMI().waitUntilClickable();
        tabEMI().click();
        dropdownEmiBanks().selectByVisibleText(emiDetails.getBankName());
        radioButtonEMIOption(emiDetails.getMonth()).click();

        if (emiDetails.getCreditCardNumber() != null && !emiDetails.getCreditCardNumber().equals("")) {
            textBoxCardNumber().clearAndType(emiDetails.getCreditCardNumber());
        }
        if (emiDetails.getExpMonth() != null && !emiDetails.getExpMonth().equals("")) {
            fillExpiryMonth(emiDetails.getExpMonth());
        }
        if (emiDetails.getExpYear() != null && !emiDetails.getExpYear().equals("")) {
            fillExpiryYear(emiDetails.getExpYear().substring(2, 4));
        }
        if (emiDetails.getCvvNumber() != null && !emiDetails.getCvvNumber().equals("")) {
            textBoxCVVNumber().clearAndType(emiDetails.getCvvNumber());
        }
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public void fillAndSubmitEMIDCDetails(PaymentDTO emiDetails) {

    }

    public UIElement subscriptionDetails()
    {
        //Never going to execute on default
        return null;
    }
    public UIElement subscriptionTray()
    {
        //Never going to execute on default
        return null;
    }

    @Override
    public boolean wallet_dropdown() {

        return false;
    }

    @Override
    public UIElement txtPromoCode()
    {
        return null;
    }

    @Override
    public UIElement verifyVPALinkText() {
        return null;
    }

    @Override
    public UIElement loginSection() {return  null;}
    @Override
    public UIElement MerchantName() {return  null;}
    @Override
    public UIElement mobileNumber() {return  null;}

    @Override
    public void directLogin(User user) {

    }

    @Override
    public String getErrorMessageAfterEnteringCard()
    {
        return null;
    }

    @Override
    public List<WebElement> getBankMandateList() {
        return null;
    }

    @Override
    public List<WebElement> getBankMandateListNew() {
        return null;
    }

    @Override
    public UIElement IfscDetails() {
        return null;
    }

    @Override
    public UIElement BankDetails() {
        return null;
    }

    @Override
    public UIElement UserBankName() {
        return null;
    }

    @Override
    public UIElement applyPromoText() {
        return null;
    }

    @Override
    public UIElement tpvAccountAlert() {
        return null;
    }

    @Override
    public UIElement tpvAccountInfo() {
        return null;
    }

    @Override
    public UIElement tpvAccountAlertInfo() {
        return null;
    }

    @Override
    public UIElement PayButtonWithWallet() {
        return null;
    }

    @Override
    public UIElement PayButtonWithPPBL() {
        return null;
    }

    @Override
    public UIElement PayButtonWithSC() {
        return null;
    }

    @Override
    public UIElement PayButtonWithPostPaid() {
        return null;
    }

    @Override
    public void clickRetryIncorrectOTPBtn(){
    }

    @Override
    public UIElement getBankOfferDiscountMsg() {
        return null;
    }

    @Override
    public UIElement verified2FAPasscodeErrorMsg() {
        return null;
    }
    @Override
    public UIElement applyOfferText() {
        return null;
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
    public UIElement verified2FATextForgotPasscode() {
        return null;
    }

    @Override
    public UIElement verified2FALinkForgotPasscode() {
        return null;
    }

    public UIElement payViaNotificationTab() {return null;}

    public  TextBox cardShortcut(String lastFourDigit)
    {
        return null;
    }
    public  TextBox savedToken(String lastFourDigit)
    {
        return null;
    }
}
