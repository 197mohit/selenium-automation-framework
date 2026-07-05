package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.UIElement;
import org.openqa.selenium.By;

public class DCCPaymentReceiptPage extends BasePage {

    public DCCPaymentReceiptPage() {
        super("Dcc Express Page");
    }
    public UIElement emailReceiptButton(){
        return new UIElement(By.xpath("//button[@id='email-cta-btn']"),getPageName(),"Email Receipt Button");
    }

    public UIElement emailOverlayTitle(){
        return new UIElement(By.xpath("//div[@id='emailOverlay']//p[@class='title']"),getPageName(),"Title of overlay");
    }

    public UIElement emailOverlaySubTitle(){
        return new UIElement(By.xpath("//div[@id='emailOverlay']//p[@class='subtitle']"),getPageName(),"SubTitle of overlay");
    }

    public UIElement emailTextBox(){
        return new UIElement(By.xpath("//div[@id='emailOverlay']//input[@id='emailAddr']"),getPageName(),"Email Text Box");
    }

    public Button sendEmailButton(){
        return new Button(By.xpath("//div[@id='emailOverlay']//button[@class='mail-btn']"),getPageName(),"Email Receipt Button");
    }

    public UIElement emailError(){
        return new UIElement(By.xpath("//div[@id='emailOverlay']//div[@class='input-group']//p[@id='mailError']"),getPageName(),"Invalid email error");
    }

    public void enterMailIdandSendButton(String emailId){
        emailTextBox().click();
        emailTextBox().sendKeys(emailId);
        sendEmailButton().click();
    }

}
