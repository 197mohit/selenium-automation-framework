package com.paytm.framework.ui.element;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Assertion;
import com.paytm.framework.reporting.TestStep;
import com.paytm.framework.reporting.Utility;
import org.fest.assertions.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CheckBox extends UIElement {

    @Deprecated
    public CheckBox(By by, String pageName) {
        super(by, pageName);
    }

    public CheckBox(By by, String pageName, String elementName) {
        super(by, pageName, elementName);
    }

    public CheckBox(WebElement webElement, String pageName, String elementName) {
        super(webElement, pageName, elementName);
    }

    @TestStep
    public void toggle() {
        this.report.info("Click [" + getElementName() + "] on [" + getPageName() + "]");
        click();
    }

    @TestStep
    public void check() {
        this.report.info("Check [" + getElementName() + "] on [" + getPageName() + "]");
        if (!isChecked()) {
            toggle();
        }
    }

    @TestStep
    public void unCheck() {
        this.report.info("Uncheck [" + getElementName() + "] on [" + getPageName() + "]");
        if (isChecked()) {
            toggle();
        }
    }

    @Utility
    public boolean isChecked() {
        return getWrappedElement().isSelected();
    }

    @Assertion
    public void assertChecked() {
        this.report.info("Assert [" + getElementName() + "] is checked on [" + getPageName() + "]");
        Assertions.assertThat(isChecked()).as(getElementName() + " is expected to be checked but is not").isEqualTo(true);
    }

    @Assertion
    public void assertUnChecked() {
        this.report.info("Assert [" + getElementName() + "] is un-checked on [" + getPageName() + "]");
        Assertions.assertThat(isChecked()).as(getElementName() + " is expected to be not checked but is").isEqualTo(false);
    }

    @Utility
    public void waitUntilChecked() {
        this.report.info("Wait until [" + getElementName() + "] is checked on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(ExpectedConditions.elementSelectionStateToBe(getBy(), true));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }

    @Utility
    public void waitUntilUnChecked() {
        this.report.info("Wait until [" + getElementName() + "] is un-checked on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(ExpectedConditions.elementSelectionStateToBe(getBy(), false));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }
}
