package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.By;

public abstract class ModalV3 extends UIElementV3 {

    public ModalV3(By locator, String name) {
        super(locator, name);
    }

    @TestStep
    public void accept() {
        throw new UnsupportedOperationException();
    }

    @TestStep
    public void reject() {
        throw new UnsupportedOperationException();
    }

}
