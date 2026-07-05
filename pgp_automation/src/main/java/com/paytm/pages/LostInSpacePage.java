package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class LostInSpacePage extends BasePage {

    public LostInSpacePage() {
        super("Lost in Space Page");
    }

    public UIElement imgLostInSpace() {
        return new UIElement(By.cssSelector("div#bodymovin"), getPageName(), "imgLostInSpace");
    }
    //  //div[@class='wrapper']/h5[ contains(text(),'You are lost in Space.')]

    public UIElement txtLostInSpace() {
        return new UIElement(By.xpath("//div[@class='wrapper']/h5[ contains(text(),'You are lost in Space.')]"), getPageName(), "txtLostInSpace");
    }

    @Override
    public void waitUntilLoads() {
        imgLostInSpace().waitUntilVisible();
    }

}
