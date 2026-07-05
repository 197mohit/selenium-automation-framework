package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Link;
import com.paytm.framework.ui.element.TextBox;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class OTPSlider extends BasePage {

    public OTPSlider() {
        super("OTPSlider Page");
    }

    public UIElement HeaderMsg()
    {
        return new UIElement(By.xpath("//*[text()='Complete Payment']"),getPageName(),"OTP Slider Header");
    }

    public UIElement CloseButton()
    {
        return new UIElement(By.xpath("//*[text()='Complete Payment']/preceding-sibling::img"),getPageName(),"Close Button");
    }

    public UIElement FooterMsg()
    {
        return new UIElement(By.xpath("//*[text()=\"Complete Payment\"]/following-sibling::h4"),getPageName(),"Footer Message");
    }
    public UIElement ResendButton()
    {
        return new UIElement(By.xpath("//*[text()=\"Resend OTP\"]"),getPageName(),"Resend Button");
    }

    public UIElement SubmitButton()
    {
        return new UIElement(By.xpath("//*[text()=\"Submit\"]"),getPageName(),"Submit Button");
    }

    public UIElement OTPField()
    {
        return new UIElement(By.xpath("//*[text()=\"Enter OTP\"]"),getPageName(),"OTPField");
    }
    public TextBox otpBox() {
        return new TextBox(By.xpath("//input[@type='tel' and @maxlength='6' or @id='otp']"), getPageName(), "otp-field");
    }

    public void submitOtp(String otp) {
        this.otpBox().sendKeys(otp);
        this.SubmitButton().click();
    }
    public UIElement RequestMessage(){
        return new UIElement(By.xpath("//p[contains(text(),'sent')]"),getPageName(),"Request Message Text");
    }

    public void VerifyRequestMsg(String Message){
        RequestMessage().waitUntilVisible();
        RequestMessage().assertText(Message);
    }

    public Link cancelPayment()
    {
        return new Link(By.xpath("//a[text()='Cancel Payment']"),getPageName(),"cancel-payment");
    }

    public String cancelPaymentAlignment()
    {
        return new UIElement(By.xpath("//a[text()='Cancel Payment']/parent::div"),getPageName(),"alignment-cancel-payment").getAttribute("style");
    }

    public UIElement crossIcon()
    {
        return new UIElement(By.cssSelector(".popup-global img"),getPageName(),"close-button-feeedback");
    }

}
