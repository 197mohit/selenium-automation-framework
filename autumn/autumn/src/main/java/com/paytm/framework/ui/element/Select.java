package com.paytm.framework.ui.element;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.TestStep;
import com.paytm.framework.reporting.Utility;
import com.paytm.framework.ui.MoreExpectedConditions;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class Select extends UIElement {

    @Deprecated
    public Select(By by, String pageName) {
        super(by, pageName);
    }

    public Select(By by, String pageName, String elementName) {
        super(by, pageName, elementName);
    }

    @Utility
    public boolean isMultiple() {
        return new org.openqa.selenium.support.ui.Select(getWrappedElement()).isMultiple();
    }

    @TestStep
    public void deselectByIndex(int index) {
        this.report.info("De-select [" + getElementName() + "] option at position [" + index + "] on [" + getPageName() + "]");
        new org.openqa.selenium.support.ui.Select(getWrappedElement()).deselectByIndex(index);
    }

    @TestStep
    public void selectByValue(String value) {
        this.report.info("Select [" + getElementName() + "] option with value [" + value + "] on [" + getPageName() + "]");
        new org.openqa.selenium.support.ui.Select(getWrappedElement()).selectByValue(value);
    }

    @Utility
    public WebElement getFirstSelectedOption() {
        return new org.openqa.selenium.support.ui.Select(getWrappedElement()).getFirstSelectedOption();
    }

    @TestStep
    public void selectByVisibleText(String text) {
        this.report.info("Select [" + getElementName() + "] option with text [" + text + "] on [" + getPageName() + "]");
        new org.openqa.selenium.support.ui.Select(getWrappedElement()).selectByVisibleText(text);
    }

    @TestStep
    public void deselectByValue(String value) {
        this.report.info("De-Select [" + getElementName() + "] option with value [" + value + "] on [" + getPageName() + "]");
        new org.openqa.selenium.support.ui.Select(getWrappedElement()).deselectByValue(value);
    }

    @TestStep
    public void deselectAll() {
        this.report.info("De-select [" + getElementName() + "] all options on [" + getPageName() + "]");
        new org.openqa.selenium.support.ui.Select(getWrappedElement()).deselectAll();
    }

    @Utility
    public List<WebElement> getAllSelectedOptions() {
        return new org.openqa.selenium.support.ui.Select(getWrappedElement()).getAllSelectedOptions();
    }

    @Utility
    public List<WebElement> getOptions() {
        return new org.openqa.selenium.support.ui.Select(getWrappedElement()).getOptions();
    }

    @TestStep
    public void deselectByVisibleText(String text) {
        this.report.info("De-select [" + getElementName() + "] option with text [" + text + "] on [" + getPageName() + "]");
        new org.openqa.selenium.support.ui.Select(getWrappedElement()).deselectByVisibleText(text);
    }

    @TestStep
    public void selectByIndex(int index) {
        this.report.info("Select [" + getElementName() + "] option at position [" + index + "] on [" + getPageName() + "]");
        new org.openqa.selenium.support.ui.Select(getWrappedElement()).selectByIndex(index);
    }

    @Utility
    public void waitUntilSelected() {
        this.report.info("Wait until [" + getElementName() + "] option is selected on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(ExpectedConditions.elementSelectionStateToBe(getBy(), true));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }

    @Utility
    public void waitUntilDeSelected() {
        this.report.info("Wait until [" + getElementName() + "] option is de-selected on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(ExpectedConditions.elementSelectionStateToBe(getBy(), false));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }

    @Utility
    public void waitUntilOptionToBeSelectedByVisibeText(String optionText) {
        this.report.info("Wait until [" + getElementName() + "] option with text [" + optionText + "] is selected on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(MoreExpectedConditions.optionToBeSelectedInElement(new org.openqa.selenium.support.ui.Select(getWrappedElement()), optionText, true));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }

    @Utility
    public void waitUntilOptionToBeSelectedByValue(String optionValue) {
        this.report.info("Wait until [" + getElementName() + "] option with value [" + optionValue + "] is selected on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(MoreExpectedConditions.optionToBeSelectedInElement(new org.openqa.selenium.support.ui.Select(getWrappedElement()), optionValue, false));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }
}