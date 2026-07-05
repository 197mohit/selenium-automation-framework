package com.paytm.framework.ui.element;

import com.paytm.framework.reporting.Assertion;
import com.paytm.framework.reporting.Utility;
import org.fest.assertions.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class Table extends UIElement {

    @Deprecated
    public Table(By by, String pageName) {
        super(by, pageName);
    }

    public Table(By by, String pageName, String elementName) {
        super(by, pageName, elementName);
    }

    @Utility
    public int getRowCount() {
        return getRows().size();
    }

    @Utility
    public int getColumnCount() {
        return getWrappedElement().findElements(By.cssSelector("th")).size();
    }

    @Utility
    public WebElement getCellAtIndex(int rowIdx, int colIdx) {
        WebElement row = getRows().get(rowIdx);
        List<WebElement> cells;
        // Cells are most likely to be td tags
        if ((cells = row.findElements(By.tagName("td"))).size() > 0) {
            return cells.get(colIdx - 1);
        }
        // Failing that try th tags
        else if ((cells = row.findElements(By.tagName("th"))).size() > 0) {
            return cells.get(colIdx - 1);
        } else {
            final String error = String.format("Could not find cell at row: %s column: %s", rowIdx, colIdx);
            throw new RuntimeException(error);
        }
    }

    @Utility
    public String getRowValue(String rowName) {
        return null;
    }

    @Utility
    public String getCellTextAtIndex(int rowIdx, int colIdx) {
        return getCellAtIndex(rowIdx, colIdx).getText();
    }

    @Utility
    private List<WebElement> getRows() {
        List<WebElement> rows = new ArrayList<WebElement>();
        //Header rows
        List<WebElement> headerRows = getWrappedElement().findElements(By.cssSelector("thead tr"));
        if (headerRows.size() > 0) {
            rows.add(headerRows.get(0));
        } else {
            rows.add(null);
        }
        //Body rows
        List<WebElement> bodyRows = getWrappedElement().findElements(By.cssSelector("tbody tr"));
        if (bodyRows.size() > 0) {
            rows.addAll(bodyRows);
        }
        //Footer rows
        /*List<WebElement> footerRows = getWrappedElement().findElements(By.cssSelector("tfoot tr"));
        rows.addAll(footerRows);*/

        return rows;
    }

    @Utility
    public List<WebElement> getCells(WebElement row) {
        List<WebElement> cells;
        // Cells are most likely to be td tags
        if ((cells = row.findElements(By.tagName("td"))).size() > 0) {
            return cells;
        }
        // Failing that try th tags
        else if ((cells = row.findElements(By.tagName("th"))).size() > 0) {
            return cells;
        } else {
            final String error = String.format("Could not find any cell.");
            throw new RuntimeException(error);
        }
    }

    @Utility
    public List<String> getRowValue(int row) {
        List<WebElement> cellsElements = getCells(getRows().get(row));
        List<String> value = new ArrayList<>();
        for (WebElement ele : cellsElements)
            value.add(ele.getText());
        return value;
    }


    @Utility
    public int getColIdx(String colHeader) {
        try {
            for (int idx = 1; ; idx++) {
                if (getCellTextAtIndex(0, idx).equalsIgnoreCase(colHeader)) {
                    return idx;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Column header not found");
        }

    }

    @Assertion
    public void assertCellText(int rowIdx, int colIdx, String text) {
        this.report.info("Assert [" + getElementName() + "] cell[" + rowIdx + "][" + colIdx + "] text equals [" + text + "on [" + getPageName() + "]");
        Assertions.assertThat(getCellAtIndex(rowIdx, colIdx).getText().trim()).isEqualToIgnoringCase(text);
    }

    @Assertion
    public void assertCellText(int rowIdx, String colHeader, String text) {
        this.report.info("Assert [" + getElementName() + "] cell[" + rowIdx + "][" + colHeader + "] text equals [" + text + "on [" + getPageName() + "]");
        Assertions.assertThat(getCellAtIndex(rowIdx, getColIdx(colHeader)).getText().trim()).isEqualToIgnoringCase(text);
    }

    @Assertion
    public void assertCellContainsText(int rowIdx, int colIdx, String text) {
        this.report.info("Assert [" + getElementName() + "] cell[" + rowIdx + "][" + colIdx + "] contains text [" + text + "on [" + getPageName() + "]");
        Assertions.assertThat(getCellAtIndex(rowIdx, colIdx).getText().trim()).containsIgnoringCase(text);
    }

    @Assertion
    public void assertCellContainsText(int rowIdx, String colHeader, String text) {
        this.report.info("Assert [" + getElementName() + "] cell[" + rowIdx + "][" + colHeader + "] contains text [" + text + "on [" + getPageName() + "]");
        Assertions.assertThat(getCellAtIndex(rowIdx, getColIdx(colHeader)).getText().trim()).containsIgnoringCase(text);
    }

    @Assertion
    public void assertCellDoesNotContainText(int rowIdx, int colIdx, String text) {
        this.report.info("Assert [" + getElementName() + "] cell[" + rowIdx + "][" + colIdx + "] doesn't contain text [" + text + "on [" + getPageName() + "]");
        Assertions.assertThat(getCellAtIndex(rowIdx, colIdx).getText().trim()).doesNotContain(text);
    }

    @Assertion
    public void assertCellDoesNotContainText(int rowIdx, String colHeader, String text) {
        this.report.info("Assert [" + getElementName() + "] cell[" + rowIdx + "][" + colHeader + "] doesn't contain text [" + text + "on [" + getPageName() + "]");
        Assertions.assertThat(getCellAtIndex(rowIdx, getColIdx(colHeader)).getText().trim()).doesNotContain(text);
    }

    @Utility
    private boolean isSortIconDisplayed(int colIdx) {
        try {
            return getCellAtIndex(0, colIdx).findElement(By.xpath(".//span[contains(@class, 'DataTables_sort_icon')]")).isDisplayed();
        } catch (NoSuchElementException e) {
            //do nothing
        }
        return false;
    }

    @Assertion
    public void assertColumnSortable(int colIdx) {
        this.report.info("Assert column " + colIdx + " is sortable on [" + getPageName());
        Assertions.assertThat(isSortIconDisplayed(colIdx)).isTrue();
    }

    @Assertion
    public void assertColumnSortable(String colHeader) {
        this.report.info("Assert column " + colHeader + " is sortable on [" + getPageName());
        Assertions.assertThat(isSortIconDisplayed(getColIdx(colHeader))).isTrue();
    }

    @Assertion
    public void assertColumnNotSortable(int colIdx) {
        this.report.info("Assert column " + colIdx + " is not sortable on [" + getPageName());
        Assertions.assertThat(isSortIconDisplayed(colIdx)).isFalse();
    }

    @Assertion
    public void assertColumnNotSortable(String colHeader) {
        this.report.info("Assert column " + colHeader + " is sortable on [" + getPageName());
        Assertions.assertThat(isSortIconDisplayed(getColIdx(colHeader))).isFalse();
    }

}