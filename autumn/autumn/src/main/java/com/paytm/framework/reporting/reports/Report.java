package com.paytm.framework.reporting.reports;

import java.io.File;

/**
 * Created by deepakkumar on 16/3/18.
 */
public interface Report

{
    void step(String s, Object... valuesToBeReplacePlaceholders);

    void info(String s, Object... valuesToBeReplacePlaceholders);

    void warn(String s, Object... valuesToBeReplacePlaceholders);

    void error(String s, Object... valuesToBeReplacePlaceholders);

    void debug(String s, Object... valuesToBeReplacePlaceholders);

    void attachHtml(File file);

    void attachImage(File file, String imageName);

}
