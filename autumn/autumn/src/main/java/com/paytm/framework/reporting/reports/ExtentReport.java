package com.paytm.framework.reporting.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.paytm.framework.reporting.Extent;
import com.paytm.framework.reporting.Reporter;

import java.io.File;
import java.io.IOException;

/**
 * Created by deepakkumar on 16/3/18.
 */
public class ExtentReport implements Report {

    private final ExtentReports extent = Extent.REPORT;
    private final ThreadLocal<ExtentTest> test = Extent.TEST;

    public ExtentReport(final File file) {
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            Reporter.report.error("Couldn't create file in  ExtentReport"+e.getMessage());
        }
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(file.getPath());
        htmlReporter.config().setTestViewChartLocation(ChartLocation.BOTTOM);
        htmlReporter.config().setChartVisibilityOnOpen(false);
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle(file.getName());
        htmlReporter.config().setEncoding("utf-8");
        htmlReporter.config().setReportName(file.getName());
        this.extent.attachReporter(htmlReporter);
    }

    public ExtentReport() {
        this(new File(System.getProperty("REPORT_MAIN_DIRECTORY") + "/html/extentReport.html"));
    }

    @Override
    public void step(String s, Object... valuesToBeReplacePlaceholders) {
        this.test.get().info(formatLoggingString("<b>" + s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void info(String s, Object... valuesToBeReplacePlaceholders) {
        this.test.get().info(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void warn(String s, Object... valuesToBeReplacePlaceholders) {
        this.test.get().warning(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void error(String s, Object... valuesToBeReplacePlaceholders) {
        this.test.get().error(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void debug(String s, Object... valuesToBeReplacePlaceholders) {
        this.test.get().debug(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void attachImage(File file, String imageName) {
        try {
            this.test.get().addScreenCaptureFromPath(file.getName());
        } catch (IOException e) {
            warn("Exception occured {}", e);
        }
    }

    @Override
    public void attachHtml(File file) {
            this.test.get().info("<a href=\"" + file.getName() + "\" target=\"_blank\"><b>API Response</b></a><br>");
    }

    private String formatLoggingString(String s, Object... valuesToBeReplacePlaceholders) {
        for (Object o : valuesToBeReplacePlaceholders) {
            s = s.replaceFirst("\\{\\}", o.toString());
        }
        return s;
    }
}
