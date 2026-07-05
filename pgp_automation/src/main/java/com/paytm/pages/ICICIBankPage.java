package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.TextBox;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by anjukumari on 24/03/18
 */
public class ICICIBankPage extends BasePage {

    private Report report = com.paytm.framework.reporting.Reporter.report;

    public ICICIBankPage() {
        super("icici-bank-page");
    }

    public void assertATMPINBank(String text) {
        this.report.info("Assert [{}] contains text [{}]", pageName, text);
        Assertions.assertThat(DriverManager.getDriver().switchTo().frame(DriverManager.getDriver().findElement(By.id("output_frame"))).findElement(By.tagName("body")).getText()).containsIgnoringCase(text);
    }

    public void assertOtpOptionBankPage(String text) {
        this.report.info("Assert [{}] contains text [{}]", pageName, text);
        Assertions.assertThat(DriverManager.getDriver().findElement(By.cssSelector("#pwdbaseotppage .wrapper")).getText()).containsIgnoringCase(text);
    }

    public TextBox fieldOtp() {
        DriverManager.getDriver().switchTo().frame(0);
        return new TextBox(By.id("txtAutoOtp"), getPageName(), "otp-field") {
            @Override
            public void assertVisible() {
                try {
                    super.assertVisible();
                } catch (Throwable e) {
                    throw e;
                } finally {
                    DriverManager.getDriver().switchTo().defaultContent();
                }
            }
        };
    }

    public TextBox fieldAtmPin() {
        DriverManager.getDriver().switchTo().frame(0);
        return new TextBox(By.id("cardPin"), getPageName(), "atm-pin-field") {
            @Override
            public void assertVisible() {
                try {
                    super.assertVisible();
                } catch (Throwable e) {
                    throw e;
                } finally {
                    DriverManager.getDriver().switchTo().defaultContent();
                }
            }
        };
    }

    @Override
    public void waitUntilLoads() {
        try {
            WebDriver driver = DriverManager.getCurrentWebDriver();
            Awaitility.await().atMost(ExecutionConfig.MAX_PAGE_LOAD_WAIT_TIME.toSeconds(), TimeUnit.SECONDS).until(() -> driver.getCurrentUrl().contains("icici"));
        } catch (ConditionTimeoutException e) {
            throw new TimeoutException(MessageFormat.format("Waited for {0} to be loaded but is not", this), e);
        }
    }

    @Override
    public String toString() {
        return this.pageName;
    }
}
