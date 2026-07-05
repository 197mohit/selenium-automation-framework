package com.paytm.pages;

import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.*;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.paytm.utils.merchant.util.AuthUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CashierPageEnhancedWeb extends CashierPageEnhancedWAP {

    public CashierPageEnhancedWeb() {
        super();
        this.pageName = "cashier-page-enhanced-web-theme";
    }


    @Override
    public Button bankMandateConfirmPay(){
       // return new Button(By.xpath("//button[@class='btn btn-primary w100 pos-r  '][contains(.,'Confirm')]"), getPageName(), "bankmandate-pay-confirm");
        return new Button(By.xpath("//*[contains(@class,'btn btn-primary w100 pos-r ')]"), getPageName(), "bankmandate-pay-confirm");
    }

    @Override
    public UIElement imgScanPayQRCode() {
        return new UIElement(By.xpath("//img[contains(@src,'data:image/png;base64')]"), getPageName(), "img-ScanPay-QRCode");
    }

    @Override
    public TextBox textBoxPhoneNumber() {
        return new TextBox(By.id("inp"), getPageName(), super.textBoxPhoneNumber().getElementName()) {
            @Override
            public void clearAndType(CharSequence... keysToSend) {
                int i = 0;
                do {
                    super.clearAndType("");
                    super.clearAndType(keysToSend);
                    i++;
                    pause(2);
                } while (!this.getAttribute("value").equals(keysToSend[0].toString()) && i < 6);
            }
        };
    }


    @Override
    public RadioButton radioButtonPostpaid() {
        return new RadioButton(By.cssSelector("input[value='pdc']"), getPageName(), "paymode") {
            @Override
            public void assertSelected() {
                if (!isPaytmCCChecked()) {
                    throw new AssertionError("Expecting " + this.getElementName() + " to be selected but is not");
                }
            }
        };
    }

    @Override
    public void fillExpiryMonth(String expiryMonth) {
        this.textBoxExpiryMonth().waitUntilEditable();
        this.textBoxExpiryMonth().clearAndType(expiryMonth);
    }

    @Override
    public void fillExpiryYear(String expiryYear) {
        this.textBoxExpiryYear().waitUntilEditable();
        this.textBoxExpiryYear().clearAndType(expiryYear);
    }

    public CheckBox rememberMeCheckbox() {
        return new CheckBox(By.xpath("//div[contains(@class,'custom-check')]/input[contains(@type, 'checkbox')]"),
                getPageName(), "Remember-this-mobile-number-for-future-login_checkbox") {
            @Override
            public boolean isChecked() {
                return this.isSelected();
            }
        };
    }

    @Override
    public void assertVisible(){
        new UIElement(By.xpath("//span[contains(text(),'100% Secure Payments Powered by Paytm')]"),getPageName(),"cashierpage-text").assertVisible();
    }

    @Override
    public void directLogin(User user)
    {

    }

    @Override
    public boolean isPPIChecked() {
        try {
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".paytm-wallet >.ppberr")).apply(DriverManager.getDriver());
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public boolean isPPBLChecked() {
        return false;
    }

    @Override
    public int savedUpiListSize(){
        List<WebElement> links;
        links = DriverManager.getCurrentWebDriver().findElements(By.xpath("(//label[@aria-label='upipush']/following-sibling::span//span[@class='bank-d'])"));
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
                if (cardNumber.startsWith(wl.getText().split(" ")[0])
                        && cardNumber.endsWith(wl.getText().split(" ")[3])) {
                    count++;
                    break;
                }
            }
        }
        Assertions.assertThat(count).withFailMessage("Expected " + cardNumbers.length + " cards but found " + count + " cards.").isEqualTo(cardNumbers.length);
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

    @Override
    public UIElement phoneNum() {
        return new UIElement(By.xpath("//i/preceding-sibling::div[1]"), getPageName(), "phone-number");
    }

    public Table tableConvFeeCharge() {
        return new Table(By.xpath(""), getPageName(), "") {
            @Override
            public int getRowCount() {
                return UIElements.getMultiple(By.xpath("//label[text()='Convenience Charges']/following-sibling::ul/li"), getPageName(), "All pay modes on Convenience fee dropdown").size();
            }

            public String getRowValue(String rowName) {
                UIElement uiElement = new UIElement(By.xpath("//label[text()='Convenience Charges']/following-sibling::ul/li/div[contains(text(),'" + rowName + "')]/following-sibling::div/p"), getPageName(), "convenience-fee-in-conv-fee-table") {

                    @Override
                    public String getText() {
                        return super.getText().replace("GST", "").replace("as applicable", "").replace("+", "").replace("Rs", "").trim();
                    }
                };
                return uiElement.getText();
            }
        };
    }

    public Button buttonPPBLPayNow() {
        return new Button(By.cssSelector("div.clearfix.passcode+div button"), getPageName(), "PPBL-pay-button");
    }

    public TextBox textBoxPPBLPassCode() {
        return new TextBox(By.cssSelector("input.form-ctrl.mask"), getPageName(), super.textBoxPPBLPassCode().getElementName());
    }

    @Override
    public TextBox textBoxPostpaidPassCode() {
        return new TextBox(By.cssSelector("input.form-ctrl.mask"), getPageName(), super.textBoxPostpaidPassCode().getElementName());
    }

    public CheckBox checkboxPPBL() {
        return new CheckBox(By.cssSelector("input[value='ppb']"), getPageName(), "ppbl-checkbox") {

            @Override
            public boolean isChecked() {
                UIElement uiElement = new UIElement(By.cssSelector("[data-key='ppb']"), getPageName(), "");
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

    public UIElement insufficientPPBLBalanceIconMsg() {
        return new UIElement(By.cssSelector("section.pc-wrap div div p"), getPageName(), "ppbl-insufficient" +
                "Balance");
    }

    @Override
    public UIElement ppblNotificationMsg() {
        return new UIElement(By.cssSelector(".ppberr .undefined"), getPageName(), "ppbl-notification-msg");
    }



    @Override
    public UIElement EMIsavedCard(String cardNumber) {
        return new UIElement(By.xpath("(//span[text()='" + cardNumber.substring(cardNumber.length() - 4, cardNumber.length()) + "']//ancestor::span//preceding-sibling::label//input[@value='sc'])[2]"), getPageName(), "saved-card-radio-btn");
    }

    @Override
    public void RequestloginOTP(String mobileNumber) {

        loginStrip().click();
        textBoxPhoneNumber().waitUntilEditable();
        textBoxPhoneNumber().clearAndType(mobileNumber);
        buttonSecureSignIn().waitUntilClickable();
        buttonSecureSignIn().click();
        waitUntilLoads();
    }


    @Override
    public List<UIElement> textBoxPRNNumber() {
        return UIElements.getMultiple(By.xpath("//input[@data-type='prn-box']"), getPageName(), "textBoxPRNNumber");
    }

    @Override
    public UIElement buttonPRNVerify() {
        return new UIElement(By.xpath("//button//span[text()='Verify']"), getPageName(), "buttonPRNVerify");
    }


    public Boolean isLogoutVisible() {

        boolean flag = false;

        if (DriverManager.getDriver().findElements(By.xpath("//div[contains(text(), 'Login with Another Paytm Account')]")).size() != 0) {

            flag = true;

        }
        return flag;

        // return new UIElementV3(By.xpath(("//div[contains(text(), 'Login with Another Paytm Account")),"logout_dropdown").isVisible().getAsBoolean();
    }

    @Override
    public boolean wallet_dropdown() {
        DriverManager.getDriver().findElement(By.cssSelector("i.arrow-down-wallet")).click();
        List<String> walletMapping = new ArrayList<>();
        walletMapping = Arrays.asList(DriverManager.getDriver().findElement(By.cssSelector(".animated")).getText().split(":"));

        for (String wallet : walletMapping) {

            System.out.println("Value: " + wallet);

        }
        boolean status = walletMapping.get(3).trim().equalsIgnoreCase(walletMapping.get(1).replace("Paytm Wallet", "").trim());
        return status;
    }

    @Override
    public UIElement mobileNumber() {
        return new UIElement(By.xpath("//section/span[2]"), getPageName(), "Mobile Number");
    }

    @Override
    public UIElement postPaid()
    {
        return new UIElement (By.xpath("//div[contains(@class,'_3ijw brdr-box border-global')]//div[contains(@class,'_3pkP')]//span[contains(text(),'Postpaid')]"),getPageName(),"Static-postpaid-on-login-screen");
    }

    @Override
    public UIElement wallet()
    {
        return new UIElement (By.xpath("//div[contains(@class,'_3ijw brdr-box border-global')]//div[contains(@class,'_3pkP')]//span[contains(text(),'Wallet')]"),getPageName(),"Static-wallet-on-login-screen");
    }

    @Override
    public UIElement cards()
    {
        return new UIElement (By.xpath("//div[contains(@class,'_3ijw brdr-box border-global')]//div[contains(@class,'_3pkP')]//span[contains(text(),'Cards')]"),getPageName(),"Static-cards-on-login-screen");
    }

    @Override
    public UIElement uPIBankAC()
    {
        return new UIElement (By.xpath("//div[@id='ptm-login']//span[text()='UPI']"),getPageName(),"Static-upiBankAccount-on-login-screen");
    }

    @Override
    public UIElement newPaymentMethod()
    {
        return new UIElement (By.xpath("//h3[contains(text(),'Other Payment Options')]"),getPageName(),"New-Payment-option-on-login-screen");
    }

    @Override
    public Link bankmandateAuthMode(String authmode){
        return new Link(By.xpath("//*[contains(text(),'" + authmode +"')]/preceding-sibling::span"), getPageName(), "bank-mandate-authmode");
    }

    @Override
    public List<String> upiHandlers()
    {
        List<UIElement> upiHandlers = UIElements.getMultiple(By.xpath("//*[text()='Know more']/following::div[5]"),getPageName(),"upi-handlers");
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
    public UIElement paytmHeaderLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'images/logo-blue.png')])[1]"),getPageName(),"paytm-logo-header");
    }

    @Override
    public UIElement paytmFooterLogo(){
        return new UIElement(By.xpath("(//img[contains(@src, 'assets/images/logo-blue.png')])[1]"),getPageName(),"paytm-logo-footer");
    }

    @Override
    public UIElement getWalletFreezeErrorMessage(){
        return new UIElement(By.xpath("//*[@class='know-more']/../span"), getPageName(), "ptm-wallet-frreze-msg-enhanced");
    }
    @Override
    public UIElement knowMoreLink(){
        //return new UIElement(By.xpath("//*[contains(@class, 'know-more')]"), getPageName(), "know-more-enhanced");
        return new UIElement(By.xpath(".//a[contains(text(),'Know More')]"), getPageName(), "know-more-enhanced");
    }
    @Override
    public UIElement getKnowMoreLinkText(){
        return new UIElement(By.xpath("//*[contains(@class,'popup-global')]/div/div[1]"), getPageName(), "ptm-balace-unavailable-msg");
    }
    @Override
    public UIElement knowMoreLinkPopup(){
        // "Okay. Got it" will be clicked present in know more popup
       return new UIElement(By.xpath("//*[contains(@class, 'btn btn-primary')]"), getPageName(), "ptm-pref-btn");
    }

    @Override
    public UIElement getPaytmLogoWhite()
    {
        return new Link(By.xpath("//*[contains(@class,'top-headerbox')]//*[contains(@src,'pg-white')]"), getPageName(),"paytmLogoWhite");
    }
    @Override
    public UIElement selectBankHeading(){
        return new UIElement(By.xpath("//div[contains(@class,'pu-title')]"), getPageName(), "Bank Heading");
    }
    @Override
    public UIElement payText(){
        return new UIElement(By.xpath("//span[contains(text(),'Subscribe')]/following::div"), getPageName(),"UPI CTA");
    }

    public UIElement verifyUpiNumericID() {
        return new UIElement(By.xpath("//span[contains(text(),'Verified:')]"),getPageName(),"verified UPI NUMBER");
    }
    public UIElement UpiNumericId(){
        return new UIElement(By.xpath("//input[contains(@placeholder,'Enter Mobile No / UPI Number')]"),getPageName(),"UPI NUMBER");
    }

    @Override
    public UIElement getUltimateBeneficiaryName(){
        return new UIElement(By.xpath("//*[contains(@class,'ellipsis xs-txtcontrol')]"), getPageName(), "ultimate-beneficiary-name");
    }

    public UIElement addnpayInfoIcon(){
        return new UIElement(By.xpath("//img[@class='_24Ma']"), getPageName(), "addnpay-info-icon");
    }
    public UIElement addnpayInfoText(){
        return new UIElement(By.xpath("//div[@class=\"_3Prd\"]/div[1]"),getPageName(),"addnpay-info-text");
    }
    public UIElement addnpayInfoButton() {
        return new UIElement(By.xpath("//button[contains(text(),'Okay, Got it')]"),getPageName(),"addnpay-info-button");
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
    public UIElement insufficientBalanceIconMsg() {
        return new UIElement(By.xpath("//*[contains(text(),'Insufficient Wallet Balance')]"), getPageName(), "wallet insufficient balance");

    }

    public UIElement payViaNotificationTab()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Pay on Paytm App')]"),getPageName(),"pay via notification heading");
    }

    @Override
    public TextBox phoneNO_PayViaNotification()
    {
        return new TextBox(By.xpath("//input[@id='mobile_input']"), getPageName(), "phone-number-field");
    }

    @Override
    public Button proceedSecurelyBtn_PayViaNotification()
    {
        return new Button(By.xpath("//button[contains(@class,'ptm-login-btn')]"), getPageName(), "sign-in-button" /*this.buttonPGPayNow().getElementName()*/);
    }

    @Override
    public UIElement unregisteredUserMsg_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Install Paytm App')]"),getPageName(),"Install app message");
    }

    @Override
    public UIElement resendNotification_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Resend Notification/SMS')]"),getPageName(),"Resend notification button");
    }

    @Override
    public UIElement retryLimitExhaustedMSG_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Max Attempts Exhausted')]"),getPageName(),"Max attempts ehxhausted banner");
    }

    @Override
    public UIElement editPhoneNo_payViaNotification()
    {
        return new UIElement(By.xpath("//*[contains(text(),'Edit Number')]"),getPageName(),"Edit phone no button");
    }
    @Override
    public UIElement qrImg(){
        return new UIElement(By.xpath("//*[@class='ptm-pay-with-upi']"),getPageName(),"qr-upi-images");
    }

}


