package com.paytm.framework.reporting.reports;

import java.io.File;

/**
 * Created by deepakkumar on 22/3/18.
 */
public class MultiReport implements Report {

    private final Report[] reports;

    public MultiReport(Report... reports) {
        this.reports = reports;
    }

    @Override
    public void step(String s, Object... valuesToBeReplacePlaceholders) {
        for (Report report : this.reports) {
            report.step(s, valuesToBeReplacePlaceholders);
        }
    }

    @Override
    public void info(String s, Object... valuesToBeReplacePlaceholders) {
        for (Report report : this.reports) {
            report.info(s, valuesToBeReplacePlaceholders);
        }
    }

    @Override
    public void warn(String s, Object... valuesToBeReplacePlaceholders) {
        for (Report report : this.reports) {
            report.warn(s, valuesToBeReplacePlaceholders);
        }
    }

    @Override
    public void error(String s, Object... valuesToBeReplacePlaceholders) {
        for (Report report : this.reports) {
            report.error(s, valuesToBeReplacePlaceholders);
        }
    }

    @Override
    public void debug(String s, Object... valuesToBeReplacePlaceholders) {
        for (Report report : this.reports) {
            report.debug(s, valuesToBeReplacePlaceholders);
        }
    }

    @Override
    public void attachImage(File file, String imageName) {
        for (Report report : this.reports) {
            report.attachImage(file, imageName);
        }
    }

    @Override
    public void attachHtml(File file) {
        for (Report report : this.reports) {
            report.info("<a href=\"" + file.getName() + "\" target=\"_blank\"><b>API Response</b></a><br>", "");
        }
    }
}
