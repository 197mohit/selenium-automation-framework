package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.TextBox;
import org.openqa.selenium.By;

public class NativePlusHoldpayPage extends BasePage {

    public NativePlusHoldpayPage() {
        super("Native Plus hold and pay");
    }

    public NativePlusHoldpayPage launch(String url) {
        DriverManager.getDriver().get(url);
        return this;
    }

    public String fillJsonForm(String jsonString)
    {
        return "document.getElementById('jsonValue').value='"+jsonString+"';";

    }

    public Button submitButton() {
        return new Button(By.id("submitBtn"), getPageName(), "submitButton");
    }

    public void fillAndSubmitJsonForm(String jsonString) {
        executeJavaScript(fillJsonForm(jsonString));
        submitButton().click();
        waitUntilLoads();
    }
}
