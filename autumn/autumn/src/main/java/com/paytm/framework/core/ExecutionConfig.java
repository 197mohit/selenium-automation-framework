package com.paytm.framework.core;

import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.framework.utils.PropertyUtil;

import java.time.Duration;

public class ExecutionConfig {

    public static final String PLATFORM;
    public static final String BROWSER;
    public static final Duration MAX_PAGE_LOAD_WAIT_TIME;
//    public static final int MAX_PAGE_LOAD_WAIT_TIME;
    //public static final int MAX_ELEMENT_LOAD_WAIT_TIME;
    public static final Duration MAX_ELEMENT_LOAD_WAIT_TIME;
    public static final int TEST_CASE_RETRY_COUNT;
    public static final String EXECUTION_ENVIRONMENT;
    public static final String HUB_NODE_URL;
    public static final String SEND_EXEC_REPORT_EMAIL;
    public static final String SMTP_HOSTNAME;
    public static final String SMTP_PORT;
    public static final String SMTP_USERNAME;
    public static final String SMTP_PASSWORD;
    public static final String TEMP_DATA_PATH;
    public static final String EXEC_REPORT_EMAIL_RECEIVER;
    public static final String MOBILE_EMULATION;
    public static final String SUITE_XML_FILE;
    public static final String CURRENT_PROFILE;
    public static final String ANALYSIS_PATH;
    public static final String USER_AGENT;

    static {
        try {
            PLATFORM = loadProperty("PLATFORM", "mac");
            BROWSER = loadProperty("BROWSER", "chrome");
            MAX_PAGE_LOAD_WAIT_TIME = Duration.parse(loadProperty("MAX_PAGE_LOAD_WAIT_TIME", "PT60S"));
//            MAX_PAGE_LOAD_WAIT_TIME = Integer.parseInt(loadProperty("MAX_PAGE_LOAD_WAIT_TIME", "60"));
            MAX_ELEMENT_LOAD_WAIT_TIME = Duration.parse(loadProperty("MAX_ELEMENT_LOAD_WAIT_TIME", "PT30S"));
            TEST_CASE_RETRY_COUNT =Integer.parseInt( loadProperty("TEST_CASE_RETRY_COUNT","0"));
            EXECUTION_ENVIRONMENT = loadProperty("EXECUTION_ENVIRONMENT", "local");
            HUB_NODE_URL = loadProperty("HUB_NODE_URL", "");
            SMTP_HOSTNAME = loadProperty("SMTP_HOSTNAME", "");
            SMTP_PORT = loadProperty("SMTP_PORT", "");
            SMTP_USERNAME = loadProperty("SMTP_USERNAME", "");
            SMTP_PASSWORD = loadProperty("SMTP_PASSWORD", "");
            TEMP_DATA_PATH = CommonUtils.getPathWithValidSeperator(loadProperty("TEMP_DATA_DIRECTORY", ""));
            SEND_EXEC_REPORT_EMAIL = loadProperty("SEND_EXEC_REPORT_EMAIL", "");
            EXEC_REPORT_EMAIL_RECEIVER = loadProperty("EXEC_REPORT_EMAIL_RECEIVER", "");
            MOBILE_EMULATION = loadProperty("MOBILE_EMULATION","");
            CommonUtils.createDirectory(TEMP_DATA_PATH);
            SUITE_XML_FILE = loadProperty("suiteXmlFile", "").replace(".xml", "").trim();
            CURRENT_PROFILE = loadProperty("currentProfile", "");
            ANALYSIS_PATH = loadProperty("REPORT_ANALYSIS_DIR", "");
            USER_AGENT = loadProperty("USER_AGENT", "");

        }catch (Throwable e) {
            Reporter.report.error("Something wrong !!! Check configurations."+e.getMessage());
            throw new RuntimeException("Something wrong !!! Check configurations.", e);
        }
    }

    private static String loadProperty(String vairableName, String def) {
        String temp = System.getProperty(vairableName, def);
        System.out.println("PROPERTY LOADED " + vairableName + ": " + temp);
        return temp;
    }
}