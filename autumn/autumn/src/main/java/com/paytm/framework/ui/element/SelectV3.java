package com.paytm.framework.ui.element;

import org.openqa.selenium.By;

public abstract class SelectV3 extends UIElementV3 {

    public SelectV3(By locator, String name) {
        super(locator, name);
    }

    public abstract void select(String value);

    public void select(String... values) {
        throw new UnsupportedOperationException();
    }

    public abstract void selectExact(String value);

    public void selectExact(String... values) {
        throw new UnsupportedOperationException();
    }
}
