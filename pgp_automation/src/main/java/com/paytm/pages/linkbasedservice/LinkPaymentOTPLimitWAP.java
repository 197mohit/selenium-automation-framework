package com.paytm.pages.linkbasedservice;

import com.paytm.base.test.User;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.TextBox;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.utils.merchant.util.AuthUtil;
import org.openqa.selenium.By;

public class LinkPaymentOTPLimitWAP extends LinkPaymentOTPLimit {

    public LinkPaymentOTPLimitWAP() {
        super("link-otp-limit-page-enhanced-wap-theme");
    }

    public TextBox textBoxMobileNumber() {
        return new TextBox(By.cssSelector("#mobileNumberMobile.form-ctrl"), getPageName(), "mobile no");
    }

    public void enterUserAndAmount(User user, String txnAmount) {

        textBoxAmount().clearAndType(txnAmount);
        pause(1);
        textBoxMobileNumber().clearAndType(user.mobNo());
        buttonSubmitLogin().click();

    }

}
