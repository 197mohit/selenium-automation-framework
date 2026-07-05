package com.paytm.framework.ui.element;

import com.paytm.framework.conditions.CString;
import com.paytm.framework.conditions.Condition;
import com.paytm.framework.conditions.Wait;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.core.ExecutionConfig;
import com.paytm.framework.reporting.TestStep;
import com.paytm.framework.reporting.Utility;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static java.text.MessageFormat.format;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class UIElementV3 {

    private final UIElementV3 self = this;
    private final By locator;
    private final String name;
//    Wait wait = new Wait(value -> 5, ExecutionConfig.MAX_ELEMENT_LOAD_WAIT_TIME / 5, 1000);
Wait wait = new Wait(value -> 5, 60 / 20, 1000);

    public UIElementV3(By locator, String name) {
        this.locator = locator;
        this.name = name;

    }

    protected WebElement element() throws NoSuchElementException {
        return DriverManager.getDriver().findElement(this.locator);
    }

    @TestStep("Click on %e")
    public void click() {
        wait.apply(this.isClickable());
        new Actions(DriverManager.getDriver()).moveToElement(this.element()).click().perform();
    }

    @Utility("Check that %e is present or not")
    public Condition isPresent() {
        return () -> {
            try {
                return this.element() != null;
            } catch (NoSuchElementException e) {
                return false;
            }
        };
    }

    @Utility("Check that %e is visible or not")
    public Condition isVisible() {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return isPresent().getAsBoolean() && visibilityOfElementLocated(locator).apply(DriverManager.getDriver()) != null;
            }

            @Override
            public String toString() {
                return format("{0} is visible", name);
            }
        };
    }

    @Utility("Check that %e is clickable or not")
    public Condition isClickable() {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return isPresent().getAsBoolean() && elementToBeClickable(locator).apply(DriverManager.getDriver()) != null;
            }

            @Override
            public String toString() {
                return format("{0} is clickable", name);
            }
        };
    }

    @Utility("Check that %e is enabled or not")
    public Condition isEnabled() {
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return isPresent().getAsBoolean() && element().isEnabled();
            }

            @Override
            public String toString() {
                return format("{0} is enabled", name);
            }
        };
    }

    public CString content() {
        String text = this.element().getText();
        return new CString(text) {
            @Override
            public String toString() {
                return format("{0} content - {1}", self, text);
            }
        };
    }

    public CString get(String attribute) {
        String value = self.element().getAttribute(attribute);
        return new CString(value) {
            @Override
            public String toString() {
                return format("{0} attribute value - {1} - for attribute name {1}", self, value, attribute);
            }
        };
    }

    public Style style() {
        return new Style(self);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public class Style {
        private UIElementV3 uiElement;

        public Style(UIElementV3 uiElement) {
            this.uiElement = uiElement;
        }

        public CString get(String property) {
            String value = this.uiElement.element().getCssValue(property);
            return new CString(value) {
                @Override
                public String toString() {
                    return format("{0} css value - {1} - for property name {1}", uiElement, value, property);
                }
            };
        }
    }
}
