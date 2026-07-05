package com.paytm.pages;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.ui.element.*;
import com.paytm.utils.merchant.util.AuthUtil;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CashierPageCheckoutjsWeb extends CashierPageCheckoutjsWap {

    public CashierPageCheckoutjsWeb() {
        super();
        setPageName("Checkoutjs cashier page web");
    }


//    <------------------------------------------------------------------------------>

    @Override
    public Button buttonSecureSignIn() {
        return new Button(By.xpath("//*[text()='Proceed'] | //button[contains(@class,'ptm-login-btn')]"), getPageName(), "sign-in-button");
    }

    @Override
    protected void fillAndSubmitEMI_SavedCard(Constants.PayMode payMode, PaymentDTO emiDetails) {
        tabSavedEmi().waitUntilClickable();
        tabSavedEmi().click();
        pause(2);
        proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(Cvv_cardIframe());
        textBoxCVVNumberSavedcardEMI().clearAndType(emiDetails.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        buttonPGPayNow().click();
    }

    @Override
    public Link tabSavedEmi() {
        return new Link(By.xpath("//*[contains(text(),'EMI Available')]"), getPageName(), "tab-saved-emi");
    }

    @Override
    public Button buttonPGPayNow() {
        //return new Button(By.xpath("//*[@id='checkout-button']/button"), getPageName(), "pay-button");
        return new Button(By.xpath("(//div[@id='checkout-button']/button)[last()]"), getPageName(), "pay-button");
    }

    @Override
    public Button buttonPostPaidPayNow() {
        return new Button(By.xpath("//button[contains(@class, 'ptm-custom-btn ptm-hvr-pop')]"), getPageName(), "pay-button Of Postpaid");
    }

    public UIElement BankMandateRadioButton() {
        /*return new Button(By.xpath("//*[contains(text(),'Bank Account E-NACH')]"), getPageName(),
                "bankMandate-radio-checkbox");*/
        return new UIElement(By.xpath("//*[text()='Bank Account (E-mandate)']"), getPageName(), "Bank mandate pay");
    }

    @Override
    public UIElement convinenceCharge() {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-conv-wrapper')]"), getPageName(), "convenience-fee-dropdown");
    }

    @Override
    public UIElement merchantLOGO() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-brand-logo')]"), getPageName(), "merchant-logo");
    }

    @Override
    public UIElement loginStrip() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-login-box')]"), getPageName(), "login-strip");
    }

    public Button proceedBtn() {
        return new Button(By.xpath("//button[text()='Proceed']"), getPageName(), "proceed-button");
    }

    public UIElement alertMessage(){
        return new UIElement(By.xpath("//div[text()='Subscription payments will happen via wallet']"),getPageName(),"alert-message");
    }

    @Override
    public UIElementV3 tabPPBL() {
        return new UIElementV3(By.xpath("//*[contains(text(),'PPBL')]"), "ppbl-pay-mode-tab");
    }

    @Override
    public UIElement applyPromoText() {
        return new UIElement(By.xpath("//*[contains(text(), 'ashback')][contains(text(),'applicable')]"), getPageName(), "applyPromoText");
    }

    @Override
    public UIElement subsLabelDueInfo(){
        return new UIElement(By.xpath(".//div[@class='payment-mode-warp xs-paymode-wrap  ']//div[@class='ptm-sub-wraper']//div[@class='ptm-c-p']//label//img"), getPageName(), "Due-info-label");
        //return new UIElement(By.xpath("//*[contains(@class,'ptm-subs-due-info')]"), getPageName(), "Due-info-label");
    }

    @Override
    public UIElement savedBankMandate(){
        return new UIElement(By.xpath("//*[@class='ptm-pay-method-cont']//p[starts-with(text(),' XX ')]"),getPageName(),"saved-bank-mandate");
    }

    @Override
    public UIElement savedBankMandateName(){
        return new UIElement(By.xpath("//*[@class='ptm-pay-method-cont']//p[starts-with(text(),' XX ')]"),getPageName(),"saved-bank-mandate-name");
    }

   /* @Override
    public UIElement subscriptionDetails() {
        // return new UIElement(By.xpath("//*[@class='ptm-info']"), getPageName(), "view-subscription_details");
         return new UIElement(By.xpath("(//img[@class='ptm-info'])[2]"), getPageName(), "view-subscription_details");
        //return new UIElement(By.xpath("//div[contains(text(),'Subscription')]"),getPageName(), "Subscription-Details");
    }*/

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
    public TextBox textBoxCardNumber() {
        return new TextBox(By.xpath("//*[@id='cardNumber'] | //*[@id='cardnumber']"),
                getPageName(), "cardNumber");
    }
    @Override
    public TextBox textBoxShortcutCardNumber() {

        return new TextBox(By.id("cardNumber"),
                getPageName(), "card-number-field");
    }

    @Override
    public UIElement subsDetailsRecurringAmount(){
        return new UIElement(By.xpath("//*[contains(text(),'Recurring Bill Amount*')]"),getPageName(),"Subs-Recurring-Amount-info-label");
        //return new UIElement(By.xpath("//*[contains(text(),'Recurring Amount')]"),getPageName(),"Subs-Recurring-Amount-info-label");
    }

    public UIElement upiAutoPay(){
        return new UIElement(By.xpath("//*[@alt='UPI AUTOPAY']"), getPageName(), "UPI-autopay-tab");
    }

    @Override
    public Link tabUPI() {
        // return new Link(By.xpath("//*[contains(@class,'upi')]//img"), getPageName(), "upi-paymode-tab");
        return new Link(By.xpath("//p[text()='UPI']"), getPageName(), "upi-paymode-tab");

    }
    @Override
    public RadioButton radioButtonPaytmPostpaid() {
        return new RadioButton(By.xpath("//*[@id='checkout-bank-credit']//span"),getPageName(), super.radioButtonPaytmPostpaid().getElementName());
    }

    @Override
    public UIElement getCardLogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='checkout-card']//img"), getPageName(), "CARD Logo Url");
    }
    @Override
    public UIElement getEMILogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='checkout-emi']//img"), getPageName(), "EMI Logo Url");
    }

    @Override
    public UIElement postpaidSignUpStrip() {
        return new UIElement(By.xpath(("(//*[text() ='Signup to Paytm Postpaid'])[2]")),getPageName(),"");
    }

    @Override
    public UIElement getUpiLogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='checkout-upi']//img"), getPageName(), "UPI Logo Url");
    }

    @Override
    public UIElement getNBLogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id='checkout-nb']//img"), getPageName(), "NB Logo Url");
    }

    @Override
    public TextBox textBoxPPBLPassCode() {
        return new TextBox(By.xpath("(//input[@id='ppbl_pin'])[1]"), getPageName(), "ppbl-passcode-field");
    }

    @Override
    public UIElement toBePaidTab() {
        //return new UIElement(By.xpath("//*[text() ='To be paid now']"), getPageName(), "to-be-paid");
        return new UIElement(By.xpath("//div[contains(@class,'payment-mode-warp')]//*[text() ='To be paid now']"), getPageName(), "to-be-paid");
    }
    @Override
    public UIElement insufficientPPIBalanceIconMsg()
    {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-insufficient-balance')]"),getPageName(),"wallet insufficient balance message");
    }

    public Link tabSavedUPI(int index) {
        return new Link(By.xpath("(//div[@id=\"checkout-upi-push\"])[" + index +"]//span[contains(@class,\"checkmark\")]"), getPageName(), "saved-upi-tab") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }

    @Override
    public List<WebElement> checkboxVPAList() {
        return DriverManager.getDriver().findElements(By.id("checkout-upi-push"));
    }

    @Override
    public UIElement footerLOGO() {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-ftr-logo')]"), getPageName(), "ptm-footer-logo");
    }

    @Override
    public UIElement save_card_visible() {

        return new UIElement(By.xpath("//*[text() ='or Choose your saved cards']"),getPageName(),"save-card-not-present");

    }
    @Override
    public UIElement getPaytmLogoBlue() {
        return new Link(By.xpath("//*[contains(@class,'ptm-hedrlogo')]//*[contains(@src, 'paytm-pg-blue')]"), getPageName(),"paytm-header-logo-blue");
    }
    @Override
    public UIElement footerLogoBlue() {
        return new UIElement(By.xpath("//*[contains(@id,'footer-main')]//*[contains(@src,'logo-pg')]"), getPageName(), "ptm-footer-logo-blue");
    }

    @Override
    public CheckBox checkBoxPPI() {
        return new CheckBox(By.xpath("//span[@class=\"ptm-check\"]"), getPageName(), "checkbox-ppi");
    }

    @Override
    protected void fillAndSubmitSavedCardDetails(PaymentDTO savedCardDetails) {
        tabSavedCard().click();

        if (!(savedCardDetails.getCvvNumber() == null || savedCardDetails.getCvvNumber().isEmpty())) {
            // DriverManager.getDriver().switchTo().frame(sc_CheckoutIframe());
            textBoxSavedCardCVV().clearAndType(savedCardDetails.getCvvNumber());
            DriverManager.getDriver().switchTo().defaultContent();
        }
        buttonPGPayNow().click();
    }

    @Override
    public Link tabSavedCard() {
        return new Link(By.xpath("//div[@id='ptm-checkout-sc']//span[contains(@class,\"ptm-checkmark\")]"), getPageName(), "saved-cards-tab")  {
            @Override
            public boolean isSelected() {
                Link checked = new Link(By.xpath("//div[@id='ptm-checkout-sc']//span[contains(@class,\"ptm-checkmark\")]"),
                        getPageName(), "checked-paymode");
                if (checked.isElementPresent())
                    return true;
                else
                    return false;
            }
        };
    }

    @Override
    public UIElement getBankOfferDiscountMsg(){
        return new UIElement(By.cssSelector(".ptm-message"),"cashier-page","discount-msg");
    }
    public TextBox textBoxExpiryMonthEMI() {
        return new TextBox(
                By.id("mm"),
                getPageName(), "expiry-month-field-emi");
    }

    public TextBox textBoxCVVNumber() {
        return new TextBox(By.id("cvv"),
                getPageName(), "cvv-field");

    }

    public TextBox textBoxExpiryYearEMI() {
        return new TextBox(
                By.id("yy"),
                getPageName(), "expiry-year-field-emi");
    }
    public Button proceedToConvertEMI() {
        return new Button(By.xpath("//button[text()='Proceed to Convert to EMI']"), getPageName(), "proceed-to-convert-EMI");
    }
    @Override
    public void fillAndSubmitEMIDetails(PaymentDTO emiDetails)
    {
        scrollToElement(tabEMI());
//        scrollTo(tabEMI());
        tabEMI().click();

        DriverManager.getDriver().switchTo().frame(ccdc_emiIframe());
        textBoxCardNumberEMI().clearAndType(emiDetails.getEmiCard());
        DriverManager.getDriver().switchTo().defaultContent();
        waitUntilLoads();
        proceedToSelectEmiPlan().waitUntilVisible();
        proceedToSelectEmiPlan().click();
        //emiPlan().click();
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
    public  void fillAndSubmitEMIDCDetails(PaymentDTO emiDetails){
        scrollToElement(tabEMI());
        tabEMI().click();
        EmiRadioButton().click();
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
        kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        kfscloseButton().click();
        buttonPGPayNow().click();
    }

    public UIElement emiPlanCard(){
        return new Link(By.xpath("//div[@class='ptm-scemi-card ptm-pos-r ptm-global-border xs-plancard  ptm-active-plan ptm-nav-selectable ptm-nav-focused']"), getPageName(), "emi-plan-card");
    }

    @Override
    public UIElement getUserDeactivatedErrorMessage(){
        return new UIElement(By.xpath("//div[@class='ptm-wallet-inactive ']"), getPageName(), "ptm-wallet-inactive");
    }
    @Override
    public List<String> upiHandlers()
    {
        List<UIElement> upiHandlers = UIElements.getMultiple(By.xpath("//*[text()='Verify VPA']/following::div[1]"),getPageName(),"upi-handlers");
        List<String> list = new ArrayList<>();
        for(UIElement upi : upiHandlers)
        {
            list.add(upi.getText());
        }

        return list;
    }
    @Override
    public TextBox textBoxVPA() {
        return new TextBox(By.xpath("//input[@placeholder='Enter VPA'] | //input[contains(@id,'ptm-upi-input')]"),getPageName(),"UPI VPA");
    }

    @Override
    public UIElement enabledPaymodes(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-wrap')]//*[contains(@class,'ptm-txinfo')]"), getPageName(), "Enabled-Paymodes");
    }

    @Override
    public UIElement qrSubText(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-pay-subtext')]"),getPageName(),"qr_Subtext");
    }

    @Override
    public UIElement qrImg(){
        return new UIElement(By.xpath("//*[@class='ptm-pay-with-upi']"),getPageName(),"qr-upi-images");
    }

    @Override
    public UIElement upiPaymodeUpiAppImg(){
        return new UIElement(By.xpath("//*[contains(@id,'checkout-upi')]//*[@class='ptm-diff-upi-apps']"),getPageName(),"upi-paymode-upiApp-img");
    }

    @Override
    public UIElement upiPaymodeUpiAppText(){
        return new UIElement(By.xpath("//div[contains(@id,'checkout-upi')]//*[contains(@class,'ptm-more-upi-apps-txt')]"),getPageName(),"upi-app-text");
    }
    public UIElement verifyUpiNumericID()
    {
        return new UIElement(By.xpath("//a[text()='Verify']"),getPageName(),"UPI NUMBER");
    }
    public UIElement UpiNumericId(){
        return new UIElement(By.xpath("//input[@placeholder='Enter Mobile No./UPI No.']"),getPageName(),"UPI NUMBER");
    }

    @Override
    public UIElement loginKnowMoreLink(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-kno-link')]"), getPageName(), "ptm-know_more");
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
    public UIElement knowMoreLinkPopup(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-overlay-wrapper')]//*[contains(@class, 'ptm-body-bg')]"), getPageName(), "ptm-pref-btn");
    }
    @Override
    protected void fillAndSubmitUPIDetails(PaymentDTO upiDetails) {
        scrollToElement(tabUPI());
        tabUPI().waitUntilClickable();
        tabUPI().click();
        textBoxVPA().click();
        textBoxVPA().clearAndType(upiDetails.getVpa());
//        verifyVPALinkText().click();
//        pause(2);
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    protected void fillAndSubmitUPIDetailsWithSinglePayMode(PaymentDTO upiDetails) {
        textBoxVPA().click();
        textBoxVPA().clearAndType(upiDetails.getVpa());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
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
    public UIElement errorTextsInUPIFlow()
    {
        return new UIElement(By.xpath("//span[contains(@class,'ptm-nid-error')]"), getPageName(),"Error Msg Paragraph");
    }

    @Override
    public UIElement postpaidContainer(){
        return new UIElement(By.xpath("//*[contains(@id,'checkout-bank-credit')]"),getPageName(),"postapid");
    }

    @Override
    public UIElement kfsAdhereText() {
        return new UIElement(By.xpath("//*[contains(@class,'sc-emi-pay-overlap')]//*[contains(@class,'ptm-appin-img')]"), getPageName(), "adhere-text");
    }

    @Override
    public UIElement kfsLink() {
        return new UIElement(By.xpath("//*[contains(@class,'sc-emi-pay-overlap')]//a[contains(@class,'ptm-pointer-cursor')]"), getPageName(), "adhere-text");
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
    public UIElement kfsTableText() {
        return new UIElement(By.xpath("//*[contains(@class,'kfs-details-wrap')]"), getPageName(), "kfs-table-text");
    }

    @Override
    public UIElement kfsDisclaimerText() {
        return new UIElement(By.xpath("//*[contains(@class,'disclaimer')]"), getPageName(), "kfs-disclaimer-text");
    }

    @Override
    public UIElement kfsLoanConsentText() {
        return new UIElement(By.xpath("//*[contains(@class,'loan-consent')]"), getPageName(), "kfs-loan-consent-text");
    }

    @Override
    public UIElement kfscloseButton() {
        return new UIElement(By.xpath("//div[@id='checkout-overlay-box']//following::span"), getPageName(), "kfs-close");
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

    @Override
    public TextBox textBoxSavedCardCVV() {
        waitUntilFrameAppearsAndSwitchToIt(tokenCVVFrame());
        return new TextBox(By.id("ppbl_pin"), getPageName(), "saved-cards-cvv");
    }
    @Override
    public TextBox textBox2FAPassCode() {
        return new TextBox(By.xpath("//input[contains(@placeholder,'Enter Passcode')]"), getPageName(), "ppbl-passcode-field");
    }

    @Override
    public UIElement verified2FAIncorrectPasscodeErrorMsg()
    {
        return new UIElement(By.xpath("//p[contains(text(),'Enter valid passcode')]"), getPageName(),"verified 2FA passcode Error msg ");
    }

    @Override
    public UIElement verified2FAPasscodeErrorMsg()
    {
        return new UIElement(By.xpath("//p[contains(text(),'Enter Passcode')]"), getPageName(),"verified 2FA passcode Error msg ");
    }

    @Override
    public UIElement verified2FALinkForgotPasscode() {
        return new Button(By.xpath("//button[contains(text(),'Forgot Passcode?')]"), getPageName(), "Forgot Passcode?");
    }

    @Override
    public UIElement verified2FATextForgotPasscode() {
        return new UIElement(By.xpath("//div[contains(text(),'Forgot Passcode')]"),getPageName(),"Forgot Passcode");
    }
    @Override
    public UIElement searchBox() {
        return new UIElement(By.xpath("//input[@placeholder='Search']"), getPageName(), "search box");
    }
    @Override
    public UIElement UPIwindow() {
        return new UIElement(By.xpath("//div[contains(text(),\"Enter UPI ID\")]"), getPageName(), "UPIwindow");
    }
    @Override
    public UIElement applyOffer(String offerNumber) {
        return new UIElement(By.xpath("//p[contains(text(),\"Apply Offer\")]["+offerNumber+"]"), getPageName(), "applyOffer");
    }
    public UIElement applyOfferWallet() {
        return new UIElement(By.xpath("//div[@id=\"checkout-home\"]/.//div[3]//./p[contains(text(), 'Apply Offer')]"), getPageName(), "applyOfferWallet");
    }
    @Override
    public UIElement dropDownEMIOffers() {
        return new UIElement(By.xpath("//p[contains(text(),\"No Cost EMI\")]/following::div[1]"), getPageName(), "dropDownEMIOffers");
    }
    @Override
    public UIElement offerVisibleEMI() {
        return new UIElement(By.xpath("//div[contains(text(),\"Select an EMI Plan\")]"), getPageName(), "offerVisibleEMI");
    }
    @Override
    public UIElement checkboxNB() {
        return new UIElement(By.xpath("//p[contains(text(),\"HDFC\")]/preceding-sibling::div/img"), getPageName(), "checkboxNB");
    }
    @Override
    public UIElement checkboxCard() {
        return new UIElement(By.xpath("//div[contains(text(),\"Enter Debit or Credit Card details\")]"), getPageName(), "checkboxCard");
    }

    public UIElement hideButtonForDiscount()
    {
        return new UIElement(By.xpath("//*[text()='Hide']"),getPageName(),"Hide button in cashier page");
    }

    @Override
    public UIElement payViaNotificationTab()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Pay on Paytm App')]"),getPageName(),"pay via notification heading");
    }

    @Override
    public void payByEMI(CashierPage cashierPage, PaymentDTO paymentDTO, Boolean isEMIDC) {
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
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
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
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
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabCreditCard().click();
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
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabNetBanking().click();
        dropdownNB().selectByValue(paymentDTO.getBankName());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    @Override
    public void payByEMISavedCard(CashierPage cashierPage, PaymentDTO paymentDTO, Boolean isEMIDC) {
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabSavedEmi().click();
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
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        scrollToElement(tabUPI());
        tabUPI().waitUntilClickable();
        tabUPI().click();
        textBoxVPA().click();
        textBoxVPA().clearAndType(paymentDTO.getVpa());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }
    @Override
    public void payBySavedCard(CashierPage cashierPage, PaymentDTO paymentDTO){
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabSavedCard().waitUntilClickable();
        cashierPage.tabSavedCard().click();
        cashierPage.waitUntilLoads();
        cashierPage.textBoxSavedCardCVV().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
    }

}
