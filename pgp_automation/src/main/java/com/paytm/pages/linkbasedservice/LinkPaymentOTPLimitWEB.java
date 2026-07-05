package com.paytm.pages.linkbasedservice;

import com.paytm.base.test.User;
import com.paytm.framework.ui.element.TextBox;
import org.openqa.selenium.By;

public class LinkPaymentOTPLimitWEB extends LinkPaymentOTPLimit {

    public LinkPaymentOTPLimitWEB() {
        super("link-otp-limit-page-enhanced-web-theme");
    }

    public TextBox textBoxMobileNumber() {
        return new TextBox(By.cssSelector("#mobileNumberDesktop.form-ctrl"), getPageName(), "mobile no");
    }

    public void enterUserAndAmount(User user, String txnAmount) {
        textBoxMobileNumber().clearAndType(user.mobNo());
        textBoxAmount().clearAndType(txnAmount);
            buttonSubmitLogin().click();

    }

}
