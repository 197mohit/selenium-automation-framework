package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.Table;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.ui.element.UIElements;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.List;

public class BankMandateRevampPage extends BankMandatePage {


    public BankMandateRevampPage() {
        super();
        setPageName("Bank-Mandate-Revamp-Subscription-page");
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
        return new UIElement(By.xpath("//*[@class='ptm-lock-img']"), getPageName(), "Activate-Subscription-Button");
    }

    //
    public UIElement payButton() {
        pause(2);
        return new UIElement(By.xpath("//*[@class='textItem _yocA']"),getPageName(), "Pay-Button");
    }
    public UIElement payToSubscribe(){
        pause(1);
        return new UIElement(By.xpath("//button[contains(@class,'btn-primary')]"),getPageName(), "pay-subscription-Button");
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


}
