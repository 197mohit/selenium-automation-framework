package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.TextBox;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class IntermediateOTPPage extends BasePage {

    public IntermediateOTPPage(String pageName) {
        super(pageName);
    }

    public UIElement otpPageSubmitButton()
    {
        return new UIElement(By.xpath("//input[@value='Submit']"),getPageName(),"submit-btn");
    }

    public void otpPageTextbox(String otp)
    {
        new TextBox(By.xpath("//input[@name='customerpin']"),getPageName(),"otp-textbox").clearAndType(otp);
    }


}
