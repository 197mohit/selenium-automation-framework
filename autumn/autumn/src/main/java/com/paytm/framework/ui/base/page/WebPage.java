package com.paytm.framework.ui.base.page;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.JavascriptExecutor;

import java.util.function.BooleanSupplier;

import static java.text.MessageFormat.format;

public abstract class WebPage {

    private final String url;

    public WebPage(String url) {
        this.url = url;
    }

    public WebPage() {
        this(null);
    }

    @TestStep
    public void launch() {
        DriverManager.getDriver().get(this.url);
    }

    public BooleanSupplier isLoading() {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return ((JavascriptExecutor) DriverManager.getDriver()).executeScript("return document.readyState").equals("loading");
            }

            @Override
            public String toString() {
                return format("{0} is loading", WebPage.this);
            }
        };
    }

    public BooleanSupplier hasLoaded() {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return ((JavascriptExecutor) DriverManager.getDriver()).executeScript("return document.readyState").equals("complete");
            }

            @Override
            public String toString() {
                return format("{0} has loaded", WebPage.this);
            }
        };
    }

    @Override
    public abstract String toString();
}
