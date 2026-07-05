package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.PopUpV2;
import com.paytm.framework.ui.element.TextBox;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;


import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by anjukumari on 24/03/18
 */
public class BajajFinservBankPage extends BasePage {

    public BajajFinservBankPage() {
        super("bajaj-finserv-bank-page");
//        waitUntilLoads();
    }

    public TextBox otpTextBox() {
        return new TextBox(By.id("otp"), getPageName(), "otp-field");
    }

    public Button submitButton() {
        //return new Button(By.cssSelector(".pay-btn"), getPageName(), "submit-button");
        return new Button(By.xpath("//*[@id='pay' or @id='confirmPayment' or @id='submit']"), getPageName(), "submit-button");
    }

    public Button buttonCancel() {
        return new Button(By.xpath("//*[@id='cancel' or @class='BankPayCancel']"), getPageName(), "cancel-button");
    }

    public PopUpV2 modalCancelPayment() {
        return new PopUpV2(By.className("modal-main"), "cancel-payment-modal") {
            @Override
            public void accept() {
                Button buttonYes = new Button(By.xpath("//*[@id='closeYes' or @class='bordered-btn']"), getPageName(), "cancel-payment-modal-accept-button");
                buttonYes.waitUntilClickable();
                buttonYes.click();
            }

            @Override
            public void close() {
                Button buttonNo = new Button(By.xpath("//*[@id='closeNo' or @class='blue-btn']"), getPageName(), "cancel-payment-modal-close-button");
                buttonNo.waitUntilClickable();
                buttonNo.click();
            }
        };
    }

    public void inputOtp(String text) {
        otpTextBox().clearAndType(text);
    }

    public void clickSubmit() {
        submitButton().click();
    }

    @Override
    public void waitUntilLoads() {
        try {
            WebDriver driver = DriverManager.getCurrentWebDriver();
            Awaitility.await().ignoreException(NoSuchElementException.class).atMost(ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME.toSeconds(), TimeUnit.SECONDS).until(() -> driver.findElement(By.id("otp")).isDisplayed());
        } catch (ConditionTimeoutException e) {
            throw new TimeoutException(MessageFormat.format("Waited for {0} to be loaded but is not", this), e);
        }
    }

    @Override
    public String toString() {
        return this.pageName;
    }
}
