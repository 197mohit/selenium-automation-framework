package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.*;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DirectBankOTPPage extends BasePage {

    public DirectBankOTPPage() {
        super("direct-bank-otp-page");
    }

    public TextBox otpBox() {
        return new TextBox(By.xpath("//input[@type='tel' and @maxlength='6' or @id='otp']"), getPageName(), "otp-field");
    }

    public Button confirmPayment() {
  //      return new Button(By.cssSelector(".pay-btn"), getPageName(), "submit-button");
        return new Button(By.xpath("//button[contains(text(),'Submit') or @id='pay' or @id='confirmPayment']"), getPageName(), "submit-button");
    }

    public Button cancel() {

        return new Button(By.xpath("//button[contains(text(),'Cancel') or @id='cancel'] | //a[@id='cancel']"), getPageName(), "cancel-button") {
            @Override
            public void click() {
                this.waitUntilClickable();
                super.click();
            }
        };
    }

    public void assertVisible(){
        this.otpBox().assertVisible();
    }

    public void submitOtp(String otp) {
        this.otpBox().clearAndType(otp);
        this.confirmPayment().click();
    }

    public void cancelAtOtp(String otp) {
        this.otpBox().sendKeys(otp);
        this.cancel().click();
    }

    public PopUpV2 modalCancelPayment() {
        return new PopUpV2(By.className("modal-main"), "cancel-payment-modal") {
            @Override
            public void accept() {
                Button buttonYes = new Button(By.xpath("//*[@id='closeYes' or @class='bordered-btn']"), getPageName(), "cancel-payment-modal-accept-button");
                buttonYes.waitUntilClickable();
                buttonYes.click();
            }

            @Override
            public void close() {
                Button buttonNo = new Button(By.xpath("//*[@id='closeNo' or @class='blue-btn']"), getPageName(), "cancel-payment-modal-close-button");
                buttonNo.waitUntilClickable();
                buttonNo.click();
            }
        };
    }

    @Override
    public void waitUntilLoads() {
        try {
            WebDriver driver = DriverManager.getDriver();
            Awaitility.await().ignoreException(NoSuchElementException.class).atMost(ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME.toSeconds(), TimeUnit.SECONDS).until(() -> driver.findElement(By.xpath("//*[text()='Complete Payment'] | //*[contains(text(),'Complete the Payment on the Bank website')]")).isDisplayed());
        } catch (ConditionTimeoutException e) {
            throw new TimeoutException(MessageFormat.format("Waited for {0} to be loaded but is not", this), e);
        }
    }

    public UIElement RequestMessage(){
        return new UIElement(By.xpath("//img[contains(@src,'visaverified.png')]/preceding-sibling::p | //p[@id='message'] | //img[contains(@src,'visaverified.png')]/../p"),getPageName(),"Request Message Text");
        //return new UIElement(By.id("message"),getPageName(),"Request Message Text");
    }

    public void VerifyRequestMsg(String Message){
        RequestMessage().waitUntilVisible();
        RequestMessage().assertText(Message);
    }

    public UIElement ResendOTPLink(){
        return new UIElement(By.xpath("//*[text()='Resend OTP'] | //*[@id='resendOTP'] | //*[@id='Resend'] | //*[text()='Resend'] "),getPageName(),"Resend OTP");
    }

    //TODO: locator changed for enhancedWap
    public UIElement GoToBankWebsiteLink(){
        return new UIElement(By.id("gotoBankWebsite"),getPageName(),"Go to Bank website");
    }

    public UIElement errorMessage(){
        return new UIElement(By.id("otpInvalid"),getPageName(),"Request Message Text");
    }

    public void VerifyErrorMessage(String Message) {
        String error =null;

            error = errorMessage().getText();
            Assert.assertEquals(error,Message);

    }
    public UIElement BankName() {
        return new UIElement(By.xpath("//*[@id='bankName']"), getPageName(), "Bank Name");
    }

    public UIElement BankLogo() {
        pause(1);
        return new UIElement(By.xpath("//*[@id=\"bankImg\"]"), getPageName(), "Bank Logo");
    }

    public void assertBankLogo(String bankName) {
        pause(1);
        new UIElement(By.xpath("//img[contains(@src,'"+bankName+"')]"), getPageName(), bankName +" Logo").assertVisible();
    }
    public String getAmountWithCurrency()
    {
        return new UIElement(By.cssSelector(".sop .fr"),getPageName(),"txn-amount").getText();
    }

    public String getOtpTextboxPlaceholder()
    {
        return new TextBox(By.xpath("//input[@type='tel' and @maxlength='6' or @id='otp']/following-sibling::p"),getPageName(),"otp-missing").getText();
    }

    public String getOTPTextboxErrorMessage()
    {
        return new TextBox(By.xpath("//input[@type='tel' and @maxlength='6' or @id='otp']/preceding-sibling::p"),getPageName(),"otp-missing").getText();
    }

    public String fetchExpiryTime()
    {
        return new UIElement(By.cssSelector("._2zwl p._55bk"),getPageName(),"expiry min and seconds").getText();
    }

    public UIElement alertExpiry()
    {
        return new UIElement(By.cssSelector("._1VcH p._55bk"),getPageName(),"alertExpiry");
    }

    public List<UIElement> captureFeedback()
    {
        return UIElements.getMultiple(By.xpath("//ul/li/label"),getPageName(),"feedbacks");
    }

    public Link cancelPayment()
    {
        return new Link(By.xpath("//a[text()='Cancel Payment']"),getPageName(),"cancel-payment");
    }

    public String cancelPaymentAlignment()
    {
        return new UIElement(By.xpath("//a[text()='Cancel Payment']/parent::div"),getPageName(),"alignment-cancel-payment").getAttribute("style");
    }

    public UIElement closeButton()
    {
        return new UIElement(By.cssSelector(".popup-global img"),getPageName(),"close-button");
    }

    @Override
    public String toString() {
        return this.pageName;
    }

    public UIElement RequestOTPTimer() {
        return null;
    }

    public TextBox findOtpBox() {
        return new TextBox(By.xpath("//input[@type='text']"), getPageName(), "opt-box-field");
    }

    public Button clickOnSubmit() {
        return new Button(By.xpath("//input[@type='submit']"), getPageName(), "click-submit-button");
    }
}
