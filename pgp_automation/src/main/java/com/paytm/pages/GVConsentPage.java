package com.paytm.pages;

import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.Button;
import com.paytm.framework.ui.element.RadioButton;
import com.paytm.framework.ui.element.TextBox;
import org.openqa.selenium.By;

public class GVConsentPage extends BasePage {

    public GVConsentPage() {
        super("GV Consent Page");
    }

    public  void proceedToBuyGiftVoucher(){
        DriverManager.getDriver().findElement(By.xpath("//input[@value='Proceed to Buy Gift Voucher']")).click();
    }

}
