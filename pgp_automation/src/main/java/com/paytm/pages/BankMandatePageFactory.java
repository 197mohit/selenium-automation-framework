package com.paytm.pages;

import com.paytm.appconstants.Constants.Theme;

/**
 * Created by Sourav Singh on 28/10/21.
 */
public class BankMandatePageFactory {

    public static BankMandatePage getBankMandatePage(String theme) {
        switch (theme.toLowerCase()) {
            case Theme.CHECKOUTJS_WEB:
            case Theme.CHECKOUTJS_WAP:
            case Theme.CHECKOUTJSE_WEB:
            case Theme.ENHANCED_WEB:
            case Theme.ENHANCEDWAP:
                return new BankMandatePage();
            case Theme.CHECKOUTJSE_WEB_REVAMP:
            case Theme.CHECKOUTJS_WEB_REVAMP:
            case Theme.CHECKOUTJS_WAP_REVAMP:
            case Theme.CHECKOUTJS_WEB_REVAMP_2:
            case Theme.ENHANCED_WEB_REVAMP:
            case Theme.ENHANCED_WAP_REVAMP:
                return new BankMandateRevampPage();
            default:
                throw new RuntimeException("Invalid BankMandate Page Theme: " + theme);
        }
    }
}
