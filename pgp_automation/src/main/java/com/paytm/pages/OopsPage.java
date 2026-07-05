package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class OopsPage extends BasePage {

    public OopsPage() {
        super("Oops Page");
    }

    public UIElement imgOops() {
        return new UIElement(By.cssSelector(".white_container.width-pad>div>img"), getPageName(), "imgOops");
    }

    @Override
    public void waitUntilLoads() {
        imgOops().waitUntilVisible();
    }

}
