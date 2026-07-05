package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.Utility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public abstract class TableV3 extends UIElementV3 {

    public TableV3(By locator, String name) {
        super(locator, name);
    }

    @Utility
    public WebElement getCellAtIndex(int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Utility
    private List<UIElement> getRows() {
        throw new UnsupportedOperationException();
    }

    @Utility
    public List<UIElement> getCells(WebElement row) {
        throw new UnsupportedOperationException();
    }

    @Utility
    public List<String> getRowValues(int row) {
        throw new UnsupportedOperationException();
    }

    @Utility
    public List<String> getColumnValues(int col) {
        throw new UnsupportedOperationException();
    }

}
