package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.By;

public abstract class FormV3 extends UIElementV3 {

    public FormV3(By locator, String name) {
        super(locator, name);
    }

    @TestStep("Submit %e")
    public void submit() {
        throw new UnsupportedOperationException();
    }
}
