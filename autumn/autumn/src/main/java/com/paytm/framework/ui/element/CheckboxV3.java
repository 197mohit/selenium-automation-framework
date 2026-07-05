package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.TestStep;
import org.openqa.selenium.By;

public class CheckboxV3 extends RadioButtonV3 {

    public CheckboxV3(By locator, String name) {
        super(locator, name);
    }

    @TestStep
    public void unCheck() {
        if (this.isSelected().getAsBoolean()) {
            this.click();
        }
    }
}
