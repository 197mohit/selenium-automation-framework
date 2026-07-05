package com.paytm.pages;

import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.PopUpV2;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;
import org.testng.Assert;

public class DirectBankOtpPageWap extends DirectBankOTPPage {

    public UIElement ResendOTPLink()
    {
        return new UIElement(By.xpath("//*[text()='Request Another OTP'] | //*[text()='Resend OTP'] | //*[text()='Resend']"),getPageName(),"Resend OTP");
    }

    public UIElement GoToBankWebsiteLink()
    {
        return new UIElement(By.cssSelector("._VkVw ._eG4r"),getPageName(),"Go to Bank website"){
            @Override
            public void assertVisible()
            {
                super.waitUntilVisible();
                super.assertVisible();
            }
        };
    }

    public UIElement RequestMessage()
    {
        return new UIElement(By.xpath("//p[contains(text(),'sent')  and contains(@class,'_1VcH ')] | //img[contains(@src,'visaverified.png')]/../p"),getPageName(),"Request Message Text");
    }


    public void VerifyErrorMessage(String Message) {
        String error =null;
        error = errorMessage().getText();
        Assert.assertEquals(error,Message);

    }

    public UIElement errorMessage(){
        return new UIElement(By.className("_1AAl"),getPageName(),"Request Message Text");
    }

    public UIElement RequestOTPTimer(){
        return new UIElement(By.xpath("//*[text()='You can request another OTP in']"),getPageName(),"Request OTP Timer");
    }

    public UIElement BankLogo() {
        return new UIElement(By.cssSelector("._2_R2 ._t6VL"), getPageName(), "Bank Logo");
    }

    public PopUpV2 modalCancelPayment(){
        return new PopUpV2(By.cssSelector("._1mqY.pos-r"), "cancel-payment-modal") {

            @Override
            public void accept() {
                Button buttonOther = new Button(By.xpath("//label[text()='Other']"),getPageName(),"Other button");
                buttonOther.waitUntilClickable();
                buttonOther.click();
                Button buttonSubmit = new Button(By.xpath("//button[text()='Submit']"), getPageName(), "cancel-payment-modal-accept-button");
                buttonSubmit.waitUntilClickable();
                buttonSubmit.click();
            }

            @Override
            public void close() {
                Button buttonNo = new Button(By.xpath("//*[@id='closeNo' or @class='blue-btn']"), getPageName(), "cancel-payment-modal-close-button");
                buttonNo.waitUntilClickable();
                buttonNo.click();
            }
        };
    }

    }
