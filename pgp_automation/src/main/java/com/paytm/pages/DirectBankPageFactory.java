package com.paytm.pages;

import com.paytm.appconstants.Constants;

public class DirectBankPageFactory {

    public static DirectBankOTPPage getDirectBankPage(String theme) {
        switch (theme.toLowerCase()) {

            case Constants.Theme.ENHANCEDWAP:
                return new DirectBankOtpPageWap();
            case Constants.Theme.ENHANCED_WEB:
                return new DirectBankOTPPage();
            case Constants.Theme.ENHANCED_WAP_REVAMP:
                return new DirectBankOtpPageWap();
            case Constants.Theme.ENHANCED_WEB_REVAMP:
                return new DirectBankOTPPage();
            default:
                throw new RuntimeException("Invalid DirectBank Otp Page Theme: " + theme);
        }
    }

}
