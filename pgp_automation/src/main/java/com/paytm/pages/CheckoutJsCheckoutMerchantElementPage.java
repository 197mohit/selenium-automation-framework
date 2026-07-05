package com.paytm.pages;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.element.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CheckoutJsCheckoutMerchantElementPage extends CheckoutJsCheckoutPage {

    private Report report;
    private boolean deleteCookie = true;
    private static String CHECKOUTJS_LOAD_URL = "https://pgp-automation.paytm.in/merchantpgpui/checkoutjs/merchants/{mid}";

    public CheckoutJsCheckoutMerchantElementPage() {
        this.pageURL = LocalConfig.CHECKOUTJS_MERCHANT_ELEMENT_URL;
        this.report = Reporter.report;
    }

    @Override
    protected Button initializeCheckoutJs() {
        return new Button(By.id("init-element"), getPageName(), "Initialize checkoutjs button");
    }

    @Override
    protected TextBox mconfig() {
        return new TextBox(By.xpath("//textarea[@id='txt-area-mconfig']"), getPageName(), "mconfig textbox");
    }

    private UIElement createElementSection() {
        return new UIElement(By.id("elements"), getPageName(), "createElement Section");
    }

    private Select paymodeDropdown() {
        return new Select(By.id("ddl-create-element"), getPageName(), "paymodeDropdown");
    }

    private Button closeElementButton() {
        return new Button(By.xpath("//button[text()='Close']"), getPageName(), "close elementButton");
    }

    private List<Button> closeElementButtons() {
        return UIElements.getButtons(By.xpath("//button[text()='Close']"), getPageName(), "close elementButtons");
    }

    private Button createElementButton(){
        return new Button(By.id("btn-create-element"), getPageName(), "createElementButton");
    }

    private Button invokeElementButton(){
        return new Button(By.xpath("//div[@id='created-elements']//button[text()='Invoke']"), getPageName(), "invokeElementButton");
    }

    @Override
    public void createCheckoutJsOrder(MerchantConfig merchantConfig) {
        String config = merchantConfig.toString();
        int retryInitialize = 3;
        mconfig().clearAndType(config);
        pause(1);
        initializeCheckoutJs().click();
        pause(8);
        DriverManager.getWebDriverElementWait().until(
                ExpectedConditions.visibilityOf(createElementSection())
        );
    }

    public void createAndInvokePaymode(String payMode) {
        //closeAllInvokedPaymodes();
        pause(5);
        selectPaymodeFromDropDown(payMode);
        pause(1);
        createElementButton().click();
        pause(1);
        invokeElementButton().click();
    }

    private void closeAllInvokedPaymodes() {
        closeElementButtons().forEach(UIElement::click);
    }

    public void selectPaymodeFromDropDown(String payMode) {
        switch (payMode) {
            case "CC":
            case "DC":
            case "SAVED_CARD":
                paymodeDropdown().selectByValue("CARD");
                break;
            case "EMI":
                paymodeDropdown().selectByValue("EMI");
                break;
            case "NB":
                paymodeDropdown().selectByValue("NB");
                break;
            case "UPI":
                paymodeDropdown().selectByValue("UPI");
                break;
            case "PAY WITH PAYTM":
                paymodeDropdown().selectByValue("PAY WITH PAYTM");
                break;
            case "SCAN AND PAY":
                paymodeDropdown().selectByValue("SCAN AND PAY");
        }
    }
}
