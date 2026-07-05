package com.paytm.pages;

import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Table;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.ui.element.UIElements;
import org.openqa.selenium.By;

public class OdishaPaymentPage extends BasePage {


    public OdishaPaymentPage() {
        super("Odisha Payment Page");
    }


    public Table tableOdishaPaymentForm() {
        return new Table(By.xpath(""), getPageName(), "") {
            @Override
            public int getRowCount() {
                return UIElements.getMultiple(By.cssSelector("div.oi-pd-wrap>div"), getPageName(), "Subscription Details").size();
            }

            public String getRowValue(String rowName) {
                return new UIElement(By.xpath("//div[text()='" + rowName + ":']//following-sibling::div"), getPageName(), "Subscription Details "+ rowName).getText();
            }
        };
    }


    public UIElement downloadRecieptButton() {
        return new UIElement(By.xpath("//*[@id='downloadReceipt']"), getPageName(), "confirmButton-text");
    }


    public UIElement printRecieptButton() {
        return new UIElement(By.xpath("//*[@id='printReceipt']"), getPageName(), "confirmButton-text");
    }


    public UIElement nextScreenTimer() {
        return new UIElement(By.cssSelector("strong#timer"), getPageName(), "confirmButton-text");
    }

    public UIElement clickhereLinkButton() {
        return new UIElement(By.linkText("click here"), getPageName(), "confirmButton-text");
    }

    public enum odishaPaymentDetails {

        DEPOSITOR_NAME("Name of Depositor"),
        CHALLAN_REF_NO("Treasury Challan Reference Number"),
        BANK_TRANSACTION_NO("Bank Transaction Number"),
        BANK_TRANSACTION_DATE("Bank Transaction Date & Time"),
        CHALLAN_AMOUNT("Total Challan Amount");

        private final String paymentDetails;

        odishaPaymentDetails(String paymentDetails) {
            this.paymentDetails = paymentDetails;
        }

        @Override
        public String toString() {
            return paymentDetails;
        }

    }


}
