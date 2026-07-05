package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class UpiConsent extends BasePage {

    public UpiConsent(){
        super("UpiConsent");
    }

    private UIElement pushElement1(){
        return new UIElement(By.xpath("//*[@id=\"app\"]/main/div[2]/div[3]/section[2]/section/div[1]/div/span"), getPageName(), "upiPush");
    }

    private UIElement pushElement2(){
        return new UIElement(By.xpath("//*[@id=\"app\"]/main/div[2]/div[3]/section[2]/section/div[1]/div/span"), getPageName(), "upiPush");
    }

    private UIElement pushElement3(){
        return new UIElement(By.xpath("//*[@id=\"app\"]/main/div[2]/div[3]/section[3]/section/div[1]/div/label/input"), getPageName(), "upiPush");
    }

    private UIElement bhimUpi(){
        return new UIElement(By.xpath("//*[@id=\"app\"]/main/div[2]/div[3]/section[7]/section/div[1]/div/label/input"), getPageName(), "upiPush");
    }

    private UIElement upiLimitMsg(){
        return new UIElement(By.xpath("//*[@id=\"app\"]/main/div[2]/div[3]/section[2]/section/div[2]/div/p"), getPageName(), "upiLimitMsg");
    }

    private UIElement walletCheckbox(){
        return new UIElement(By.xpath("//*[@id=\"checkbox\"]"), getPageName(), "walletCheckBox");
    }

    private UIElement bankDownMsgElement(){
        return new UIElement(By.xpath("//*[@id=\"app\"]/main/div[2]/div[3]/section[3]/section/div[2]/div/p"), getPageName(), "bankDownMsgElement");
    }

    private UIElement npciDownMsgElement(){
        return new UIElement(By.xpath("//*[@id=\"app\"]/main/div[2]/div[3]/section[7]/section/div[2]/div/p"), getPageName(), "npciDownMsgElement");
    }

    public void selectUpiPush()
    {
        pushElement2().click();
    }

    public String getLimitMsg()
    {
        String msg = "";
        Boolean ispresent = upiLimitMsg().isElementPresent();
        if(ispresent)
        {
            msg = upiLimitMsg().getText();
            return msg;
        }
            msg = "false";
            return msg;
    }

    public void uncheckWallet(){ walletCheckbox().click(); }

    public String isUpiEnabled()
    {
        if(pushElement1().isEnabled() && pushElement2().isEnabled() && pushElement3().isEnabled() && bhimUpi().isEnabled())
        {
            return "false";
        }
        return "true";
    }

    public String isBankDown()
    {
        if(!pushElement1().isEnabled() && !pushElement2().isEnabled() && pushElement3().isEnabled() && !bhimUpi().isEnabled())
        {
            return "false";
        }
        return "true";
    }

    public String npciDownMsg(){
        bhimUpi().click();
        return npciDownMsgElement().getText();
    }

    public String bankDownMsg(){
        pushElement3().click();
        return bankDownMsgElement().getText();
    }
}
