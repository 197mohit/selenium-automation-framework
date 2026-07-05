package com.paytm.framework.reporting.reports;


import org.testng.Reporter;

import java.io.File;

/**
 * Created by deepakkumar on 17/3/18.
 */
public class ReportNGReport implements Report {

    @Override
    public void step(String s, Object... valuesToBeReplacePlaceholders) {
        Reporter.log("<b><br><font color=\"blue\">STEP:</font> " + formatLoggingString(s, valuesToBeReplacePlaceholders), true);
    }

    @Override
    public void info(String s, Object... valuesToBeReplacePlaceholders) {
        Reporter.log("<br><font color=\"banana yellow\">INFO:</font> " + formatLoggingString(s, valuesToBeReplacePlaceholders), true);
    }

    @Override
    public void warn(String s, Object... valuesToBeReplacePlaceholders) {
        Reporter.log("<br><font color=\"orange\">WARN:</font> " + formatLoggingString(s, valuesToBeReplacePlaceholders), true);
    }

    @Override
    public void error(String s, Object... valuesToBeReplacePlaceholders) {
        Reporter.log("<br><font color=\"red\">ERROR:</font> " + formatLoggingString(s, valuesToBeReplacePlaceholders), true);
    }

    @Override
    public void debug(String s, Object... valuesToBeReplacePlaceholders) {
        Reporter.log("<br><font color=\"red\">DEBUG:</font> " + formatLoggingString(s, valuesToBeReplacePlaceholders), true);
    }

    @Override
    public void attachHtml(File file) {
        Reporter.log("<br><a href=\"" + file.getName() + "\" target=\"_blank\"><b>API Response: </b></a>");
    }

    @Override
    public void attachImage(File file, String imageName) {

    }


    private String formatLoggingString(String s, Object... valuesToBeReplacePlaceholders) {
        for (Object o : valuesToBeReplacePlaceholders) {
            s = s.replaceFirst("\\{\\}", o.toString());
        }
        return s;
    }
}
