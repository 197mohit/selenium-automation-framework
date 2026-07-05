package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.RadioButton;
import com.paytm.framework.ui.element.TextBox;
import org.openqa.selenium.By;

public class BankPage extends BasePage {

    public BankPage() {
        super("HDFC Bank Page");
    }

    public Button SubmitButton() {
        return new Button(By.id("submitBtn"), getPageName(), "SubmitButton");
    }

    private Button otpCancel() {
        return new Button(By.id("cmdSubmit"), getPageName(), "otpCancel");
    }

    public void submitHDFCTxn() {
        new RadioButton(By.cssSelector("input[value='payerAuth']"), getPageName()).click();
        new Button(By.id("submitBtn"), getPageName()).click();
        new TextBox(By.id("txtPassword"), getPageName()).sendKeys("indu@123");
        new Button(By.cssSelector("#cmdSubmit[type='submit']"), getPageName()).click();
        waitUntilLoads();
    }

    public void cancelTxn() {
        otpCancel().click();
        DriverManager.getDriver().switchTo().alert().accept();
        waitUntilLoads();
    }


}
