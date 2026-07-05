package com.paytm.framework.reporting.reports;


import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


/**
 * Created by nikunjkumar on 28/3/18.
 */
public class AllureReport implements Report {

    @Override
    public void step(String s, Object... valuesToBeReplacePlaceholders) {
        innerStep(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void info(String s, Object... valuesToBeReplacePlaceholders) {
        innerStep(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void warn(String s, Object... valuesToBeReplacePlaceholders) {
        innerStep(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void error(String s, Object... valuesToBeReplacePlaceholders) {
        innerStep(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void debug(String s, Object... valuesToBeReplacePlaceholders) {
        innerStep(formatLoggingString(s, valuesToBeReplacePlaceholders));
    }

    @Override
    public void attachHtml(File file) {
        attachHTMLFile(file);
    }

    @Override
    public void attachImage(File file, String imageName) {
        if (!imageName.isEmpty())
            attachScreenshot(file, imageName);
        else
            attachScreenshot(file, "Screenshot");
    }

    private String formatLoggingString(String s, Object... valuesToBeReplacePlaceholders) {
        for (Object o : valuesToBeReplacePlaceholders) {
            s = s.replaceFirst("\\{\\}", o.toString());
        }
        return s;
    }

    @Step("{0}")
    private void innerStep(String step) {
    }

    @Attachment(value = "{1}", type = "image/png")
    private byte[] attachScreenshot(File file, String imageName) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException ex) {
            warn("Exception occured {}", ex);
            return null;
        }
    }

    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] attachScreenshot(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException ex) {
            warn("Exception occured {}", ex);
            return null;
        }
    }

    @Attachment(value = "Response", type = "text/html")
    private byte[] attachHTMLFile(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException ex) {
            warn("Exception occured {}", ex);
            return null;
        }
    }
}
