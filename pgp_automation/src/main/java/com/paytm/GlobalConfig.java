package com.paytm;

import com.paytm.framework.utils.PropertyUtil;

import java.io.File;
import java.time.Duration;

public class GlobalConfig {

    public static final String PLATFORM;
    public static final String BROWSER;
    public static final Duration MAX_PAGE_LOAD_WAIT_TIME;
    public static final Duration MAX_ELEMENT_LOAD_WAIT_TIME;
    public static final String TEST_CASE_RETRY_COUNT;
    public static final String TEST_STEP_RETRY_COUNT;
    public static final String EXECUTION_ENVIRONMENT;
    public static final String HUB_NODE_URL;


    static {
        try {
            PropertyUtil.getInstance().load(new File("globalconfig.properties"));
            PLATFORM = System.getProperty("OperatingSystem");
            BROWSER = PropertyUtil.getInstance().getValue("BROWSER");
            MAX_PAGE_LOAD_WAIT_TIME = Duration.parse(PropertyUtil.getInstance().getValue("MAX_PAGE_LOAD_WAIT_TIME"));
            MAX_ELEMENT_LOAD_WAIT_TIME = Duration.parse(PropertyUtil.getInstance().getValue("MAX_ELEMENT_LOAD_WAIT_TIME"));
            TEST_CASE_RETRY_COUNT = PropertyUtil.getInstance().getValue("TEST_CASE_RETRY_COUNT");
            TEST_STEP_RETRY_COUNT = PropertyUtil.getInstance().getValue("TEST_STEP_RETRY_COUNT");
            EXECUTION_ENVIRONMENT = PropertyUtil.getInstance().getValue("EXECUTION_ENVIRONMENT");
            HUB_NODE_URL = PropertyUtil.getInstance().getValue("HUB_NODE_URL");

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Something wrong !!! Check configurations.", e);
        }
    }
}