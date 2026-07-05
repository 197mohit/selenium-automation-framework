package com.paytm.framework.ui.element;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Assertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.TestStep;
import com.paytm.framework.reporting.Utility;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.ui.MoreExpectedConditions;
import org.fest.assertions.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.interactions.Coordinates;
//import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Arrays;
import java.util.List;

public class UIElement implements WebElement, WrapsElement, Locatable {

    JavascriptExecutor js;
    WebDriver driver;
    Report report = com.paytm.framework.reporting.Reporter.report;
    private String elementName;
    private String pageName;
    private By by;
    private WebElement webElement;

    @Deprecated
    public UIElement(By by, String elementName) {
        this.by = by;
        this.pageName = "web-page";
        this.elementName = elementName;
        this.driver = DriverManager.getDriver();
        this.js = (JavascriptExecutor) driver;
    }

    public UIElement(By by, String pageName, String elementName) {
        this.by = by;
        this.pageName = pageName;
        this.elementName = elementName;
        this.driver = DriverManager.getDriver();
        this.js = (JavascriptExecutor) driver;
    }

    public UIElement(WebElement webElement, String pageName, String elementName) {
        this.webElement = webElement;
        this.pageName = pageName;
        this.elementName = elementName;
        this.driver = DriverManager.getDriver();
        this.js = (JavascriptExecutor) driver;
    }

    public UIElement(By by, String pageName, String elementName, Report report) {
        this.by = by;
        this.pageName = pageName;
        this.elementName = elementName;
        this.driver = DriverManager.getDriver();
        this.js = (JavascriptExecutor) driver;
        this.report = report;
    }

    public String getPageName() {
        return this.pageName;
    }

    public String getElementName() {
        return this.elementName;
    }

    public By getBy() {
        return this.by;
    }

    @TestStep
    public void click() {
        this.report.info("Click [" + elementName + "] on [" + pageName + "]");
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", getWrappedElement());
    }

    private void highlightElement(WebElement element, int duration) throws InterruptedException {
        String original_style = element.getAttribute("style");
        js.executeScript(
                "arguments[0].setAttribute(arguments[1], arguments[2])",
                element,
                "style",
                "border: 5px solid red; border-style: solid;");
        if (duration > 0) {
            Thread.sleep(duration * 500);
            js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2])",
                    element,
                    "style",
                    original_style);
        }
    }

    @TestStep
    public void sendKeys(CharSequence... keysToSend) {
        this.report.info("Enter text [" + Arrays.toString(keysToSend) + "] in [" + elementName + "] on [" + pageName + "]");
        getWrappedElement().sendKeys(keysToSend);
    }

    @Utility
    public Point getLocation() {
        return getWrappedElement().getLocation();
    }

    @TestStep
    public void submit() {
        this.report.info("Click [" + elementName + "] on [" + pageName + "] to submit");
        getWrappedElement().submit();
    }

    @Utility
    public String getAttribute(String name) {
        return getWrappedElement().getAttribute(name);
    }

    @Utility
    public String getCssValue(String propertyName) {
        return getWrappedElement().getCssValue(propertyName);
    }

    @Utility
    public Dimension getSize() {
        return getWrappedElement().getSize();
    }

    @Utility
    @Override
    public Rectangle getRect() {
        throw new UnsupportedOperationException();
    }

    public List<WebElement> findElements(By by) {
        return getWrappedElement().findElements(by);
    }

    @Utility
    public String getText() {
        return getWrappedElement().getText();
    }

    @Utility
    public String getTagName() {
        return getWrappedElement().getTagName();
    }

    @Utility
    public boolean isSelected() {
        return getWrappedElement().isSelected();
    }

    public WebElement findElement(By by) {
        return getWrappedElement().findElement(by);
    }

    @Utility
    public boolean isEnabled() {
        return getWrappedElement().isEnabled();
    }

    @Utility
    public boolean isDisplayed() {
        return getWrappedElement().isDisplayed();
    }

    @TestStep
    public void clear() {
        this.report.info("Clear text in [" + elementName + "] on [" + pageName + "]");
        getWrappedElement().clear();
    }

    public WebElement getWrappedElement() {
        if (this.webElement == null) {
            try {
                Reporter.report.debug("find element: '" + this.elementName + "'");
                this.webElement = DriverManager.getDriver().findElement(this.by);
            } catch (NoSuchElementException e) {
                Assertions.fail(this.elementName + " is not found on " + this.pageName, e);
//                throw new NoSuchElementException(this.elementName + " is not found on " + this.pageName, e);
            }
        }
        return this.webElement;
    }

    @Utility
    public Coordinates getCoordinates() {
        return ((Locatable) getWrappedElement()).getCoordinates();
    }

    public boolean elementWired() {
        return (webElement != null);
    }

    public void focus() {
        this.report.info("Focus [" + elementName + "] on [" + pageName + "]");
        new Actions(DriverManager.getDriver()).moveToElement(getWrappedElement()).perform();
    }

    @Assertion
    public void assertVisible() {
        this.report.info("Assert [" + elementName + "] is visible on [" + pageName + "]");
        if (!isElementPresent() || ExpectedConditions.visibilityOfElementLocated(this.by).apply(this.driver) == null) {
            Assertions.fail(elementName + " is expected to be visible but it is not");
//            throw new AssertionError(elementName + " is expected to be visible but it is not");
        }
    }

    @Assertion
    public void assertNotVisible() {
        this.report.info("Assert [" + elementName + "] is not visible on [" + pageName + "]");
        if (isElementPresent() && ExpectedConditions.visibilityOfElementLocated(this.by).apply(this.driver) instanceof WebElement) {
            Assertions.fail(elementName + " is expected to be not visible but it is");
//            throw new AssertionError(elementName + " is expected to be not visible but it is");
        }
    }

    @Assertion
    public void assertText(String text) {
        this.report.info("Assert [" + elementName + "] text equals [" + text + "] on [" + pageName + "]");
        if (!isElementPresent() || !ExpectedConditions.textToBe(this.by, text).apply(this.driver)) {
            Assertions.fail("text of " + elementName + " is expected to be equal to " + text + " but it is not");
//            throw new AssertionError("text of " + elementName + " is expected to be equal to " + text + " but it is not");
        }
    }

    @Assertion
    public void assertContainsText(String text) {
        this.report.info("Assert [" + elementName + "] contains text [" + text + "] on [" + pageName + "]");
        if (!isElementPresent() || !ExpectedConditions.textToBePresentInElementLocated(this.by, text).apply(this.driver)) {
            Assertions.fail(elementName + " is expected to contain text - '" + text + "' but it is not");
//            throw new AssertionError(elementName + " is expected to contain text - '" + text + "' but it is not");
        }
    }

    @Assertion
    public void assertDoesNotContainText(String text) {
        this.report.info("Assert [" + elementName + "] doesn't contain text [" + text + "] on [" + pageName + "]");
        if (isElementPresent() && ExpectedConditions.textToBePresentInElementLocated(this.by, text).apply(this.driver)) {
            Assertions.fail(elementName + " is expected to not contain text - '" + text + "' but it does");
//            throw new AssertionError(elementName + " is expected to not contain text - '" + text + "' but it does");
        }
    }

    @Assertion
    public void assertValue(String value) {
        this.report.info("Assert [" + elementName + "] value equals [" + value + "] on [" + pageName + "]");
        if (!isElementPresent() || !ExpectedConditions.attributeToBe(this.by, "value", value).apply(this.driver))
            Assertions.fail("value of " + elementName + " is expected to be equal to " + value + " but it is not");
//            throw new AssertionError("value of " + elementName + " is expected to be equal to " + value + " but it is not");
    }

    @Assertion
    public void assertAttribute(String attribute, String value) {
        this.report.info("Assert [" + elementName + "] value equals [" + value + "] on [" + pageName + "]");
        if (!isElementPresent() || !ExpectedConditions.attributeToBe(this.by, attribute, value).apply(this.driver)) {
            Assertions.fail("element attribute - " + attribute + " is expected to have value equals to " + value + " but it does not");
//            throw new AssertionError("element attribute - " + attribute + " is expected to have value equals to " + value + " but it does not");
        }
    }

    @Assertion
    public void assertContainsValue(String value) {
        this.report.info("Assert [" + elementName + "] contains value [" + value + "] on [" + pageName + "]");
        String assertionMsg = "value of " + elementName + " is expected to be contain value - " + value + " but it is does not";
        try {
            WebElement webElement = ExpectedConditions.presenceOfElementLocated(this.by).apply(this.driver);
            Assertions.assertThat(webElement.getAttribute("value")).as(assertionMsg).containsIgnoringCase(value);
        } catch (NoSuchElementException e) {
            Assertions.fail(assertionMsg);
//            throw new AssertionError(assertionMsg);
        }
    }

    @Assertion
    public void assertDoesNotContainValue(String value) {
        this.report.info("Assert [" + elementName + "] doesn't contain value [" + value + "] on [" + pageName + "]");
        String assertionMsg = "value of " + elementName + " is expected to be not contain value - " + value + " but it is does";
        try {
            WebElement webElement = ExpectedConditions.presenceOfElementLocated(this.by).apply(this.driver);
            Assertions.assertThat(webElement.getAttribute("value")).as(assertionMsg).doesNotContain(value);
        } catch (NoSuchElementException e) {
            //do nothing as it satisfies the assertion
        }
    }

    @Utility
    private boolean isFocused() {
        return getWrappedElement().equals(DriverManager.getDriver().switchTo().activeElement());
    }

    @Utility
    public void switchToFrame() {
        DriverManager.getDriver().switchTo().frame(getWrappedElement());
    }

    @Assertion
    public void assertIsFocused() {
        this.report.info("Assert [" + elementName + " is focused on [" + pageName + "]");
        Assertions.assertThat(isFocused()).as(elementName + " is expected to be focused but is not").isEqualTo(true);
    }

    @Assertion
    public void assertIsNotFocused() {
        this.report.info("Assert [" + elementName + " is not focused on [" + pageName + "]");
        Assertions.assertThat(isFocused()).as(elementName + " is expected to be not focused but is").isEqualTo(false);
    }

    @Utility
    public void waitUntilContainsText(String text) {
        String debugMsg = "Wait until [" + this.elementName + "] contains text [" + text + "] on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.textToBePresentInElementLocated(getBy(), text));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilDoesNotContainText(String text) {
        String debugMsg = "Wait until [" + this.elementName + "] contains text [" + text + "] on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(getBy(), text)));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilVisible() {
        String debugMsg = "Wait until [" + this.elementName + "] is visible " + "on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.visibilityOfElementLocated(getBy()));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilPresent() {
        String debugMsg = "Wait until [" + this.elementName + "] is present " + "on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.presenceOfElementLocated(getBy()));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilNotVisible() {
        String debugMsg = "Wait until [" + this.elementName + "] is not visible " + "on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.invisibilityOfElementLocated(getBy()));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilEditable() {
        String debugMsg = "Wait until [" + this.elementName + "] is editable " + "on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.elementToBeClickable(getBy()));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilNotEditable() {
        String debugMsg = "Wait until [" + this.elementName + "] is not editable " + "on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(getBy())));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilContainsAttributeValue(String attributeName, String attributeValue) {
        String debugMsg = "Wait until [" + this.elementName + "] attribute [" + attributeName + "] contains value [" + attributeValue + "] on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(MoreExpectedConditions.attributeValueToBeContainedInElement(getBy(), attributeName, attributeValue));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilDoesNotContainAttributeValue(String attributeName, String attributeValue) {
        String debugMsg = "Wait until [" + this.elementName + "] attribute [" + attributeName + "] does not contains value [" + attributeValue + "] on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.not(MoreExpectedConditions.attributeValueToBeContainedInElement(getBy(), attributeName, attributeValue)));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilContainsAttribute(String attributeName) {
        String debugMsg = "Wait until [" + this.elementName + "] contains attribute [" + attributeName + "] on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(MoreExpectedConditions.attributeToBeContainedInElement(getBy(), attributeName));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilDoesNotContainAttribute(String attributeName) {
        String debugMsg = "Wait until [" + this.elementName + "] does not contains attribute [" + attributeName + "] on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.not(MoreExpectedConditions.attributeToBeContainedInElement(getBy(), attributeName)));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    public UIElement and() {
        return this;
    }

    public void scrollToView() {
        this.report.info("Scroll [" + elementName + "] to view on [" + pageName + "]");
        String script = "arguments[0].scrollIntoView(true);";
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript(script, getWrappedElement());
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        return null;
    }

    @Assertion
    public void assertDisabled() {
        this.report.info("Assert [" + elementName + " is disabled on [" + pageName + "]");
        if (isElementPresent() && ExpectedConditions.elementToBeClickable(by).apply(this.driver) instanceof WebElement) {
            Assertions.fail(elementName + " is expected to be disabled but it is not");
//            throw new AssertionError(elementName + " is expected to be disabled but it is not");
        }
    }

    @Assertion
    public void assertEnabled() {
        this.report.info("Assert [" + elementName + " is enabled on [" + pageName + "]");
        if (!isElementPresent() || ExpectedConditions.elementToBeClickable(by).apply(this.driver) == null) {
            Assertions.fail(elementName + " is expected to be enabled but is not");
//            throw new AssertionError(elementName + " is expected to be enabled but is not");
        }
    }

    @Assertion
    public void assertClickable() {
        this.report.info("Assert [" + getElementName() + "] is clickable on [" + getPageName() + "]");
        if (!isElementPresent() || ExpectedConditions.elementToBeClickable(by).apply(this.driver) == null) {
            Assertions.fail(elementName + " is expected to be clickable but is not");
//            throw new AssertionError(elementName + " is expected to be clickable but is not");
        }
    }

    @Assertion
    public void assertNotClickable() {
        this.report.info("Assert [" + getElementName() + "] is not clickable on [" + getPageName() + "]");
        if (isElementPresent() && ExpectedConditions.elementToBeClickable(by).apply(this.driver) instanceof WebElement) {
            Assertions.fail(elementName + " is expected to be not clickable but is");
//            throw new AssertionError(elementName + " is expected to be not clickable but is");
        }
    }

    @Utility
    public void waitUntilClickable() {
        String debugMsg = "Wait until [" + this.elementName + "] is clickable on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.elementToBeClickable(getBy()));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    @Utility
    public void waitUntilNotClickable() {
        String debugMsg = "Wait until [" + this.elementName + "] is not clickable on [" + this.pageName + "]";
        this.report.info(debugMsg);
        try {
            DriverManager.getWebDriverElementWait().withMessage(debugMsg).until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(getBy())));
        } catch (TimeoutException e) {
            Assertions.fail("" + e.getMessage());
        }
    }

    public boolean isElementPresent() {
        try {
            ExpectedConditions.presenceOfElementLocated(this.by).apply(this.driver);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}