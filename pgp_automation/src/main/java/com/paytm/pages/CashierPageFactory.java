package com.paytm.pages;

import com.paytm.appconstants.Constants.Theme;

/**
 * Created by sureshgupta on 06/10/17.
 */
public class CashierPageFactory {

    public static CashierPage getCashierPage(String theme) {
        switch (theme.toLowerCase()) {
            case Theme.MERCHANT:
//                return new CashierPageMerchant();
            case Theme.MERCHANT3:
//                return new CashierPageMerchant3();
            case Theme.MERCHANT4:
                return new CashierPageMerchant4();
            case Theme.MERCHANT5:
//                return new CashierPageMerchant5();
            case Theme.MERCHANTLOW5:
//                return new CashierPageMerchantLow5();
            case Theme.ENHANCEDWAP:
            case Theme.MERCHANTLOWCCDD:
                return new CashierPageEnhancedWAP();
            case Theme.ENHANCED_WEB:
                return new CashierPageEnhancedWeb();
            case Theme.CHECKOUTJS_WEB:
                return new CashierPageCheckoutjsWeb();
            case Theme.CHECKOUTJS_WAP:
                return new CashierPageCheckoutjsWap();
            case Theme.CHECKOUTJSE_WEB:
                return new CashierPageCheckoutJsElementWeb();
            case Theme.CHECKOUTJSE_WEB_REVAMP:
                return new CashierPageCheckoutJsElementWeb();  //TODO: need to check element with devs
            case Theme.CHECKOUTJSWEB_REVAMP:
                return new CashierPageCheckoutjsWeb();
            case Theme.CHECKOUTJSWAP_REVAMP:
                return new CashierPageCheckoutjsWap();
            case Theme.ENHANCEDWEB_REVAMP:
                return new CashierPageEnhancedWeb();
            case Theme.ENHANCEDWAP_REVAMP:
                return new CashierPageEnhancedWAP();
            case Theme.CHECKOUTJS_WEB_REVAMP:
                return new CashierPageCheckoutjsWeb();
            case Theme.CHECKOUTJS_WAP_REVAMP:
                return new CashierPageCheckoutjsWap();
            case Theme.CHECKOUTJS_WEB_REVAMP_2:
                return new CashierPageCheckoutjsWapRevamp2();
            case Theme.ENHANCED_WEB_REVAMP:
                return new CashierPageEnhancedWeb();
            case Theme.ENHANCED_WAP_REVAMP:
                return new CashierPageEnhancedWAP();
            case Theme.LIGHTENING_WEB_REVAMP:
                return new LighteningCheckoutPage();


            default:
                throw new RuntimeException("Invalid Cashier Page Theme: " + theme);
        }
    }
}
