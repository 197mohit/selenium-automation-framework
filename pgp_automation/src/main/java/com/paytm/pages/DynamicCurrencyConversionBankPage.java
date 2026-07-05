package com.paytm.pages;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.*;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DynamicCurrencyConversionBankPage extends BasePage {

    private Report report;
    private boolean deleteCookie = true;

    public DynamicCurrencyConversionBankPage(){

        super("Dcc Bank Page");
        this.pageURL = LocalConfig.PGP_RESP_HOST + Constants.PagePath.FETCH_DCC_BANKPAGE_PATH;

    }

    public boolean isDeleteCookie() {
        return deleteCookie;
    }

    public void setDeleteCookie(boolean deleteCookie) {
        this.deleteCookie = deleteCookie;
    }

    public UIElement successButton() {
        return new Button(By.xpath("//span[text()='Successful']/parent::button[@class='btn btnd']"), getPageName(), "Success-Button");
    }

    public UIElement failureButton() {
        return new Button(By.xpath("//span[text()='Failure']/parent::button[@class='btn btnl']"), getPageName(), "Failure-Button");
    }

    public void clickSuccessButton(){
        waitUntilLoads();
        successButton().click();
    }
    
}