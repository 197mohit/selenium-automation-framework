package com.paytm.pages.linkbasedservice;

import com.paytm.base.test.User;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.TextBox;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.utils.merchant.util.AuthUtil;
import org.openqa.selenium.By;

public abstract class LinkPaymentOTPLimit extends BasePage {

    public LinkPaymentOTPLimit(String pageName) {
       super(pageName);
    }

    public abstract TextBox textBoxMobileNumber();

    public abstract void enterUserAndAmount(User user, String txnAmount);

    public TextBox textBoxAmount() {
        return new TextBox(By.cssSelector("#tAmount.form-ctrl"), getPageName(), "amount");
    }

    public Button buttonSubmitLogin() {
        return new Button(By.id("proceedToPay"), getPageName(), "Pay button");
    }

    public TextBox textBoxOtp() {
        return new TextBox(By.cssSelector("#otp.form-ctrl"), getPageName(), "OTP box");
    }

    public Button buttonOtpSubmit() {
        this.waitUntilLoads();
        return new Button(By.xpath("//span[text()='Verify']"), getPageName(), "OTP submit");
    }

    public void enterCorrectOTP(User user)
    {
        String otp =  AuthUtil.getOtp(user.mobNo());
        textBoxOtp().clearAndType(otp);
        buttonOtpSubmit().click();

    }

    public String fetchOrderId()
    {
        String oId  = DriverManager.getDriver().findElement(By.cssSelector("div.c_dark .f-17")).getText();
        oId = oId.replace("Order ID: ","");
        oId = oId.substring(0,8).trim() + oId.substring(8).trim();
        return oId;
    }

    public void enterOTP(String otp) {
        textBoxOtp().clearAndType(otp);
        buttonOtpSubmit().click();

    }

    public UIElement changeNumber()
    {
        return new UIElement(By.cssSelector("#changeNumber.link-btn"),getPageName(),"change-number");
    }

    public UIElement requestAnotherOTP()
    {
      return new UIElement(By.cssSelector("#resendBtn.link-btn"),getPageName(),"request-otp");
    }

    public UIElement errorMessage() {
        pause(4);
        return new UIElement(By.cssSelector("#otpSubmitErr.error"), getPageName(), "error-msg");

    }

    public void launchLoginPage(String pageUrl) {
        super.pageURL = pageUrl;
        launch();
    }


}
