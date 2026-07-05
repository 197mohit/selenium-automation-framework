package com.paytm.pages.linkbasedservice;

import com.paytm.appconstants.Constants.Theme;

public class LinkOTPLimitFactory {

    public static LinkPaymentOTPLimit getLinkPage(String theme) {
        switch (theme.toLowerCase()) {
            case Theme.ENHANCED_WEB:
                return new LinkPaymentOTPLimitWEB();
            case Theme.ENHANCEDWAP:
                return new LinkPaymentOTPLimitWAP();

            default:
                throw new RuntimeException("Invalid Link Page Theme: " + theme);
        }
    }

}
