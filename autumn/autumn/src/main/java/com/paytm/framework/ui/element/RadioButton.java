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

public class RadioButton extends UIElement {

    @Deprecated
    public RadioButton(By by, String pageName) {
        super(by, pageName);
    }

    public RadioButton(By by, String pageName, String elementName) {
        super(by, pageName, elementName);
    }
    
    public RadioButton(WebElement webElement, String pageName, String elementName) {
		super(webElement, pageName, elementName);
	}

    @TestStep
    public void select() {
        this.report.info("Select [" + getElementName() + "] on [" + getPageName() + "]");
        if (!isSelected()) {
            click();
        }
    }

    @Utility
    public boolean isSelected() {
        return getWrappedElement().isSelected();
    }

    @Assertion
    public void assertSelected() {
        this.report.info("Assert [" + getElementName() + "] is selected on [" + getPageName() + "]");
        Assertions.assertThat(getWrappedElement().isSelected()).as(getElementName() + " is expected to be selected but is not").isEqualTo(true);
    }

    @Assertion
    public void assertDeSelected() {
        this.report.info("Assert [" + getElementName() + "] is not selected on [" + getPageName() + "]");
        Assertions.assertThat(getWrappedElement().isSelected()).as(getElementName() + " is expected to be not selected but is").isEqualTo(false);
    }

    @Utility
    public void waitUntilSelected() {
        this.report.info("Wait until [" + getElementName() + "] is selected on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(ExpectedConditions.elementSelectionStateToBe(getBy(), true));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }

    @Utility
    public void waitUntilDeSelected() {
        this.report.info("Wait until [" + getElementName() + "] is not selected on [" + getPageName() + "]");
        try {
            DriverManager.getWebDriverElementWait().until(ExpectedConditions.elementSelectionStateToBe(getBy(), false));
        } catch (TimeoutException e) {
            //swallowing the exception as this function is meant to be used as pre-step to a main-step, so no need to fail it if we get TimeOutException
        }
    }

}