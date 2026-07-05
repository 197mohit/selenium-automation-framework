package com.paytm.pages;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.PagePath;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.convenienceFeeElements;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.framework.api.CustomRequestSpecBuilder;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.*;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.paytm.apphelpers.CommonHelpers.emiCalc;

public abstract class CashierPage extends BasePage {

    public final String INVALID_CARD_NUMBER = "Please enter a valid card number";
    public final String INVALID_CVV = "Invalid CVV";
    public final String INVALID_EXPIRY_DATE = "EXPIRY DATE";
    public final String INVALID_SAVED_CARD_CVV = "ENTER CVV";
    public final String INVALID_IMPS_MOBILE_NUMBER = "MOBILE NUMBER";
    public final String INVALID_IMPS_MMID = "MMID";
    public final String INVALID_IMPS_OTP = "OTP";
    public final String EMPTY_CVV = "CVV";
    public final String INVALID_LOGIN_CREDENTIALS = "Please enter valid Username and Password";
    public final String EMPTY_CREDIT_CARD_NUMBER = "ENTER CREDIT CARD NUMBER";
    public final String EMPTY_EXPIRY_DATE = "EXPIRY DATE";
    public final String EMPTY_CVV_NUMBER = "CVV/SECURITY CODE";
    public final String INVALID_PAYMENT_DETAILS = "Entered payment details are not applicable for this subscription type";
    public final String SOMETHING_WENT_WRONG = "Something went wrong, please try using another payment mode";
    public final String COD_PAY = "You'll receive an automated call from us to confirm this order. And at the time of delivery, Rs {amount} will have to be given to the courier boy";
    public final String POSTPAID_INCORRECT_PASSCODE = "Invalid credentials";
    public final String POSTPAID_EMPTY_PASSCODE = "To complete the payment enter your Paytm Passcode";
    String xpathBuilder = "//section[contains(@class,'v-h')]";
    private SoftAssertions softly = new SoftAssertions();

    public CashierPage(String pageName) {
        super(pageName);
        this.pageURL = LocalConfig.PGP_HOST + PagePath.COMMON_CASHIER_PAGE_PATH;
        //this.waitUntilLoads();
        //takeScreenshot("Cashier Page");
    }

    private static void validateImage(String url) {
        BaseApi baseApi = new BaseApiV2();
        baseApi.setMethod(BaseApi.MethodType.GET);
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        requestSpecBuilder.setBaseUri(url);
        requestSpecBuilder.build();
        Assertions.assertThat(baseApi.execute().getStatusCode()).isEqualTo(200);
    }

    public abstract UIElement promoCode();

    public abstract boolean wallet_dropdown();

    public abstract Button buttonPGPayNow();

    public abstract UIElement lblCardNo();

    public abstract TextBox lblSavedCardCVV();

    public abstract Button buttonPpblSumbit();

    public abstract List<WebElement> lblUPIpush();

    public abstract Button BtnRemoveSavedCard();

    public abstract Button btnRemoveConfirmYes();

    public abstract UIElement insufficientBalanceIcon();

    public abstract UIElement insufficientBalanceIconMsg();

    public abstract UIElement imgDefaultCVVIcon();

    public abstract UIElement imgAmexCVVIcon();

    public abstract UIElement loginStrip();


    public abstract UIElement lblCVV();

    public abstract UIElement lblExpMonth();

    public abstract RadioButton radioBtnNetBankingOrATM(String bankName);

    public abstract Button btnCheckBalancePostpaid();

    public abstract UIElement lblInsufficientBalancePostpaid();

//=============================================================================//

    public abstract UIElement WalletTitle();

    public abstract UIElement savedCard(String cardNumber);

    public abstract UIElement EMIsavedCard(String cardNumber);

    public abstract UIElement subscriptionDetails();

    public abstract UIElement subscriptionTray();

    public abstract void RequestloginOTP(String mobileNumber);

    public UIElement subsLabelDueInfo() {
        return null;
    }
    public UIElement subsDetailsInfo(){
        return  new UIElement(By.xpath("//img[contains(@class,'info-icon xs-c-pointer')]"),getPageName(),"Subs Details");
    }
    public UIElement subDetailsPage(){
        return new UIElement(By.xpath("//div[contains(text(),'Amount to be Paid Now')]"), String.valueOf(getPopupHeadText()),"Subs Page Name");
    }
    public UIElement subsDetailsRecurringAmount() {
        return null;
    }
    public UIElement toBePaidTab() {
        return null;
    }


    public abstract String ResponseMsgRequestOtp();

    /**
     * use checkBoxPPI
     *
     * @return
     */
    public abstract CheckBox walletBalanceCheck();

    public abstract CheckBox checkBoxPPI();

    /*
    use checkBoxPpbl
     */
    @Deprecated
    public abstract CheckBox ppblBalanceCheck();

    public abstract CheckBox checkboxPaytmCC();

    public abstract UIElement upiPushSection();

    public abstract UIElement rdbtnSavedCardATMPin();

    public abstract UIElement radioBtnSavedCard(String cardId);

    public abstract Button savedCardPayNow();

    public abstract UIElement lblErrMsg();

    public abstract UIElement imgScanPayQRCode();

    public abstract UIElement imgPaytmQRSymbol();

    public abstract UIElement imgUpiQRSymbol();

    public abstract UIElement prnVerifySection();

    public abstract List<UIElement> textBoxPRNNumber();

    public abstract UIElement buttonPRNVerify();

    public abstract boolean isPPIChecked();

    public abstract boolean isPPBLChecked();

    public abstract boolean isPaytmCCChecked();

    public abstract UIElement error_invalidExpiryDate();

    public abstract UIElement ppblNotificationMsg();

    public abstract TextBox textBoxSavedCardCVV();

    public abstract UIElement applyOfferText();

    public abstract UIElement applyOfferTextSavedInstruments();

    public Link viewAllArrow(){
        return null;
    }


    public UIElement newCardRadioBtn(){
        return new UIElement(By.xpath("(//*[@class=\"ptm-checkmark\"])[2]"), getPageName(), "new card radio button");
    }

    public abstract void logout(User user);

    public abstract void fillAndSubmitEMIDetails(PaymentDTO emiDetails);

    public abstract void fillAndSubmitEMIDCDetails(PaymentDTO emiDetails);

    public UIElement ccdc_cardIframe() {
        return null;
    }

    public UIElement ccdc_emiIframe() {
        return new UIElement(By.className("ptm-emi-iframe"), getPageName(), "ptm-emi-iframe");
    }

    public UIElement kfs_frame() {
        return new UIElement(By.className("ptm-kfs-iframe"), getPageName(), "ptm-kfs-iframe");
    }

    public UIElement ppbl_passcodeIframe(){ return new UIElement(By.className("ptm-ppb-iframe"),getPageName(),"ptm-ppb-iframe");}

    public  UIElement insufficientPPIBalanceIconMsg(){return null;}

    public  UIElement merchantLOGO(){ return null;}

    public  UIElement footerLOGO(){ return null;}

    public UIElementV3 stripPayUsingPaytmApp() {
        return new UIElementV3(By.xpath("//*[contains(text(), 'Pay Using Paytm App')]"), "Pay-Using-Paytm-App-Strip");
    }

    public UIElement sc_CheckoutIframe() {
        return null;
    }
    public Link tabCreditCard() {
        return new Link(By.linkText("Credit Card"), getPageName(), "credit-card-tab");
    }

    public Link tabDebitCard() {
        return new Link(By.linkText("Debit Card"), getPageName(), "debit-card-tab");
    }
    public UIElement infoIconForPCFNew() {
        return new UIElement(By.xpath("//img[contains(@class, 'ptm-addnPayMinAmtIcon')]"), getPageName(), "info-icon");
    }

    public UIElement PlatformFeeText() {
        return new UIElement(By.xpath("//div[contains(@class, 'ptm-fee-content') and contains(text(), 'Platform fees')]"), getPageName(), "platform-fee-text");
    }
    public UIElement FeesAppliedtext() {
        return new UIElement(By.xpath("//span[@class='ptm-fee-applied-txt ptm-body-color']"), getPageName(), "Fees Applied text");
    }

    public UIElementV3 tabCard(int index) {
        if (index > 1) throw new IndexOutOfBoundsException("index cannot be greater than 1");
        return new UIElementV3(By.xpath(".//*[contains(concat(' ',normalize-space(@class),' '),' payment-type-methods ')]//*[@data-key='card'][" + (index + 1) + "]"), "card-tab-" + index);
    }

    public UIElementV3 tabPPI() {
        return new UIElementV3(By.cssSelector(".payment-type-methods .paytm-wallet"), "ppi-pay-mode-tab");
    }

    public UIElementV3 tabPPBL() {
        return new UIElementV3(By.cssSelector(".payment-type-methods *[data-key=ppb]"), "ppbl-pay-mode-tab");
    }
    public UIElementV3 tabBOB() {
        return new UIElementV3(By.xpath("//div[contains(@class, 'ptm-nb-list-item')]//div[contains(@class, 'ptm-bank-name') and text()='BANK OF BARODA']"), "bank-of-baroda-tab");    }
    

    public UIElementV3 tabPDC() {
        return new UIElementV3(By.cssSelector(".payment-type-methods *[data-key=pdc]"), "ppbl-pay-mode-tab");
    }

    public Link UseAnotherCard() {
        return new Link(By.xpath("//input[@name='sc_paymode' and @value='emi']/parent::label/span"), getPageName(),"Use-another-card");
    }

     public TextboxV3 emiAnotherCard() {
        return new TextboxV3(null, "emi-another-card-error-msg");
    }

    public Link GetAllTextDom() {
        return new Link(By.xpath("//*[contains(.,.)]"), getPageName(), "Get-all-text");
    }

//    public Link tabAdvanceDeposit() {
//        return new Link(By.linkText("Paytm Advance Account"), getPageName(), "paytm-advancedeposit-tab");
//    }

    public Link tabNetBanking() {
        return new Link(By.linkText("Net Banking"), getPageName(), "net-banking-tab") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }



    public Link tabNetBankingNEW() {
        return new Link(By.xpath("//p[contains(@class, 'ptm-paymode-name')][contains(text(), 'Net Banking')]"), getPageName(), "net-banking-tab") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }

    public UIElement paybuttonNB() {
        return new UIElement(By.xpath("//button[.//span[contains(text(), 'PAY')]]"), getPageName(), "pay-button");
    }


    public UIElement otpLimitReachedMsg(){
        throw new UnsupportedOperationException();
    }
    public UIElement validateOTPLimitBreachOnTop() {
        throw new UnsupportedOperationException();
    }

    public void fillLoginOtp(String otp){
        throw new UnsupportedOperationException();
    }

    public Button otpVerifyButton() {
        throw new UnsupportedOperationException();
    }

    public TextBox textBoxPhoneNumberNEW() {
        return new TextBox(By.xpath("//input[@id='mobile_input']"), getPageName(), "phone-number-field");
    }

    public UIElement ProceedButtonClick() {
        return new UIElement(By.xpath("//button[@class='ptm-login-btn'][contains(text(), 'Proceed Securely')]"), getPageName(), "proceed-button");
    }

    public TextBox EnterOTP() {
        return new TextBox(By.xpath("//input[@id='ptm-otp-input']"), getPageName(), "enter-otp");
    }

    public UIElement VerifyOTPButton() {
        return new UIElement(By.xpath("//button[@class='ptm-login-btn ptm-enterotp-btn'][contains(text(), 'Proceed Securely')]"), getPageName(), "Verify-otp");
    }

    public UIElement getErrorMessageInvalidOTPNew(){
        return new UIElement(By.xpath("//div[contains(@class, '_1HbL') and contains(@class, '_11dj') and contains(@class, 'o-h') and contains(@class, 'fs12') and contains(@class, 'mt5') and text()='OTP is invalid']"),getPageName(),"Invalid_otp");
    }

    public abstract UIElement tabAdvanceDeposit();
    public abstract UIElement loginSection();

    public abstract UIElement txtPromoCode();
    public  UIElement upiAutoPay(){
        return null;
    };


    public Link tabBankMandate(){throw new UnsupportedOperationException();}

    public UIElement savedBankMandateName(){return null;}

    public Link selectBankMandateBank(String bankname){throw new UnsupportedOperationException();}

    public Link bankmandateAuthMode(String authmode){throw new UnsupportedOperationException();}


    public Link tabATM() {
        return new Link(By.linkText("ATM"), getPageName(), "atm-tab");
    }

    public Link tabEMI() {

        return new Link(By.xpath("//*[text()='EMI']"), getPageName(), "emi-tab");
    }

    public Link tabCOD() {
        return new Link(By.linkText("Cash On Delivery (COD)"), getPageName(), "cod-tab");
    }

    public Link tabIMPS() {
        return new Link(By.linkText("IMPS"), getPageName(), "imps-tab");
    }

    public Link tabUPI() {
        return new Link(By.linkText("BHIM UPI"), getPageName(), "upi-tab");
    }
    public UIElement UPICollectTab() {
        return new UIElement(By.xpath("//input[@id='ptm-upi-input']"), getPageName(), "UPI collect tab");
    }

    public abstract UIElement verifyVPALinkText();

    public UIElement tabUPIId(){
        return new UIElement(By.xpath("//*[@value='UPI']"), getPageName(), "select-upi-id");
    }

    public Link tabSavedCard() {
        return new Link(By.linkText("Saved Details"), getPageName(), "saved-cards-tab");
    }
    public Link tabSavedCardBOB() {
        return new Link(By.xpath("//*[@id=\"ptm-sc\"]"), getPageName(), "saved-cards-tab");
    }
    public CheckBox saveCardForUser()
    {
        return new CheckBox(By.id("storeCard"), getPageName(), "store-card-checkbox");
    }

    public UIElement tabCheckoutUPIPollingImg() {
        return new UIElement(By.xpath("//*[@id=\"checkout-upi\"]"), getPageName(), "UPI polling img");
    }

    public abstract Link tabSavedEmi();

    public void clickSavedCardTab() {
        tabSavedCard().click();
    }

    public void assertSavedCardVisibility() {
        this.tabSavedCard().assertVisible();
    }

    public void assertSavedCardNotVisible() {
        this.tabSavedCard().assertNotVisible();
    }

    public String addNPayOverlayLocator() {
        String xPath = "//*[@id='add-money-payment-modes']";
        return xPath;
    }

    public Button bankMandateConfirmPay(){
        throw new UnsupportedOperationException();
    }

    public UIElement clickaddNPayOverlayElement() {
        return new UIElement(By.xpath("//div[@id='paymentOptionId']"), getPageName(), "Payment methods grid");
    }

    public Button buttonCODPayNow() {
        return new Button(By.xpath(
                "//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//div[contains(@id,'msg') and (contains(@style,'block') or contains(@style,'') and not(contains(@style,'none')))]//input[@value='Complete Order']"),
                getPageName(), "cod-pay-button");
    }

    public Button buttonWalletPayNow() {
        return new Button(By.cssSelector(".fullWalletDeduct:not(.f-hide)  div [class^='btn-']"), getPageName(),
                "wallet-pay-button");
    }

    public Link linkWalletCancel() {
        return new Link(By.cssSelector(".fullWalletDeduct:not(.f-hide) .cancel"), getPageName(), "cancel-wallet-txn-button");
    }

    public Link linkPGCancel() {
        return new Link(By.cssSelector(".cards-tabs:not(.hide) .content.active .cancel"), getPageName(),
                "cancel-pg-txn-button");
    }

    public TextBox textBoxVPA() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active .upiPayMode"), getPageName(),
                "vpa-field");
    }

    public abstract CheckBox checkboxPPBL();

    public UIElement insufficientPPBLBalanceIconMsg() {
        return new UIElement(By.cssSelector("section.pc-wrap div div p"), getPageName(), "ppbl-insufficient" +
                "Balance");
    }

    public List<WebElement> checkboxVPAList() {
        return DriverManager.getDriver().findElements(By.cssSelector("[name='upiPush']"));
    }

    public Button rdbtnSavedCard3DPin() {
        return new Button(By.xpath(
                "//span[text()='Enter 3D secure PIN/OTP in the next step']/preceding-sibling::input[@class='pcb checkbox paymentIdebit fl']\n"),
                getPageName(), "saved-card-3d-pin-checkbox");
    }

    public UIElement savedCardImage() {
        return new Button(By.xpath("//*[@class='cvv' and @style='display: block;']/parent::div/span"), getPageName(),
                "saved-card-atm-pin-checkbox");
    }

    public UIElement dailybreachmsg() {
        return new Button(By.xpath("//*[contains(text(), 'Payment failed as merchant has crossed his daily acceptance limit')]"), getPageName(),
                "Daily limit breach message");
    }

    public UIElement weeklybreachmsg() {
        return new Button(By.xpath("//*[contains(text(), 'Payment failed as merchant has crossed his weekly acceptance limit')]"), getPageName(),
                "Daily limit breach message");
    }

    public UIElement monthlybreachmsg() {
        return new Button(By.xpath("//*[contains(text(), 'Payment failed as merchant has crossed his Monthly acceptance limit')]"), getPageName(),
                "Monthly limit breach message");
    }

    public TextBox textBoxCardNumber() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active .cd input:first-child"),
                getPageName(), "card-number-field");
    }

    public TextBox textBoxCardShortcut() {
// Switch to the iframe
        waitUntilFrameAppearsAndSwitchToIt(cardShortcutFrame());
        return new TextBox(By.id("cardNumber"),
            getPageName(), "card-number-field");
    }
    public TextBox ExpiryMonthCardShortcut() {
        return new TextBox(By.id("cardExpirationMonth"),
                getPageName(), "cardExpirationMonth");
    }

    public TextBox ExpiryYearCardShortcut() {
        return new TextBox(By.id("cardExpirationYear"),
                getPageName(), "cardExpirationYear");
    }
    public TextBox CVVCardShortcut() {
        return new TextBox(By.id("cvv"),
                getPageName(), "cvv");
    }
    public static CheckBox SaveShortcutCard() {
        return new CheckBox(By.xpath("//*[@id=\"save-card-container\"]/span[2]"), "saveNewcard");
    }


    public Select dropdownExpiryMonth() {
        return new Select(
                By.cssSelector(".cards-tabs:not(.hide) .content.active .mb10 .fl:not(.img) select[name='expiryMonth']"),
                getPageName(), "expiry-month-dropdown");
    }

    public Select dropdownExpiryYear() {
        return new Select(
                By.cssSelector(".cards-tabs:not(.hide) .content.active .mb10 .fl:not(.img) select[name='expiryYear']"),
                getPageName(), "expiry-year-dropdown");
    }

    public TextBox textBoxCVVNumber() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active .cvv-block input[name='cvvNumber']"),
                getPageName(), "cvv-field");
    }

    public UIElement checkBoxSaveCard() {
        return new UIElement(By.xpath("//div[@class='pu-title']/parent::div//input[@type='checkbox']"),
                getPageName(), "prompt-save-card-checkbox");
    }

    public RadioButton radioBtnSaveCard() {
        return new RadioButton(By.cssSelector(".cards-tabs:not(.hide) .content.active .fl .pcb.button-checkbox button"),
                getPageName(), "saved-cards-radiobutton");
    }

    public Select dropdownNBOtherBank() {
        return new Select(By.cssSelector(".cards-tabs:not(.hide) .content.active .nbSelect"), getPageName(),
                "dropdownNBOtherBank");
    }

    public UIElement loginLoader() {
        return new UIElement(By.cssSelector("img.fl.mr10"), getPageName(), "login-loader");
    }

    public Link btnLogin() {
        return new Link(By.cssSelector("#login-btn"), getPageName(), "login-button");
    }

    public UIElement loginModal() {
        return new UIElement(By.id("login-modal"), getPageName(), "login-modal");
    }

    public TextBox textBoxPhoneNumber() {
        return new TextBox(By.xpath("//input[@id='mobile_input']"), getPageName(), "phone-number-field");
    }

    public TextBox textBoxPassword() {
        return new TextBox(By.cssSelector("input[name='password']"), getPageName(), "password-field");
    }

    public Button buttonSecureSignIn() {
        return new Button(By.cssSelector(".btn.btn-primary.ng-binding"), getPageName(), "secure-sign-in-button");
    }

    public UIElement totalAmount() {
        return new UIElement(By.xpath("//*[@id=\"totalAmountSpan\"]"), getPageName(), "total-amount");
    }

    public UIElement totalAmtPG() {
        return new UIElement(By.id("totaltxnAmt"), getPageName(), "pg-total-amount");
    }

    public UIElement totalAmtPGNew() {
        return new UIElement(By.xpath("//button[contains(@class, 'ptm-custom-btn')]"), getPageName(), "pg-total-amount");
    }

    public UIElement totalAmtAtPG() {
        return new UIElement(By.xpath("//span[contains(@class,'textItem')][contains(text(),'1.07')]"), getPageName(), "pg-total-amount");
    }

    public UIElement chargeFeeAmtAtPG() {
        return new UIElement(By.xpath("//span[contains(@class,'ptm-fee-sub-heading')]"), getPageName(), "pg-charge-fee");
    }

    public UIElement totalAmtPPI() {
        return new UIElement(By.xpath("//*[@id='addMoney-totaltxnAmt']/parent::strong/span[not(@style='display: none;')][2]"), getPageName(), "ppi-total-amount");
    }

    public UIElement baseAmtPG() {
        return new UIElement(By.id("baseAmt"), getPageName(), "pg-base-amount");
    }

    public UIElement baseAmtPPI() {
        return new UIElement(By.xpath("//*[@id='addMoney-baseAmt']/parent::span/span[not(@style='display: none;')][2]"), getPageName(), "ppi-base-amount");
    }

    public UIElement chargeFeeAmtPG() {
        return new UIElement(By.xpath("//*[@class='fw700']//child::i"), getPageName(), "pg-charge-fee-amount");
    }

    public UIElement chargeFeeAmtPGNew() {
        return new UIElement(By.xpath("//*[@class='_1XBI fw700']"), getPageName(), "pg-charge-fee-amount");
    }

    public UIElement chargeFeeAmtPPI() {
        return new UIElement(By.xpath("//*[@id='addMoney-chargeFeeAmt']/parent::span/span[not(@style='display: none;')][2]"), getPageName(), "ppi-charge-fee-amount");
    }

    public UIElement netBankingOtherBank() {
        return new UIElement(By.xpath("//span[text()='Select from all other banks']"), getPageName(), "select-from-all-other-banks-button");
    }

    public UIElement radioBtnSearchedBank(String bankName) {
        return new UIElement(By.xpath("//li//span[contains(text(),'" + bankName + "')]"), getPageName(), "searched-bank-radiobutton");
    }

    public TextBox textBoxSearchBank() {
        return new TextBox(By.xpath("//input[@placeholder='Search Bank']"), getPageName(), "search-bank-field");
    }

    public RadioButton radioButtonEMIOption(int months) {

        return new RadioButton(By.cssSelector(".cards-tabs:not(.hide) .content.active .emi-bank-map:not(.hide) .emi-bank-plans:not(.hide) input[value$='" + months + "'] +span button"), getPageName(), "emi-radiobutton");
    }

    public List<WebElement> EMIOptions(String bankName) {
        UIElement ul = new UIElement(By.cssSelector(".cards-tabs:not(.hide) .content.active .emi-bank-map:not(.hide) ."
                + bankName.toUpperCase() + "-bank:not(.hide) ul"), getPageName(), "emi-options");
        return ul.findElements(By.tagName("li"));
    }

    public UIElement EMIMonths(String bankName, int months) {
        return new RadioButton(By.cssSelector(".cards-tabs:not(.hide) .content.active .emi-bank-map:not(.hide) ."
                + bankName.toUpperCase() + "-bank:not(.hide) input[value='" + bankName.toUpperCase() + "|" + months
                + "'] ~ span.emi-month"), getPageName(), "emi-months");
    }

    public WebElement EMIMonths(WebElement parentElement) {
        return parentElement.findElement(By.cssSelector(".emi-month.medium.b"));
    }

    public RadioButton rdbtn3DSecurePin() {
        return new RadioButton(By.xpath(
                "//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//div[contains(@class,'content') and contains(@class,'active')]//div[@class='idebitOption'][2]//button"),
                getPageName(), "3d-secure-pin-radiobutton") {
            @Override
            public boolean isSelected() {
                UIElement uiElement = new UIElement(By.xpath("//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//div[contains(@class,'content') and contains(@class,'active')]//div[@class='idebitOption'][2]//button/span[@class='tick cb-icon-check']"), getPageName(), "");
                return uiElement.isDisplayed();
            }
        };

    }

    public RadioButton rdbtnATMPin() {
        return new RadioButton(By.xpath(
                "//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//div[contains(@class,'content') and contains(@class,'active')]//div[@class='idebitOption'][1]//button"),
                getPageName(), "atm-pin-radiobutton") {
            @Override
            public boolean isSelected() {
                UIElement uiElement = new UIElement(By.xpath("//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//div[contains(@class,'content') and contains(@class,'active')]//div[@class='idebitOption'][1]//button/span[@class='tick cb-icon-check']"), getPageName(), "");
                return uiElement.isDisplayed();
            }
        };
    }

    public UIElement EMIAmount(String bankName, int months) {
        return new RadioButton(By.xpath(
                "//div[contains(@class,'cards-tabs') and not(contains(@class,'hide'))]//div[contains(@class,'content') and contains(@class,'active')]//*[contains(@class,'emi-bank-map') and not(contains(@class,'hide'))]//div/input[@value='"
                        + bankName + "|" + months + "']/parent::div/following-sibling::div/span[@class='emi-amt']"),
                getPageName(), "emi-amount");
    }

    public WebElement EMIAmount(WebElement parentElement) {
        return parentElement.findElement(By.cssSelector(".emi-amt"));
    }

    public UIElement EMIInterest(String bankId, int months) {
        return new RadioButton(By.xpath("//ul[@id='" + bankId + "-emi-plans']//span[@class='emi-interest']/span[contains(text(),'" + months + "')]"),
                getPageName(), "emi-interest");
    }

    public WebElement EMIInterest(WebElement parentElement) {
        return parentElement.findElement(By.cssSelector(".emi-interest span"));
    }

    public UIElement hybridMoneyAmount() {
        return new UIElement(By.cssSelector(".hybridMoneyAmount"), getPageName(), "hybrid-money-amount");
    }

    public UIElement savedCardNumber() {
        return new UIElement(By.cssSelector(".cards-tabs:not(.hide) .content.active input[name='prependedradio']"),
                getPageName(), "saved-card-number");
    }

    public List<WebElement> savedCardNumbers() {
        return DriverManager.getDriver()
                .findElements(By.cssSelector(".cards-tabs:not(.hide) .content.active input[name='prependedradio']"));
    }

    public UIElement savedCardBlock(String cardNumber) {
        // format card number in XXXX format
        return new UIElement(
                By.xpath("//input[@value='" + cardNumber + "']/ancestor::div[contains(@class, 'control-group')]"),
                getPageName(), "saved-card-block");
    }

    public UIElement savedCardBlock(int cardPosition) {
        return new UIElement(By.xpath("//div[contains(@class, 'control-group')][" + cardPosition
                + "]//input[contains(@class,'savedCardLabel')]"), getPageName(), "saved-card-block");
    }

    public UIElement paymentDeclinedAlert() {
        return new UIElement(By.cssSelector(".notification.alert.mt10"), getPageName(), "payment-declined-alert");
    }

    public UIElement defaultPaymentType() {
        return new UIElement(By.cssSelector(".cards-tabs:not(.hide) .card.active>a"), getPageName(),
                "default-payment-type");
    }

    public Link btnLogout() {
        return new Link(By.cssSelector("#logout-btn"), getPageName(), "logout-button");
    }

    public UIElement loggedInUserPhone() {
        return new UIElement(By.cssSelector(".fl.user-name.right-text>span"), getPageName(), "logged-in-user-phone-number");
    }

    public TextBox textBoxMMID() {
        return new TextBox(By.cssSelector("#mmid"), getPageName(), "mmid-field");
    }

    public TextBox textBoxOTP() {
        return new TextBox(By.cssSelector("#otp"), getPageName(), "otp-field");
    }

    public UIElement imgOOPS() {
        return new UIElement(By.cssSelector(".white_container.width-pad>div>img"), getPageName(), "oops-image");
    }

    public TextBox textBoxIMPSMobile() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active #mobileNo"), getPageName(),
                "imps-mobile-number-field");
    }

    public TextBox textBoxIMPSMMID() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active  #mmid"), getPageName(),
                "imps-mmid-field");
    }

    public TextBox textBoxIMPSOTP() {
        return new TextBox(By.cssSelector(".cards-tabs:not(.hide) .content.active #otp"), getPageName(),
                "imps-otp-field");
    }

    public UIElement loginFrame() {
        return new UIElement(By.cssSelector("div.md-show #login-iframe.loaded"), getPageName(), "login-frame");
    }

    public UIElement cardShortcutFrame() {
        return new UIElement(By.cssSelector(".ptm-card-iframe"), getPageName(), "card-shortcut-frame");
    }

    public UIElement tokenCVVFrame() {
        return new UIElement(By.cssSelector(".ptm-sc-iframe.show-cvv-iframe"), getPageName(), "token-cvv-frame");
    }

    public void switchToLoginFrame() {
        loginFrame().switchToFrame();
    }

    public boolean waitForNewWindow(int timeout) {
        boolean flag = false;
        int counter = 0;
        while (!flag) {
            try {
                Set<String> winId = DriverManager.getDriver().getWindowHandles();
                if (winId.size() > 1) {
                    flag = true;
                    return flag;
                }
                Thread.sleep(1000);
                counter++;
                if (counter > timeout) {
                    return flag;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        return flag;
    }
    public UIElement getTextCODMessage() {
        return new UIElement(
                By.xpath("//div[contains(@id,'cod') and contains(@id,'msg') and not(contains(@style,'none'))]//span"),
                getPageName(), "cod-info-msg");
    }

    public UIElement txnDetailsContainer() {
        return new UIElement(By.cssSelector("div.card.summary-card.mb20"), getPageName(), "txn-details-container");
    }

    public Link linkShowMore() {
        return new Link(By.cssSelector(".btn-show-payment-details"), getPageName(), "show-payment-details-button");
    }

    public UIElement loginContainer() {
        return new UIElement(By.id("login-container"), getPageName(), "login-container");
    }

    public UIElement paymentContainer() {
        return new UIElement(By.className("cards-content"), getPageName(), "payment-container");
    }

    public UIElement notificationContainer() {
        return new UIElement(By.className("notification-container"), getPageName(), "notification-container");
    }

    public UIElement labelInvalidCCNum() {
        return new UIElement(By.xpath("//*[@id=\"card\"]/ul/li[1]/label[contains(@class,'red-text')]"), getPageName(),
                "invalid-cc-number-msg");
    }

    public UIElement labelInvalidExpdate() {
        return new UIElement(By.xpath("//*[@id=\"card\"]/ul/li[2]/label[contains(@class,'red-text')]"), getPageName(),
                "invalid-expiry-date-msg");
    }

    public UIElement labelInvalidCVV() {
        return new UIElement(By.xpath("//*[@id=\"ccCvvWrapper\"]/div[1]/label[contains(@class,'red-text')]"),
                getPageName(), "invalid-cvv-number-msg");
    }

    public UIElement pcfAllIn1QrAlertMsg() {
        return new UIElement(By.xpath("//div[@data-key='qr-section']/div[1]/section"),
                getPageName(), "alert-msg-for-pcf-allIn1Qr");
    }

    public RadioButton radioButtonPaytmPostpaid() {
        return new RadioButton(By.xpath("//*[@id='paytmDigitalCard']/span"), getPageName(), "paytm-digital-card-radiobutton");
    }

    public RadioButton radioButtonPaytmPostpaidClickable() {
        return new RadioButton(By.xpath("//*[@id='paytmDigitalCard']/span/button/span[@class='tick cb-icon-check']"), getPageName(),
                "paytm-digital-card-radiobutton");
    }

    public RadioButton radioButtonWalletUnchecked() {
        return new RadioButton(By.xpath("//*[@id='pc']//*[@class='tick cb-icon-check-empty']"), getPageName(),
                "ppi-checked-radiobutton");
    }

    public RadioButton radioButtonWalletChecked() {
        return new RadioButton(By.xpath("//*[@id='pc']//*[@class='tick cb-icon-check']"), getPageName(),
                "ppi-unchecked-radiobutton");
    }

    public RadioButton radioButtonWallet() {
        return new RadioButton(By.xpath("//*[@id='checkbox' and @class='xs-c-pointer']"), getPageName(),
                "ppi-radiobutton");
    }

    public abstract UIElement postpaidOnboard_TNC();

    public abstract UIElement postpaidOnboard_Accept_PayButton();

    public  abstract UIElement applyPromoText();

    public  abstract Link tabUPISavedVPA();

    public  abstract UIElement OfferStripSavedPaymode();

    public  abstract UIElement tpvAccountAlert();

    public abstract UIElement tpvAccountInfo();

    public abstract UIElement tpvAccountAlertInfo();

    public TextBox textBoxPPBLPassCode() {
        return new TextBox(By.xpath("//input[@id='ppbl_pin']"), getPageName(), "ppbl-passcode-field");
    }
    public UIElement applyEMIPromoText()
    {
        return new UIElement(By.xpath("//*[@id=\"planDetails\"]/div[4]"), getPageName(), "applyPromoText");
    }

    public TextBox textBoxPostpaidPassCode() {
        return new TextBox(By.xpath("//input[@placeholder='Enter Passcode']"), getPageName(), "postpaid-passcode-field");
    }

    public Button buttonPostPaidPayNow() {
        return new Button(By.xpath("//input[@value='Proceed to Pay']"), getPageName(), "postpaid-pay-button");
    }

    public TextBox loginOtpBox() {
        return new TextBox(By.id("inp"), getPageName(), "otp-field");
    }

    public abstract UIElement convinenceCharge();

    public abstract UIElement convinenceFeeCCAmount();

    public abstract UIElement convinenceFeeDCAmount();

    public abstract UIElement convinenceFeeNBAmount();
    public abstract UIElement MerchantName();
    public abstract UIElement convinenceFeeUPIAmount();
    public abstract UIElement mobileNumber();
    public abstract UIElement convinenceFeeWalletAmount();

    public abstract List<UIElement> ListOfPayModsOnCashier();

    public List<UIElement> ListOfPayModsOnConvinenceFeeDropDown() {
        return UIElements.getMultiple(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//li"), getPageName(), "All pay modes on Convenience fee dropdown");//UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='Paytm Balance']/following-sibling::div"), getPageName(), "Convenience fee PAYTM WALLET");
    }

    public int getConvenienceFeeTableRowCount() {
        return UIElements.getMultiple(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//li"), getPageName(), "All pay modes on Convenience fee dropdown").size();
    }

    public Table tableConvFeeCharge() {
        return new Table(By.xpath(""), getPageName(), "") {
            @Override
            public int getRowCount() {
                return UIElements.getMultiple(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//li"), getPageName(), "All pay modes on Convenience fee dropdown").size();
            }

            public String getRowValue(String rowName) {
                UIElement uiElement = new UIElement(By.xpath("//span[text()='Convenience Charges']/ancestor-or-self::section[1]//div[text()='" + rowName + "']/following-sibling::div"), getPageName(), "Convenience fee for " + rowName + " paymode") {
                    @Override
                    public String getText() {

                        return super.getText().replace("GST", "").replace("as applicable", "").replace("+", "").replace("Rs", "").trim();
                    }
                };
                return uiElement.getText();
            }
        };
    }

    public RadioButton radioButtonPostpaid() {
        return null;
    }

    public abstract CheckBox rememberMeCheckbox();

    public abstract PopUpV2 modalRetryPayment();

    /**
     * use radioBtnSavedCard
     *
     * @param cardId
     * @return
     */
    @Deprecated
    public RadioButton radioBtnSelectSavedCard(String cardId) {
        String id = "cvv_" + cardId;
        return new RadioButton(By.id(id), "cashier page", "saved-card-id:" + cardId);
    }

    public abstract Select dropdownEmiBanks();

    public abstract UIElement emiDurationOption(int EMIMonth);

    public abstract UIElement selectEMIPlan();

    public abstract UIElement labelPaymodeInfoMsg();

    public abstract Select dropdownNB();

    public abstract UIElement postpaidSignUpStrip();

    public abstract UIElement PayButtonWithWallet();

    public abstract UIElement PayButtonWithPPBL();

    public abstract UIElement PayButtonWithSC();

    public abstract UIElement PayButtonWithPostPaid();

    public UIElement getGiftVoucherBalance(){
        return new UIElement( By.xpath("//*[contains(text(),'Voucher')]/parent::span/child::label/span[2]"),getPageName(),"Gift Voucher Balance");
    }

    public UIElement payButtonForSavedCard(){
        return new UIElement( By.xpath("//button[contains(@class, 'ptm-custom-btn')][contains(text(), 'Pay ₹1')]"),getPageName(),"Saved Card Pay Button");
    }

    public UIElement giftVoucherHeader(){
        return new UIElement(By.xpath("//*[contains(text(),'Voucher')]"),getPageName(),"Gft Voucher Name");
    }

    public UIElement offerStripVisible(){
        return new UIElement(By.xpath("//div[contains(@id, 'checkout-offers')]"),getPageName(),"offer-strip-visible");
    }

    public Button loginProceedButton() {
        throw new UnsupportedOperationException();
    }

    public UIElement getAddnPayLimitError(){
        return new UIElement(By.xpath("//*[@id=\"ptm-card\"]/div[2]/div/section/div/div[2]/div[2]/div[2]/div"), getPageName(), "AddnPayViaCC");
    }

    public UIElement getAddnPayLimitErrorOnCheckoutJS(){
        return new UIElement(By.xpath("//*[@id=\"checkout-overlay-box\"]/div/div/div[2]/div/div/div"), getPageName(), "AddnPayViaCConCheckout");
    }

    public UIElement getUItextMsgOfPaymodes(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-qr-wrap')]//*[contains(@class,'ptm-txinfo')]"), getPageName(), "getUItextMsgOfPaymodes");
    }

    public UIElement getUItextMsgOfPaymodesloginOtp(){
        return new UIElement(By.xpath("//*[@class='ptm-body-color ptm-txinfo ptm-bottom12']"), getPageName(), "getUItextMsgOfPaymodesloginOtp");
    }

    public UIElement getUItextMsgOfPaymodesScanNPay(){
        return new UIElement(By.xpath("//*[@class='_2NFB sub-label mt5']"), getPageName(), "getUItextMsgOfPaymodesScanNPay");
    }

    public UIElement loginPosition() {
        return new UIElement(By.className("ptm-login-qr-container"), getPageName(), "login-position");
    }
    public UIElement TextUpi() {
        return new UIElement(By.className("ptm-upi-txinfo"), getPageName(), "qr-container");
    }
    public UIElement savedcardsandbalance(){
        return new UIElement(By.linkText("or use saved cards and balance"), getPageName(), "saved-cards-and-balance");
    }

    public CashierPage validateInsufficientIcon(String theme) {
        switch (theme.toLowerCase()) {
            case Constants.Theme
                    .MERCHANT4:
                this.softly.assertThat(this.insufficientBalanceIconMsg().isDisplayed()).isTrue().as("Insufficient msg not displayed");
                this.softly.assertThat(this.insufficientBalanceIconMsg().getText()).isEqualToIgnoringCase("You do not have " +
                        "sufficient balance for this transaction").as("Insufficient msg displayed");
                break;
            case Constants.Theme
                    .MERCHANTLOW5:
                this.softly.assertThat(this.insufficientBalanceIcon().isDisplayed()).as("Insufficient Icon displayed");
                this.insufficientBalanceIcon().click();
                this.softly.assertThat(this.insufficientBalanceIconMsg().getAttribute("style")).contains("display");
                this.softly.assertThat(this.insufficientBalanceIconMsg().getText()).as("Insufficient Icon message").isEqualTo(
                        "This payment method is disabled because your balance " + "is insufficient for this transaction.");
                break;

        }
        return this;
    }

    public CashierPage AssertAll() {
        this.softly.assertAll();
        return this;
    }

    public void fillExpiryMonth(String expiryMonth) {
        dropdownExpiryMonth().selectByVisibleText(expiryMonth);
    }

    public void fillExpiryYear(String expiryYear) {
        dropdownExpiryYear().selectByVisibleText(expiryYear);
    }

    public void select3DSecurePinOptionIfDisplayed() {
        if (rdbtn3DSecurePin().isDisplayed()) {
            rdbtn3DSecurePin().click();
        }
    }

    public void selectATMPinOptionIfDisplayed() {
        if (rdbtn3DSecurePin().isDisplayed()) {
            rdbtn3DSecurePin().click();
        }
    }

    public void fillDCDetails(PaymentDTO dcDetails) {
        if (!(dcDetails.getDebitCardNumber() == null || dcDetails.getDebitCardNumber().isEmpty()))
            textBoxCardNumber().clearAndType(dcDetails.getDebitCardNumber());
        if (!(dcDetails.getExpMonth() == null || dcDetails.getExpMonth().isEmpty()))
            fillExpiryMonth(dcDetails.getExpMonth());
        if (!(dcDetails.getExpYear() == null || dcDetails.getExpYear().isEmpty()))
            fillExpiryYear(dcDetails.getExpYear().substring(2, 4));
        if (!(dcDetails.getCvvNumber() == null || dcDetails.getCvvNumber().isEmpty())) {
            textBoxCVVNumber().waitUntilVisible();
            textBoxCVVNumber().clearAndType(dcDetails.getCvvNumber());
        }
    }

    public void fillCCDetails(PaymentDTO ccDetails) {
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

    public void fillEMIDetails(PaymentDTO emiDetails) {
        pause(1);
        dropdownEmiBanks().selectByVisibleText(emiDetails.getBankName());
            textBoxCardNumber().clearAndType(emiDetails.getCreditCardNumber());
            System.out.println();
        }

    //// div[@class='mt10 ml20 fl paytmCCDeduct']
    public void fillAndSubmitCCDetails(PaymentDTO ccDetails, Boolean saveCard) {
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
        if(saveCard) {
            checkBoxSaveCard().click();
        }
        buttonPGPayNow().click();
    }

    public void fillAndSubmitCCDetailsWithSinglePayMode(PaymentDTO ccDetails, Boolean saveCard) {
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
        if(saveCard) {
            checkBoxSaveCard().click();
        }
        buttonPGPayNow().click();
    }

    public void fillInvalidCCDetails(PaymentDTO ccDetails){
    }

    public void fillAndSubmitDCDetails(PaymentDTO dcDetails, Boolean saveCard) {

        scrollToElement(tabDebitCard());
        tabDebitCard().click();
        if (!(dcDetails.getDebitCardNumber() == null || dcDetails.getDebitCardNumber().isEmpty()))
            textBoxCardNumber().clearAndType(dcDetails.getDebitCardNumber());
        if (!(dcDetails.getExpMonth() == null || dcDetails.getExpMonth().isEmpty()))
            fillExpiryMonth(dcDetails.getExpMonth());
        if (!(dcDetails.getExpYear() == null || dcDetails.getExpYear().isEmpty()))
            fillExpiryYear(dcDetails.getExpYear().substring(2, 4));
        if (!(dcDetails.getCvvNumber() == null || dcDetails.getCvvNumber().isEmpty())) {
            textBoxCVVNumber().waitUntilVisible();
            textBoxCVVNumber().clearAndType(dcDetails.getCvvNumber());
        }
        if(saveCard) {
            checkBoxSaveCard().click();
        }
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    private void fillAndSubmitDCDetailsWithAtmPin(PaymentDTO dcDetails) {
        tabDebitCard().waitUntilClickable();
        tabDebitCard().click();
        if (!(dcDetails.getDebitCardNumber() == null || dcDetails.getDebitCardNumber().isEmpty()))
            textBoxCardNumber().clearAndType(dcDetails.getDebitCardNumber());
        if (!(dcDetails.getExpMonth() == null || dcDetails.getExpMonth().isEmpty()))
            fillExpiryMonth(dcDetails.getExpMonth());
        if (!(dcDetails.getExpYear() == null || dcDetails.getExpYear().isEmpty()))
            fillExpiryYear(dcDetails.getExpYear().substring(2));
        pause(2);
        selectATMPinOptionIfDisplayed();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    protected void fillAndSubmitSavedCardDetails(PaymentDTO savedCardDetails) {

        tabSavedCard().waitUntilClickable();
        tabSavedCard().click();

        if (rdbtn3DSecurePin().isDisplayed()) {
            rdbtn3DSecurePin().click();
        }
        if (!(savedCardDetails.getCvvNumber() == null || savedCardDetails.getCvvNumber().isEmpty())) {
            textBoxSavedCardCVV().waitUntilVisible();
            textBoxSavedCardCVV().clearAndType(savedCardDetails.getCvvNumber());
        }
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    private void fillAndSubmitSavedUPIDetails(PaymentDTO paymentDTO) {
        tabSavedUPI(paymentDTO.savedVpaIndex).click();

        //checkboxVPAList().get(0).click();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    public Link tabSavedUPI(int index) {
        return null;
    }

    public List<UIElement> tabSavedVPAs() {
        return UIElements.getMultiple(By.cssSelector(".ptm-pay-method .ptm-radio-wrap .ptm-svpa-bank "), getPageName(), "saved-vpa-tab");
    }

    public int savedUpiListSize(){
        List<WebElement> links;
        links = DriverManager.getCurrentWebDriver().findElements(By.xpath("//*[@id=\"checkout-upi-push\"]"));
        return links.size();
    }

    public UIElement getUPILogo(){
        return null;
    }

    public UIElement getBankLogo(){
        return new UIElement(By.xpath("//img[contains(@class,'ptm-pos-a')]"), getPageName(), "Bank Logo Url");
    }
    public UIElement getBankLogoEMI(){
        return new UIElement(By.xpath("//*[contains(@src,'/native/bank/HDFC.png')]"), getPageName(), "Bank Logo Url");
    }

    protected void fillAndSubmitEMI_SavedCard(PayMode payMode, PaymentDTO emiDetails) {

    }

    protected void fillAndSubmitCODDetails() {
        scrollToElement(tabCOD());
        tabCOD().waitUntilClickable();
        tabCOD().click();
        buttonCODPayNow().waitUntilClickable();
        buttonCODPayNow().click();
    }

    protected void fillAndSubmitUPIDetails(PaymentDTO upiDetails) {
        scrollToElement(tabUPI());
        tabUPI().waitUntilClickable();
        tabUPI().click();
        tabUPIId().click();
        textBoxVPA().clearAndType(upiDetails.getVpa());
//        verifyVPALinkText().click();
        textBoxVPA().click();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    protected void fillAndSubmitUPIDetailsWithSinglePayMode(PaymentDTO upiDetails) {
        tabUPIId().click();
        textBoxVPA().clearAndType(upiDetails.getVpa());
//        verifyVPALinkText().click();
        textBoxVPA().click();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    protected void fillAndSubmitNBDetails(PaymentDTO nbDetails) {
        scrollToElement(tabNetBanking());
        tabNetBanking().click();
        dropdownNB().selectByValue(nbDetails.getBankName());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    protected void fillAndSubmitNBDetailsWithSinglePayMode(PaymentDTO nbDetails) {
        scrollToElement(tabNetBanking());
        tabNetBanking().click();
        dropdownNB().selectByValue(nbDetails.getBankName());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    public void fillAndSubmitWalletDetails(PaymentDTO walletDetails) {
        this.checkBoxPPI().waitUntilChecked();
        this.checkBoxPPI().check();
        buttonWalletPayNow().waitUntilClickable();
        buttonWalletPayNow().click();
    }

    public void fillAndSubmitWalletPasscodeDetails(PaymentDTO walletDetails) {
        this.checkBoxPPI().waitUntilChecked();
        this.checkBoxPPI().check();
        textBox2FAPassCode().waitUntilEditable();
        textBox2FAPassCode().clearAndType(walletDetails.getPasscode());
        buttonWalletPayNow().waitUntilClickable();
        buttonWalletPayNow().click();
    }


    public void fillAndSubmitBankMandateDetails(PaymentDTO mandateDetails) {

        BankMandateRadioButton().click();
        bankmandateAuthMode(mandateDetails.getMandateAuthMode()).click();
        buttonPGPayNow().click();
    }

    public UIElement getPendingTxnTimer(){
        return new UIElement(By.cssSelector(".ptm-timer-box .ptm-countdown .ptm-timer-left"),getPageName(),"timer-logo");
    }

    public UIElement getPendingTxnMsg(){
        return new UIElement(By.cssSelector(".ptm-timer-box .ptm-countdown .ptm-overlay-txt"),getPageName(),"timer-logo");
    }

    public void validatePendingTxnMsg(){
        String pendingTxnMsg = getPendingTxnMsg().getText();
        Assertions.assertThat(pendingTxnMsg).isEqualTo("Your payment request is taking more than usual time. Please stay with us and do not close the window or press back.");
    }

    public UIElement getPopupHeadText(){
        return new UIElement(By.cssSelector(".ptm-process-wrap .ptm-process-heading"),getPageName(),"processing-your-payment-msg");
    }



    public boolean validateProcessingYourPaymentHeadText(){
        if(getPopupHeadText()!=null) {
            String processingYourPaymentHeadText = getPopupHeadText().getText();
            Assertions.assertThat(processingYourPaymentHeadText).isEqualTo("Processing Your Payment");
            return true;
        }else {
            return false;
        }
    }

    public UIElement getPaymentTextMsg(){
        return new UIElement(By.cssSelector(".ptm-process-wrap .ptm-overlay-txt"),getPageName(),"text-payment-msg");
    }

    public void validateProcessingYourPaymentMsg(){
        String processingYourPaymentMsg = getPaymentTextMsg().getText();
        Assertions.assertThat(processingYourPaymentMsg).isEqualTo("Please do not close this window or press back while we confirm your payment status");
    }

    public UIElement getValidatingResponseHeadText(){
        return new UIElement(By.xpath("//div[span/text() = \"Validating Response\"]"), getPageName(), "processing-your-payment-msg");
    }

    public void validateProcessingYourPaymentHead(){
        if(getPopupHeadText() != null){
            String validatingResponseHeadText = getPopupHeadText().getText();
            Assertions.assertThat(validatingResponseHeadText).isEqualTo("Awaiting response...");
        }

    }

    public void validateResponseHeadText(){
        try{
            DriverManager.setWebDriverElementWait(Duration.ofSeconds(200));
            getValidatingResponseHeadText().waitUntilVisible();
            String validatingResponseHeadText = getValidatingResponseHeadText().getText();
            Assertions.assertThat(validatingResponseHeadText).isEqualTo("Validating Response");
        }
        finally {
            Duration waitTime = Duration.parse(System.getProperty("MAX_ELEMENT_LOAD_WAIT_TIME","60"));
            DriverManager.setWebDriverElementWait(waitTime);
        }
    }

    public void validateResponseTextMsg(){
        String validatingResponseTextMsg = getPaymentTextMsg().getText();
        Assertions.assertThat(validatingResponseTextMsg).isEqualTo("Please stay with us while we try to fetch the final payment status");
    }


    public void fillAndSubmitSavedBankMandateDetails(PaymentDTO mandateDetails) {

        SavedBankMandateRadioButton(mandateDetails.getSavedBankMandateAccount()).click();
        bankmandateAuthMode(mandateDetails.getMandateAuthMode()).click();
        proceedBtn().click();
    }

    public void fillAndSubmitPPBLDetail(PaymentDTO ppblDetails) {
        checkboxPPBL().check();
        textBoxPPBLPassCode().waitUntilEditable();
        textBoxPPBLPassCode().clearAndType(ppblDetails.getPasscode());
        buttonPpblSumbit().waitUntilClickable();
        buttonPpblSumbit().click();
    }

    private void fillAndSubmitAdvanceDepositDetails() {
        tabAdvanceDeposit().click();
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    protected void fillAndSubmitDigitalCardDetails(PaymentDTO digitalCardDetails) {
        if (!isPaymodeModeSelected(PayMode.PAYTM_DIGITAL_CARD)) {
            radioButtonPaytmPostpaid().click();
        }
        //textBoxPostpaidPassCode().clearAndType(digitalCardDetails.getPasscode());    //REMOVED PASSCODE ON POSTPAID
        buttonPostPaidPayNow().waitUntilClickable();
        buttonPostPaidPayNow().click();
    }

    public abstract void submitPostPaidOnboarding();

    public abstract void directLogin(User user);

    public void signin(String mobileNumber, String password) {
        this.waitUntilContainsText("Login");
        if (!loginModal().isDisplayed()) {
            btnLogin().waitUntilClickable();
            btnLogin().click();
        }
        waitUntilFrameAppearsAndSwitchToIt(loginFrame());
        textBoxPhoneNumber().waitUntilEditable();
        textBoxPhoneNumber().clearAndType(mobileNumber);
        textBoxPassword().clearAndType(password);
        buttonSecureSignIn().waitUntilClickable();
        buttonSecureSignIn().click();
        waitUntilLoads();
    }

    public void login(String mobileNumber) {
        signin(mobileNumber, "Paytm@1234");
    }

    public void login(User user) {
        signin(user.mobNo(), user.password());
        pause(4);
    }

    public void login(String mobileNumber, String password) {
        signin(mobileNumber, password);
    }

    public void vanSignin(String mobileNumber, String password) {
//        vanLoginMobile().click();
        CrossButtonEnterMobile().click();
        textBoxPhoneNumber().clearAndType(mobileNumber);
        buttonSecureSignInVan().click();
        pause(2);
        String otp;
        otp = "123456";
        fillLoginOtp(otp);
        otpVerifyButtonVan().click();
        waitUntilLoads();
    }
    public void vanSignIn(User user){
        vanSignin(user.mobNo(),user.password());
        pause(4);
    }
    public UIElement vanLoginMobile(){
        return new UIElement(By.xpath("//*[@id=\"mobile_input\"]"),getPageName(),"Mobile-number");
    }

    public Button CrossButtonEnterMobile()
    {
        return new Button(By.xpath("//span[@class='ptm-cross ptm-pos-r']"),getPageName(), "Croos button on enter mobile number");
    }
    public Button buttonSecureSignInVan() {
        return new Button(By.xpath("//button[contains(@class,'ptm-login-btn')]"), getPageName(), "sign-in-button" /*this.buttonPGPayNow().getElementName()*/);
    }


    public Button otpVerifyButtonVan() {
        return new Button(By.xpath("//button[contains(@class,'ptm-enterotp-btn')]"), getPageName(), "otp-verify-button");
    }


    @Step("Pay through {0}")
    public void payBy(PayMode payMode, PaymentDTO paymentDTO) {
        switch (payMode) {
            case CC:
                clickPgOverlay();
                fillAndSubmitCCDetails(paymentDTO, false);
                break;
            case CC_WITH_SAVECARD:
                clickPgOverlay();
                fillAndSubmitCCDetails(paymentDTO, true);
                break;
            case PPBL:
                clickPgOverlay();
                fillAndSubmitPPBLDetail(paymentDTO);
                break;
            case DC:
                clickPgOverlay();
                fillAndSubmitDCDetails(paymentDTO, false);
                break;
            case DC_WITH_SAVECARD:
                clickPgOverlay();
                fillAndSubmitDCDetails(paymentDTO, true);
                break;
            case SAVED_CARD:
                clickPgOverlay();
                fillAndSubmitSavedCardDetails(paymentDTO);
                break;
            case SAVED_UPI:
                clickPgOverlay();
                fillAndSubmitSavedUPIDetails(paymentDTO);
                break;
            case EMI:
             //   clickPgOverlay();
                fillAndSubmitEMIDetails(paymentDTO);
                break;
            case NB:
                clickPgOverlay();
                fillAndSubmitNBDetails(paymentDTO);
                break;
            case COD:
                clickPgOverlay();
                fillAndSubmitCODDetails();
                break;
            case UPI:
                clickPgOverlay();
                fillAndSubmitUPIDetails(paymentDTO);
                break;
            case WALLET:
                fillAndSubmitWalletDetails(paymentDTO);
                break;
            case WALLET_PASSCODE:
                fillAndSubmitWalletPasscodeDetails(paymentDTO);
                break;
            case PAYTM_DIGITAL_CARD:
                fillAndSubmitDigitalCardDetails(paymentDTO);
                break;
            case DC_WITH_ATMPIN:
                clickPgOverlay();
                fillAndSubmitDCDetailsWithAtmPin(paymentDTO);
                break;
            case EMI_SAVED_CARD:
                clickPgOverlay();
                fillAndSubmitEMI_SavedCard(payMode, paymentDTO);
                break;
            case POSTPAID_ONBOARDING:
                submitPostPaidOnboarding();
                break;
            case ADVANCE_DEPOSIT_ACCOUNT:
                clickPgOverlay();
                fillAndSubmitAdvanceDepositDetails();
                break;
            case ZEST:
                clickPgOverlay();
                fillAndSubmitZestDetails(paymentDTO);
                break;
            case MGV:
                clickPgOverlay();
                MGVradioButton().click();
                buttonPGPayNow().click();
                break;
            case BANK_MANDATE:
                clickPgOverlay();
                fillAndSubmitBankMandateDetails(paymentDTO);
                break;
            case SAVED_BANK_MANDATE:
                clickPgOverlay();
                fillAndSubmitSavedBankMandateDetails(paymentDTO);
                break;
            case EMI_DC:
                fillAndSubmitEMIDCDetails(paymentDTO);
                break;
            case CC_WITH_SINGLE_PAYMODE:
                fillAndSubmitCCDetailsWithSinglePayMode(paymentDTO, false);
                break;
            case UPI_WITH_SINGLE_PAYMODE:
                fillAndSubmitUPIDetailsWithSinglePayMode(paymentDTO);
                break;
            case NB_WITH_SINGLE_PAYMODE:
                fillAndSubmitNBDetailsWithSinglePayMode(paymentDTO);
                break;
                
            default:
                throw new RuntimeException("invalid pay mode");
        }
        waitUntilLoads();
    }

    @Step("Pay -> {0}")
    public void payBy(PayMode payMode) {
//        ExtentTestManager.getTest().pass(Icon.PAYMENT + payMode);
        PaymentDTO paymentDTO = new PaymentDTO();
        payBy(payMode, paymentDTO);
    }

    public void payBy(PayMode payMode, PaymentDTO savedCardDetails, String cardNumber) {
        if (payMode == PayMode.SAVED_CARD) {
            tabSavedCard().click();
            savedCard(cardNumber).click();

            if (!(savedCardDetails.getCvvNumber() == null || savedCardDetails.getCvvNumber().isEmpty())) {
                textBoxSavedCardCVV().waitUntilVisible();
                textBoxSavedCardCVV().clearAndType(savedCardDetails.getCvvNumber());
            }
            pause(2);
            buttonPGPayNow().click();
            pause(3);
        }

       /* else if(payMode == PayMode.EMI) {
            tabEMI().click();
            pause(1);
            dropdownEmiBanks().selectByVisibleText(savedCardDetails.getBankName());
            emiDurationOption(savedCardDetails.getMonth()).click();
            EMIsavedCard(cardNumber).click();
            textBoxSavedCardCVV().clearAndType(savedCardDetails.getCvvNumber());
            buttonPGPayNow().waitUntilClickable();
            buttonPGPayNow().click();
        }*/
        else if (payMode == PayMode.EMI_SAVED_CARD) {
            fillAndSubmitEMI_SavedCard(payMode, savedCardDetails);
//            tabSavedCard().click();
//            savedCard(cardNumber).click();
//            linksavedEMIPlans().click();
//            emiDurationOption(savedCardDetails.getMonth()).waitUntilClickable();
//            emiDurationOption(savedCardDetails.getMonth()).click();
//            textBoxSavedCardCVV().waitUntilVisible();
//            textBoxSavedCardCVV().clearAndType(savedCardDetails.getCvvNumber());
//            buttonPGPayNow().waitUntilClickable();
//            buttonPGPayNow().click();
        } else {
            payBy(payMode, savedCardDetails);

        }

    }

    /**
     * @param payMode
     * @return true when paymode is selected
     */
    public Boolean isPaymodeModeSelected(PayMode payMode) {
        try {
            switch (payMode) {
                case WALLET:
                    return isPPIChecked();
                case UPI_PUSH:
                    return checkboxVPAList().get(0).isSelected();
                case PPBL:
                    return isPPBLChecked();
                case PAYTM_DIGITAL_CARD:
                    return isPaytmCCChecked();
                default:
                    Assertions.fail("Invalid payment mode selected");
                    return false;
            }
        } catch (NoSuchElementException ex) {
            Reporter.report.warn("Paymode: " + payMode + " not available iin cashier page");
            return false;
        }

    }

    public Boolean verifyPaymentModeDisplayed(PayMode paymentMode) {
        switch (paymentMode) {
            case CC:
                return (tabCreditCard().isElementPresent() && tabCreditCard().isDisplayed());
            case DC:
                return (tabDebitCard().isElementPresent() && tabDebitCard().isDisplayed());
            case NB:
                return (tabNetBanking().isElementPresent() && tabNetBanking().isDisplayed());
            case ATM:
                return (tabATM().isElementPresent() && tabATM().isDisplayed());
            case EMI:
                return (tabEMI().isElementPresent() && tabEMI().isDisplayed());
            case COD:
                return (tabCOD().isElementPresent() && tabCOD().isDisplayed());
            case IMPS:
                return (tabIMPS().isElementPresent() && tabIMPS().isDisplayed());
            case SAVED_CARD:
                return (tabSavedCardBOB().isElementPresent() && tabSavedCardBOB().isDisplayed());
            case UPI:
                return (tabUPI().isElementPresent() && tabUPI().isDisplayed());
            default:
                Assertions.fail("Invalid payment mode selected");
                return false;
        }
    }

    public void verifyCardDisplayed(String... cardNumbers) {
        int count = 0;
        for (String cardNumber : cardNumbers) {
            for (WebElement wl : this.savedCardNumbers()) {
                if (cardNumber.startsWith((wl.getAttribute("value") == null ? wl.getText() : wl.getAttribute("value")).split(" ")[0])
                        && cardNumber.endsWith((wl.getAttribute("value") == null ? wl.getText() : wl.getAttribute("value")).split(" ")[3])) {
                    count++;
                    break;
                }
            }
        }
        Assertions.assertThat(count).isEqualTo(cardNumbers.length);
    }

    public void verifyUPISavedDisplayed() {
        boolean upiFlag = false;
        PaymentDTO paymentDTO = new PaymentDTO();
        for (WebElement wl : savedCardNumbers()) {
            try {
                if (wl.getAttribute("value").contains(paymentDTO.getVpa())) {
                    upiFlag = true;
                    break;
                }
            } catch (NullPointerException ex) {
                Reporter.report.error("Getting Null pointer", getPageName());
            }
        }
        Assertions.assertThat(upiFlag).as("Saved VPA is not displayed in saved details").isTrue();
    }

    public void removeSavedCard() {
        BtnRemoveSavedCard().click();
        btnRemoveConfirmYes().click();
    }

    public void verifyEMIOptions(String bankName, Double transactionAmount) {
        List<WebElement> emiOptions = EMIOptions(bankName);
        for (WebElement emiOption : emiOptions) {
            int month = Integer.parseInt(EMIMonths(emiOption).getText().split(" ")[0]);
            Double displayedEMI = Double.parseDouble(EMIAmount(emiOption).getText().split(" ")[1]);
            Double EMIInterest = Double.parseDouble(EMIInterest(emiOption).getText().split("%")[0]);
            Double emiAmountCalc = emiCalc(transactionAmount, EMIInterest, month);
            Assertions.assertThat(emiAmountCalc.equals(displayedEMI)).isTrue();
        }
    }

    public void validate() {
        // Assertions.assertThat(notificationContainer().isDisplayed()).isTrue();
//        Assertions.assertThat(txnDetailsContainer().isDisplayed()).isTrue();
        Assertions.assertThat(paymentContainer().isDisplayed()).isTrue();
    }

    public void assertVisible(){
    }

    public synchronized boolean isSavedCardPresentAtPosition(final String cardNumber, final int position) {
        boolean isPresent = false;
        String actualMaskedCardNumber = savedCardBlock(position)
                .findElement(By.xpath(".//input[@name='prependedradio']")).getAttribute("value");
        String formattedCardNumber = StringUtils.deleteWhitespace(actualMaskedCardNumber);
        int lengthOfActualCardNum = formattedCardNumber.length();
        int lengthOfExpectedCardNum = cardNumber.length();
        if (lengthOfActualCardNum != lengthOfExpectedCardNum) {
            isPresent = false;
        }
        if (formattedCardNumber.startsWith(cardNumber.substring(0, 3)) && formattedCardNumber
                .endsWith(cardNumber.substring(lengthOfExpectedCardNum - 4, lengthOfExpectedCardNum))) {
            isPresent = true;
        }
        return isPresent;
    }

    public void validateSavedCardImage() {
        String result = savedCardImage().getCssValue("background-image").toString();
        validateImage(result.split("\"")[1]);
        Assertions.assertThat(savedCardImage().isDisplayed()).isTrue();

    }

    public void verifyFirstVPASelected() {
        Assertions.assertThat(checkboxVPAList().size()).isNotEqualTo(0).as("UPI Push list are not displayed");
        WebElement webElement = checkboxVPAList().get(0);
        Assertions.assertThat(webElement.isSelected()).isTrue();
    }

    public void validateBinImage(String imageName) {
        String result = textBoxCardNumber().getCssValue("background-image").toString();
        String url = result.split("\"")[1];
        validateImage(url);
        Assertions.assertThat(url).containsIgnoringCase(imageName);
        Assertions.assertThat(textBoxCardNumber().isDisplayed()).isTrue();
    }

    public void validateSavedCardImage(String imageName) {
        String result = savedCardImage().getCssValue("background-image").toString();
        String url = result.split("\"")[1];
        validateImage(url);
        Assertions.assertThat(url).containsIgnoringCase(imageName);
        Assertions.assertThat(savedCardImage().isDisplayed()).isTrue();
    }

    public void validatedailybreachMsg(){
        String breachmsg = dailybreachmsg().getText();
        Assertions.assertThat(breachmsg).isEqualTo("Payment failed as merchant has crossed his daily acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");
    }

    public void validateweeklybreachMsg(){
        String breachmsg = weeklybreachmsg().getText();
        Assertions.assertThat(breachmsg).isEqualTo("Payment failed as merchant has crossed his weekly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");
    }

    public void validatemonthlybreachMsg(){
        String breachmsg = monthlybreachmsg().getText();
        Assertions.assertThat(breachmsg).isEqualTo("Payment failed as merchant has crossed his Monthly acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.");
    }


    public CashierPage verifyPaymodeLocations() {

        this.softly.assertThat(verifyElementLocations(this.checkBoxPPI(),
                this.upiPushSection())).as("Wallet is displayed above UPI push");
        this.softly.assertThat(verifyElementLocations(this.upiPushSection(),
                this.checkboxPPBL())).as("UPI push is displayed above PPBL");
        this.softly.assertThat(verifyElementLocations(this.checkboxPPBL(),
                this.checkboxPaytmCC())).as("PPBL is displayed above Paytm postpaid");
        return this;
    }
    public void validateUPILimitMsg(String userErrorMsgForUPI){
        String upilimitmsg = UPIlimitmsg().getText();
        Assertions.assertThat(upilimitmsg).isEqualTo(userErrorMsgForUPI);
    }
    public UIElement UPIlimitmsg() {
        return new TextBox(By.xpath("//div[contains(text(), 'UPI transaction above Rs. 100000 is not allowed for this merchant category.')]"), getPageName(),
                "");}

    public void validateCollectUPILimitMsg(String userErrorMsgForUPI){
    }
    public void validateIntentUPILimitMsg(String userErrorMsgForUPI){
    }
    public void validateUPIPPIWalletLimitMsg(String userErrorMsgForUPI){
        String upilimitmsg = PPIWALLETlimitmsg().getText();
        System.out.println("upilimitmsg: "+upilimitmsg );
        Assertions.assertThat(upilimitmsg).isEqualTo("This merchant is not accepting Credit Card, Wallet, Credit Line on UPI.");
    }

    public UIElement PPIWALLETlimitmsg() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10)); // wait for up to 10 seconds
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ptm-cc-on-upi"), "This merchant is not accepting Credit Card, Wallet, Credit Line on UPI."));
        WebElement element = DriverManager.getDriver().findElement(By.cssSelector(".ptm-cc-on-upi"));
        return new UIElement(element, getPageName(),
                "");
    }
    public void validateColletPPIWalletLimitMsg(String userErrorMsgForUPI){
    }
    public void validateIntentPPIWalletLimitMsg(String userErrorMsgForUPI){
    }
    public void EligibilityUPICC_PPIWALLET(){
    }
    public void validateUPICCLimitMsg(String userErrorMsgForUPI){
    }
    public void validateUPICCLimitMsgInnerUPI(String userErrorMsgForUPI){
    }
    public void validateUPICCNotAllowed(String userErrorMsgForUPI){
    }
    public abstract TextBox cardShortcut(String lastFourDigit);
    public abstract TextBox savedToken(String lastFourDigit);
    public TextBox textBoxShortcutCardNumber() {
        return new TextBox(By.xpath("//div[@class='card-content']//input[@id='cardNumber'] | //*[@id='cardnumber']"),
                getPageName(), "card-number-field");
        //div[@class='card-content']//input[@id='cardNumber'] | //input[@id='cardnumber']
    }


    /**
     * @param element1
     * @param element2
     * @return true when element1 is above element2 <br>
     * false when element2 is above element2
     */
    private boolean verifyElementLocations(UIElement element1, UIElement element2) {
        boolean flag;
        int y1 = element1.getLocation().getY();
        int y2 = element2.getLocation().getY();
        flag = y1 < y2;
        return flag;
    }

    public void togglePaymodeCheckbox(PayMode payMode) {

        switch (payMode) {
            case WALLET:
                checkBoxPPI().click();
                break;
            case UPI_PUSH:
                checkboxVPAList().get(0).click();
                break;
            case PPBL:
                checkboxPPBL().click();
                break;
            case PAYTM_DIGITAL_CARD:
                checkboxPaytmCC().click();
                break;
            default:
                Assertions.fail("Invalid payment mode selected");
                break;
        }
    }

    public UIElement sectionSavedCard(String firstSixDigits) {
        return new UIElement(By.cssSelector(".SC-tab div[data-firstsixdigits=\"" + firstSixDigits + "\"]"), getPageName(), "saved-card-block");
    }

    public WebElement addNPayOverlay() throws Exception {
        return DriverManager.getDriver().findElement(By.xpath(addNPayOverlayLocator()));
    }

    public void clickPgOverlay() {
        try {
            DriverManager.getDriver().manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            if (addNPayOverlay().isDisplayed()) {
                clickaddNPayOverlayElement().click();
            }
        } catch (Exception e) {
        } finally {
            DriverManager.getDriver().manage().timeouts().implicitlyWait((ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME).toSeconds(), TimeUnit.SECONDS);

        }
    }

    public void clickFailedTxnGotItButtonIfDisplayed() {
        //Do Nothing
    }

    public void clickInvalidOTPEnteredButtonIfDisplayed() {
        throw new UnsupportedOperationException();
    }

    public Link tabUPIIntent() {
        throw new UnsupportedOperationException();
    }

    public Link tabUPICollect(){
        throw new UnsupportedOperationException();
    }

    public RadioButton upiIntentTabTick(){
        throw new UnsupportedOperationException();
    }

    public RadioButton upiCollectTabTick(){
        throw new UnsupportedOperationException();
    }

    public UIElement promoOffersList(){
        throw new UnsupportedOperationException();
    }

    public UIElement offerAppliedMessage(){
        throw new UnsupportedOperationException();
    }

    public UIElement promoInvalidMessage(){
        throw new UnsupportedOperationException();
    }

    public UIElement promoOfferNotAllowedAddNPayMessage(){
        throw new UnsupportedOperationException();
    }

    public UIElement phoneNum() {
        return null;
    }

    public UIElement savedBankMandate(){
        return null;
    }

    public void validateUpiSubPayModeNotAllowed(String userErrorMsgForUPI){
    }

    public String saveCardPosition(int cardPosition) {
        return DriverManager.getDriver().findElements(By.cssSelector(".ib.vm.ml5")).get(cardPosition - 1).getText();
    }

    public UIElement overlay() {
        return new UIElement(By.xpath("//div[contains(@id,'ptm-spinner')]"), getPageName(), "overlay");
    }

    public void scrollTo(int cordinate) {
        switch (cordinate) {
            case 0:
                ((JavascriptExecutor) DriverManager.getDriver())
                        .executeScript("window.scrollTo(document.body.scrollHeight,0)");
                break;
            case -1:
                ((JavascriptExecutor) DriverManager.getDriver())
                        .executeScript("window.scrollTo(0,document.body.scrollHeight)");
                break;
            default:
                ((JavascriptExecutor) DriverManager.getDriver())
                        .executeScript("window.scrollTo(document.body.scrollHeight," + cordinate + ")");


        }

    }

    public UIElement retryBtnPopupClosedByUser(){
        return new Button(By.xpath("//button[contains(text(),'Retry')]"),getPageName(),"retry-button");
    }

    public UIElement walletEmiErrorMsgPopUp(){
        return new Button(By.xpath("//div[@class='ptm-overlay-head']//child::p"),getPageName(),"emi-wallet error msg");
    }

    public UIElement walletEmiErrorMsgPopUpOkButton(){
        return new Button(By.xpath("//div[@class='ptm-notify-box']//child::div"),getPageName(),"emi-wallet error OK button");
    }


    public UIElement RequestOTP() {
        waitUntilLoads();
        return new Button(By.xpath("//*[contains(text(),'OTP not received?')]/a"), getPageName(), "request-another-otp");
    }

    public void clickChangeNumber() {
        waitUntilLoads();
        DriverManager.getDriver().findElement(By.linkText("Change Number")).click();
    }

    public Boolean isLogoutVisible() {
        boolean flag = false;
        if (DriverManager.getDriver().findElements(By.xpath("//div[contains(text(), 'Login with Another Paytm Account')]")).size() != 0) {
            flag = true;
        }
        return flag;
        // return new UIElementV3(By.xpath(("//div[contains(text(), 'Login with Another Paytm Account")),"logout_dropdown").isVisible().getAsBoolean();
    }

    public UIElement notificationBar() {
        pause(2);
        return new UIElement(By.xpath(xpathBuilder), getPageName(), "notification-text");
    }

    public UIElement notificationBarOK() {
        return new UIElement(By.xpath(xpathBuilder + "//button"), getPageName(), "accept-notification");
    }

    public String GetHeaderErrorColor() {
        String color = DriverManager.getDriver().findElement(By.xpath("//header//section/div")).getAttribute("style");
        //System.out.println(color);
        String[] hexValue = color.replace("background:", "").replace("rgb(", "").replace(")", "").replace(" ", "").replace(";", "").split(",");
        int hexValue1 = Integer.parseInt(hexValue[0]);
        hexValue[1] = hexValue[1].trim();
        int hexValue2 = Integer.parseInt(hexValue[1]);
        hexValue[2] = hexValue[2].trim();
        int hexValue3 = Integer.parseInt(hexValue[2]);
        return String.format("#%02x%02x%02x", hexValue1, hexValue2, hexValue3);
    }

    public void fillAndSubmitZestDetails(PaymentDTO emiDetails) {
        tabEMI().click();
        dropdownEmiBanks().selectByVisibleText(emiDetails.getBankName());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }

    public Link linksavedEMIPlans() {
        return new Link(By.cssSelector("section[data-key='sc'].active a"), getPageName(), "emi-plans-link") {
            @Override
            public void click() {
                super.waitUntilClickable();
                super.click();
            }
        };
    }
        public UIElement GetHeaderError () {
            return new Button(By.xpath("//header//section/div//span"), getPageName(),
                    "saved-card-atm-pin-checkbox");
        }

        public UIElement MGVradioButton(){
            return new Button(By.xpath("//input[@name='mgv']"), getPageName(),
                    "mgv-radio-checkbox");
        }



    public UIElement BankMandateRadioButton(){
        return new Button(By.xpath("//input[@name='paymode' and @value='bm']"), getPageName(),
                "bankMandate-radio-checkbox");
    }

    public UIElement BankMandateOption(){
        return new Button(By.xpath("//*[@class='ptm-paymode-name ptm-lightbold'][text()='Bank Account (E-mandate)']"), getPageName(),
                "E-mandate");
    }

    public UIElement  SavedBankMandateRadioButton(String BankAccountName){
        return new Button(By.xpath("//*[@class='bank-d'][contains(text(), '"+BankAccountName+"')]"), getPageName(),
                "SavedbankMandate-radio-checkbox");
    }


    public Button MGVerror() {
        return new Button(By.xpath("//*[@class='saotp']/parent::div//p"), getPageName(), "MGVerror");
    }


    public UIElement ErrorOkButton() {
        return new UIElement(By.xpath("//*[text()='OK']"),getPageName(),"ErrorOkButton");
    }

    public UIElement ErrorRetryButton() {
        return new UIElement(By.xpath("//*[text()='Retry']"),getPageName(),"ErrorRetryButton");
    }

    public List<String> preLoginScreenElements()
    {
        List<UIElement> uiElements = UIElements.getMultiple(By.cssSelector(".pos-r.p-option._Qqs3 p"),getPageName(),"pre-login-validation");
        List<String> list = new ArrayList<>();
        for(UIElement ui : uiElements)
        {
            list.add(ui.getText());
        }

        return list;
    }
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

    public int getNBIconCount()
    {

        return  UIElements.getMultiple(By.xpath("//img[contains(@src,'bank')]"),getPageName(),"All NB icon count").size();
    }

    public UIElement nbIcon(String bankName)
    {
        return new UIElement(By.cssSelector("img[src$='"+bankName+"’]"),getPageName(),"nb-icons");

    }

    private Button selectBank(String bankName)
    {
        return new Button(By.xpath("//div[text()='"+bankName+"']"),getPageName(),"bank-icon");
    }


    private void searchBank(String bankName)
    {
        new TextBox(By.xpath("//input[@placeholder='Search Bank']"),getPageName(),"search-bank-nb-txt").clearAndType(bankName);
        new TextBox(By.xpath("//span[text()='"+bankName+"']"),getPageName(),bankName + " nb").click();
    }

    public CashierPage selectOtherNetBanking(String bankName)
    {
        selectBank("Others").waitUntilClickable();
        selectBank("Others").click();
        searchBank(bankName);
        return this;
    }

    public CashierPage assertSelectedBank(String bankName)
    {
        bankName = bankName.replace("Bank","").trim();
        selectedBank().assertText(bankName.toUpperCase());
        return this;
    }

    public UIElement selectedBank()
    {
            return new UIElement(By.cssSelector(".type-option-list.nb-banks.active"),getPageName(),"selected-bank");
    }

    public String bankFormErrorMsg()
    {
        return new UIElement(By.cssSelector("._2Wp7.ib"),getPageName(),"failedbankForm-error-nessage").getText();
    }

    public UIElement bankTansfer(){
        return new UIElement(By.xpath("//*[@id='checkout-banktransfer']/div"),getPageName(),"bank-transfer");
    }
    public Button checkoutButton(){
        return new Button(By.xpath("//*[@id='checkout-button']/button"),getPageName(),"checkout-button");
    }
    public String vanNumber(){
        return DriverManager.getDriver().findElement(By.xpath("//*[text()='Account No. (VAN)']/following-sibling::div")).getText();
    }
    public Button proceedButton(){
        return new Button(By.xpath("//*[contains(text(), 'Proceed')]"),getPageName(),"proceed-button");
    }

    public Button vanPayButton(){
        return new Button(By.xpath("//*[text()='Please make sure you have copied the Bank Account Details before proceeding to the merchant website.']/following-sibling::button[1]"),getPageName(),"proceeed-button");
    }
    public UIElement getErrorMessageInavlidMobileNumber(){

        return new UIElement(By.xpath("//*[text()='Invalid Mobile']"),getPageName(),"Invalid-mobile-number");
    }

    public UIElement getErrorMessageInvalidOTP(){
        return new UIElement(By.xpath(" //*[text()='Please Enter Valid Otp']"),getPageName(),"Invalid_otp");
    }
    public UIElement bankForm(){
        return new UIElement(By.xpath("//*[@id=\"checkout-banktransfer\"]/section"),getPageName(),"bank-form");
    }
    public UIElement bankTransferLoginPopup(){
        return new UIElement(By.xpath("//*[text()='Enter Mobile number to get account details for payment']"),getPageName(),"bankTransfer-Login-page");
    }
    public UIElement bankTransferOTPPopup(){
        return new UIElement(By.xpath("//*[text()='Enter OTP']//parent::div"),getPageName(),"bankTransfer-otp-Page");
    }

    public UIElement backButton(){
        return new Button(By.xpath("//*[text()='< BACK']"),getPageName(),"back-button");
    }
    public UIElement vanMobileNumber(){
        return new UIElement(By.xpath("//*[@id=\"bt_mobile_input\"]"),getPageName(),"van-mobile-number");
    }
    public UIElement vanOTP(){
        return new UIElement(By.xpath("//*[@id=\"ptm-otp-input\"]"),getPageName(),"Van-otp");
    }
    public UIElement vanCheckout(){
        return new UIElement(By.xpath("//*[@id=\"checkout-button\"]"),getPageName(),"van-chekout-button");
    }
    public UIElement bankTransferLoginButton(){
        return new UIElement(By.xpath("//*[@id=\"checkout-button\"]/button"),getPageName(),"verify-button");
    }
    public UIElement vanCheckoutElement(){
        return new UIElement(By.xpath("//*[@id=\"checkout-banktransfer\"]"),getPageName(),"checkout-banktransfer");
    }
    public UIElement confirmBox(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-overlay-container')]"),getPageName(),"confirm-button");
    }
    public Button cancelButton(){
        return new Button(By.xpath("//*[contains(text(), 'Cancel')]"),getPageName(),"cancel-button");
    }
    public UIElement howItWorksLabel(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-bt-help')]"),getPageName(),"how-it-worksScreen");
    }
    public UIElement howItWorksScreen(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-overlay-container')]"),getPageName(),"how-it-worksScreen");
    }
    public UIElement viewTermsAndConditionLabel(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-bt-tnc')]"),getPageName(),"view-terms-and-condition");
    }
    public UIElement viewTermsAndConditionScreen(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-overlay-container')]"),getPageName(),"view-terms-and-condition");
    }
    public UIElement convenienceFeeLabel(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-conv-wrapper')]/span"),getPageName(),"convenienceFeeLabel");
    }
    public UIElement buttonSlider(){
        return new UIElement(By.xpath("//*[@class=\"ptm-btn-slider ptm-pos-a\"]/child::span[3]"),getPageName(),"button-slider-for-proceed");
    }

    public UIElementV3 backBtn() {
        throw new UnsupportedOperationException();
    }
    public UIElement feedbackCrossBtn(){
        return new UIElement(By.xpath("//*[@alt='close']"),getPageName(),"cross-button");
    }
    public UIElement feedbackSkipFeedback(){
        return new UIElement(By.xpath("//a[text()='Skip Feedback']"),getPageName(),"skip-anyway-btn");
    }
    public UIElement feedbackSubmitBtn(){
        return new UIElement(By.xpath("//button[text()='Submit Feedback']"),getPageName(),"submit-button");
    }
    public UIElement feedbackRadioBtnOther(){
        return new UIElement(By.xpath("//input[@value='other']"),getPageName(),"radio-button-other");
    }
    public UIElement feedbackRadioExpPaymode(){
        return new UIElement(By.xpath("//input[@value='could_not_find_the_expected_paymode']"),getPageName(),"nnot-find-expected-mode");
    }
    public UIElement  feedbackRadioInsuffBalnce(){
        return new UIElement(By.xpath("//input[@value='insufficient_paytm_wallet_balance']"),getPageName(),"insuff-paytm-balance");
    }
    public UIElement feedbackRadioCashbackNotWork(){
        return new UIElement(By.xpath("//input[@value='expected_cashback_promo_did_not_work']"),getPageName(),"cashback-not-workingg");
    }
    public UIElement feedbackRadioForgetPaytmPass(){
        return new UIElement(By.xpath("//input[@value='forgot_paytm_bank_passcode']"),getPageName(),"forget-paytm-pass");
    }
    public UIElement feedbackTextArea(){
        return new UIElement(By.xpath("//*[@placeholder= 'Please let us know the reason (Optional)']"),getPageName(),"text-area-optional");
    }
    public UIElement feedbackPleaseSelectReason(){
        return new UIElement(By.xpath("//p[text()='Please select a feedback']"),getPageName(),"please-select-reason");
    }
    public UIElement feedbackBox(){
        return new UIElement(By.xpath("//div[contains(@class,'_ASM7 popup-global pos-f')]"),getPageName(),"feedback-box");
    }

    public UIElement getUserPrfile() {
        return new UIElement(By.xpath("//header//div/img"),
                getPageName(), "user-profile-arrow");
    }
    public void scrollToElement(WebElement scrollTo) {

        ((JavascriptExecutor)DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView();", scrollTo);

    }


    public abstract String getErrorMessageAfterEnteringCard();

    public abstract UIElement getErrorCC_EMI_NOTSET();
    public abstract UIElement save_card_visible();
    public abstract UIElement getError_invalidCVV();



    public UIElement cardMessage(){
        return new UIElement(By.xpath("//section[contains(@class,'active')]//p[contains(text(),'future payments')]/parent::div/parent::div/following-sibling::div//p"),
                getPageName(), "cardMessage");

    }


    public JsonPath getPushAppData() {
       JavascriptExecutor JS = (JavascriptExecutor) DriverManager.getDriver();
        String pushApp = (String)JS.executeScript("return pushAppData");

        if(!pushApp.isEmpty() && !pushApp.equalsIgnoreCase(null)) {
            String[] split_string = pushApp.split("\\.");

            String base64EncodedBody = split_string[0];
            Base64 base64Url = new Base64(true);

            String body = new String(base64Url.decode(base64EncodedBody));
            JsonPath jsonPath = new JsonPath(body);
            Reporter.report.info("Push APP Data : " + body);
            System.out.println("Push APP Data : " +body);

            return jsonPath;
        }

        else
        {
            throw new RuntimeException("Push App Data is incorrect: " + pushApp);
        }
    }

    public Object getAppData() {
        JavascriptExecutor JS = (JavascriptExecutor) DriverManager.getDriver();
        return JS.executeScript("return APP_DATA");

    }

    public UIElement savedCardDisplayName(){
        return new UIElement(By.xpath("//section[@data-key='sc']//span[@class='bank-d']"),
                getPageName(), "cardMessage");

    }



    public UIElement findIfsc()
    {
        return new UIElement(By.linkText("Find IFSC"),getPageName(),"selected-bank");
    }

    public abstract List<WebElement> getBankMandateList();
    public abstract List<WebElement> getBankMandateListNew();

    public abstract UIElement IfscDetails();
    public abstract UIElement BankDetails();
    public abstract UIElement UserBankName();

    public UIElement merchantName(){
        return new UIElement(By.xpath("//*[@id=\"ptm-checkout-header\"]/div[2]/div[2]/span[1]/span"),
                getPageName(), "merchantName");
    }

    public void scrollUpToHeader(){
        Actions action = new Actions((WebDriver) DriverManager.getDriver());
        WebElement from = (WebElement) DriverManager.getDriver().findElement(By.xpath("//*[@id=\"checkout-card\"]/div[1]/label/div/p"));
        action.moveToElement(from).moveToElement((WebElement) DriverManager.getDriver().findElement(By.xpath("//*[@id=\"ptm-checkout-sc\"]/div/div/label/div/p[1]"))).click().build().perform();
    }

    public List<UIElement> listOfPayModes()
    {
        return UIElements.getMultiple(By.xpath("//span[contains(@class,'bank-d')]"),getPageName(),"list-payMode");
    }
    public TextBox textBoxExpiryMonthEMI()
    {
        return null;
    }
    public TextBox textBoxExpiryYearEMI()
    {
        return null;
    }

   public UIElement emiPlan(){
       return null;
   }
    public UIElement emiPlanCard(){
        return null;
    }

    public void clickRetryIncorrectOTPBtn(){
    }


    public UIElementV3 lblVPAErrMsg() {
        return new UIElementV3(By.cssSelector(".payment-type-methods *[data-key=upi] .errorRed"), "label-vpa-err-msg");
    }

    public Link payModeNextToPPILoggedIn() {
        return new Link(By.xpath("//section[contains(@class,'paytm-wallet')]/../following-sibling::*[1]"), "paymode-next-to-wallet-loggedin");
    }

    public Link payModeNextToPPILoggedOut() {
        return new Link(By.xpath("//*[contains(@class,'payment-type-methods')]/*[1]"), "paymode-next-to-wallet-loggedout");
    }

    public UIElement cancelPaymentYes() {
        return new UIElement(By.xpath("//div[@class='_38GU btn btn-primary pos-r xs-cp-btn']"), getPageName(), "cancelYesButton");
    }

    public void closeChildWindow(){
        WebDriver wb = DriverManager.getDriver();
        Set<String> ls = wb.getWindowHandles();
        Iterator<String> it = ls.iterator();
        String parent = it.next();
        String child = it.next();
        wb.switchTo().window(child);
        wb.close();
        wb.switchTo().window(parent);
    }

    public void successfulTransactionButton(){
        WebDriver wb = DriverManager.getDriver();
        Set<String> ls = wb.getWindowHandles();
        Iterator<String> it = ls.iterator();
        String parent = it.next();
        String child = it.next();
        wb.switchTo().window(child);
        wb.findElement(By.xpath("//button//*[text()='Successful']")).click();
        wb.switchTo().window(parent);
    }


    public void clickChangeBank() {
        DriverManager.getDriver().findElement(By.xpath("//*[text()='Change Bank']")).click();
    }


    public Link ifscErrorMessage(){
        return new Link(By.xpath("//*[@placeholder='Enter IFSC']/../p"),getPageName(),"ifsc-error-message");
    }

    public Link bankAccountErrorMessage(){
        return new Link(By.xpath("//*[@placeholder='Enter Bank Account Number']/../p"),getPageName(),"Bank-account-error-message");
    }

    public Link userErrorMessage(){
        return new Link(By.xpath("//*[@placeholder='Enter your name (As per Bank Records)']/../p"),getPageName(),"user-name-error-message");
    }

    public List<UIElement> listOfNbchannel() {

        tabNetBanking().click();
        return UIElements.getMultiple(By.xpath("//ul[contains(@class,'bank-result-global')]/li"), getPageName(), "listOfNbchannel");
    }

    public UIElement payButton(){
        return new UIElement(By.xpath("//*[@id='checkout-button']/button"),"cashier-page","pay-button");
    }

    public UIElement payButtonPromoText(){
        return new UIElement(By.xpath("//*[@id='checkout-button']/button/span"),"cashier-page","pay-button");
    }
    public UIElement payButtonPromoTextNew(){
        return new UIElement(By.xpath("//button[contains(@class, 'ptm-custom-btn') and contains(text(), 'Pay')]"),"cashier-page","pay-button");
    }
    public void validateButtonText(String DA, String discount){
        String buttonText = payButtonPromoText().getText();
        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
    }

    public void validateBankAccountErrorMsg(){
        Assertions.assertThat(bankAccountErrorMessage().getText()).isEqualTo("Please enter your bank account number");
    }

    public void validateIfscErrorMsg(){
        Assertions.assertThat(ifscErrorMessage().getText()).isEqualTo("Please enter your bank IFSC");
    }

    public void validateUserErrorMsg(){
        Assertions.assertThat(userErrorMessage().getText()).isEqualTo("Please enter your name");
    }
    public Button proceedBtn() {
        return new Button(By.xpath("//*[text()='Proceed']"), getPageName(), "proceed-button");
    }
    public Button proceedEMIBtn(){
        return new Button(By.xpath("//*[text()='Proceed to Convert to EMI']"), getPageName(), "proceed-emi-button");
    }

    public Button closeCcDcDetailBtn() {
        return new Button(By.xpath("//img[@alt='close']"), getPageName(), "close-button");
    }

    public Button closePMDetailBtn() {
        return new Button(By.xpath("//*[@alt='close']"), getPageName(), "close-button");
    }


    public UIElement cvvNotReq() {
             return new UIElement(By.xpath("//*[@id='cvv-not-reqd']"),getPageName(),"cvv-not-req");
           }

    public Button closeCardPay() {
        return new Button(By.xpath("//span[@class='ptm-cross ptm-pos-r']"), getPageName(), "close-card-pay-button");
    }

    public void setPageNElementTimeout(Duration second){
        DriverManager.setWebDriverElementWait(second);
        DriverManager.setWebDriverPageWait(second);
        DriverManager.getDriver().manage().timeouts().implicitlyWait(second.toSeconds(), TimeUnit.SECONDS);
    }
    public void resetPageNElementTimeout(){
        DriverManager.resetWebDriverElementWait();      // reseting to origin time
        DriverManager.resetWebDriverPageWait();
        DriverManager.getDriver().manage().timeouts().implicitlyWait(ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME.toSeconds(), TimeUnit.SECONDS);
    }

    public void validateSubscriptionTrayText(){
        Assertions.assertThat(subscriptionTray().getText()).isEqualTo("Next payment due on");
    }

    public UIElement invalidVpaText(){
        return new UIElement(By.xpath("//*[@id=\"ptm-upi-input\"]//following-sibling::p"), getPageName(), "invalid-vpa");
    }
    
    // AI-Generated: 2025-01-27 - Function creation
    public UIElement invalidCardholderNameText(){
        return new UIElement(By.xpath("//*[@id=\"invalid_cardholder_name\"]"), getPageName(), "invalid-cardholder-name");
    }
    public UIElement fortnightFreqText(){
        return new UIElement(By.xpath(".//div[contains(@class,'ptm-subsDetails')]//div[text()='Every fortnight']"),getPageName(),"everyfortnight-text");
        //return new UIElement(By.xpath("//*[@class='ptm-subsDetails']//div[text()='Every fortnight']"),getPageName(),"everyfortnight-text");
    }
    public UIElement alertMessage(){
        return new UIElement(By.xpath("//*[@class='ptm-overlay-subtxt ptm-body-color']"),getPageName(),"alert-message");
    }

    public UIElement getPaytmLogo() {
        return new Link(By.xpath("(//*[contains(@src,'logo-white')])[1]"), getPageName(),"paytmLogo");
    }

    public UIElement getPaytmLogoNew() {
        return new Link(By.xpath("//img[contains(@src, '/checkoutjs/') and contains(@src, '/assets/images/paytm-pg-white.svg') and @class=' ptm-logo false ']"), getPageName(),"paytmLogo");
    }
    public void failureTransactionButton(){
        WebDriver wb = DriverManager.getDriver();
        Set<String> ls = wb.getWindowHandles();
        Iterator<String> it = ls.iterator();
        String parent = it.next();
        String child = it.next();
        wb.switchTo().window(child);
        wb.findElement(By.xpath("//span[text()='Failure']")).click();
        wb.switchTo().window(parent);
    }
    public UIElement getEMILogo(){return null;}
    public UIElement getCardLogo(){return null;}
    public UIElement getUpiLogo(){return null; }
    public UIElement getNBLogo(){return null;}


    public void assertDuplicateAttempt(){
        this.duplicateAttempt().assertVisible();
    }

    public UIElement duplicateAttempt(){
        return new UIElement(By.xpath("//div[@class='pay-failed']//h4[text()='Duplicate Attempt']"), getPageName(), "duplicate-attempt-text");
    }

    public Button closePageDuplicateAttemp(){
        return new Button(By.xpath("//button[text()='Close Page']"), getPageName(), "close-page-button");
    }

    public UIElement getTextandamount(){
        return new UIElement(By.xpath("(//div[contains(@class,'ptm-select-txt ptm-select-sticky xs-ptm-hidden ptm-body-bg')])[1]"),getPageName(),"amount text");
    }

    public UIElement postPaid(){
        return new UIElement (By.xpath("//div[contains(@class,'ptm-login-box')]//div[contains(@class,'ptm-token-wrap')]//div[contains(text(),'Postpaid')]/img"),getPageName(),"Static-postpaid-on-login-screen");
    }
    public UIElement wallet(){
        return new UIElement (By.xpath("//div[contains(@class,'ptm-login-box')]//div[contains(@class,'ptm-token-wrap')]//div[contains(text(),'Wallet')]/img"),getPageName(),"Static-wallet-on-login-screen");
    }
    public UIElement cards(){
        return new UIElement (By.xpath("//div[contains(@class,'ptm-login-box')]//div[contains(@class,'ptm-token-wrap')]//div[contains(text(),'Cards')]/img"),getPageName(),"Static-cards-on-login-screen");
    }
    public UIElement uPIBankAC(){
        return new UIElement (By.xpath("//div[contains(@class,'ptm-login-box')]//div[contains(@class,'ptm-token-wrap')]//div[contains(text(),'UPI Bank A/C')]/img"),getPageName(),"Static-upiBankAccount-on-login-screen");
    }

    public UIElement newPaymentMethod(){
        return new UIElement (By.xpath("//div[contains(@class, 'ptm-grid-wrap ptm-w100-ib')]//div[contains(@class, 'ptm-grid-inner')]//div[contains(text(),'New Payment Option')]"),getPageName(),"New-Payment-option-on-login-screen");
    }
    public UIElement linkedFDBalance(){
        return new UIElement(By.xpath("//*[@class='ptm-pay-method-cont']//p[text()='Linked FD balance ₹268']"), getPageName(), "Linked-FD-balance");
    }
    public UIElement redemptionTextLabel(){
        return new UIElement(By.xpath("//div[contains(@class, 'redemption-container')]"),getPageName(),"redemption-text");
    }

    public UIElement upiIntentPayButton(){
        throw new UnsupportedOperationException();
    }
    public UIElement enterPPBLPasscodeMessage(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-ppbl-passcode')]"),getPageName(),"enter-ppbl-passcode-message");
    }

    public UIElement getBackButton() {

        return new UIElement(By.xpath("//*[contains(@class, 'ptm-back-btn')]"),getPageName(),"find-back-button");
    }

    public UIElement PressNoForCancelTxn() {

        return new UIElement(By.xpath("//button[@class='ptm-cp-btn ptm-cp-n-btn']"),getPageName(),"find-no-button-for-the-cancel-txn");
    }
    public UIElement PressYesForCancelTxn() {

        return new UIElement(By.xpath("//button[@class='ptm-cp-btn ptm-cp-y-btn']"),getPageName(),"find-yes-button-for-cancel-txn");
    }

    public UIElement getUpiText() {
        return new UIElement(By.xpath("//*[@class= 'ptm-overlay-head']/div"),getPageName(),"upi text");
    }

    public UIElement getUpiHandleSuggestion() {
        return new UIElement(By.xpath("(//*[contains(@class, 'xs-suggest-card-box')])[1]"),getPageName(),"upi-handle-text");
    }


    public UIElement getConFeeTextOnPayButton() {
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-d-block ptm-small-txt ptm-sub-text')]"),getPageName(),"con-fee-text-on-payButton");
    }
    public  UIElement getNoCostEmiDiscountText() {
        return new UIElement(By.xpath("//p[contains(@class,'ptm-cashback-txt')]"),getPageName(),"ptm-cashback-txt ptm-nav-selectable ptm-nav-clickable");
    }
    public UIElement getPaytmFeaturedText() {
        return new UIElement(By.xpath("//*[contains(text(),'Paytm Featured')]"), getPageName(), "paytm-featured");
    }

    public UIElement disableMessage(){
        return new UIElement(By.xpath("//*[contains(text(),'The payment option is experiencing downtime')]"),
                getPageName(), "disableMessage");
    }

    public UIElement bankMandateSubscribe() {
        return null;
    }
    public UIElement donotwanttobuyproductorserviceanymore()
    {
        return  new UIElement(By.xpath("//label[@class='ptm-lbl ptm-pos-r' and text()='Do not want to buy this product/service anymore']"),getPageName(),"cancel payment option");
    }

    public UIElement verifyNoCostEmiAppliedText(){
        return new UIElement(By.xpath("//p[@id='changePlan']/following-sibling::div/p"), getPageName(),"ptm-applied-para");
    }
    public UIElement getFooterText(){
        return new UIElement(By.xpath("//*[@id='footer-text']"), getPageName(), "footer-text");
    }

    public UIElement VerifyMobileNumber(){
        return new UIElement(By.xpath("//div[@class='_1BoS _ss_0']"), getPageName(), "Verify Mobile Number");
    }

    public UIElement invalidLink(){
        return new UIElement(By.xpath("//div[@class='invalid-link']"), getPageName(), "Verify Invalid Link");
    }

    public UIElement amountAtCashierPage(){
        return new UIElement(By.xpath("//*[contains(text(),'1,00,00,00,000')]"), getPageName(), "Amount showing in cashier page");
    }

    public UIElement merchantNameAtCashierPage(){
        return new UIElement(By.xpath("//span[contains(text(),'upgrade display name')]"), getPageName(), "Merchant Name showing in cashier page");
    }

    public UIElement merchantDescriptionAtCashierPage(){
        return new UIElement(By.xpath("//div[contains(text(),'1234567890 123456789 123456789')]"), getPageName(), "Merchant Description showing in cashier page");
    }
    //--
    public UIElement getNBTabForOldFlow(){
        return new UIElement(By.xpath("//span[contains(text(), 'Net')]"), getPageName(), "Net Banking Tab");
    }
    public UIElement getPayButton(){
        return new UIElement(By.xpath("//span[contains(text(),'PAY ')]"), getPageName(), "Pay btn");
    }
    public UIElement locationPopUPCross() {
        return new UIElement(By.xpath("(//span[@class=\"ptm-cross ptm-pos-r\"])[3]"), getPageName(), "locationPopUP Cross");
    }
    public UIElement SuccessfullScreen() {
        return new UIElement(By.xpath("//div[@class='lh-2 s_color f-17 m-hide g-color']"), getPageName(), "Payment Successful");

    }
    public UIElement LinkTxnSuccessfullScreenMessage() {
        return new UIElement(By.xpath("//span[@class='paymentStatusText']"), getPageName(), "Payment Successful");
    }
    public UIElement LinkTxnSuccessfullScreenMessageNew() {
        return new UIElement(By.xpath("//div[@class='thanks_title']"), getPageName(), "Payment Successful");
    }

    public UIElement failTxnScreen() {
        return new UIElement(By.xpath("//span[@class='error ']"), getPageName(), "Payment cannot be Successful");
    }
    public UIElement getmerchantName() {
        return new UIElement(By.xpath("//*[@id='merchantName']"), getPageName(), "Merchant Name");
    }

    public UIElement LocationPopUpMsg() {
        return new UIElement(By.xpath("//div[text()='Location Permission Blocked']"), getPageName(), "Location Permission Blocked");
    }

    public void payByNBForOldFlow(){
        getNBTabForOldFlow().click();
        getPayButton().click();
        if(locationPopUPCross().isElementPresent()) {
            locationPopUPCross().click();
            getPayButton().click();
        }

    }

    public UIElement getNBTabForNewFlow(){
        return new UIElement(By.xpath("//p[contains(text(), 'Net Banking')]"), getPageName(), "Net Banking Tab");
    }
    public UIElement getPayButtonNew(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-custom-btn ptm-hvr-pop')]"), getPageName(), "Net Banking Tab");
    }
    public UIElement locationPopUPCrossNew() {
        return new UIElement(By.xpath("(//span[@class=\"ptm-cross ptm-pos-r\"])[3]"), getPageName(), "locationPopUP Cross");
    }
    public void payByNBForNewFlow() throws InterruptedException {
        getNBTabForNewFlow().click();
        getPayButtonNew().click();
        if(locationPopUPCrossNew().isElementPresent()) {
            locationPopUPCrossNew().click();
            getPayButtonNew().click();
        }

    }

    public UIElement donothaveaccesstoregisteredmobilenumber() {
        return new UIElement(By.xpath("//label[text()='Do not have access to registered mobile number' and @class='ptm-lbl ptm-pos-r']"), getPageName(), "cancel payment option");
    }
    //button[text()='Submit']
    public UIElement submitoption() {
        return new UIElement(By.xpath("(//button[text()='Submit'])[2]"), getPageName(), "submit");
    }
    public UIElement canceltxntext() {
        return new UIElement(By.xpath("//div[text()='User cancelled the transaction from 3D secure/OTP page']"), getPageName(), "canceltxntext");
    }

    public UIElement firstBank() {
        return new UIElement(By.xpath("//*[@id='ptm-nb']//div[contains(@class,'active')]"), getPageName(), "First Bank Name");
    }

    public WebElement PaymodeEnableDisableWalletMessage(){
        return new UIElement(By.xpath("//div[@id='checkout-wallet']//p[contains(text(), 'Paytm Balance')]"),getPageName());
    }

    public WebElement PaymodeEnableDisablePostpaidMessage(){
        return new UIElement(By.xpath("//p[contains(text(),'Paytm Postpaid')]"),getPageName());
    }



    public UIElement getPaytmLogoBlue() {
        return null;
    }
    public UIElement footerLogoBlue() {
        return null;
    }


    public String getChildWindowURL(){
        WebDriver wb = DriverManager.getDriver();
        Set<String> ls = wb.getWindowHandles();
        Iterator<String> it = ls.iterator();
        String parent = it.next();
        String child = it.next();
        wb.switchTo().window(child);
        String childWindowURL = wb.getCurrentUrl();
        wb.switchTo().window(parent);
        return childWindowURL;
    }

    public Link tabEnach() {
        return new Link(By.xpath(".//div[@class='_3-zL border-global']//span"),getPageName(), "E-nach");
    }
    public UIElement amountToBePaid() {
        return new UIElement(By.xpath("//div[text()='Amount to be Paid Now']"), getPageName(), "amount-to-be-paid-now");
    }
    public abstract UIElement getBankOfferDiscountMsg();


    public UIElement paymentOptions(){
        return new UIElement(By.xpath("//div[@class='ptm-grid-inner']"),getPageName());
    }

    public UIElement paymentOptionsOfflineFlow(){
        return new UIElement(By.xpath("//div[@class='grid-view clearfix']"),getPageName());
    }
    public UIElement getNewpaymentOption(){
        return new UIElement(By.xpath("//*[contains(text(),'New Payment Option')]"),getPageName(),"New Payment Option");
    }
    public UIElement hidepaymodelableTrue(){
        return new UIElement(By.xpath("//div[contains(@class, 'payment-mode-warp xs-paymode-wrap  elementjs-paymode-mb0')]"),getPageName(), "Text at this xpath");
    }
    public UIElement hidepaymodelableFalse(){
        return new UIElement(By.xpath("//div[contains(@class, 'ptm-pay-method-cont')]//p"),getPageName(),"Text at this xpath");
    }
    public UIElement vpaerrormsg(){
        return new UIElement(By.xpath("//*[@class='ptm-nid-error']"),getPageName(),"Vpa error msg text");
    }
    public UIElement loginStripWithQR(){
        return new UIElement(By.xpath("//div[@class='_3ijw brdr-box border-global pos-r o-h']"),getPageName());
    }
    public UIElement QRCode(){
        return new UIElement(By.xpath("//div[@id='ptm-qr']"),getPageName());
    }

    public UIElement getEMIStrip()
    {
        return  new UIElement(By.xpath("//div[@class='ptm-emi-text ptm-secondary ptm-secondary-bg']"),getPageName(),"ptm-emi-text ptm-secondary ptm-secondary-bg");
    }
    public UIElement otherPaymentOption()
    {
        return new UIElement(By.xpath("//*[text()='Other Payment Options']"),getPageName(),"other payment option in cashier page");
    }
    public UIElement ccdc_expCvv_cardIframe() {
        return new UIElement(By.xpath("(//*[@id=\"ptm-checkout-iframe\"]/iframe)[2]"),getPageName(),"ptm-ppb-iframe");
    }
    public TextBox textBoxCardNumberEMI() {
        return new TextBox(By.xpath("//input[@id=\"cardnumber\"]"), getPageName(), "emi-card-number-field");
    }
    public TextBox EnterMobileNumber_For_EmiDcEligibility() {
        return new TextBox(By.xpath("//div[contains(text(),'Enter Mobile Number')]/preceding-sibling::input"), getPageName(), "emi-dc-eligibility-mobile-number-field");
    }
    public Button checkEMIEligibility() {
        return new Button(By.xpath("//button[text()='Check EMI Eligibility']"), getPageName(), "check-EMI-eligibility-button");
    }
    public Button proceedToConvertEMI() {
        return new Button(By.xpath("//button[text()='Proceed to Convert to EMI']"), getPageName(), "proceed-to-convert-EMI");
    }

    public void selectEMIBank(PaymentDTO paymentDTO){
        String bankName = paymentDTO.getBankName();
        UIElement EMIBank = new UIElement(By.xpath("//*[text()='"+bankName+"']"), getPageName(), "Select Bank from EMI Bank List");
        EMIBank.click();
    }

    public void fillEMICardDetails(PaymentDTO paymentDTO){
        DriverManager.getDriver().switchTo().frame(ccdc_emiIframe());
        textBoxCardNumber().clearAndType(paymentDTO.getEmiCard());
	}
    public Select dropdownEmiBanksV5() {
//        new UIElement(By.cssSelector(".ptm-emi-bank-select"), getPageName(), "emi-bank-dropdown").click();
        return new Select(By.xpath("//div[contains(text(),'View All')]"), getPageName(), "emi-bank-dropdown") {
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

    public UIElement viewAllEmiButton() {
        return new Select(By.xpath("//div[contains(text(),'View All')]"), getPageName(), "emi-bank-dropdown");
        }

    public UIElement upiIdRadioBtn() {
        return new UIElement(By.xpath("//*[@class='ptm-lbl']/span"),getPageName(),"select upi id radio btn");
    }

    public UIElement nextPayment(){
        return new UIElement(By.xpath("//div[text()='Next Payment']"), getPageName(), "Next Payment");
    }

    public UIElement subsDetailsValidity(){
        return  new UIElement(By.xpath("//div[text()='Validity']"), getPageName(), "Validity");

    }
    public UIElement payUPI(){
        return new UIElement(By.xpath("//div[@class='_3-zL border-global']"),getPageName(), "Pay UPI");
    }
    public  UIElement paybyPaytm(){
        return new UIElement(By.xpath("//span[text()='Paytm Balance']"), getPageName(), "Pay by Paytm");
    }
    public Table tableSubUI() {
        return new Table(By.xpath(""), getPageName(), "") {
            public int getRowCount() {
                return UIElements.getMultiple(By.xpath("//div[@class='_3Fpw clearfix sub-am']"), getPageName(), "Subscription Details").size();
            }
            public String getRowValue(String rowName) {
                return new UIElement(By.xpath("(//div[text()='" + rowName + "'])[2]//following-sibling::div"), getPageName(), "Subscription Details "+ rowName).getText();
            }
        };
    }
    public Table tableSubdetailsUI() {
        return new Table(By.xpath(""), getPageName(), "") {
            public int getRowCount() {
                return UIElements.getMultiple(By.xpath("//div[@class='brdr-box border-global p-16 fs14 clearfix']"), getPageName(), "Subscription Details").size();
            }
            public String getRowValue(String rowName) {
                return new UIElement(By.xpath("//div[text()='" + rowName + "']//following-sibling::div"), getPageName(), "Subscription Details " + rowName).getText();
            }
        };
    }
    public UIElement subsRecurringBillAmount(){
        return new UIElement(By.xpath("//div[text()= 'Recurring Bill Amount']"),getPageName(), "subsRecurringBillAmount");
    }
    public UIElement subsDetailsFrequency(){
        return new UIElement(By.xpath("//div[text()= 'Frequency']"), getPageName(), "subsDetailsFrequency");
    }
    public UIElement recurringBillFrequency(){
        return new UIElement(By.xpath("//div[text() = 'Recurring Bill Frequency']"), getPageName(), "Recurring Bill Frequency");
    }


    public List<String> getChildElementsText(List<UIElement> elements){
        List<String> actualValues = new ArrayList<>();
        for(int i=0; i<elements.size(); i++){
            pause(1);
            actualValues.add(elements.get(i).getText());
        }
        return actualValues;
    }

    public List<UIElement> subscriptionDetailsOnCashierPage(){
        List<UIElement> subElements = UIElements.getMultiple(By.xpath("//div[contains(@class,'payment-mode-warp')]//div[@class='ptm-sub-hd-inner']//span"), getPageName(), "listOfNbchannel");
        return subElements;
    }

    public List<UIElement> subscriptionDetailsOnInfoTab(){
        List<UIElement> subElements = UIElements.getMultiple(By.xpath(".//div[contains(@class,'ptm-subsDetails')]//div[contains(@class,'ptm-subs-txt')]"), getPageName(), "listOfNbchannel");
        return subElements;
    }

    public UIElement getNoteStrip(){
        return new UIElement(By.xpath("//p[@class=\"ptm-chng-intrst ptm-warning-bg ptm-body-bg ptm-body-color \"]"),getPageName(),"Note Strip" );
    }
    public UIElement cardNotSupported(){
        return new UIElement(By.id("cardNotSupportedElement"),getPageName(),"Enter card for EMI");
    }
    public UIElement proceedToSelectEmiPlan(){
        return new UIElement(By.xpath("(//*[contains(text(),\"Proceed to Select EMI Plan\")])[2]"),getPageName(),"Enter card for EMI");
    }
    public UIElement SubventionDetails(){
        return new UIElement(By.xpath("//*[@class=\"ptm-chng-emicbk ptm-scemi-clebel ptm-secondary\"]"),getPageName(),"subvention amount applied");
    }
    public UIElement PromoDetails(){
        return new UIElement(By.xpath("//*[@class=\"ptm-scemi-clebel ptm-secondary \"]"),getPageName(),"promo code applied");
    }
    public UIElement EMIDetails(){
        return new UIElement(By.xpath("(//*[@class=\"ptm-secondary ptm-nocost\"])[1]"),getPageName(),"Zero/low cost emi");
    }
    public UIElement SubventionDetailsOnConvertToEMIPage(){
        return new UIElement(By.xpath("(//*[@class=\"ptm-chng-emicbk ptm-scemi-clebel ptm-secondary\"])[2]"),getPageName(),"subvention applied");
    }
    public UIElement PromoDetailsOnConvertToEMIPage(){
        return new UIElement(By.xpath("(//*[@class=\"ptm-scemi-clebel ptm-secondary\"])[1]"),getPageName(),"promo applied");
    }
    public UIElement LoanAmount(){
        return new UIElement(By.xpath("//*[@id=\"planDetails\"]/div[1]"),getPageName(),"Loan/EMI amount needs to pay");
    }
    public UIElement crossButtonOnConvertToEMIPage(){
        return new UIElement(By.xpath("(//*[@class=\"ptm-close-wrap ptm-pos-a ptm-nav-back\"])[2]"),getPageName(),"Back from final page");
    }
    public  UIElement CrossButtonOnEntercardpage(){
        return  new UIElement(By.xpath("//*[@class=\"ptm-close-wrap ptm-pos-a ptm-nav-back\"]"),getPageName(),"Back from enter card page");
    }
    public  UIElement ViewAll(){

        return  new UIElement(By.xpath("//*[contains(text(),\"View All\")]"),getPageName(),"View all EMI banks");
    }
    public  UIElement ZeroLowCostEmiTab(){
        return  new UIElement(By.xpath("//*[@class=\"ptm-emi-type-block\"]/*[contains(text(),\"Zero/Low Cost EMI\")]"),getPageName(),"Zero/Low Cost EMI banks");
    }
    public  UIElement StandardEmi(){
        return  new UIElement(By.xpath("//*[@class=\"ptm-emi-type-block\"]/*[contains(text(),\"Standard  EMI\")]"),getPageName(),"Standard  EMI");
    }
    public  UIElement TnCButton(){
        return  new UIElement(By.xpath("//*[@class=\" ptm-primary ptm-emiTnc\"]"),getPageName(),"Terms and condition button");
    }


    public  UIElement TnCButton1(){
        return  new UIElement(By.xpath("//a[text()='Terms and Conditions']"),getPageName(),"Terms and condition button");
    }

    public TextBox textBoxCVVNumberSavedcardEMI() {
        return new TextBox(By.id("ppbl_pin"),
                getPageName(), "cvv-field");
    }
    public  UIElement SavedcardEMISubventionStrip(){
        return  new UIElement(By.xpath("//div[@class='ptm-secondary ptm-secondary-bg']"),getPageName(),"0/low case emi message");
    }
    public CheckBox checkBoxPPICheckoutjsV5() {
        return new CheckBox(By.xpath("(//span[@class=\"ptm-check\"])[2]"), getPageName(), "checkbox-ppi");
    }
    public UIElement Cvv_cardIframe() {

        return new UIElement(By.xpath("//*[@id=\"ptm-checkout-iframe\"]/iframe"), getPageName(), "ptm-ppb-iframe");

    }

	public UIElement getUserDeactivatedErrorMessage(){
        return new UIElement(By.xpath("//*[@class = 'ptm-wallet-inactive']"), getPageName(), "ptm-wallet-inactive");
    }

    public UIElement getWalletDisabledErrorMessage(){
        return new UIElement(By.xpath("//span[@class='_P7Gf']"), getPageName(), "ptm-wallet-new-user");
    }

    public Boolean isWalletDisabled(){
        UIElement walletDisabled = new UIElement(By.xpath("//input[contains(@class, 'ptm-wallet-disabled')]"), getPageName(), "ptm-wallet-disabled");
        return walletDisabled.isElementPresent();
    }

    public String getKnowMoreText(){
        this.knowMoreLink().click();
        this.pause(2);
        String guidelines = this.getKnowMoreLinkText().getText();
        this.pause(1);
        this.knowMoreLinkPopup().click();
        return guidelines;
    }

    public UIElement knowMoreLink(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-know_more')]"), getPageName(), "ptm-know_more");
    }

    public UIElement loginKnowMoreLink(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-kno-link')]"), getPageName(), "ptm-know_more");
    }

    public UIElement getKnowMoreLinkText(){
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-wallet-kn-msg')]"), getPageName(), "ptm-wallet-kn-msg");
    }

    public UIElement knowMoreLinkPopup(){
        // "Okay. Got it" will be clicked present in know more popup
        return new UIElement(By.xpath("//*[contains(@class, 'ptm-pref-btn')]"), getPageName(), "ptm-pref-btn");
    }
    public UIElement getRupeeSymbol()
    {
        return new UIElement(By.xpath("(//*[contains(@class,'rupee-symbol ptm-body-color')])[3]"),getPageName(),"rupee-symbol");
    }

    public UIElement USDCurrency()
    {
        return new UIElement(By.xpath("//div[contains(@class,'rupee-symbol') and contains(@class,'ptm-body-color')]"),getPageName(),"USD currency");
    }


    public  UIElement qrCodeCheckoutJSText(){return null;}

    public  UIElement enabledPaymodes(){return null;}

    public UIElement infoStripPaymodes(){return null;}

    public  UIElement noQRAvailableText(){return null;}

    public  UIElement noQRInfoPaymodes(){return null;}

    public  UIElement noQRPaymodesPresent(){return null;}

    public UIElement qrSubText(){return null;}

    public UIElement qrImg(){
            return new UIElement(By.xpath("//*[@class='ptm-pay-with-upi']"),getPageName(),"qr-upi-images");
        }

    public UIElement qrCodeCheckoutJSTextNew(){
            return new UIElement(By.xpath("//span[text()='Scan with any UPI App']"),getPageName(),"qr-code-text");
        }

        public UIElement qrSubTextNew(){
            return new UIElement(By.xpath("//span[@class='ptm-more-upi-apps-txt ptm-body-color' and text()=' & more']"),getPageName(),"qr-code-sub-text");
        }
        public UIElement qrCodeImgNew(){
            return new UIElement(By.xpath("//*[@class='ptm-qr-scan']"),getPageName(),"qr-upi-images");
    }

    public UIElement qrImgNew(){
        return new UIElement(By.xpath("//*[@class='_3YNr']"),getPageName(),"qr-upi-images");
    }

    public UIElement upiPaymodeUpiAppImg(){return null;}

    public UIElement upiPaymodeUpiAppText(){return null;}



    public UIElement sbiBank() {
        return new UIElement(By.xpath("//div[text()='SBI']"), getPageName(), "SBI Bank ");
    }

    public UIElement kotakBank() {
        return new UIElement(By.xpath("//div[text()='Kotak']"), getPageName(), "Kotak Bank ");
    }

    public UIElement axisBank() {
        return new UIElement(By.xpath("//div[text()='AXIS']"), getPageName(), "AXIS Bank ");
    }
    public UIElement upiTitle(){return null;}
    public UIElement easypaylogo()
    {
        return new UIElement(By.xpath("//img[@alt='easypay logo']"),getPageName(),"easypaylogo");
    }

    public UIElement safetyPopup(){
          return new UIElement(By.xpath("//div[contains(text(),'Safety Tip')]"),getPageName(),"safetyTip" );
            }

    public UIElement safetyMessage(){
            return new UIElement(By.xpath("//div[contains(text(),'Please ensure you are paying only to a trusted mer')]"),getPageName(),"dsm-message");
         }

    public UIElement getBinerror()
    {
        return new UIElement(By.xpath("//div[contains(@class,'_1Bvb  _11dj o-h fs12 mt5')]"),getPageName(),"InvalidBin-error");
    }
    public Button closeSubsDetailsTab(){return null;}

    public UIElement enhancedUPIHandles(){
        return new UIElement(By.xpath("//*[text()='Verify VPA']/following::div[3]"), getPageName(), "UPI Handles");
    }
    public UIElement getEnhancedUPIText(){
        return new UIElement(By.xpath("//*[@id='ptm-upi-input']/preceding::div[5]"), getPageName(), "UPI Text");
    }
    public UIElement getDefaultAuthMode(){
        return new UIElement(By.xpath("//input[@value='DEBIT_CARD']"), getPageName(), "Default Authmode");
    }
        public UIElement proceedBankTransferBtn () {
            return new UIElement(By.xpath("(//button[text()='Proceed'])[2]"), getPageName(), "Bank Transfer Pay Button");
        }
        public UIElement DuplicateBankTransferPopUp () {
            return new UIElement(By.xpath("//h4[contains(text(),'Duplicate Attempt')]"), getPageName(), "Bank Transfer Duplicate TXN popup");
        }
        public UIElement DuplicateBankTransferMessage () {
            return new UIElement(By.xpath("//div[contains(text(),'One Transaction attempt has already been made')]"), getPageName(), "Bank Transfer Duplicate TXN Message");
        }
        public UIElement DuplicateBankTransferCloseBtn () {
            return new UIElement(By.xpath("//button[contains(text(),'Close Page')]"), getPageName(), "Bank Transfer Duplicate Popup close Btn");
        }
    public UIElement ICICICobrandingheadercolor()
    {
        return new UIElement(By.xpath("//*[@id='ptm-checkout-header']"),getPageName(),"headercolor");
    }

    public UIElement paytmHeaderLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'images/logo-blue.png')])[1]"),getPageName(),"paytm-logo");    }

    public UIElement paytmFooterLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'assets/logo.png')])[2]"),getPageName(),"paytm-logo");
    }

    public UIElement phonepe()
    {
        return new UIElement(By.xpath("//*[@class='ptm-upi-app-name' and contains(text(),'PhonePe')]"),getPageName(),"phonepe");

    }
    public UIElement googlepay()
    {
        return new UIElement(By.xpath("//*[@class='ptm-upi-app-name' and contains(text(),'Google Pay')]"),getPageName(),"gpay");

    }
    public UIElement paytm()
    {
        return new UIElement(By.xpath("//*[@class='ptm-upi-app-name' and contains(text(),'Paytm')]"),getPageName(),"paytmlogo");
    }

    public UIElement alertTexttoProceedEMI()
    {
        return new UIElement(By.xpath("//h2[@class='ptm-body-color']"),getPageName(),"popUpBox");
    }
    public Button proceedDismiss()
    {
        return new Button(By.xpath("//button[@class='ptm-custom-btn ptm-btn-white ptm-emi-dismiss-button']"),getPageName(),"dismissButton");
    }

    public UIElement emiSubventionOfferStrip()
    {
        return new UIElement(By.xpath("//div[@id=\"checkout-offers\"]/span[1]/div"),getPageName(),"offer-details");
    }

    public UIElement emiSubventionOfferStripNEW()
    {
        return new UIElement(By.xpath("//div[@id='checkout-offers']"),getPageName(),"offer-details");
    }
    public UIElement viewAllOffersAvialable()
    {
        return new UIElement(By.xpath("//div[@id=\"checkout-offers\"]/span[2]"),getPageName(),"view All offer");
    }
    public UIElement viewAllOffers()
    {
        return new UIElement(By.xpath("//span[contains(@class,'ptm-bo-viewall') and .//span[normalize-space()='View All']]"),getPageName(),"view-All-offer");
    }
    public UIElement viewAllOffersAvialable_HideButton()
    {
        return new UIElement(By.xpath("//div[@id=\"checkout-offers\"]/span[contains(text(),'Hide')]"),getPageName(),"view-All-offer-hide-button");
    }
    public Button offerButton()
    {
        return new Button(By.xpath("//span[contains(@style,'border-radius: 50%') and contains(@style,'position: relative')]"), getPageName(), "offer-button");
    }
    public Button continueButton()
    {
        return new Button(By.xpath("//button[contains(@class,'ptm-nav-selectable') and normalize-space()='Continue']"), getPageName(), "offer-continue-button");
    }
    public Button viewOKButton()
    {
        return new Button(By.xpath("//button[contains(@class,'ptm-promocode-ok-button') and normalize-space()='Okay, got it']"), getPageName(), "view-ok-button");
    }
    public Button knowMoreButton()
    {
        return new Button(By.xpath("//*[@id='know_more']"), getPageName(), "know-more-button");
    }
    public Button crossButton()
    {
        return new Button(By.xpath("//div[contains(@class,'ptm-close-wrap') and contains(@class,'ptm-nav-back')]//span[contains(@class,'ptm-cross')]"), getPageName(), "cross-button");
    }
    public Button crossButtonx()
    {
        return new Button(By.xpath("(//span[contains(@class,'ptm-cross')])[last()]"), getPageName(), "cross-button");
    }
    public UIElement offerPage()
    {
        return new UIElement(By.xpath("//p[@class='ptm-offer-title']"),getPageName(),"offer-detail-page");
    }
    public UIElement offerText()
    {
        return new UIElement(By.xpath("//p[@class='ptm-small-txt']"),getPageName(),"offer-Text");
    }

    public UIElement offerTC()
    {
        return new UIElement(By.xpath("//p[@class='ptm-small-txt-link ptm-primary']"),getPageName(),"offer-TC");
    }
    public UIElement emiTC()
    {
        return new UIElement(By.xpath("//a[@class=' ptm-primary ptm-emiTnc']"),getPageName(),"EMI-TC");
    }
    public UIElement emiSubventionOfferAvialbaleOnCard()
    {
        return new UIElement(By.xpath("(//div[@class='ptm-overlay-maintxt ptm-semibold ptm-body-color'])[2]"),getPageName(),"offer-TC");
    }


    public Button QrKnowMore()
    {
        return new Button(By.xpath("//*[contains(text(),'Know More')]"),getPageName(),"qrknowmore");
    }
    public UIElement walletcheckbox()
    {
        return new UIElement(By.xpath("//*[@id='wallet_checkbox']"),getPageName(),"WalletCheckbox");
    }

    public UIElement verifyUpiNumericID(){
        return new UIElement(By.xpath("//a[text()='Verify']"),getPageName(),"UPI NUMBER");
    }
    public UIElement UpiNumericId(){
        return new UIElement(By.xpath("//input[@placeholder='Enter Mobile No./UPI No.']"),getPageName(),"UPI NUMBER");
    }

    public UIElement getWalletFreezeErrorMessage(){
        return new UIElement(By.xpath("//*[contains(@class, \"ptm-msg-warn-clr\")]"), getPageName(), "ptm-wallet-frreze-msg");
    }

    public TextBox textBoxVPA1() {
        return new TextBox(By.xpath("//input[contains(@id,'ptm-upi-input') and contains(@placeholder,'Enter VPA')]"),getPageName(),"vpa-field");
    }
    public UIElement headerTxtColor()
    {
        return new UIElement(By.xpath("//div[@class='_2eSq ellipsis xs-txtcontrol fw700']"), getPageName(), "headerTxtColor");
    }
    public UIElement bodyBck_color()
    {
        return new UIElement(By.xpath("//div[@class='main-inner pos-r body-bg-global ']"), getPageName(), "bodyBck_color");
    }
    public UIElement textColor()
    {
        return new UIElement(By.xpath("(//div[@class='fl xs-am'])[1]"), getPageName(), "textColor");
    }
    public UIElement paybuttonbck_color()
    {
        return new UIElement(By.xpath("//button[@class=' btn btn-primary w100 pos-r _2fNU   ']"), getPageName(), "paybuttonbck_color");
    }
    public UIElement headerBck_color()
    {
        return new UIElement(By.xpath("//*[@id='ptm-checkout-header']"), getPageName(), "headerBck_color");
    }
    public UIElement getPaytmLogoWhite()
    {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-hedrlogo')]//*[contains(@src, 'paytm-pg-white')]"), getPageName(),"paytm-header-logo-white");
    }

    public UIElement imgLoginQRCode()
    {
        return new UIElement(By.xpath("//*[contains(@class,'qrContainer')]/img"), getPageName(),"paytm-login-QR");
    }

    public UIElement knowMorePaymodeInfo()
    {
        return new UIElement(By.xpath("//p[contains(@class,'ptm-qr-sub-text')]"), getPageName(),"know-more-paymodes");
    }

    public UIElement UpiNumericIdErrorMsgClass()
    {
        return new UIElement(By.xpath("//span[@class='_2WVN']"), getPageName(),"Upi Message class");
    }

    public UIElement UpiNumericIdVerificationMsgClass()
    {
        return new UIElement(By.xpath("//span[@class='_KaHx']"), getPageName(),"Upi Verified Message class");
    }

    public UIElement getUltimateBeneficiaryName(){
        return new UIElement(By.xpath("//*[contains(@class,'ptm-name-txt')]"), getPageName(), "ultimate-beneficiary-name");
    }


    public UIElement selectBankHeading(){
        return new UIElement(By.xpath("//div[contains(@class,'circle')]/img/ancestor::div[4]/div"), getPageName(), "Select your Bank");
    }
    public UIElement payText() {
        return new UIElement(By.xpath("//button[contains(text(),'Subscribe')]/following::span"), getPageName(), "Wallet CTA");
    }
    public UIElement subConfirmTxtBM()
    {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-sub-confirm ptm-sub-center')]"), getPageName(),"bankmandate-sub-confirm-text");
    }
    public TextBox retryVPATextBox()
    {
        return new TextBox(By.xpath("//*[contains(@class,'ptm-margin0 ptm-upi-input')]"), getPageName(),"UPI-Retry-VPA-Text");
    }
    public UIElement payWithOtherUpiAppsPreLogin()
    {
        return new UIElement(By.xpath("//div[contains(text(),' Pay with Other UPI Apps')]"), getPageName(),"pay with other upi apps");
    }

    public UIElement verifiedVpaID()
    {
        return new UIElement(By.xpath("//span[contains(text(),'Verified VPA ID')]"), getPageName(),"verified vpa id ");
    }


    public UIElement paytmLogoNew()
    {
        return new UIElement(By.xpath("//img[contains(@src,'logo-paytm.svg')]"), getPageName(),"paytm logo ");
    }
    public UIElement upiLogoNew()
    {
        return new UIElement(By.xpath("//img[contains(@src,'upi-icon.png')]"), getPageName(),"upi logo ");
    }
    public UIElement payWithPaytmInputBoxText()
    {
        return new UIElement(By.xpath("//input[contains(@placeholder,'Enter Mobile No / UPI Number')]"), getPageName(),"Enter Mob No. text in pay with paytm input box");
    }

    public UIElement errorTextsInUPIFlow()
    {
        return new UIElement(By.xpath("//p[contains(@class,'_KyO_')]"), getPageName(),"Error Msg Paragraph");
    }

    public UIElement getTotalAmountOnCCDC()
    {
        return new UIElement(By.xpath("//span[contains(@class,'textItem _yocA')][2]"), getPageName(),"Total Amount on cc-dc");
    }

    


    public UIElement bankFooterLogo(){ return null; }
    public UIElement addnpayInfoIcon(){ return null; }
    public UIElement addnpayInfoText(){ return null; }
    public UIElement addnpayInfoButton(){ return null; }

    public UIElement upiPollingPageInfoText(){return null;}
    public UIElement upiPollingPageTxnAmount(){return null;}
    public UIElement upiPollingPageWarningText(){return null;}
    public UIElement upiPollingPageMobileLogo(){return null;}
    public UIElement infoIconForPCF(){return null; }

    public UIElement payButtonConvFeeMsg(){return null; }

    public UIElement convFeeMessageCashierPage(){return null; }

    public UIElement currencyPageINR() {
        return new UIElement(By.xpath("//*[@id='dcc-inner-content']/div[@class='ptm-radio-wrap'][1]/label/div/p/span[1]"), getPageName(), "inr-currency-page");
    }
    public UIElement currency() {
        return new UIElement(By.xpath("//*[@id='dcc-inner-content']/div[@class='ptm-radio-wrap'][1]/label/div/p/span[2]"), getPageName(), "inr-currency-page");
    }
    public UIElement paymodeDisabledContainer() {
        return new UIElement(By.xpath("//*[contains(@class,'payment-mode')]"), getPageName(), "disabled-payment-container");
    }

    public UIElement checkedPPIForCheckoutJS() {
        return new UIElement(By.xpath("(//span[@class=\"ptm-check\"])"), getPageName(), "checked-checkbox-ppi");
    }

    public UIElement uncheckedPPIForCheckoutJS() {
        return new UIElement(By.xpath("(//span[@class=\"ptm-check\"])[1]"), getPageName(), "unchecked-checkbox-ppi");
    }

    public UIElement postpaidContainer(){
        return new UIElement(By.xpath("//*[contains(@id,'checkout-bank-credit')]"),getPageName(),"postapid");
    }

    public UIElement HybridInsufficientWalletAmtMsg(){
        return new UIElement(By.xpath("//*[@class='ptm-small-txt ptm-msg-red ptm-padding-r45']"),getPageName(), "Hybrid Insufficient Wallet Amt Msg");
    }

    public UIElement TotalAmountMsg() {
        return new UIElement(By.xpath("//p[@class='det m-mt-12']"), getPageName(), "Total Amount");
    }

    public UIElement kfsAdhereText() {
        return new UIElement(By.xpath("//*[contains(@class,'sc-emi-pay-overlap')]//*[contains(@class,'ptm-appin-img')]"), getPageName(), "adhere-text");
    }
    public UIElement kfsAdhereText_NewFlow() {
        return new UIElement(By.xpath("//*[contains(@class,'sc-emi-pay-overlap')]//*[contains(@class,'ptm-kfs-text')]"), getPageName(), "adhere-text");
    }

    public UIElement EmiRadioButton() {
        return new UIElement(By.xpath("//div[@class='ptm-round-sep ptm-scnew-card']//*[contains(@class,'ptm-checkmark')]"),getPageName(),"emi-radio");
    }

    public UIElement kfsLink() {
        return new UIElement(By.xpath("//*[contains(@class,'sc-emi-pay-overlap')]//a[contains(@class,'ptm-pointer-cursor')]"), getPageName(), "adhere-text");
    }

    public UIElement kfsUpperText() {
        return new UIElement(By.xpath("//*[contains(@class,'template-head')]"), getPageName(), "kfs-text");
    }

    public UIElement kfsBankDateText() {
        return new UIElement(By.xpath("//*[contains(@class,'template-subhead')]"), getPageName(), "bank-date");
    }

    public UIElement kfsTableText() {
        return new UIElement(By.xpath("//*[contains(@class,'kfs-details-wrap')]"), getPageName(), "kfs-table-text");
    }

    public UIElement kfsDisclaimerText() {
        return new UIElement(By.xpath("//*[contains(@class,'disclaimer')]"), getPageName(), "kfs-disclaimer-text");
    }

    public UIElement kfsLoanConsentText() {
        return new UIElement(By.xpath("//*[contains(@class,'loan-consent')]"), getPageName(), "kfs-loan-consent-text");
    }

    public UIElement kfscloseButton() {
        return new UIElement(By.xpath("(//*[contains(@class,'ptm-cross ptm-pos-r')])[5]"), getPageName(), "kfs-close");
    }
    public UIElement kfsCheckBox() {
        return new UIElement(By.xpath("//div[contains(@class,'ptm-emi-overlay') and contains(@class,'sc-emi-pay-overlap')]//label[contains(@id,'kfsCheckbox')]//input[@type='checkbox']"), getPageName(), "kfs-checkBox");
    }

    public UIElement kfscloanAmount() {
        return new UIElement(By.xpath("//*[contains(@id,'loanAmount')]"), getPageName(), "kfs-loan");
    }

    public UIElement kfsctenure() {
        return new UIElement(By.xpath("//*[contains(@id,'tenure')]"), getPageName(), "kfs-loan");
    }

    public UIElement kfsinstallments() {
        return new UIElement(By.xpath("//*[contains(@id,'installments')]"), getPageName(), "kfs-installments");
    }


    public UIElement PostpaidErrorMessage(){
        return new UIElement(By.xpath("//section[contains(@class,'pc-wrap xs-p8')]"),getPageName());
    }

    public UIElement PaymentRequesterName()
    {
        return new UIElement(By.xpath("//span[(text()='Name')]"), getPageName(),"paymentRequesterDetails name");
    }

    public UIElement PaymentRequesterMobileNumber()
    {
        return new UIElement(By.xpath("//span[(text()='Mobile Number')]"), getPageName(),"paymentRequesterDetails mobilenumber");
    }
    public UIElement EMI_DCPcfText()
    {
        return new UIElement(By.xpath("//span[contains(text(),'Convenience fee')]"), getPageName(),"Pcf charged on EMI DC txn");
    }
    public UIElement cc_dc_iframe()
    {
        return new UIElement(By.xpath("//*[contains(@class,'ptm-card-iframe')]"), getPageName(),"ptm-card-iframe");
    }
    public UIElement NoPaymentOptionAvailable()
    {
        return new UIElement(By.xpath("//*[contains(text(),'No Payment Options available')]"), getPageName(),"No Payment Options available");
    }

    public TextBox textBox2FAPassCode(){
        return null;
    }

    public TextBox textBox2FAIncorrectPasscode() {
        return null;
    }

    public TextBox textBox2FAIncompletePasscode() {
        return null;
    }




    public UIElement verified2FAIncorrectPasscodeErrorMsg(){
        return null;
    }

    public abstract UIElement verified2FAPasscodeErrorMsg();

    public abstract UIElement verified2FALinkForgotPasscode();

    public abstract UIElement verified2FATextForgotPasscode();

    public UIElement getUpiSubPayModeErrorMsg(){
        return new UIElement(By.xpath("//*[@class=\"_1Kqg\"]"),getPageName(),"text-UpisubpayModeEligibility-msg");
    }

    public void validateUpiSubPayModeErrorMsg(String errorMsg){
        String uiTextError = getUpiSubPayModeErrorMsg().getText();
        Assertions.assertThat(uiTextError).isEqualTo(errorMsg);
    }

    public UIElement searchBox() {
        return new UIElement(By.xpath("//input[@placeholder='Search']"), getPageName(), "SearchBox for Offers");
    }
    public UIElement UPIwindow() {
        return new UIElement(By.xpath("//div[contains(text(),\"Enter UPI ID\")]"), getPageName(), "UPIwindow");
    }
    public UIElement applyOffer(String offerNumber) {
        return new UIElement(By.xpath("//p[contains(text(),\"Apply Offer\")]["+offerNumber+"]"), getPageName(), "applyOffer");
    }
    public UIElement applyOfferWallet() {
        return new UIElement(By.xpath("//div[@id=\"checkout-home\"]/.//div[3]//./p[contains(text(), 'Apply Offer')]"), getPageName(), "applyOfferWallet");
    }
    public UIElement dropDownEMIOffers() {
        return new UIElement(By.xpath("//p[contains(text(),\"No Cost EMI\")]/following::div[1]"), getPageName(), "dropDownEMIOffers");
    }
    public UIElement offerVisibleEMI() {
        return new UIElement(By.xpath("//div[contains(text(),\"Select an EMI Plan\")]"), getPageName(), "offerVisibleEMI");
    }
    public UIElement checkboxNB() {
        return new UIElement(By.xpath("//p[contains(text(),\"HDFC\")]/preceding-sibling::div/img"), getPageName(), "checkboxNB");
    }
    public UIElement errorTextMessage() {
        return new UIElement(By.xpath("//*[@class='ptm-scan-err-msg']"), getPageName(), "Error Field for Wallet/Postpaid");
    }
    public UIElement checkboxCard() {
        return new UIElement(By.xpath("//div[contains(text(),\"Enter Debit or Credit Card details\")]"), getPageName(), "checkboxCard");
    }
    public UIElement amountToBePaidOnCashierPage(){
        return new UIElement(By.xpath("(//*[text()='₹']//parent::span)[2]"), getPageName(), "Amount To Be Paid");
    }
    public UIElement clickAPRIcon(){
        return new UIElement(By.xpath("//img[contains(@class,'addnPayMinAmtIcon')]"), getPageName(), "APR Icon");
    }

    public UIElement verifyAPRTittle(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-s-title')]"), getPageName(), "APR tittle verify");
    }
    public UIElement verifyAPRText(){
        return new UIElement(By.xpath("//div[contains(@class,'ptm-fs14')]"), getPageName(), "APR text verify");
    }
    public UIElement closeAPRButton(){
        return new UIElement(By.xpath("//img[contains(@class,'ptm-close-img')]"), getPageName(), "APR close button");
    }


    public  UIElement linkDescriptionOnLinkStatusPage(){
        return new UIElement(By.xpath("//*[@id=\"linkDescription\"]"),getPageName(),"Link Description");
    }

    public UIElement redirectTimeOnLinkStatusPage(){
        return new UIElement(By.xpath("//span[@id='redirectTime']"),getPageName(),"Redirect Time");
    }

    public UIElement OrderIdLinkstatus(){
        return new UIElement(By.xpath("//p[contains(@class, 'f-17') and contains(@class, 'isValidOrder')]"),getPageName(),"Order ID Link Status");
    }

    public UIElement offerStripHideButton(){
        return null;
    }

    public List<String> UPIIntentApps()
    {
        return null;
    }

    public UIElement payViaNotificationTab()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Pay on Paytm App')]"),getPageName(),"pay via notification heading");
    }

    public TextBox phoneNO_PayViaNotification()
    {
        return new TextBox(By.xpath("//input[@id='mobile_input']"), getPageName(), "phone-number-field");
    }
    public Button proceedSecurelyBtn_PayViaNotification()
    {
        return new Button(By.xpath("//button[contains(@class,'ptm-login-btn')]"), getPageName(), "sign-in-button" /*this.buttonPGPayNow().getElementName()*/);
    }

    public UIElement unregisteredUserMsg_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Install Paytm App')]"),getPageName(),"Install app message");
    }

    public UIElement editPhoneNo_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Edit Number')]"),getPageName(),"Edit phone no button");
    }

    public UIElement resendNotification_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Resend Notification/SMS')]"),getPageName(),"Resend notification button");
    }
    public UIElement retryLimitExhaustedMSG_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Max Attempts Exhausted')]"),getPageName(),"Retry limit breached message");
    }
    public UIElement kfsTableTextNew() {
        return new UIElement(By.xpath("//*[contains(@class,'hdfc-table')]"), getPageName(), "kfs-table-text");
    }
    public UIElement kfsTableTextNewPart2() {
        return new UIElement(By.xpath("(//*[contains(@class,'hdfc-table')])[2]"), getPageName(), "kfs-table-text");
    }
    public UIElement kfsTableTextNewAnnexB() {
        return new UIElement(By.xpath("(//*[contains(@class,'hdfc-table')])[3]"), getPageName(), "kfs-table-text");
    }
    public UIElement kfsTableTextNewtable() {
        return new UIElement(By.xpath("(//*[contains(@class,'hdfc-table')])[4]"), getPageName(), "kfs-table-text");
    }
    public UIElement kfs_impLinks() {
        return new UIElement(By.xpath("(//*[contains(@class,'hdfc-table')])[5]"), getPageName(), "kfs-table-text");
    }
    public UIElement kfsRepayment_Date() {
        return new UIElement(By.xpath("//div[@class='mt-10']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_refNo() {
        return new UIElement(By.xpath("//span[text()='Reference Number: ']"), getPageName(), "kfs-repayment");
    }

    public UIElement kfsRepayment_refNo_Id() {
        return new UIElement(By.xpath("//span[@id='referenceId2']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_Customer() {
        return new UIElement(By.xpath("//span[text()='Customer: ']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_loanType() {
        return new UIElement(By.xpath("//*[@data-kfs-text='loanType']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_tenure() {
        return new UIElement(By.xpath("//span[text()='Tenure: ']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_amt() {
        return new UIElement(By.xpath("//span[text()='Amount Financed: ']"), getPageName(), "kfs-repayment");
    }

    public UIElement kfsRepayment_AmtFinanced() {
        return new UIElement(By.xpath("//span[@id='sanctionedLoanAmount4']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_instal() {
        return new UIElement(By.xpath("//span[text()='Total Installment: ']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_freq() {
        return new UIElement(By.xpath("//div[text()='Frequency: Monthly']"), getPageName(), "kfs-repayment");
    }
    public UIElement kfsRepayment_CurrINR() {
        return new UIElement(By.xpath("//div[text()='Currency: INR']"), getPageName(), "kfs-repayment");
    }
    public Select dropdownLanguageSupport() {
        return new Select(By.xpath("//*[@id='langSelect']"), getPageName(), "dropdown");
    }

    public UIElement feesAppliedText(){
        return new Select(By.xpath("//span[@class='ptm-fee-applied-txt ptm-body-color']"), getPageName(), "Fees applied text");
    }
    public UIElement iButton(){
        return new Select(By.xpath("//img[@class='ptm-info ptm-addnPayMinAmtIcon ptm-mb-2']"), getPageName(), "i button");
    }

    public UIElement iButton_Heading(){
        iButton().click();
        waitUntilLoads();
        return new Select(By.xpath("//div[@class='ptm-s-title ptm-body-color ptm-mb15']"), getPageName(), "i-button Fees applied text");
    }

    public UIElement iButton_ConvFeeHeading(){
        iButton().click();
        waitUntilLoads();
        return new Select(By.xpath("//div[contains(@class,'ptm-fee-wrapper')]/div[1]//div[contains(@class,'ptm-fee-sub-heading')]"), getPageName(), "i-Button Conv fees Heading");
    }


    public UIElement iButton_ConvFeeAmount(){
        iButton().click();
        waitUntilLoads();
        return new UIElement(By.xpath("//div[contains(@class,'ptm-fee-heading')]//div[contains(@class,'ptm-fee-sub-heading')]//div[contains(@class,'rupee-symbol')]"), getPageName(), "USD fee amount in ibutton");
    }

    public UIElement iButton_ConvFeeText(){
        iButton().click();
        waitUntilLoads();
        return new Select(By.xpath("//div[contains(@class,'ptm-fee-wrapper')]/div[1]//div[contains(@class,'ptm-fee-content')]"), getPageName(), "i-Button Conv fees Text");
    }

    public UIElement iButton_PlatformFeeHeading(){
        iButton().click();
        waitUntilLoads();
        return new Select(By.xpath("//div[contains(@class,'ptm-fee-wrapper')]/div[3]//div[contains(@class,'ptm-fee-sub-heading')]"), getPageName(), "i-Button Platform fees Heading");
    }

    public UIElement iButton_PlatformFeeText(){
        iButton().click();
        waitUntilLoads();
        return new Select(By.xpath("//div[contains(@class,'ptm-fee-wrapper')]/div[3]//div[contains(@class,'ptm-fee-content')]"), getPageName(), "i-Button Platform fees Text");
    }

    public UIElement KFSloanConsent() {
        return new Select(By.xpath("//div[@id='loan-consent']"), getPageName(), "loan-consent");
    }


    public UIElement pcfConvenienceInfoIcon() {
        return new UIElement(By.xpath("//*[@class='_Eevm']"), getPageName(),"pcf convenience fees Icon");
    }
    public UIElement pcfConvenienceInfoHeader() {
        return new UIElement(By.xpath("//*[text()='Convenience Fees']"), getPageName(),"pcf convenience fees Header");
    }
    public UIElement pcfConvenienceInfoHeaderNew() {
        return new UIElement(By.xpath("//*[text()='Fees Applied']"), getPageName(),"pcf convenience fees Header");
    }
    public UIElement pcfConvenienceInfoTextNew1() {
        return new UIElement(By.xpath("//div[@class='ptm-fee-content' and contains(text(), 'Convenience fees are fees applied by PG to end customers')]"), getPageName(),"pcf convenience fees Text");
    }
    public UIElement pcfConvenienceInfoIconNew() {
        return new UIElement(By.xpath("//img[@class='ptm-info ptm-addnPayMinAmtIcon ptm-mb-2']"), getPageName(),"pcf convenience fees Icon");
    }
    public UIElement pcfConvenienceInfoText() {
        return new UIElement(By.xpath("//*[text()='Convenience Fees']/following-sibling::div"), getPageName(),"pcf convenience fees Text");
    }
    public UIElement pcfConvenienceInfoTextNew() {
        return new UIElement(By.xpath("//*[@class='_3ZFr']"), getPageName(),"pcf convenience fees Text");
    }

    public TextBox textBoxUpiMobileNumber()
    {
        return new TextBox(By.xpath("//input[@placeholder='Enter Mobile No / UPI Number']"), getPageName(), "phone-number-field");
    }

    public List<WebElement> savedVPAList() {
        return DriverManager.getDriver().findElements(By.xpath("//div[contains(text(),'@paytm')]"));
    }

    public List<WebElement> deleteSavedVPAList() {
        return DriverManager.getDriver().findElements(By.xpath("//div[contains(text(),'@paytm')]//following-sibling::img"));
    }

    public Button buttonOk() {
        return new Button(By.xpath( "//button[contains(text(),'Ok')]"), getPageName(), "ptm-custom-btn ptm-hvr-pop");
    }

    public Button buttonDismiss() {
        return new Button(By.xpath( "//button[contains(text(), 'Dismiss')]"), getPageName(), "ptm-custom-btn ptm-btn-white ptm-emi-dismiss-button");
    }

    public UIElement displayDeleteVPA(){
        return new UIElement( By.xpath("//h2[contains(text(), 'Deleting')]"),getPageName(),"ptm-body-color");
    }
    public UIElement NameOnCard(){
        return new UIElement( By.xpath("//input[@id='name_on_card']"),getPageName(),"name_on_card");
    }
    public UIElement EmailOnCard(){
        return new UIElement( By.xpath("//input[@id='email_on_card']"),getPageName(),"email_on_card");
    }
    public UIElement PostalCode(){
        return new UIElement( By.xpath("//input[@id='postal_code']"),getPageName(),"postal_code");
    }
    
    public UIElement PhoneNumber(){
        return new UIElement( By.xpath("//input[@id='phone_number']"),getPageName(),"phone_number");
    }
    
    public UIElement PhoneCode(){
        return new UIElement( By.xpath("//input[@id='phone_code']"),getPageName(),"phone_code");
    }
    
    public UIElement CountryCode(){
        return new UIElement( By.xpath("//input[@id='cont_code']"),getPageName(),"cont_code");
    }
    
    public TextBox HouseNo(){
        return new TextBox( By.xpath("//input[@id='house_no']"),getPageName(),"house_no");
    }

    /** Validation message below house number when required AVS field is empty (custom checkout / AVS). */
    public UIElement houseNoError(){
        return new UIElement(By.id("houseNoError"), getPageName(), "houseNoError");
    }
    
    public UIElement Street(){
        return new UIElement( By.xpath("//input[@id='street']"),getPageName(),"street");
    }
    
    public UIElement CityCode(){
        return new UIElement( By.xpath("//input[@id='city_code']"),getPageName(),"city_code");
    }
    
    public UIElement StateCode(){
        return new UIElement( By.xpath("//input[@id='state_code']"),getPageName(),"state_code");
    }

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

    public void payByCC(CashierPage cashierPage, PaymentDTO paymentDTO) {
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabCreditCard().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().click();
    }

    public void payByNB(CashierPage cashierPage, PaymentDTO paymentDTO) {
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.tabNetBanking().click();
        dropdownNB().selectByValue(paymentDTO.getBankName());
        buttonPGPayNow().waitUntilClickable();
        buttonPGPayNow().click();
    }
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

  public List<WebElement> ConvenineceFeeWrapperForUpiIntent() {
    return DriverManager.getDriver().findElements(By.xpath("//div[@class=\"ptm-fee-wrapper\"]"));
  }

  /**
   * Checks if convenience fee wrapper is present
   *
   * @return true if convenience fee wrapper is present, false otherwise
   */
  public boolean isConvenienceFeeWrapperPresent() {
    List<WebElement> wrappers = ConvenineceFeeWrapperForUpiIntent();
    if (wrappers.isEmpty()) {
      return false;
    }
    WebElement firstWrapper = wrappers.get(0);
    String actualHeading = firstWrapper.findElement(By.className("ptm-fee-sub-heading")).getText();
    return actualHeading.contains("Convenience Fees");
  }

  /**
   * Checks if platform fee wrapper is present
   *
   * @return true if platform fee wrapper is present, false otherwise
   */
  public boolean isPlatformFeeWrapperPresent() {
    List<WebElement> wrappers = ConvenineceFeeWrapperForUpiIntent();
    System.out.println("Wrapper size is" + wrappers.size());
    if (wrappers.size() < 2) {
      System.out.println("Inside this as wrapper size is less than 2");
      WebElement secondWrapper = wrappers.get(0);
      return secondWrapper.findElement(By.className("ptm-fee-sub-heading")).getText()
          .contains("Platform Fee");
    }
    WebElement secondWrapper = wrappers.get(1);
    return secondWrapper.findElement(By.className("ptm-fee-sub-heading")).getText()
        .contains("Platform Fee");
  }

  /**
   * returns convenience fee elements
   */
  public HashMap<String, WebElement> getConvenienceFeeWrapperElements() {
    HashMap<String, WebElement> convFeeElements = new HashMap<>();
    List<WebElement> wrappers = ConvenineceFeeWrapperForUpiIntent();
    if (wrappers.isEmpty()) {
      return convFeeElements;
    }
    WebElement wrapper = wrappers.get(0);

    convFeeElements.put(convenienceFeeElements.convFeeHeading.name(),
        wrapper.findElement(By.className("ptm-fee-sub-heading")));
    convFeeElements.put(convenienceFeeElements.convFeeText.name(),
        wrapper.findElement(By.className("ptm-fee-content")));

    //fetching key name and fee value for each subtype
    List<WebElement> feeRows = wrapper.findElements(By.className("ptm-fee-subtype"));
    for (WebElement row : feeRows) {
      String title = row.findElement(By.className("ptm-fee-subtype-content")).getText();
      System.out.println(title.contains(convenienceFeeElements.bankAccountTitle.name()));

      if (title.contains(convenienceFeeElements.bankAccountTitle.toString())) {
        convFeeElements.put(convenienceFeeElements.bankAccountTitle.name(),
            row.findElement(By.className("ptm-fee-subtype-content")));
        convFeeElements.put(convenienceFeeElements.bankAccountSubTitle.name(),
            row.findElement(By.cssSelector(".ptm-d-block.ptm-fee-subtype-subtext")));
        convFeeElements.put(convenienceFeeElements.bankAccountFee.name(),
            row.findElement(By.className("ptm-fee-subtype-value")));
      } else if (title.contains(convenienceFeeElements.subTypeCreditCardTitle.toString())) {
        convFeeElements.put(convenienceFeeElements.subTypeCreditCardTitle.name(),
            row.findElement(By.className("ptm-fee-subtype-content")));
        convFeeElements.put(convenienceFeeElements.creditCardFee.name(),
            row.findElement(By.className("ptm-fee-subtype-value")));
      } else if (title.contains(convenienceFeeElements.subTypePPIWalletTitle.toString())) {
        convFeeElements.put(convenienceFeeElements.subTypePPIWalletTitle.name(),
            row.findElement(By.className("ptm-fee-subtype-content")));
        convFeeElements.put(convenienceFeeElements.ppiWalletFee.name(),
            row.findElement(By.className("ptm-fee-subtype-value")));

      } else if (title.contains(convenienceFeeElements.subTypeCreditLineTitle.toString())) {
        convFeeElements.put(convenienceFeeElements.subTypeCreditLineTitle.name(),
            row.findElement(By.className("ptm-fee-subtype-content")));
        convFeeElements.put(convenienceFeeElements.creditLineFee.name(),
            row.findElement(By.className("ptm-fee-subtype-value")));
      }
    }
    return convFeeElements;
  }

  /**
   * returns platform fee elements
   */
  public HashMap<String, WebElement> getPlatformFeeWrapperElements() {
    HashMap<String, WebElement> platformFeeElements = new HashMap<>();
    List<WebElement> wrappers = ConvenineceFeeWrapperForUpiIntent();
    if (wrappers.isEmpty()) {
      return platformFeeElements;
    }
    WebElement wrapper = wrappers.get(0);
    if (wrappers.size() == 2) {
      wrapper = wrappers.get(1);
    }
    WebElement feeRow = wrapper.findElement(By.className("ptm-fee-heading"));
    platformFeeElements.put(convenienceFeeElements.platformFeeHeading.name(),
        feeRow.findElements(By.className("ptm-fee-sub-heading")).get(0));
    platformFeeElements.put(convenienceFeeElements.platformFeeText.name(),
        wrapper.findElement(By.className("ptm-fee-content")));
    platformFeeElements.put(convenienceFeeElements.platformFeeAmount.name(),
        feeRow.findElements(By.className("ptm-fee-sub-heading")).get(1));
    return platformFeeElements;
  }


  public UIElement upiIntentPcfInfoText() {
    return new UIElement(By.xpath("//*[@class=\"ptm-upi-pcf-info-txt\"]"),
        getPageName(), "pcfInfoTextForUpiIntent");
  }

  public UIElement upiIntentPcfInfoIcon() {
    return new UIElement(By.xpath("//*[@class=\"ptm-upi-pcf-info-icon\"]"),
        getPageName(), "pcfInfoIconForUpiIntent");
  }

    public UIElement FeeTextForUpiIntentQr() {
        return new UIElement(
            By.xpath("//div[@class=\"ptm-txinfo ptm-upi-txinfo\"]//span[@class=\"ptm-body-color\"]"),
            getPageName(), "fee-text-for-upi-intent");
    }

    public UIElement tabNBFCCardless() {
        return new UIElement(By.xpath("//*[text()='NBFC/Cardless']"), getPageName(), "tab NBFC");
    }
    public UIElement verifyNBFCText() {
        return new UIElement(By.xpath("//h3[text()='Non-Banking Financial Company (NBFC)']"), getPageName(), "NBFC text");
    }
    public UIElement verifyInterestRate() {
        return new UIElement(By.xpath("//*[contains(@class,\"ptm-scemi-intrst\")]"), getPageName(), "NBFC text");
    }

    public UIElement verifyNoCostEMITag() {
        return new UIElement(By.xpath("//*[contains(text(),\"No Cost EMI\")]"), getPageName(), "NO Cost EMI");
    }
    public UIElement clickEmiCardless() {
        return new UIElement(By.xpath("//div[span[text()='Bajaj Finserv EMI Card']]"), getPageName(), "Bajaj Finserv EMI Card");
    }
    public UIElement clickonBajaj() {
        return new UIElement(By.xpath("//img[contains(@src,'BAJAJFN.png')]"), getPageName(), "Bajaj Finserv EMI Card");
    }
    
}

