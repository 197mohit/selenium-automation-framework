package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.Table;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.ui.element.UIElements;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.List;

public class BankMandatePage extends BasePage {


    public BankMandatePage() {
        super("Bank-Mandate-Subscription-page");
    }



    public Table tableSubscriptionForm() {
        return new Table(By.xpath(""), getPageName(), "") {
            @Override
            public int getRowCount() {
                return UIElements.getMultiple(By.xpath("//div[text()='Subscription Details']//following-sibling::div/div[1]"), getPageName(), "Subscription Details").size();
            }

            public String getRowValue(String rowName) {
                return new UIElement(By.xpath("//div[text()='" + rowName + "']//following-sibling::div"), getPageName(), "Subscription Details "+ rowName).getText();
            }
        };
    }

    public Button proceedBtn() {
        return new Button(By.xpath("//button[text()='Proceed']"), getPageName(), "proceed-button");
    }

    public UIElement confirmButton() {
        pause(2);
        return new UIElement(By.xpath("//*[@class='ptm-custom-btn' and text()='Confirm']"), getPageName(), "confirmButton-text");
    }

    public UIElement payButton() {
        pause(2);
        return new UIElement(By.xpath("//*[@class='ptm-lock-img']"),getPageName(), "Pay-Button");
    }

    public UIElement activateSubscription() {
        pause(2);
        //return new UIElement(By.xpath("//*[text()='Activate Subscription']"),getPageName(), "activate-subscription-Button");
        return new UIElement(By.xpath("//*[text()='Activate Subscription']"),getPageName(), "activate-subscription-Button");
    }

    public UIElement payToSubscribe(){
        pause(5);
        return new UIElement(By.xpath("//button[contains(@class,'btn-primary')]"),getPageName(), "pay-subscription-Button");
    }

    public UIElement saveSubscribe(){
        pause(2);
        return new UIElement(By.xpath("//span/img[@class='_3D4s pos-r']"),getPageName(), "pay-subscription-Button");
    }

    public enum subscriptionDetails {

        CUSTOMER_NAME("Customer Name"),
        FREQUENCY("Frequency"),
        MAX_AMOUNT("Recurring Bill Amount*"),
        START_DATE("Start Date"),
        EXPIRY_DATE("Expiry Date"),
        PURPOSE("Purpose");

        private final String SubsDetail;

        subscriptionDetails(String SubsDetail) {
            this.SubsDetail = SubsDetail;
        }

        @Override
        public String toString() {
            return SubsDetail;
        }

    }
    public UIElement bankMandateText(){
        return new UIElement(By.xpath("//div[@class='_25dh plr-16']//*[@class='_1DMl fs12']"), getPageName(), "BM Text");
    }
    public UIElement debitAmountText(){
            return new UIElement(By.xpath("//div[text()='Savings Account']/following::span[5]"), getPageName(), "AT");
    }
    public UIElement bankMandateAdvisoryText() {
        return new UIElement(By.xpath("//div[text()='Savings Account']/following::div[2]"), getPageName(), "Advisory Text");
    }
}
