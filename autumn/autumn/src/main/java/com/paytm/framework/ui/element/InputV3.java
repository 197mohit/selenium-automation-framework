package com.paytm.framework.ui.element;

import com.paytm.framework.conditions.Condition;
import com.paytm.framework.reporting.Utility;
import org.openqa.selenium.By;

import static java.text.MessageFormat.format;

public class InputV3 extends UIElementV3 {

    public InputV3(By locator, String name) {
        super(locator, name);
    }

    @Utility
    public Condition isSelected() {
        String name = this.toString();
        return new Condition() {
            @Override
            public boolean getAsBoolean() {
                return element().isSelected();
            }

            @Override
            public String toString() {
                return format("{0} is selected", name);
            }
        };
    }

}
