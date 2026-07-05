package com.paytm.pages;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.support.ui.WebDriverWait;


public class CashierPageCheckoutjsWap extends CashierPage {

    public CashierPageCheckoutjsWap() {
        super("Checkoutjs cashier page wap");
    }
    @Override
    public UIElement getErrorCC_EMI_NOTSET()
    {
        return new UIElement(By.xpath("//*[@id='cardNotSupportedElement']"),getPageName(),"error-message");
    }


    @Override
    public UIElement getError_invalidCVV()
    {
        return new UIElement(By.xpath("//*[@id='invalid_cvv']"),getPageName(),"error-message");
    }

    @Override
    public UIElement ccdc_cardIframe() {
        return new UIElement(By.className("ptm-card-iframe"), getPageName(), "ptm-card-iframe");
    }

    @Override
    public UIElement sc_CheckoutIframe() {
        return new UIElement(By.className("ptm-sc-iframe"), getPageName(), "sc-iframe");
    }

    @Override
    public UIElement promoOffersList(){
        return new UIElement(By.id("checkout-offers"),getPageName(),"promoList");
    }

    @Override
    public void fillAndSubmitCCDetails(PaymentDTO ccDetails, Boolean saveCard) {
        tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(ccdc_cardIframe());
        if (!(ccDetails.getCreditCardNumber() == null || ccDetails.getCreditCardNumber().isEmpty()))
            textBoxCardNumber().clearAndType(ccDetails.getCreditCardNumber());
        if (!(ccDetails.getExpMonth() == null || ccDetails.getExpMonth().isEmpty()))
            fillExpiryMonth(ccDetails.getExpMonth());
        if (!(ccDetails.getExpYear() == null || ccDetails.getExpYear().isEmpty()))
            fillExpiryYear(ccDetails.getExpYear().substring(2));
        if (!(ccDetails.getCvvNumber() == null || ccDetails.getCvvNumber().isEmpty())) {
            textBoxCVVNumber().waitUntilVisible();
            textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        }
        DriverManager.getDriver().switchTo().defaultContent();
        pause(2);
        if(saveCard) {
            //find xpath for saveCard checkbox in checkoutjs
        }
        buttonPGPayNow().click();
    }

    @Override
    public void fillAndSubmitCCDetailsWithSinglePayMode(PaymentDTO ccDetails, Boolean saveCard) {
        DriverManager.getDriver().switchTo().frame(ccdc_cardIframe());
        if (!(ccDetails.getCreditCardNumber() == null || ccDetails.getCreditCardNumber().isEmpty()))
            textBoxCardNumber().clearAndType(ccDetails.getCreditCardNumber());
        if (!(ccDetails.getExpMonth() == null || ccDetails.getExpMonth().isEmpty()))
            fillExpiryMonth(ccDetails.getExpMonth());
        if (!(ccDetails.getExpYear() == null || ccDetails.getExpYear().isEmpty()))
            fillExpiryYear(ccDetails.getExpYear().substring(2));
        if (!(ccDetails.getCvvNumber() == null || ccDetails.getCvvNumber().isEmpty())) {
            textBoxCVVNumber().waitUntilVisible();
            textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        }
        DriverManager.getDriver().switchTo().defaultContent();
        pause(2);
        if(saveCard) {
            //find xpath for saveCard checkbox in checkoutjs
        }
        buttonPGPayNow().click();
    }

    @Override
    public void fillAndSubmitDCDetails(PaymentDTO dcDetails, Boolean saveCard) {
        tabDebitCard().click();
        DriverManager.getDriver().switchTo().frame(ccdc_cardIframe());
        if (!(dcDetails.getDebitCardNumber() == null || dcDetails.getDebitCardNumber().isEmpty()))
            textBoxCardNumber().clearAndType(dcDetails.getDebitCardNumber());
        if (!(dcDetails.getExpMonth() == null || dcDetails.getExpMonth().isEmpty()))
            fillExpiryMonth(dcDetails.getExpMonth());
        if (!(dcDetails.getExpYear() == null || dcDetails.getExpYear().isEmpty()))
            fillExpiryYear(dcDetails.getExpYear().substring(2));
        if (!(dcDetails.getCvvNumber() == null || dcDetails.getCvvNumber().isEmpty())) {
            textBoxCVVNumber().waitUntilVisible();
            textBoxCVVNumber().clearAndType(dcDetails.getCvvNumber());
        }
        DriverManager.getDriver().switchTo().defaultContent();
        pause(2);
        if (saveCard) {
            //find xpath for saveCard checkbox in checkoutjs
        }
        buttonPGPayNow().click();
    }

    @Override
    public void fillAndSubmitPPBLDetail(PaymentDTO ppblDetails) {
        checkboxPPBL().check();
        DriverManager.getDriver().switchTo().frame(ppbl_passcodeIframe());
        textBoxPPBLPassCode().waitUntilEditable();
        textBoxPPBLPassCode().clearAndType(ppblDetails.getPasscode());
        DriverManager.getDriver().switchTo().defaultContent();
        buttonPGPayNow().click();
    }

    private void fillAndSubmitSavedUPIDetails(PaymentDTO paymentDTO) {
        tabSavedUPI(paymentDTO.savedVpaIndex).waitUntilClickable();
        tabSavedUPI(paymentDTO.savedVpaIndex).click();
        //checkboxVPAList().get(0).click();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public UIElement postpaidSignUpStrip() {
        return new UIElement(By.xpath(("//*[@class='_2w4Y']")),getPageName(),"");
    }

    @Override
    public Link tabSavedUPI(int index) {
        return new Link(By.xpath("(//*[@id='checkout-upi-push']/div)[" + index +"]"), getPageName(), "saved-upi-tab") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }


    public TextBox textBoxExpiryMonth() {
        return new TextBox(
                By.id("cardExpirationMonth"),
                getPageName(), "expiry-month-field");
    }

    public TextBox textBoxExpiryYear() {
        return new TextBox(
                By.id("cardExpirationYear"),
                getPageName(), "expiry-year-field");
    }


    @Override
    public void fillExpiryMonth(String expiryMonth) {
        this.textBoxExpiryMonth().waitUntilEditable();
        this.textBoxExpiryMonth().clearAndType(expiryMonth);
    }

    @Override
    public void fillExpiryYear(String expiryYear) {
        this.textBoxExpiryYear().waitUntilEditable();
        this.textBoxExpiryYear().clearAndType(expiryYear.substring(expiryYear.length() - 2));
    }

    @Override
    public Link tabCreditCard() {
        return new Link(By.xpath("//div[@id='checkout-card']//p[contains(@class,'ptm-paymode-name')]"), getPageName(), super.tabCreditCard().getElementName()){
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//div[@id='checkout-card']//p[@class='ptm-paymode-name']/parent::div//preceding-sibling::span[@class='ptm-checkmark']"),
                        getPageName(), "checked-paymode");
                if (checked.isElementPresent())
                    return true;
                else
                    return false;
            }
        };
    }

    @Override
    public UIElement MGVradioButton() {
        return new RadioButton(By.xpath("//div[@id='checkout-mgv']//input"), getPageName(), "mgv-radio-checkbox");
    }

    @Override
    public Link tabDebitCard() {
        return new Link(By.xpath("//p[contains(.,'Debit / Credit Cards')] | //div[@id='checkout-card']//p[contains(@class,'ptm-paymode-name')]"), getPageName(), super.tabCreditCard().getElementName());
    }

    @Override
    public Link tabNetBanking() {
        return new Link(By.xpath("//p[text()='Net Banking']"), getPageName(), super.tabNetBanking().getElementName()){
        @Override
        public boolean isSelected() {
            Link checked = new Link(By.xpath("//div[@id='ptm-nb-inner']"),
                    getPageName(), "checked-paymode");
            if (checked.isElementPresent())
                return true;
            else
                return false;
        }
    };
    }


    @Override
    public UIElement verifyVPALinkText() {
        return new UIElement(By.xpath("//*[text()='Verify VPA']"), getPageName(), "verify-vpa-linktext");
    }

    @Override
    public Link tabUPICollect() {
        return new Link(By.xpath("//div[text()='Pay using your UPI ID']"), getPageName(), "upi-collect-tab"){
            @Override
            public boolean isSelected() {
                return upiCollectTabTick().isElementPresent();
            }
        };
    }


    @Override
    public RadioButton upiIntentTabTick(){
        RadioButton radioButton = new RadioButton(By.xpath("//div[text()='Pay using UPI App']/parent::label/input")
                , getPageName(), "upi-intent-tab-tick");
        return radioButton;
    }

    @Override
    public RadioButton upiCollectTabTick(){
        RadioButton radioButton = new RadioButton(By.xpath("//div[text()='Pay using your UPI ID']/parent::label/input']")
                , getPageName(), "upi-collect-tab-tick");
        return radioButton;
    }


    public void fillLoginOtp(String loginOtp) {
        loginOtpBox().clearAndType(loginOtp);
    }


    @Override
    public Button buttonWalletPayNow() {
        return new Button(By.xpath
                ("//p[contains(.,'Pay using your saved payment instruments')] | //div[@id='checkout-button']/button"), getPageName(),
                "wallet-pay-button");
    }



    @Override
    public TextBox textBoxPhoneNumber() {
        return new TextBox(By.id("mobile_input"), getPageName(), super.textBoxPhoneNumber().getElementName());
    }

    public TextBox loginOtpBox() {
        return new TextBox(By.xpath("//input[@id='ptm-otp-input']"), getPageName(), "otp-box");
    }


    @Override
    public void fillAndSubmitWalletDetails(PaymentDTO walletDetails) {
        //buttonWalletPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public UIElement loginFrame() {
        return new UIElement(By.xpath("//p[contains(.,'Pay using your saved payment instruments')]"), getPageName(),
                "wallet-pay-button");
    }

    @Override
    public void login(User user) {
        signin(user.mobNo(), user.password());
        pause(4);
    }

   @Override
   public TextBox textBoxVPA() {
       return new TextBox(By.xpath("//input[contains(@placeholder,'Enter VPA')] | //label[text()='Enter VPA ID']/following-sibling::input | //input[contains(@id,'ptm-upi-input')]"), getPageName(),"vpa-field");
   }

    @Override
    protected void fillAndSubmitNBDetails(PaymentDTO nbDetails) {
        tabNetBanking().click();
        dropdownNB().selectByValue(nbDetails.getBankName());
        pause(2);
        buttonPGPayNow().click();
    }

    @Override
    protected void fillAndSubmitNBDetailsWithSinglePayMode(PaymentDTO nbDetails) {
        dropdownNB().selectByValue(nbDetails.getBankName());
        pause(2);
        buttonPGPayNow().click();
    }

    @Override
    public UIElement promoCode() {
        return null;
    }

    @Override
    public boolean wallet_dropdown() {
        return false;
    }

    @Override
    public Button buttonPGPayNow() {
        return new Button(By.xpath("//div[@id='checkout-button']/button"), getPageName(), "pay-button");
    }



    public TextBox textBoxSavedCardCVV() {
        return new TextBox(By.id("ppbl_pin"), getPageName(), "saved-cards-cvv");
    }


    @Override
    protected void fillAndSubmitSavedCardDetails(PaymentDTO savedCardDetails) {

        tabSavedCard().click();

        if (!(savedCardDetails.getCvvNumber() == null || savedCardDetails.getCvvNumber().isEmpty())) {
            DriverManager.getDriver().switchTo().frame(sc_CheckoutIframe());
            textBoxSavedCardCVV().clearAndType(savedCardDetails.getCvvNumber());
            DriverManager.getDriver().switchTo().defaultContent();
        }
        buttonPGPayNow().click();
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
        return new Button(By.xpath("//div[@id='checkout-button']/button"), getPageName(), "pay-button");
    }

    @Override
    public List<WebElement> lblUPIpush() {
        return null;
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
    public UIElement insufficientBalanceIcon() {
        return null;
    }

    @Override
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

    @Override
    public UIElement loginStrip() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-login-box')]"), getPageName(), "login-strip");
    }

    @Override
    public UIElement lblCVV() {
        return null;
    }

    @Override
    public UIElement lblExpMonth() {
        return null;
    }

    @Override
    public RadioButton radioBtnNetBankingOrATM(String bankName) {
        return null;
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
    public UIElement WalletTitle() {
        return null;
    }

    @Override
    public UIElement savedCard(String cardNumber) {
        return null;
    }

    @Override
    public UIElement EMIsavedCard(String cardNumber) {
        return null;
    }


    @Override
    public UIElement subscriptionTray() {
        return null;
    }

    @Override
    public void RequestloginOTP(String mobileNumber) {

    }

    @Override
    public String ResponseMsgRequestOtp() {
        return null;
    }

    /**
     * use checkBoxPPI
     *
     * @return
     */
    @Override
    public CheckBox walletBalanceCheck() {
        return null;
    }



    @Override
    public CheckBox ppblBalanceCheck() {
        return null;
    }

    @Override
    public CheckBox checkboxPaytmCC() {
        return null;
    }

    @Override
    public UIElement upiPushSection() {
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
        return false;
    }

    @Override
    public boolean isPPBLChecked() {
        return false;
    }

    @Override
    public boolean isPaytmCCChecked() {
        return false;
    }

    @Override
    public UIElement PayButtonWithWallet(){
        return new UIElement( By.xpath("//*[@id='checkout-wallet']//button"),getPageName(),"Pay-Button-Wallet");
    }

    @Override
    public UIElement PayButtonWithPPBL(){
        return new UIElement( By.xpath("//*[@id='checkout-payment-bank']//button"),getPageName(),"Pay-Button-PPBL");
    }

    @Override
    public UIElement PayButtonWithSC(){
        return new UIElement( By.xpath("//*[@id='ptm-checkout-sc']/following-sibling::div//button"),getPageName(),"Pay-Button-SC");
    }

    @Override
    public UIElement PayButtonWithPostPaid(){
        return new UIElement( By.xpath("//*[@id='checkout-bank-credit']//button"),getPageName(),"Pay-Button-PostPaid");
    }

    @Override
    public UIElement error_invalidExpiryDate() {
        return new UIElement(By.id("invalid_date"), pageName, "error_invalidExpiryDate");
    }


    @Override

    public UIElement ppblNotificationMsg() {
        return null;
    }


    @Override
    public void logout(User user) {

    }


    @Override
    public Link tabEMI() {
        return new Link(By.xpath("//p[contains(.,'EMI')]"), getPageName(), "emi-tab");
    }

    @Override
    public void fillAndSubmitEMIDCDetails(PaymentDTO emiDetails) {

    }


    public TextBox textBoxExpiryYearEMI() {
        return new TextBox(
                By.id("yy"),
                getPageName(), "expiry-year-field-emi");
    }
    public UIElement emiPlan(){
        return new UIElement(By.xpath("//div[@id='selectPlan']/div[contains(text(),'Select EMI Plan')][1]"), getPageName(), "emi-plan");
    }


    @Override
    public UIElement tabAdvanceDeposit() {
        return null;
    }

    @Override
    public UIElement loginSection() {
        return null;
    }

    @Override
    public UIElement txtPromoCode() {
        return null;
    }


    @Override
    public CheckBox checkboxPPBL() {
        return new CheckBox(By.xpath("//*[@id='checkout-payment-bank']/div/label/span"), getPageName(), "checkbox-ppbl");
    }

    @Override
    public UIElement postpaidOnboard_TNC() {
        return null;
    }

    @Override
    public UIElement postpaidOnboard_Accept_PayButton() {
        return null;
    }


    @Override
    public UIElement convinenceFeeCCAmount() {
        return null;
    }

    @Override
    public UIElement convinenceFeeDCAmount() {
        return null;
    }

    @Override
    public UIElement convinenceFeeNBAmount() {
        return null;
    }

    @Override
    public UIElement MerchantName() {
        return null;
    }

    @Override
    public UIElement convinenceFeeUPIAmount() {
        return null;
    }

    @Override
    public UIElement mobileNumber() {
        return null;
    }

    @Override
    public UIElement convinenceFeeWalletAmount() {
        return null;
    }

    @Override
    public List<UIElement> ListOfPayModsOnCashier() {
        return null;
    }

    @Override
    public CheckBox rememberMeCheckbox() {
        return new CheckBox(By.xpath("//label[@class='ptm-container ptm-mt-16'][contains(.,'Remember Me')]/span[@class='ptm-check']"),
                getPageName(), "Remember-this-mobile-number-for-future-login_checkbox") {
            @Override
            public boolean isChecked() {
                return this.isElementPresent();
            }
        };
    }

    @Override
    public PopUpV2 modalRetryPayment() {
        return null;
    }


    @Override
    public UIElement emiDurationOption(int EMIMonth) {
        return null;
    }

    @Override
    public UIElement selectEMIPlan() {
        return null;
    }

    @Override
    public UIElement labelPaymodeInfoMsg() {
        return null;
    }


    @Override
    public void submitPostPaidOnboarding() {

    }

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
        return DriverManager.getDriver().findElements(By.xpath("//*[contains(@class,'ptm-nb-list')]/div/div"));
    }

    @Override
    public List<WebElement> getBankMandateListNew() {
        return DriverManager.getDriver().findElements(By.xpath("//*[contains(@class,'ptm-nb-list-item')]"));
    }

    @Override
    public UIElement applyPromoText() {
        return new UIElement(By.xpath("//*[contains(text(), 'cashback')]"), getPageName(), "applyPromoText");
    }
    @Override
    public UIElement applyOfferText() {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-cash-app  ptm-body-color ptm-mg-lt-0')]//div"), getPageName(), "applyOfferText");
    }

    @Override
    public UIElement applyOfferTextSavedInstruments() {
        return new UIElement(By.xpath("//div[contains(@class, 'ptm-message ptm-bo-cashtrip')]"), getPageName(), "applyOfferText");
    }

    @Override
    public UIElement OfferStripSavedPaymode() {
        return new UIElement(By.xpath("//div[contains(text(),\"Offer Available\")]"), getPageName(), "Offer available text");
    }
    @Override
    public Link tabUPISavedVPA() {
        return new Link(By.xpath("//p[contains(text(),\"Pay using Paytm UPI\")]"), getPageName(), "saved VPA");
    }

    @Override
    public UIElement tpvAccountAlert() {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-account-alert')]/section"), getPageName(), "accountAlert");
    }

    @Override
    public UIElement tpvAccountInfo() {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-account-alert')]//img[@alt='i']"), getPageName(), "accountInfo-i");
    }

    @Override
    public UIElement tpvAccountAlertInfo() {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-account-alert')]//div[contains(@class,'ptm-tt-content')]"), getPageName(), "accountInfo");
    }

    @Override
    public Link bankmandateAuthMode(String authmode){
        switch (authmode) {
            case "Debit Card":
                return  new Link(By.xpath("//*[@value='DEBIT_CARD']"), getPageName(), "bank-mandate-authmode");

            case "Net Banking":
                return  new Link(By.xpath("//*[@value='NET_BANKING']"), getPageName(), "bank-mandate-authmode");

            default:
                return  new Link(By.xpath("//*[@value='"+authmode+"']"), getPageName(), "bank-mandate-authmode");
        }
    }

    @Override
    public Button buttonPostPaidPayNow() {
        return new Button(By.xpath("//div[contains(@class,'PAYTM_DIGITAL_CREDIT')]//button"), getPageName(), "postpaid-pay-button");
    }

    @Override
    public UIElement getBankOfferDiscountMsg(){
        return new UIElement(By.cssSelector(".ptm-message"),"cashier-page","discount-msg");
    }
    @Override
    public UIElement donothaveaccesstoregisteredmobilenumber() {
        return new UIElement(By.xpath("//label[text()='Do not have access to registered mobile number' and @class='ptm-lbl ptm-pos-r']"), getPageName(), "cancel payment option");
    }


//    <--------------------------------------------------------------------------------------------------->

    @Override
    public Select dropdownNB() {
        return new Select(By.xpath("//div[contains(@class,'ptm-nb-select')]//div[contains(.,'View All Banks')]"), getPageName(), "select-nb-banks-dropdown") {
            @Override
            public void selectByValue(String value) {
                this.waitUntilClickable();
                this.click();
                DriverManager.getWebDriverElementWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[contains(@class, 'body-bg')][contains(@class, 'overlay-container')]")));
                List<UIElement> banks = UIElements.getMultiple(By.xpath("//*[@id='checkout-nb-banks-list']//span"), getPageName(), "nb-banks-list");
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
    public Button buttonSecureSignIn() {
       // return new Button(By.xpath("//button[contains(@class,'ptm-login-btn')]"), getPageName(), "sign-in-button" /*this.buttonPGPayNow().getElementName()*/);
        return new Button(By.xpath("//*[text()='Proceed'] | //button[contains(@class,'ptm-login-btn')]"), getPageName(), "sign-in-button");
    }

    @Override
    public Button otpVerifyButton() {
        return new Button(By.xpath("//button[contains(@class,'ptm-enterotp-btn')]"), getPageName(), "otp-verify-button");
    }
    @Override
    public UIElement UserBankName()
    {
        return new UIElement(By.xpath("//*[contains(@class,'placeholder')][contains(.,'Account Holder')]/../input"),getPageName(),"selected-bank");
    }


    @Override
    public UIElement IfscDetails()
    {
        return new UIElement(By.xpath("//*[contains(@class,'placeholder')][contains(.,'IFSC')]/../input"),getPageName(),"ifsc-details-bank");
    }
    @Override
    public UIElement BankDetails()
    {
        return new UIElement(By.xpath("//*[contains(@class,'placeholder')][contains(.,'Enter Bank Account Number')]/../input"),getPageName(),"bank-Account-No");
    }

    @Override
    public void clickChangeBank() {
        DriverManager.getDriver().findElement(By.xpath("//*[text()='Change']")).click();
    }

    @Override
    public Link ifscErrorMessage(){
        return new Link(By.xpath("//*[contains(@class,'placeholder')][contains(.,'IFSC')]/../p"),getPageName(),"ifsc-error-message");
    }

    @Override
    public Link bankAccountErrorMessage(){
        return new Link(By.xpath("//*[contains(@class,'placeholder')][contains(.,'Enter Bank Account Number')]/../p"),getPageName(),"Bank-account-error-message");
    }

    @Override
    public Link userErrorMessage(){
        return new Link(By.xpath("//*[contains(@class,'placeholder')][contains(.,'Account Holder')]/../p"),getPageName(),"user-name-error-message");
    }

    @Override
    public UIElement merchantName(){
        return new UIElement(By.xpath("//*[@id=\"ptm-checkout-header\"]//span[contains(@class,'ptm-header')]"),
                getPageName(), "merchantName");
    }


    @Override
    public Button closeCcDcDetailBtn() {
        return new Button(By.xpath("//*[@id=\"checkout-card\"]//span[contains(@class,'ptm-cross')]"), getPageName(), "close-button");
    }

    @Override
    public void clickInvalidOTPEnteredButtonIfDisplayed() {
        if (failedOTPEnteredButton().isDisplayed()) {
            failedOTPEnteredButton().click();
        }
    }

    public Button failedOTPEnteredButton(){
        return new Button(By.xpath("//*[contains(.,'OTP')]//following-sibling::button[contains(@class,'retry')]"),getPageName(), "failed-txn-got-it-button");
    }

    @Override
    public UIElement totalAmtPG() {
        return new UIElement(By.xpath("(//img[@class='ptm-lock-img']/..)[1]"), getPageName(), "pg-charge-fee-amount") {
            //img[@class='ptm-lock-img']/..
            @Override
            public String getText() {
                return super.getText().trim().substring(5,9);
            }
        };
    }


    @Override
    public UIElement chargeFeeAmtPG() {
        return new UIElement(By.xpath("//span[contains(@class,'ptm-conv-txt')]"), getPageName(), "pg-charge-fee-amount") {
            //
            @Override
            public String getText() {
                pause(1);
                return super.getText().trim().substring(0,5);
            }
        };
    }

    public Select dropdownEmiBanks() {
//        new UIElement(By.cssSelector(".ptm-emi-bank-select"), getPageName(), "emi-bank-dropdown").click();
        return new Select(By.cssSelector(".ptm-emi-type-block"), getPageName(), "emi-bank-dropdown") {
            @Override
            public void selectByVisibleText(String value) {
                this.waitUntilClickable();
                this.click();
                DriverManager.getWebDriverElementWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".ptm-emi-type-block")));
                List<UIElement> banks = UIElements.getMultiple(By.cssSelector(".ptm-emi-bankcard"), getPageName(), "emi-banks-list");
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
    public void validateButtonText(String DA, String discount){
        String buttonText = payButton().getText();
        Assertions.assertThat(buttonText).contains("Effective price after offer ₹"+DA);
        String text="Pay ₹10\n" +
                "Effective price after offer ₹9.50";
    }

    @Override
    protected void fillAndSubmitUPIDetails(PaymentDTO upiDetails) {
        scrollToElement(tabUPI());
        tabUPI().waitUntilClickable();
        tabUPI().click();
        if(upiIdRadioBtn().isElementPresent()){
            upiIdRadioBtn().click();
        }
        textBoxVPA().click();
        pause(2);
        textBoxVPA().clearAndType(upiDetails.getVpa());
//        verifyVPALinkText().click();
//        pause(2);
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    protected void fillAndSubmitUPIDetailsWithSinglePayMode(PaymentDTO upiDetails) {
        if(upiIdRadioBtn().isElementPresent()){
            upiIdRadioBtn().click();
        }
        textBoxVPA().click();
        pause(2);
        textBoxVPA().clearAndType(upiDetails.getVpa());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public Link tabUPI() {
        return new Link(By.xpath("(//*[contains(@id,'checkout-upi')])[last()]//img"), getPageName(),super.tabUPI().getElementName());
    }

    @Override
    public Link tabBankMandate(){
        return new Link(By.xpath("//*[@id='checkout-bm']//*[contains(text(),'Bank Account')]"), getPageName(), "tab-bank-mandate");
    }

    public Link clearInputPhoneNumber(){
        return new Link(By.xpath("//*[contains(@class,'ptm-input-clear')]"), getPageName(), "clear-phone-number");
    }

    @Override
    public void signin(String mobileNumber, String password) {
        //        loginFrame().click();
        clearInputPhoneNumber().click();
        textBoxPhoneNumber().clearAndType(mobileNumber);
        buttonSecureSignIn().click();
        pause(2);
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        String otp = "123456";
        if(password.equals("888888")){
            otp=password;
        }
        //otp = AuthUtil.getOtp(mobileNumber);
        fillLoginOtp(otp);
        otpVerifyButton().click();
        waitUntilLoads();
    }

    @Override
    public void selectEMIBank(PaymentDTO paymentDTO){
        String bankName = paymentDTO.getBankName();
        UIElement EMIBank = new UIElement(By.xpath("//*[text()='"+bankName+"']"), getPageName(), "Select Bank from EMI Bank List");
        EMIBank.click();
    }

    @Override
    public TextBox textBoxCardNumber() {
        return new TextBox(By.xpath("//div[@class='card-content']//input[@id='cardNumber'] | //*[@id='cardnumber']"),
                getPageName(), "card-number-field");
        //div[@class='card-content']//input[@id='cardNumber'] | //input[@id='cardnumber']
    }


    @Override
    public void fillEMICardDetails(PaymentDTO paymentDTO){
        DriverManager.getDriver().switchTo().frame(ccdc_emiIframe());
        textBoxCardNumber().clearAndType(paymentDTO.getEmiCard());

    }

    @Override
    public TextBox textBoxCVVNumber() {
        return new TextBox(By.id("cvv"),
                getPageName(), "cvv-field");

    }

//    @Override
//    public void fillAndSubmitEMIDetails(PaymentDTO paymentDTO) {
//        scrollToElement(tabEMI());
//        tabEMI().click();
//        pause(1);
//        selectEMIBank(paymentDTO);
//        fillEMICardDetails(paymentDTO);
//        pause(1);
//        emiPlan().click();
//        DriverManager.getDriver().switchTo().defaultContent();
//        proceedEMIBtn().click();
//        DriverManager.getDriver().switchTo().frame(ccdc_expCvv_cardIframe());
//        textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
//        textBoxExpiryYearEMI().waitUntilEditable();
//        textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
//        pause(1);
//        textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
//        DriverManager.getDriver().switchTo().defaultContent();
//        buttonPGPayNow().click();
//
//    }

    @Override
    public void fillAndSubmitEMIDetails(PaymentDTO emiDetails)
    {
        scrollToElement(tabEMI());
//        scrollTo(tabEMI());
        tabEMI().click();
        DriverManager.getDriver().switchTo().frame(ccdc_emiIframe());
        textBoxCardNumberEMI().clearAndType(emiDetails.getEmiCard());
        waitUntilLoads();
        emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(ccdc_expCvv_cardIframe());
        textBoxExpiryMonthEMI().clearAndType(emiDetails.getExpMonth());
        textBoxExpiryYearEMI().waitUntilEditable();
        textBoxExpiryYearEMI().clearAndType(emiDetails.getExpYear().substring(2));
        textBoxCVVNumber().clearAndType(emiDetails.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        buttonPGPayNow().click();
    }

    @Override
    public TextBox textBoxExpiryMonthEMI() {
        return new TextBox(
                By.id("mm"),
                getPageName(), "expiry-month-field-emi");
    }

    @Override
    public CheckBox checkBoxPPI() {
        return new CheckBox(By.xpath("//span[@class='ptm-check']"), getPageName(), "checkbox-ppi");
    }

    @Override
    public Link tabSavedCard() {
        return new Link(By.xpath("//div[contains(@class,'ptm-sc-main')]//span[contains(@class,'check')]"), getPageName(), "saved-cards-tab")  {
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//div[@id='ptm-checkout-sc']//div[@id='ptm-checkout-iframe']"),
                        getPageName(), "checked-paymode");
                if (checked.isElementPresent())
                    return true;
                else
                    return false;
            }
        };
    }

    @Override
    public UIElement imgScanPayQRCode() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-img-container')]//img"), getPageName(), "img-ScanPay-QRCode");
    }

    public void validateBankAccountErrorMsg(){
        Assertions.assertThat(bankAccountErrorMessage().getText()).isEqualTo("Please enter bank account Number");
    }

    public void validateIfscErrorMsg(){
        Assertions.assertThat(ifscErrorMessage().getText()).isEqualTo("Please enter your bank IFSC");
    }

    public void validateUserErrorMsg(){
        Assertions.assertThat(userErrorMessage().getText()).isEqualTo("Please enter account holder's name");
    }


    public UIElement upiCollectOption() {
        return new UIElement(By.xpath("//div[@class='ptm-upi-select ']"), getPageName(), "upi-collect-option");
    }

    @Override
    public Link tabUPIIntent() {
        return new Link(By.xpath("//div[contains(text(),'Pay using UPI App')]"), getPageName(), "upi-intent-tab"){
            @Override
            public boolean isSelected() {
                return !(upiCollectOption().isElementPresent());
            }
        };
    }

    @Override
    public UIElement upiIntentPayButton(){
        return new UIElement(By.xpath("//button[text()='Select a UPI App']"), getPageName(), "upi-intent-pay-button");
    }

  @Override
  public UIElement upiIntentPcfInfoText(){
    return new UIElement(By.xpath("//*[@class=\"ptm-upi-pcf-info-txt\"]"),
        getPageName(), "pcfInfoTextForUpiIntent");
  }
  @Override
  public UIElement upiIntentPcfInfoIcon(){
    return new UIElement(By.xpath("//*[@class=\"ptm-upi-pcf-info-icon\"]"),
        getPageName(), "pcfInfoIconForUpiIntent");
  }

    @Override
    public void clickRetryIncorrectOTPBtn(){
        if (new UIElement(By.xpath("//button[text()='Retry']"), getPageName(), "merchantName").isElementPresent())
        {  new UIElement(By.xpath("//button[text()='Retry']"), getPageName(), "merchantName").click();}
    }

    @Override
    public UIElement tabUPIId(){
        return new UIElement(By.xpath("//*[text()='Pay using your UPI ID']"), getPageName(), "select-upi-id");
    }

    @Override
    public UIElement convinenceCharge() {
        return new UIElement(By.xpath("//*[contains(text(),'Convenience fee')]"), getPageName(), "Convenience-fee-not-visible");
    }

    @Override
    public UIElement save_card_visible() {

        return new UIElement(By.xpath("//*[text() ='or Choose your saved cards']"),getPageName(),"save-card-not-present");

    }
    public UIElement emiPlanCard(){
        return new Link(By.xpath("//div[@class='ptm-scemi-card ptm-pos-r ptm-global-border xs-plancard  ptm-active-plan ptm-nav-selectable ptm-nav-focused']"), getPageName(), "emi-plan-card");
    }
    @Override
    public UIElementV3 tabPPBL() {
        return new UIElementV3(By.xpath("//*[contains(text(),'PPBL')]"), "ppbl-pay-mode-tab");
    }
    @Override
    public UIElementV3 tabBOB() {
        return new UIElementV3(By.xpath("//div[contains(@class, 'ptm-nb-list-item')]//div[contains(@class, 'ptm-bank-name') and text()='BANK OF BARODA']"), "bank-of-baroda-tab");    }

    @Override
    public Link tabSavedEmi() {
        return new Link(By.xpath("//*[contains(text(),'EMI Available')]"), getPageName(), "tab-saved-emi");
    }
    @Override
    protected void fillAndSubmitEMI_SavedCard(Constants.PayMode payMode, PaymentDTO emiDetails) {
        tabSavedEmi().waitUntilClickable();
        tabSavedEmi().click();
        pause(2);
//        buttonSecureSignIn().waitUntilClickable();
//        buttonSecureSignIn().click();
//        buttonPGPayNow().waitUntilClickable();
//        buttonPGPayNow().click();
        //       DriverManager.getDriver().switchTo().defaultContent();
        proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(Cvv_cardIframe());
        textBoxCVVNumberSavedcardEMI().clearAndType(emiDetails.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        buttonPGPayNow().click();
    }
    @Override
    public UIElement noQRAvailableText(){
        return new UIElement(By.xpath("//*[@id='checkout-login']//*[contains(@class,'ptm-log-inpheading')]"), getPageName(), "QR-Not-Present");
    }

    @Override
    public UIElement noQRInfoPaymodes(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-warnn-wrap')]"), getPageName(), "Paymodes-Not-Present");
    }

    @Override
    public UIElement noQRPaymodesPresent(){
        return new UIElement(By.xpath("//*[@id='checkout-login']//div[contains(@class,'ptm-body-color ptm-txinfo')]"), getPageName(), "Paymodes-Present");
    }

    @Override
    public UIElement infoStripPaymodes(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-wrap')]//*[contains(@class,'ptm-warnn-wrap')]"), getPageName(), "Info-Strip");
    }

    @Override
    public UIElement enabledPaymodes(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-wrap')]//*[contains(@class,'ptm-body-color ptm-txinfo')]"), getPageName(), "Enabled-Paymodes");
    }

    @Override
    public UIElement upiPaymodeUpiAppText(){
        return new UIElement(By.xpath("//div[contains(@id,'checkout-upi')]//*[contains(@class,'ptm-more-upi-apps-txt')]"),getPageName(),"upi-app-text");
    }

    @Override
    public UIElement upiPaymodeUpiAppImg(){
        return new UIElement(By.xpath("//*[contains(@id,'checkout-upi')]//*[@class='ptm-diff-upi-apps']"),getPageName(),"upi-paymode-upiApp-img");
    }


    @Override
    public UIElement qrCodeCheckoutJSText(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-wrap')]//*[contains(@class,'ptm-qr-pay-text')]"), getPageName(), "QR-Code");
    }

    @Override
    public UIElement knowMoreLink(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-know_more')]"), getPageName(), "know-more");
    }
    @Override
    public UIElement loginKnowMoreLink(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-kno-link')]"), getPageName(), "ptm-know_more");
    }
    @Override
    public UIElement cards(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-grid-wrap')]//*[contains(@class,'card-main')]"), getPageName(), "Cards-Tab");
    }

    @Override
    public Button closeSubsDetailsTab() {
        return new Button(By.xpath("//*[contains(@class,'ptm-close-wrap')]"), getPageName(), "close-subs-details-tab");
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
    public UIElement paytmHeaderLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'images/logo-blue.png')])[1]"),getPageName(),"paytm-logo");
    }

    @Override
    public UIElement paytmFooterLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'images/logo-blue.png')])[2]"),getPageName(),"paytm-logo");
    }

    @Override
    public Button bankMandateConfirmPay(){
        return new Button(By.xpath("//*[contains(@class,'ptm-custom-btn')]"), getPageName(), "bankmandate-pay-confirm");
    }

    @Override
    public UIElement subscriptionDetails() {
        return new UIElement(By.xpath("(//img[@class='ptm-info'])[2]"), getPageName(), "view-subscription_details");
    }
    @Override
    public UIElement headerTxtColor()
    {
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-header-color')]"), getPageName(), "headerTxtColor");
    }
    @Override
    public UIElement bodyBck_color()
    {
        return new UIElement(By.xpath("//div[@class='ptm-select-txt ptm-select-sticky xs-ptm-hidden ptm-body-bg']"), getPageName(), "bodyBck_color");
    }
    @Override
    public UIElement textColor()
    {
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-paytxtlbl')]"), getPageName(), "textColor");
    }
    @Override
    public UIElement paybuttonbck_color()
    {
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-custom-btn')]"), getPageName(), "paybuttonbck_color");
    }

    @Override
    public UIElement bankFooterLogo(){
        return new UIElement(By.xpath("//img[contains(@class,'ptm-custom-bank-logo')]"),getPageName(),"bank-footer-logo");
    }
    @Override
    public UIElement upiPollingPageInfoText()
    {
        return new UIElement(By.xpath("//*[contains(text(), 'Open Paytm App to Complete the Payment of ')]"), getPageName(), "upi polling info text");
    }

    @Override
    public UIElement upiPollingPageTxnAmount(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-pollinghead')]"), getPageName(), "upi polling page txn amount");
    }

    @Override
    public UIElement upiPollingPageWarningText(){
        return new UIElement(By.xpath("//*[contains(text(), 'Do not close or tap the back button until payment is completed')]"), getPageName(), "upi polling page warning message");
    }

    @Override
    public UIElement upiPollingPageMobileLogo(){
        return new UIElement(By.xpath("//img[contains(@src, 'assets/images/ptm-coll-icon.png')]"), getPageName(), "upi polling page warning message");
    }

    @Override
    public UIElement infoIconForPCF(){
        return new UIElement(By.xpath("(//img[contains(@src, 'images/ic-info.svg')])[2]"),getPageName(),"pcf-info-icon");
    }
    @Override
    public UIElement payButtonConvFeeMsg(){
        return new CheckBox(By.xpath("//span[contains(@class,'ptm-d-block')]"), getPageName(), "conv-fee-msg-pay-button");

    }

    @Override
    public UIElement convFeeMessageCashierPage(){
        return new UIElement(By.className("ptm-conv-wrapper"), getPageName(), "conv-msg-cashier-page");
    }

    public UIElement otherPaymentOption()
    {
        return new UIElement(By.xpath("//*[text()='More Payment Options']"),getPageName(),"other payment option in cashier page");
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

    public void validateCollectUPILimitMsg(String userErrorMsgForCollectUPI){
        String upilimitmsg = UPICollectlimitmsg().getText();
        System.out.println("msg on ui: "+upilimitmsg);
        Assertions.assertThat(upilimitmsg).isEqualTo(userErrorMsgForCollectUPI);
    }
    public UIElement UPICollectlimitmsg() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds ptm-merchant-limit-text-warn ptm-limiText-Other
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"), "Merchant cannot accept payments on UPI at the moment. Try using other options."));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"));
        return new UIElement(element, getPageName(),
                "");

    }
    public void validateIntentUPILimitMsg(String userErrorMsgForCollectUPI){
        String upilimitmsg = UPIIntentlimitmsg().getText();
        Assertions.assertThat(upilimitmsg).isEqualTo(userErrorMsgForCollectUPI);
    }
    public UIElement UPIIntentlimitmsg() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"), "Merchant cannot accept payments on UPI at the moment. Try using other options."));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"));
        return new UIElement(element, getPageName(),
                "");
    }

    public void validateColletPPIWalletLimitMsg(String userErrorMsgForUPI){
        String upilimitmsg = UPIColletPPIWalletlimitmsg().getText();
        Assertions.assertThat(upilimitmsg).isEqualTo(userErrorMsgForUPI);
    }
    public UIElement UPIColletPPIWalletlimitmsg() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"), "Merchant cannot accept Wallet on UPI at the moment. Try using other options."));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"));
        return new UIElement(element, getPageName(),
                "");
    }

    public void validateIntentPPIWalletLimitMsg(String userErrorMsgForUPI){
        String upilimitmsg = UPIIntentPPIWalletlimitmsg().getText();
        Assertions.assertThat(upilimitmsg).isEqualTo(userErrorMsgForUPI);
    }
    public UIElement UPIIntentPPIWalletlimitmsg() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"), "Merchant cannot accept Wallet on UPI at the moment. Try using other options."));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"));
        return new UIElement(element, getPageName(),
                "");
    }
    public void EligibilityUPICC_PPIWALLET(){
        String EligibilityMsg = UPICC_PPIWALLETEligibilityMsg().getText();
        System.out.println("UPI CC PPI msg: "+EligibilityMsg);
        Assertions.assertThat(EligibilityMsg).isEqualTo("This merchant is not accepting Credit Card, Wallet, Credit Line on UPI.");
    }
    public UIElement UPICC_PPIWALLETEligibilityMsg() {
      WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
      wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-cc-on-upi"), "This merchant is not accepting Credit Card, Wallet, Credit Line on UPI."));
      WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-cc-on-upi"));
        return new UIElement(element, getPageName(),
                "");
    }
    public void validateUPICCLimitMsg(String userErrorMsgForUPI){
        String upilimitmsg = UPI_CC_Msg(userErrorMsgForUPI).getText();
        System.out.println("Msg for UPI CC: "+upilimitmsg);
        Assertions.assertThat(upilimitmsg).isEqualTo(userErrorMsgForUPI);

    }
    public UIElement UPI_CC_Msg(String userErrorMsgForUPI) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-merchant-limit-text.ptm-padding-t20.ptm-limiText-Other"), userErrorMsgForUPI));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-merchant-limit-text.ptm-padding-t20.ptm-limiText-Other"));
        return new UIElement(element, getPageName(),
                "");}
    public void validateUPICCLimitMsgInnerUPI(String userErrorMsgForUPI){
        String EligibilityMsg = UPICC_Msg(userErrorMsgForUPI).getText();
        Assertions.assertThat(EligibilityMsg).isEqualTo(userErrorMsgForUPI);
    }
    public UIElement UPICC_Msg(String userErrorMsgForUPI) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"), userErrorMsgForUPI));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-merchant-limit-text-warn.ptm-limiText-Other"));
        return new UIElement(element, getPageName(),
                "");
    }

    public void validateUPICCNotAllowed(String userErrorMsgForUPI){
        String upilimitmsg = UPICC_NotAllowed(userErrorMsgForUPI).getText();
        Assertions.assertThat(upilimitmsg).isEqualTo("This merchant is not accepting Credit Card on UPI.");
    }
    public UIElement UPICC_NotAllowed(String userErrorMsgForUPI) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-cc-on-upi.ptm-body-color"), userErrorMsgForUPI));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-cc-on-upi.ptm-body-color"));
        return new UIElement(element, getPageName(),
                "");
    }

    public void validateUpiSubPayModeNotAllowed(String userErrorMsgForUPI){
        String upiErrorMsg = UpiSubPayModeNotAllowed(userErrorMsgForUPI).getText();
        Assertions.assertThat(upiErrorMsg).isEqualTo(userErrorMsgForUPI);
    }
    public UIElement UpiSubPayModeNotAllowed(String userErrorMsgForUPI) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-cc-on-upi"), userErrorMsgForUPI));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-cc-on-upi"));
        return new UIElement(element, getPageName(),
                "");
    }

    public UIElement offerStripHideButton()
    {
        return new UIElement(By.xpath("//*[text()='Hide']"),getPageName(),"Hide button in cashier page");
    }

    public List<String> UPIIntentApps() {
        List<UIElement> uiElements = UIElements.getMultiple(By.xpath("//*[contains(@class, 'ptm-upi-bank-name ptm-ellipsis ptm-body-color')]"), getPageName(), "upi-apps");
        List<String> list = new ArrayList<>();
        for (UIElement ui : uiElements) {
            list.add(ui.getText());
        }

        return list;
    }

    public UIElement payViaNotificationTab()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Pay on Paytm App')]"),getPageName(),"pay via notification heading");
    }

    @Override
    public UIElement qrImg()
    {
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

    @Override
    public UIElement pcfConvenienceInfoIcon() {
        return new UIElement(By.xpath("//*[@class='ptm-conv-wrapper ']/img"), getPageName(),"paymentRequesterDetails name");
        //return new UIElement(By.xpath("//*[@class='_Eevm']"), getPageName(),"pcf convenience fees Icon");
    }
    @Override
    public UIElement pcfConvenienceInfoText() {
        return new UIElement(By.xpath("//*[text()='Convenience Fees']/parent::node()/following::div"), getPageName(),"pcf convenience fees Text");
    }
}
