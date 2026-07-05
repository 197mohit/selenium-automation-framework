package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.RadioButton;
import com.paytm.framework.ui.element.TextBox;
import org.openqa.selenium.By;

public class ZestBankUI extends BasePage {

    public ZestBankUI() {
        super("ZestBank Bank Page");
    }


    private Button failureBtn() {
        return new Button(By.xpath("//button[@class='btn btnl']"), getPageName(), "failure Btn");
    }

    private Button successBtn() {
        return new Button(By.xpath("//button[@class='btn btnd']"), getPageName(), "success Btn");
    }

    public void successZestTxn() {

        successBtn().click();
        waitUntilLoads();
    }
    public void failZestTxn() {

        failureBtn().click();
        waitUntilLoads();
    }

}

