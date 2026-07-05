package scripts.Native.checkoutjs;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.pages.CheckoutJsCheckoutPage;
import io.qameta.allure.Step;

public class CheckoutJsBase extends PGPBaseTest {

    @Step
    public boolean validate_MerchantPreference(String mid,String prefName,String prefValue) {
        return PGPHelpers.validate_MerchantPreference(mid, prefName,prefValue);
    }

    public final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

}
