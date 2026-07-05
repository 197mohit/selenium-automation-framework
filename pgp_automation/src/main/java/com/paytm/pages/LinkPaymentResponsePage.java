package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class LinkPaymentResponsePage extends BasePage {

    public LinkPaymentResponsePage(){
        super("Link Payment Response Page");
    }

    public UIElement imageSuccess(){
        return  new UIElement(By.xpath("//img[@src='https://pgp-automation4.paytm.in/5/images/group8.png']"),getPageName(),"Payment Success image");
    }

    public UIElement imageFail(){
        return  new UIElement(By.xpath("//img[@src='https://pgp-automation2.paytm.in/5/images/icFailed.png']"),getPageName(),"Payment Failure Image");
    }

    public UIElement imagePending(){
        return  new UIElement(By.xpath("//img[@src='https://pgp-automation4.paytm.in/5/images/icPending.png']"),getPageName(),"Payment Pending Image");
    }

    public UIElement textSuccessMessage(){
        return  new UIElement(By.xpath("//img[@class='web-icon']/.."),getPageName(),"Success message");
    }

    public UIElement textFailureMessage(){
        return  new UIElement(By.xpath("//p[@class='content']"),getPageName(),"Failure message");
    }

    public UIElement textPendingMessage(){
        return  new UIElement(By.xpath("//p[@class='content']"),getPageName(),"Pending message");
    }

    public UIElement textMerchantName(){
        return  new UIElement(By.id("merchantName"),getPageName(),"merchant name");
    }

    public UIElement textTxnAmount(){
        return  new UIElement(By.xpath("//div[@class='tAmount']"),getPageName(),"Txn Amount");
    }

    public UIElement textTransactionId(){
        return  new UIElement(By.xpath("//div[contains(text(),'Transaction ID')]"),getPageName(),"Transaction Id");
    }

    public String getTransactionId(){
        String fullString = textTransactionId().getText();
        String[] arrString = fullString.split(" ");
        String txnId = arrString[2];
        return txnId;
    }

    @Override
    public void waitUntilLoads() {
        imageSuccess().waitUntilVisible();
    }
}
