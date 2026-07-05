package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.utils.merchant.util.AuthUtil;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RiskVerificationPage extends BasePage {
    private static String RiskPwd="password";


    public RiskVerificationPage(){
        super("RiskVerificationPage");
    }

    private UIElement fillPassword(){
        return new UIElement(By.id(RiskPwd),getPageName(),"PaytmPassword");
    }
    private UIElement acceptAlert(){
        return new UIElement(By.xpath("//button[@id='form_button']"), getPageName(), "Alert");
    }
    private UIElement backButton(){
        return new UIElement(By.xpath("//img[@alt='Back']"), getPageName(), "Alert");
    }
    private UIElement cancelTxn(){
        return new UIElement(By.xpath("//button[@class='btn light']"), getPageName(),"CancelTxn");
    }
    private UIElement otptextbox(){
        return new UIElement(By.xpath("//input[@id='input_otp']"),getPageName(),"OTPSMS");
    }

    private UIElement payPass(){
        return new UIElement(By.xpath("//button[@id='proceed']"),getPageName(),"Paytm Password");
    }
    private UIElement submitotp(){
        return new UIElement(By.id("form_button"),getPageName(),"");
    }

    public void fillPwd(String Password){
        DriverManager.getDriver().switchTo().frame(0);
        fillPassword().sendKeys(Password);
        payPass().click();
    }
    public void clickAlert()
    {
        DriverManager.getWebDriverElementWait().until(ExpectedConditions.alertIsPresent());
//        new WebDriverWait(DriverManager.getDriver(),80).until(ExpectedConditions.alertIsPresent());
        Alert alert=DriverManager.getDriver().switchTo().alert();
        alert.accept();
    }
    public void cancelAlert(){
        backButton().click();
        cancelTxn().click();
    }
    public void enterSmsOtp(String phoneNumber){
       // otptextbox().sendKeys(AuthUtil.getOtp(phoneNumber));
        otptextbox().sendKeys("123456");
        submitotp().click();
    }
    public void enterMailOtp(String emailAddress){
        otptextbox().sendKeys(AuthUtil.getOtpFromEmail(emailAddress));
        submitotp().click();
    }
}
