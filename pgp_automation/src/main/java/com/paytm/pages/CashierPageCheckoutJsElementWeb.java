package com.paytm.pages;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.ui.element.*;
import com.paytm.utils.merchant.util.AuthUtil;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CashierPageCheckoutJsElementWeb extends CashierPageCheckoutjsWeb{

    public CashierPageCheckoutJsElementWeb() {
        super();
        setPageName("Checkoutjs element cashier page web");
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
        buttonPGPayNow().click();
    }

    @Override
    protected void fillAndSubmitUPIDetails(PaymentDTO upiDetails) {
        //tabUPI().waitUntilClickable();
        //tabUPI().click();
        textBoxVPA().clearAndType(upiDetails.getVpa());
//        verifyVPALinkText().click();
        pause(2);
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    protected void fillAndSubmitUPIDetailsWithSinglePayMode(PaymentDTO upiDetails) {
        textBoxVPA().clearAndType(upiDetails.getVpa());
        pause(2);
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    public UIElement verifyVPALinkText() {
        return new UIElement(By.xpath("//*[text()='Verify VPA']"), getPageName(), "verify-vpa-linktext");
    }

    public TextBox textBoxExpiryMonth() {
        return new TextBox(
                By.id("cardExpirationMonth"),
                getPageName(), "expiry-month-field");
    }

    public TextBox textBoxExpiryMonthEMI() {
        return new TextBox(
                By.id("mm"),
                getPageName(), "expiry-month-field-emi");
    }

    public TextBox textBoxExpiryYear() {
        return new TextBox(
                By.id("cardExpirationYear"),
                getPageName(), "expiry-year-field");
    }

    public TextBox textBoxExpiryYearEMI() {
        return new TextBox(
                By.id("yy"),
                getPageName(), "expiry-year-field-emi");
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
        return new Link(By.xpath("//p[contains(.,'Credit')][contains(.,'Debit')]"), getPageName(), super.tabCreditCard().getElementName()){
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//p[contains(.,'Credit')][contains(.,'Debit')]/parent::div//preceding-sibling::span[@class='ptm-checkmark']"),
                        getPageName(), "checked-paymode");
                if (checked.isDisplayed())
                    return true;
                else
                    return false;
            }
        };
    }

    @Override
    public UIElement MGVradioButton() {
        return null;
    }

    @Override
    public void clickPgOverlay() {
        try {
            DriverManager.getDriver().manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            if (addNPayOverlay().isDisplayed()) {
                clickaddNPayOverlayElement().click();
            }
        } catch (Exception e) {
        } finally {
            DriverManager.getDriver().manage().timeouts().implicitlyWait(ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME.toSeconds(), TimeUnit.SECONDS);
        }
    }

    @Override
    public Link tabDebitCard() {
        return new Link(By.xpath("//div[@id='checkout-card']//*[contains(@class,'ptm-paymode-name')]"), getPageName(), super.tabCreditCard().getElementName());
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
    public Link tabUPI() {
        return new Link(By.xpath("//p[text()='UPI']"), getPageName(), super.tabUPI().getElementName()){
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//p[text()='BHIM UPI']/parent::div//preceding-sibling::span[@class='ptm-checkmark']"),
                        getPageName(), "checked-paymode");
                if (checked.isElementPresent())
                    return true;
                else
                    return false;
            }
        };
    }

    @Override
    public TextBox textBoxCardNumber() {
        return new TextBox(By.xpath("//div[@class='card-content']//input[@id='cardNumber'] | //input[@id='cardnumber']"),
                getPageName(), "card-number-field");
    }

    public  UIElement proceedToConvertEMIButton(){
        return new UIElement(By.xpath("(//button[normalize-space()='Proceed to Convert to EMI'])[1]"), getPageName(), "proceed-to-convert-emi-button");
    }

    @Override
    public TextBox textBoxCVVNumber() {
        return new TextBox(By.id("cvv"), getPageName(), "cvv-field");
    }

    @Override
    public void signin(String mobileNumber, String password) {
        textBoxPhoneNumber().clearAndType(mobileNumber);
        buttonSecureSignIn().click();
        pause(2);
        String otp = "123456";
        // Validating OTP by Mock now and not retrieving from logs any 6 digit otp will work
        //otp = AuthUtil.getOtp(mobileNumber);
        fillLoginOtp(otp);
        buttonSecureSignIn().click();
        waitUntilLoads();
    }

    public Button otpVerifyButton() {
        return new Button(By.xpath("//button[contains(.,'Verify')]"), getPageName(), "otp-verify-button");
    }

    public void fillLoginOtp(String loginOtp) {
        loginOtpBox().clearAndType(loginOtp);
    }

    @Override
    public UIElement promoOffersList(){
        return new UIElement(By.id("checkout-offers"),getPageName(),"promoList");
    }

    @Override
    public Button buttonWalletPayNow() {
        return new Button(By.xpath
                ("//p[contains(.,'Pay using your saved payment instruments')] | //div[@id='checkout-button']/button"), getPageName(),
                "wallet-pay-button");
    }

    @Override
    public Button buttonSecureSignIn() {
        return new Button(By.xpath("//button[contains(.,'Proceed')]"), getPageName(), "sign-in-button" /*this.buttonPGPayNow().getElementName()*/);
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
        this.checkBoxPPI().waitUntilChecked();
        this.checkBoxPPI().check();
        buttonWalletPayNow().waitUntilClickable();
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
        return new TextBox(By.xpath("//input[@placeholder='Enter VPA']"), getPageName(),
                "vpa-field");
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

    @Override
    public Link tabSavedCard() {
        return new Link(By.xpath("//*[@id='ptm-checkout-sc']//input"), getPageName(), "saved-cards-tab")  {
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
    public Link tabSavedUPI(int index) {
        return new Link(By.xpath("//*[@id='checkout-upi-push']["+index+"]//span[@class='ptm-checkmark']"), getPageName(), "saved-upi-tab") {
//        return new Link(By.xpath(("//*[@id='checkout-upi-push']//input["+index+"]")), getPageName(), "saved-upi-tab") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }


    public UIElement ppblPasscodeiframe() {
        return new UIElement(By.className("ptm-ppb-iframe"), getPageName(), "ppbl-passcode-iframe");
    }
    @Override
    public TextBox textBoxSavedCardCVV() {
        return new TextBox(By.id("ppbl_pin"), getPageName(), "saved-cards-cvv");
    }
    @Override
    public TextBox textBoxPPBLPassCode() {

        return new TextBox(By.xpath("//*[@id='ppbl_pin']"), getPageName(), "ppbl passcode field");
    }
    @Override
    public void fillAndSubmitPPBLDetail(PaymentDTO ppblDetails) {
        checkboxPPBL().check();
        DriverManager.getDriver().switchTo().frame(ppblPasscodeiframe());
        textBoxPPBLPassCode().waitUntilEditable();
        textBoxPPBLPassCode().clearAndType(ppblDetails.getPasscode());
        DriverManager.getDriver().switchTo().defaultContent();
        buttonPpblSumbit().waitUntilClickable();
        buttonPpblSumbit().click();
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
    public RadioButton radioButtonPaytmPostpaid() {
        return new RadioButton(By.xpath("//*[@id='checkout-bank-credit']/div/label/input"), getPageName(), "paytm-digital-card-radiobutton");
    }
    @Override
    public Button buttonPostPaidPayNow() {
        return new Button(By.xpath("//div[@id='checkout-button']/button"), getPageName(), "pay-button");
    }

    @Override
    protected void fillAndSubmitDigitalCardDetails(PaymentDTO digitalCardDetails) {
        if (!radioButtonPaytmPostpaid().isSelected()) {
            radioButtonPaytmPostpaid().click();
        }
        buttonPostPaidPayNow().waitUntilClickable();
        buttonPostPaidPayNow().click();
    }

    @Override
    public UIElement totalAmtPG() {
        return new UIElement(By.xpath("(//img[@class='ptm-lock-img']/..)[1]"), getPageName(), "pg-charge-fee-amount") {
            @Override
            public String getText() {
                return super.getText().trim().substring(5,9);
            }
        };
    }


    @Override
    public UIElement chargeFeeAmtPG() {
        return new UIElement(By.xpath("//span[contains(@class,'ptm-conv-txt')]"), getPageName(), "pg-charge-fee-amount") {
            @Override
            public String getText() {
                pause(1);
                return super.getText().trim().substring(0,5);
            }
        };
    }

    @Override
    public void clickInvalidOTPEnteredButtonIfDisplayed() {
        if (failedOTPEnteredButton().isDisplayed()) {
            failedOTPEnteredButton().click();
        }
    }

    public Button failedOTPEnteredButton(){
        return new Button(By.xpath("//span[contains(.,'OTP')]//following-sibling::button"),getPageName(), "failed-txn-got-it-button");
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
    public UIElement subscriptionDetails() {
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
    public CheckBox checkBoxPPI() {
        return new CheckBox(By.xpath("//div[@id='checkout-wallet']//span[@class='ptm-check']"), getPageName(), "checkbox-ppi");
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
        return new Button(By.xpath("//*[@id='checkout-button']/button"), getPageName(), "pay-button");
    }

    @Override
    public UIElement lblErrMsg() {
        return null;
    }

    @Override
    public UIElement imgScanPayQRCode() {
        return new UIElement(By.xpath("//div[@class='ptm-qr-img-container']//img"), getPageName(), "img-ScanPay-QRCode");
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
    public UIElement error_invalidExpiryDate() {
        return new UIElement(By.id("invalid_date"), pageName, "error_invalidExpiryDate");
    }

    @Override
    public UIElement ppblNotificationMsg() {
        return null;
    }


    @Override
    public void logout(User user) {
    UIElement changeNumber = new UIElement(By.xpath("//*[contains(text(), 'Change')]"),getPageName(),"change-number");
    changeNumber.click();
    waitUntilLoads();
    }

    public String getNumber(){
        return null;
    }
    //pyblic
    public UIElement proceed()
    {
        return new UIElement(By.xpath("//*[@id='ptm-checkout-sc']//button"),getPageName(),"proceed-button");
    }

    @Override
    protected void fillAndSubmitEMI_SavedCard(Constants.PayMode payMode, PaymentDTO emiDetails) {
        proceedEMIBtn().waitUntilClickable();
        proceedEMIBtn().click();
        DriverManager.getDriver().switchTo().frame(sc_CheckoutIframe());
        textBoxSavedCardCVV().waitUntilVisible();
        textBoxSavedCardCVV().clearAndType(emiDetails.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public void fillAndSubmitEMIDetails(PaymentDTO emiDetails) {
        if (GetAllTextDom().getText().contains("Use another card")) {
            UseAnotherCard().click();
        }
        DriverManager.getDriver().switchTo().frame(ccdc_emiIframe());
        if (emiDetails.getCreditCardNumber() != null && !emiDetails.getCreditCardNumber().equals("")) {
            textBoxCardNumber().clearAndType(emiDetails.getEmiCard());
            System.out.println();
        }
        pause(2);
        emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        proceedToConvertEMIButton().waitUntilClickable();
        proceedToConvertEMIButton().click();
        DriverManager.getDriver().switchTo().frame(ccdc_expCvv_cardIframe());
        if (emiDetails.getExpMonth() != null && !emiDetails.getExpMonth().equals("")) {
            this.textBoxExpiryMonthEMI().waitUntilEditable();
            this.textBoxExpiryMonthEMI().clearAndType(emiDetails.getExpMonth());
        }
        if (emiDetails.getExpYear() != null && !emiDetails.getExpYear().equals("")) {
            this.textBoxExpiryYearEMI().waitUntilEditable();
            this.textBoxExpiryYearEMI().clearAndType(emiDetails.getExpYear().substring(2, 4));
        }
        if (emiDetails.getCvvNumber() != null && !emiDetails.getCvvNumber().equals("")) {
            this.textBoxCVVNumber().waitUntilEditable();
            this.textBoxCVVNumber().clearAndType(emiDetails.getCvvNumber());
        }
        DriverManager.getDriver().switchTo().defaultContent();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public  void fillAndSubmitEMIDCDetails(PaymentDTO emiDetails){
        if (GetAllTextDom().getText().contains("Use another card")) {
            UseAnotherCard().click();
        }
        EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(ccdc_emiIframe());
        if (emiDetails.getCreditCardNumber() != null && !emiDetails.getCreditCardNumber().equals("")) {
            textBoxCardNumber().clearAndType(emiDetails.getEmiCard());
            System.out.println();
        }
        pause(2);
        emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        proceedToConvertEMIButton().waitUntilClickable();
        proceedToConvertEMIButton().click();
        DriverManager.getDriver().switchTo().frame(ccdc_expCvv_cardIframe());
        if (emiDetails.getExpMonth() != null && !emiDetails.getExpMonth().equals("")) {
            this.textBoxExpiryMonthEMI().waitUntilEditable();
            this.textBoxExpiryMonthEMI().clearAndType(emiDetails.getExpMonth());
        }
        if (emiDetails.getExpYear() != null && !emiDetails.getExpYear().equals("")) {
            this.textBoxExpiryYearEMI().waitUntilEditable();
            this.textBoxExpiryYearEMI().clearAndType(emiDetails.getExpYear().substring(2, 4));
        }
        if (emiDetails.getCvvNumber() != null && !emiDetails.getCvvNumber().equals("")) {
            this.textBoxCVVNumber().waitUntilEditable();
            this.textBoxCVVNumber().clearAndType(emiDetails.getCvvNumber());
        }
        DriverManager.getDriver().switchTo().defaultContent();
        kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        kfscloseButton().click();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();

    }



    public UIElement emiPlan(){
        return new UIElement(By.xpath("//div[@id='selectPlan']/div[contains(text(),'Select EMI Plan')][1]"), getPageName(), "emi-plan");
    }

    public UIElement emiPlanCard(){
        return new Link(By.xpath("//div[@class='ptm-emi-plancard-wrap']"), getPageName(), "emi-plan-card");
    }

    @Override
    public Link tabEMI() {
        return new Link(By.xpath("//p[contains(.,'EMI')]"), getPageName(), "emi-tab");
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
    public Link tabSavedEmi() {
        return new Link(By.xpath("//*[@id='ptm-checkout-sc']/div[2]/div[2]/div"), getPageName(), "saved-emi-tab");
    }

//    @Override
//    public CheckBox checkboxPPBL() {
//            return new CheckBox(By.xpath("//*[@id='checkout-payment-bank']/div/label/input"), getPageName(), "checkbox PPBL" ){
//                @Override
//                public boolean isSelected() {
//                    CheckBox checked = new CheckBox(By.xpath("//*[@id='checkout-payment-bank']/div/label/input"),
//                            getPageName(), "checked-paymode");
//                    if (checked.isElementPresent())
//                        return true;
//                    else
//                        return false;
//                }
//            };
//        }
        @Override
    public CheckBox checkboxPPBL() {
        return new CheckBox(By.xpath("//*[@id='checkout-payment-bank']/div/label/input"), getPageName(), "ppbl-checkbox") {

            @Override
            public boolean isChecked() {
                UIElement uiElement = new UIElement(By.xpath("//*[@id='checkout-payment-bank']/div/label/input"), getPageName(), "ppbl-checkbox");
                return uiElement.getAttribute("class").contains("active");
            }

            @Override
            public void waitUntilVisible() {
                super.waitUntilVisible();
                ((JavascriptExecutor) DriverManager.getDriver()).executeScript("window.scrollTo(0,100)");
                pause(1);
            }
        };
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
    public UIElement convinenceCharge() {
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

    public Select dropdownEmiBanks() {
        new UIElement(By.cssSelector(".ptm-emi-bank-select"), getPageName(), "emi-bank-dropdown").click();
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
    public UIElement emiDurationOption(int emiPlanMonth) {
        return new UIElement(By.xpath("//*[text()='"+emiPlanMonth+" Months']"), getPageName(), "emi month plan") {
            @Override
            public void click() {
                this.waitUntilClickable();
                pause(1);
                super.click();
            }
        };
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
    public Select dropdownNB() {
        return new Select(By.xpath("//*[contains(@class,'ptm-nb-list')]"), getPageName(), "select-nb-banks-dropdown") {
            @Override
            public void selectByValue(String value) {
                this.waitUntilClickable();
                this.click();
//                DriverManager.getWebDriverElementWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[contains(@class, 'body-bg')][contains(@class, 'overlay-container')]")));
                List<UIElement> banks = UIElements.getMultiple(By.xpath("//*[contains(@class,'ptm-bank-name')]"), getPageName(), "nb-banks-list");
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
    public void submitPostPaidOnboarding() {

    }

    @Override
    public void directLogin(User user) {

    }

    @Override
    public String getErrorMessageAfterEnteringCard() {

        return null;

    }

    @Override
    public UIElement getErrorCC_EMI_NOTSET() {

        return null;
    }
    @Override
    public UIElement save_card_visible() {

        return null;
    }
    @Override
    public UIElement getError_invalidCVV() {

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
    public void clickRetryIncorrectOTPBtn(){
    }

    @Override
    public UIElement qrCodeCheckoutJSText(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-wrap')]//*[contains(@class,'ptm-qr-pay-text')]"), getPageName(), "QR-Code");
    }

    @Override
    public UIElement qrSubText(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-pay-subtext')]"),getPageName(),"qr_Subtext");
    }

    @Override
    public UIElement enabledPaymodes(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-wrap')]//*[contains(@class,'ptm-txinfo')]"), getPageName(), "Enabled-Paymodes");
    }

    @Override
    public UIElement qrImg(){
        return new UIElement(By.xpath("//*[@class='ptm-pay-with-upi']"),getPageName(),"qr-upi-images");
    }

    @Override
    public UIElement UpiNumericId(){
        return new UIElement(By.xpath("//input[@placeholder='Enter Mobile No./UPI No.']"),getPageName(),"UPI NUMBER");
    }

    @Override
    public UIElement UpiNumericIdErrorMsgClass()
    {
        return new UIElement(By.xpath("//p[@class='ptm-num-verify']"), getPageName(),"Upi Message class");
    }

    @Override
    public UIElement getErrorMessageInvalidOTP(){
        return new UIElement(By.xpath("//*[contains(text(),'Please Enter Valid Otp')]"),getPageName(),"Invalid_otp");
    }

    @Override
    public UIElement paymentContainer() {
        return new UIElement(By.xpath("//*[contains(@class,'card-content-wrap')]"), getPageName(), "payment-container");
    }
    @Override
    public UIElement paymodeDisabledContainer() {
        return new UIElement(By.xpath("//*[contains(@class,'payment-mode')]"), getPageName(), "disabled-payment-container");
    }
    @Override
    public UIElement payButtonConvFeeMsg(){
        return new CheckBox(By.xpath("//span[contains(@class,'ptm-d-block')]"), getPageName(), "conv-fee-msg-pay-button");
    }
    @Override
    public UIElement convFeeMessageCashierPage(){
        return new UIElement(By.className("ptm-conv-wrapper"), getPageName(), "conv-msg-cashier-page");
    }
    @Override
    public UIElement kfsLink() {
        return new UIElement(By.xpath("//*[contains(@class,'sc-emi-pay-overlap')]//a[contains(@class,'ptm-pointer-cursor')]"), getPageName(), "adhere-text");
    }
    @Override
    public UIElement kfscloseButton() {
        return new UIElement(By.xpath("//div[@id='checkout-overlay-box']//following::span"), getPageName(), "kfs-close");
    }
    @Override
    public UIElement kfsUpperText() {
        return new UIElement(By.xpath("//*[contains(@class,'template-head')]"), getPageName(), "kfs-text");
    }
    @Override
    public UIElement kfsBankDateText() {
        return new UIElement(By.xpath("//*[contains(@class,'template-subhead')]"), getPageName(), "bank-date");
    }
    @Override
    public UIElement kfsAdhereText() {
        return new UIElement(By.xpath("//*[contains(@class,'sc-emi-pay-overlap')]//*[contains(@class,'ptm-appin-img')]"), getPageName(), "adhere-text");
    }
    @Override
    public UIElement kfsTableText() {
        return new UIElement(By.xpath("//*[contains(@class,'kfs-details-wrap')]"), getPageName(), "kfs-table-text");
    }
    @Override
    public UIElement kfsLoanConsentText() {
        return new UIElement(By.xpath("//*[contains(@class,'loan-consent')]"), getPageName(), "kfs-loan-consent-text");
    }
    @Override
    public UIElement kfsDisclaimerText() {
        return new UIElement(By.xpath("//*[contains(@class,'disclaimer')]"), getPageName(), "kfs-disclaimer-text");
    }
    @Override
    public UIElement kfscloanAmount() {
        return new UIElement(By.xpath("//*[contains(@id,'loanAmount')]"), getPageName(), "kfs-loan");
    }
    @Override
    public UIElement kfsctenure() {
        return new UIElement(By.xpath("//*[contains(@id,'tenure')]"), getPageName(), "kfs-loan");
    }
    @Override
    public UIElement kfsinstallments() {
        return new UIElement(By.xpath("//*[contains(@id,'installments')]"), getPageName(), "kfs-installments");
    }

    public  TextBox cardShortcut(String lastFourDigit)
    {
        return new TextBox(By.xpath("//p[contains(text(),'Credit Card - "+lastFourDigit+"')]"),
                getPageName(), "card-number-field");
       }

    @Override
    public  TextBox savedToken(String lastFourDigit)
    {
        return new TextBox(By.xpath("//p[contains(text(),' Card - "+lastFourDigit+"')]"),
                getPageName(), "card-number-field");
    }


    @Override
    public void payByEMI(CashierPage cashierPage, PaymentDTO paymentDTO, Boolean isEMIDC) {
        //    if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
        //        cashierPage.viewAllOffersAvialable_HideButton().click();
        //    }
        cashierPage.waitUntilLoads();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        if (isEMIDC) {
            cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
            cashierPage.checkEMIEligibility().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
        }
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        if (isEMIDC) {
            cashierPage.kfsLink().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.kfscloseButton().click();
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.kfsCheckBox().click();
        }
        cashierPage.buttonPGPayNow().click();
    }

    @Override
    public void payByEMIBankFlow(CashierPage cashierPage, PaymentDTO paymentDTO, Boolean isEMIDC) {
        // if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
        //     cashierPage.viewAllOffersAvialable_HideButton().click();
        // }
        // cashierPage.scrollToElement(cashierPage.tabEMI());
        // cashierPage.tabEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        cashierPage.waitUntilAllAJAXCallsFinish();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        if (isEMIDC) {
            cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
            cashierPage.checkEMIEligibility().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
        }
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        if (isEMIDC) {
            cashierPage.kfsLink().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.kfscloseButton().click();
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.kfsCheckBox().click();
        }
        cashierPage.buttonPGPayNow().click();
    }
    @Override
    public void payByCC(CashierPage cashierPage, PaymentDTO paymentDTO) {
        // if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
        //     cashierPage.viewAllOffersAvialable_HideButton().click();
        // }
        // cashierPage.tabCreditCard().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().click();
    }

    @Override
    public void payByNB(CashierPage cashierPage, PaymentDTO paymentDTO) {
        // if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
        //     cashierPage.viewAllOffersAvialable_HideButton().click();
        // }
        // cashierPage.tabNetBanking().click();
        dropdownNB().selectByValue(paymentDTO.getBankName());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public void payByEMISavedCard(CashierPage cashierPage, PaymentDTO paymentDTO, Boolean isEMIDC) {
//        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
//            cashierPage.viewAllOffersAvialable_HideButton().click();
//        }
        savedToken(paymentDTO.getEmiCard().substring(paymentDTO.getEmiCard().length()-4)).click();
        pause(2);
        DriverManager.getDriver().switchTo().defaultContent();
        if (isEMIDC) {
            cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
            cashierPage.checkEMIEligibility().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
        }
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.Cvv_cardIframe());
        cashierPage.textBoxCVVNumberSavedcardEMI().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        if (isEMIDC) {
            cashierPage.kfsLink().click();
            cashierPage.waitUntilLoads();
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.kfscloseButton().click();
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.kfsCheckBox().click();
        }
        cashierPage.buttonPGPayNow().click();
    }

    @Override
    public void payByUPI(CashierPage cashierPage, PaymentDTO paymentDTO) {
        // if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
        //     cashierPage.viewAllOffersAvialable_HideButton().click();
        // }
        // scrollToElement(tabUPI());
        // tabUPI().waitUntilClickable();
        // tabUPI().click();
//        textBoxVPA().click();
//        textBoxVPA().click();
        textBoxVPA().clearAndType(paymentDTO.getVpa());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public void payBySavedCard(CashierPage cashierPage, PaymentDTO paymentDTO){
//        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
//            cashierPage.viewAllOffersAvialable_HideButton().click();
//        }
//        cashierPage.tabSavedCard().waitUntilClickable();
//        cashierPage.tabSavedCard().click();
//        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(sc_CheckoutIframe());
        cashierPage.textBoxSavedCardCVV().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
    }

}
