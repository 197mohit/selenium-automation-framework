package com.paytm.pages;

import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.Link;
import com.paytm.framework.ui.element.TextBox;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.utils.merchant.util.OtpStrings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class LinkPaymentLoginPage extends BasePage {

    public LinkPaymentLoginPage() {
        super("Link Based Payment Login Page");
    }

    public TextBox textBoxMobileNumber() {
        return new TextBox(By.xpath("//input[@placeholder='Enter your Mobile Number']"), getPageName(), "mobile no");
    }

    public Button buttonSubmitLogin() {
        return new Button(By.id("linkPaySubmit"), getPageName(), "submit login button");
    }

    public TextBox textBoxOtp() {
        return new TextBox(By.id("otp"), getPageName(), "OTP box");
    }

    public Button buttonOtpSubmit() {
        return new Button(By.id("otpSubmit"), getPageName(), "OTP submit");
    }

    public Link linkShowMore() {
        return new Link(By.cssSelector(".btn-show-payment-details"), getPageName(), "linkShowMore");
    }

    public UIElement textOrderId() {
        return new UIElement(By.xpath("//li[contains(text(),'Transaction ID')]/span"), getPageName(), "Order ID");
    }

    public UIElement procedToConvertEMI() {
        return new UIElement(By.xpath("//button[text()='Proceed to Convert to EMI']"), getPageName(),
                "Proceed To Pay EMI");
    }

    public UIElement proceedToPay() {
        return new UIElement(By.xpath("//button[text()='Proceed to Pay']"), getPageName(), "Proceed to Pay");
    }

    public UIElement skipLoginCustomerName() {
        return new UIElement(By.xpath("//input[@name='customerName']"), getPageName(), "customerName");
    }

    public UIElement skipLoginMobileNo() {
        return new UIElement(By.xpath("//input[@name='mobileNo']"), getPageName(), "mobileNo");
    }

    public UIElement skipLoginEmailId() {
        return new UIElement(By.xpath("//input[@name='emailId']"), getPageName(), "emailId");
    }

    public UIElement skipLoginAmount() {
        return new UIElement(By.xpath("//input[@name='Amount']"), getPageName(), "Amount");
    }

    public UIElement skipLoginSubmitbtn() {
        return new UIElement(By.xpath("//button[@type='submit']"), getPageName(), "submitbtn");
    }

    public UIElement paymentFormAge() {
        return new UIElement(By.xpath("//input[@name='Age']"), getPageName(), "Age");
    }

    public UIElement paymentFormDOB() {
        return new UIElement(By.xpath("//input[@placeholder='Date of Birth']"), getPageName(), "DOB");
    }

    public UIElement paymentFormAddressLine1() {
        return new UIElement(By.xpath("//input[@name='Address Line 1']"), getPageName(), "Address");
    }

    public UIElement paymentFormAddressLine2() {
        return new UIElement(By.xpath("//input[@name='Address Line 2']"), getPageName(), "Address Line");
    }

    public UIElement paymentFormCity() {
        return new UIElement(By.xpath("//input[@name='City']"), getPageName(), "City");
    }

    public UIElement paymentFormPinCode() {
        return new UIElement(By.xpath("//input[@name='Pin Code']"), getPageName(), "Pin Code");
    }

    public UIElement paymentFormState() {
        return new UIElement(By.xpath("//input[@name='State']"), getPageName(), "State");
    }

    public UIElement EdcCardEnter() {
        return new UIElement(By.xpath("//p[contains(@class,'ptm-paymode-name')]"), getPageName(), "State");
    }
    // AI-Generated: 2025-10-17 - Function creation

    public void clickCardNumberTabCenter() {
        WebElement webElement = (WebElement) DriverManager.getDriver()
                .findElement(By.cssSelector("p.ptm-paymode-name"));
        new UIElement(By.cssSelector("p.ptm-paymode-name"), getPageName(), "Card Number Tab").waitUntilVisible();
        Actions action = new Actions((WebDriver) DriverManager.getDriver());
        action.moveToElement(webElement).click().build().perform();
    }

    public UIElement EDCEMI_Paymode() {
        return new UIElement(By.xpath("//p[@class='ptm-paymode-name ptm-lightbold']"), getPageName(), "State");
    }

    public UIElement paymentFormFirstName() {
        return new UIElement(By.xpath("//input[@name='First Name']"), getPageName(), "First Name");
    }

    public UIElement paymentFormLastName() {
        return new UIElement(By.xpath("//input[@name='Last Name']"), getPageName(), "Last Name");
    }

    public UIElement paymentFormMobNumber() {
        return new UIElement(By.xpath("//input[@name='Mobile Number']"), getPageName(), "Mobile Number");
    }

    public UIElement acceptModel() {
        return new UIElement(By.xpath("//button[contains(@id,'submitWrapper')]"), getPageName(),
                "SD Merchant Popup Continue Button");
    }

    public UIElement payPartialPayment() {
        return new UIElement(By.xpath("//*[contains(@value, 'Pay partial amount')]"), getPageName(),
                "pay with PartialPayment");
    }

    public UIElement partialPaymentAmount() {
        return new UIElement(By.xpath("//input[@placeholder='Enter amount']"), getPageName(),
                "Amount pay on PartialPayment");
    }

    public UIElement payFullPayment() {
        return new UIElement(By.xpath("//*[contains(@value, 'Pay full amount')]"), getPageName(), "pay Full Payment");
    }

    public UIElement partialPaymentSubmitBtn() {
        return new UIElement(By.xpath("//button[@type='submit']"), getPageName(), "PartialPayment Submit Btn");
    }

    public UIElement EdcCardNoBox() {
        return new UIElement(By.xpath("//input[@id='cardnumber']"), getPageName(), "Card Number Box");
    }

    public UIElement EdcCardExpiryMonthBox() {
        return new UIElement(By.xpath("//input[@id='mm']"), getPageName(), "Expiry month Box");
    }

    public UIElement EdcCardExpiryYearBox() {
        return new UIElement(By.xpath("//input[@id='yy']"), getPageName(), "Expiry year Box");
    }

    public UIElement paymentOffers() {
        return new UIElement(By.xpath(
                "//div[contains(@class,'ptm-desk-offer')]/div[contains(@class,'ptm-offers-visible')]/div[2]/*/*"),
                getPageName(), "Offers Side Bar");
    }

    public UIElement EdcCardCVVBox() {
        return new UIElement(By.xpath("//input[@id='cvv']"), getPageName(), "cvv Box");
    }

    public UIElement EDCLinkPayButton() {
        return new UIElement(By.xpath("//button[contains(@class,'ptm-custom-btn ptm-hvr-pop')]"), getPageName(),
                "pay button");
    }

    public UIElement EDCLinkPayButtonNew() {
        return new UIElement(By.xpath("//button[contains(@class,'ptm-custom-btn') and contains(.,'Pay')]"),
                getPageName(), "pay button");
    }

    public UIElement EDCLinkPayButtonFSNew() {
        return new UIElement(By.xpath("//button[contains(@class,'ptm-custom-btn') and contains(.,'Pay')]"),
                getPageName(), "pay button");
    }

    // AI-Generated: 2025-09-10 - Function creation
    public UIElement EdcCardHolderNameBox() {
        return new UIElement(By.xpath("//*[@id=\"cardHolderName\"]"), getPageName(), "Card Holder Name Box");
    }

    public UIElement EnterCardHolderNameText() {
        return new UIElement(By.xpath("//p[text()='Enter the name as on the card to avail this offer']"), getPageName(),
                "Card Holder Name Text");
    }

    // AI-Generated: 2025-09-18 - Function creation
    public UIElement EdcCardNoBoxFS() {
        return new UIElement(By.xpath("//*[@id=\"cardNumber\"]"), getPageName(), "Card Number Box FS");
    }

    // AI-Generated: 2025-09-18 - Function creation
    public UIElement EdcCardExpiryMonthBoxFS() {
        return new UIElement(By.xpath("//*[@id=\"cardExpirationMonth\"]"), getPageName(), "Expiry Month Box FS");
    }

    // AI-Generated: 2025-09-18 - Function creation
    public UIElement EdcCardExpiryYearBoxFS() {
        return new UIElement(By.xpath("//*[@id=\"cardExpirationYear\"]"), getPageName(), "Expiry Year Box FS");
    }

    public void launchLoginPage(String pageUrl) {
        super.pageURL = pageUrl;
        launch();
    }

    public UIElement linkSubsStartDateDesc() {
        return new UIElement(By.xpath("//div[contains(text(),'Subscription Start Date')]"), getPageName(),
                "Subscription Start Date Text");
    }

    public UIElement linkSubsStartDate() {
        return new UIElement(By.xpath("//div[contains(text(),'Subscription Start Date')]/following-sibling::div[1]"),
                getPageName(), "Subscription Start Date");
    }

    public UIElement linkSubsExpiryDateDesc() {
        return new UIElement(By.xpath("//div[contains(text(),'Valid Till')]"), getPageName(),
                "Subscription Expiry Date Text");
    }

    public UIElement linkSubsContactMerchant() {
        return new UIElement(By.xpath("//div[contains(text(),'Contact Merchant')]"), getPageName(),
                "Contact Merchant Option");
    }

    public void openLinkAndSubmitOTP(User user, String paymentLink) {
        launchLoginPage(paymentLink);
        textBoxMobileNumber().clearAndType(user.mobNo());
        buttonSubmitLogin().click();
        submitOTP(user.mobNo());
    }

    public void submitOTP(String mobNo) {
        // String otp = Integer.toString(CommonHelpers.getRandomWithSize(6));
        String otp = Integer.toString(888888);
        textBoxOtp().clearAndType(otp);
        buttonOtpSubmit().click();
    }

    public Button invoiceProceedToPayLink() {
        return new Button(By.xpath("//div[@class='buttons']//a//button[@class='_1GEs _3D08 _smXF _KmmQ _cYMy']/.."),
                getPageName(), "Invoice Proceed Link");
    }

    public TextBox textBoxAmount() {
        return new TextBox(By.id("tAmount"), getPageName(), getPageURL());
    }

    private void changeUrlToInvoiceLink() {
        String url = invoiceProceedToPayLink().getAttribute("href");
        System.out.println("link: " + url);
        DriverManager.getDriver().get(url);
    }

    public void openLinkAndSubmitOTPForLink(User user, String paymentLink, String type, String browser) {
        launchLoginPage(paymentLink);
        if (type == "INVOICE") {
            changeUrlToInvoiceLink();
        }
        textBoxMobileNumber().clearAndType(user.mobNo());

        if (type == "GENERIC") {
            textBoxAmount().clearAndType("2000");
        }
        buttonSubmitLogin().click();
        submitOTP(user.mobNo());
    }

    public void openLink(String paymentLink, String type) {
        launchLoginPage(paymentLink);
    }

    public void fillSkipLoginForm(User user, String paymentLink, String type, String amount) {
        launchLoginPage(paymentLink);
        if (type == "INVOICE") {
            changeUrlToInvoiceLink();
        }
        skipLoginCustomerName().sendKeys("Anushka");
        skipLoginMobileNo().sendKeys("8512005349");
        skipLoginEmailId().sendKeys("anu@paytm.com");

        if (type == "GENERIC") {
            skipLoginAmount().sendKeys(amount);
        }
        skipLoginSubmitbtn().click();
    }

    public void fillPaymentForm(User user, String paymentLink, String type, String amount) {
        launchLoginPage(paymentLink);
        if (type == "INVOICE") {
            changeUrlToInvoiceLink();
        }
        paymentFormAge().sendKeys("21");
        paymentFormDOB().click();
        paymentFormAddressLine1().sendKeys("Alpha 2");
        paymentFormAddressLine2().sendKeys("Gr. Noida");
        paymentFormCity().sendKeys("GautamBudh Nagar");
        paymentFormState().sendKeys("UP");
        paymentFormPinCode().sendKeys("201308");
        paymentFormFirstName().sendKeys("Anushka");
        paymentFormLastName().sendKeys("Goldi");
        paymentFormMobNumber().sendKeys("8512005349");
        if (type == "GENERIC") {
            skipLoginAmount().sendKeys(amount);
        }
        skipLoginSubmitbtn().click();
    }

    public void openLinkAndSubmitOTPWithForms(User user, String paymentLink, String type, String formType,
            String amount) {
        if (formType.equals("SKIPLOGIN")) {
            fillSkipLoginForm(user, paymentLink, type, amount);
        }
        if (formType.equals("ALLINONE")) {
            launchLoginPage(paymentLink);
            if (type == "INVOICE") {
                changeUrlToInvoiceLink();
            }
            if (type == "GENERIC") {
                System.out.println("amount?");
                skipLoginAmount().sendKeys(amount);
                skipLoginSubmitbtn().click();
            }
        }
        if (formType.equals("PAYMENTFORM")) {
            fillPaymentForm(user, paymentLink, type, amount);
        }
    }

    public void openLinkAndSubmitOTPForSD(User user, String paymentLink, String type, String browser,
            String merchantType, String amount) {
        launchLoginPage(paymentLink);

        if (type == "INVOICE") {
            changeUrlToInvoiceLink();
        }
        if (merchantType == "SD" && acceptModel().isElementPresent()) {
            acceptModel().click();
        }
        textBoxMobileNumber().clearAndType(user.mobNo());

        if (type == "GENERIC") {
            textBoxAmount().clearAndType(amount);
        }
        buttonSubmitLogin().click();
        submitOTP(user.mobNo());
    }

    public void openLinkandSubmitOTPofmobilebinding(User user, String paymentLink, String type, String amount) {
        launchLoginPage(paymentLink);
        if (type == "INVOICE") {
            changeUrlToInvoiceLink();
        }
        if (type == "GENERIC") {
            skipLoginAmount().sendKeys(amount);
        }
        buttonSubmitLogin().click();
        submitOTP(user.mobNo());
    }

    public void openLinkofPartialPayment(User user, String paymentLink, String type, String formType, String amount) {
        launchLoginPage(paymentLink);
        if (formType.equals("PayPartialPayment")) {
            payPartialPayment().click();
            partialPaymentAmount().sendKeys(amount);
            partialPaymentSubmitBtn().click();

            if (type == "INVOICE") {
                changeUrlToInvoiceLink();
            }
            textBoxMobileNumber().clearAndType(user.mobNo());

            if (type == "GENERIC") {
                textBoxAmount().clearAndType("2000");
            }
            buttonSubmitLogin().click();
            submitOTP(user.mobNo());

        }
        if (formType.equals("PayFullPayment")) {
            payFullPayment().click();
            partialPaymentSubmitBtn().click();
            if (type == "INVOICE") {
                changeUrlToInvoiceLink();
            }
            textBoxMobileNumber().clearAndType(user.mobNo());

            if (type == "GENERIC") {
                textBoxAmount().clearAndType("2000");
            }
            buttonSubmitLogin().click();
            submitOTP(user.mobNo());
        }
    }

    public void openLinkAndSubmitOTPForLinkForTotalAmount(User user, String paymentLink, String type, String browser) {
        launchLoginPage(paymentLink);
        if (type == "INVOICE") {
            changeUrlToInvoiceLink();
        }
        textBoxMobileNumber().clearAndType(user.mobNo());

        if (type == "GENERIC") {
            textBoxAmount().clearAndType("1");
        }
        buttonSubmitLogin().click();
        submitOTP(user.mobNo());
    }

    public void OpenEdcLink(String link, User user) {
        launchLoginPage(link);
        procedToConvertEMI().click();
        textBoxMobileNumber().clearAndType(user.mobNo());
        buttonSubmitLogin().click();
        submitOTP(user.mobNo());
    }

    public void OpenEdcLink(String link) {
        launchLoginPage(link);
        procedToConvertEMI().click();

    }

    // AI-Generated: 2025-09-18 - Function creation
    public void OpenEdcLinkFS(String link) {
        launchLoginPage(link);
        proceedToPay().click();
    }

    public void OpenEdcLinkNew(String link) {
        launchLoginPage(link);
    }

    // AI-Generated: 2025-09-10 - Function creation
    public void fillEdcCardDetailsAndPay(String cardNumber) {
        EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        PaymentDTO paymentDTO = new PaymentDTO();
        EdcCardHolderNameBox().sendKeys("ABC abc");
        EdcCardNoBox().sendKeys(cardNumber);
        EdcCardExpiryMonthBox().sendKeys(paymentDTO.getExpMonth());
        EdcCardExpiryYearBox().sendKeys(paymentDTO.getExpYear().substring(2));
        EdcCardCVVBox().sendKeys(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        EDCLinkPayButton().click();
    }

    // AI-Generated: 2026-04-28 - Function creation
    public void fillEdcCardDetailsAndPayNew(String cardNumber) {
        EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(0);
        PaymentDTO paymentDTO = new PaymentDTO();
        EdcCardHolderNameBox().sendKeys("ABC abc");
        EdcCardNoBox().sendKeys(cardNumber);
        EdcCardExpiryMonthBox().sendKeys(paymentDTO.getExpMonth());
        EdcCardExpiryYearBox().sendKeys(paymentDTO.getExpYear().substring(2));
        EdcCardCVVBox().sendKeys(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        EDCLinkPayButtonNew().click();
    }

    // AI-Generated: 2025-10-17 - Bug fix
    public void StorePageFillEdcCardDetailsAndPay(String cardNumber) {
        clickCardNumberTabCenter(); // click center of card number tab
        DriverManager.getDriver().switchTo().frame(0);
        PaymentDTO paymentDTO = new PaymentDTO();
        EdcCardHolderNameBox().sendKeys("ABC abc");
        EdcCardNoBox().sendKeys(cardNumber);
        EdcCardExpiryMonthBox().sendKeys(paymentDTO.getExpMonth());
        EdcCardExpiryYearBox().sendKeys(paymentDTO.getExpYear().substring(2));
        EdcCardCVVBox().sendKeys(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        EDCLinkPayButton().click();
    }

    // AI-Generated: 2025-09-18 - Function creation
    public void fillEdcCardDetailsAndPayFS(String cardNumber) {
        EdcCardEnter().click();
        // AI-Generated: 2025-09-18 - Logic implementation
        // Try iframe switch for FS version as well
        DriverManager.getDriver().switchTo().frame(1);
        PaymentDTO paymentDTO = new PaymentDTO();
        EdcCardHolderNameBox().sendKeys("ABC abc");
        EdcCardNoBoxFS().sendKeys(cardNumber);
        EdcCardExpiryMonthBoxFS().sendKeys(paymentDTO.getExpMonth());
        EdcCardExpiryYearBoxFS().sendKeys(paymentDTO.getExpYear().substring(2));
        EdcCardCVVBox().sendKeys(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        pause(2);
        EDCLinkPayButton().click();
    }

    public void fillEdcCardDetailsAndPayFSNew(String cardNumber) {
        EdcCardEnter().click();
        DriverManager.getDriver().switchTo().frame(1);
        PaymentDTO paymentDTO = new PaymentDTO();
        EdcCardHolderNameBox().sendKeys("ABC abc");
        EdcCardNoBoxFS().sendKeys(cardNumber);
        EdcCardExpiryMonthBoxFS().sendKeys(paymentDTO.getExpMonth());
        EdcCardExpiryYearBoxFS().sendKeys(paymentDTO.getExpYear().substring(2));
        EdcCardCVVBox().sendKeys(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        pause(2);
        EDCLinkPayButtonFSNew().click();
    }

    public TextBox edcCustomerNameTextBox() {
        return new TextBox(
                By.xpath(
                        "//input[@name='customerName' and @type='text' and @placeholder='Enter your Name*' and @maxlength='30' and @autocomplete='off']"),
                getPageName(),
                "edc-customer-name-field");
    }

    public TextBox edcMobileNoTextBox() {
        return new TextBox(
                By.xpath(
                        "//input[@name='mobileNo' and @type='tel' and @placeholder='Enter your Mobile Number*' and @maxlength='15' and @autocomplete='off']"),
                getPageName(),
                "edc-mobile-no-field");
    }

    public TextBox edcEmailIdTextBox() {
        return new TextBox(
                By.xpath(
                        "//input[@name='emailId' and @type='text' and @placeholder='Enter your Email ID' and @maxlength='30' and @autocomplete='off']"),
                getPageName(),
                "edc-email-id-field");
    }

    public Button buttonProceedToPayWithAmount() {
        return new Button(
                By.xpath("//button[@type='submit' and contains(.,'Proceed to Pay') and .//span[@id='rupee']]"),
                getPageName(),
                "Proceed to Pay submit with amount");
    }
}
