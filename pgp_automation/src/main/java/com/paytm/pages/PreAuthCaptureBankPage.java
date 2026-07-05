package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class PreAuthCaptureBankPage extends BasePage {

    private boolean deleteCookie = true;

    public PreAuthCaptureBankPage() {
        super("PreAuthCapture Bank Page");
    }

    public boolean isDeleteCookie() {
        return deleteCookie;
    }

    public void setDeleteCookie(boolean deleteCookie) {
        this.deleteCookie = deleteCookie;
    }

    public UIElement submitButton_PreAuthCaptureTxn() {
        return new Button(By.xpath("//button[@type='submit']"), getPageName(), "Submit-Button");
    }
}
