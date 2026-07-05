package com.paytm.framework.reportportal;

import com.paytm.framework.utils.PropertyUtil;

import java.util.Arrays;
import java.util.List;

public class ReporterConfig {
    private static final String RP_FILE_NAME="payment_portal.properties";
    public static String RP_ENDPOINT;
    public static String RP_UUID;
    public static String RP_LAUNCH;
    public static String RP_PROJECT;
    public static final String RP_ENABLE;
    public static String RP_DESCRIPTION;
    public static String RP_ATTRIBUTES;
    public static final String RP_CONVERTIMAGE;
    public static final String RP_MODE;
    public static final String RP_SKIPPED_ISSUE;
    public static final String RP_REPORTING_ASYNC;
    public static final String RP_REPORTING_CALLBACK;
    @Deprecated
    public static final String RP_RETRY;
    public static final String RP_RERUN;
    public static String RP_RERUN_OF;
    public static final String GENERATE_FAILED_XML;
    public static String ALREADY_LAUNCHED;
    public static String AUTO_EXEC_HANDLER_URL;

    // mandate check for provided configs
//    public static Boolean mandatoryPropCheck = true;
//    public static Boolean generateFailedXml = false;
//    public static Boolean mandatoryPropCheck_execHandler = false;

    static {
        try{
            PropertyUtil.getInstance().load(RP_FILE_NAME);
        } catch (Throwable ignore){ }
        RP_ENDPOINT = loadVariable("rp.endpoint");
        RP_UUID = loadVariable("rp.uuid");
        RP_LAUNCH = loadVariable("rp.launch");
        RP_PROJECT = loadVariable("rp.project");
        RP_ENABLE = loadVariable("rp.enable");
        RP_DESCRIPTION = loadVariable("rp.description");
        RP_ATTRIBUTES = loadVariable("rp.attributes");
        RP_CONVERTIMAGE = loadVariable("rp.convertimage");
        RP_MODE = loadVariable("rp.mode");
        RP_SKIPPED_ISSUE = loadVariable("rp.skipped.issue");
        RP_RERUN = loadVariable("rp.rerun");
        RP_REPORTING_ASYNC = loadVariable("rp.reporting.async");
        RP_REPORTING_CALLBACK = loadVariable("rp.reporting.callback");
        RP_RETRY = loadVariable("rp.retry");
        RP_RERUN_OF = loadVariable("rp.rerun.of");
        GENERATE_FAILED_XML = loadVariable("generate.failed.xml");
        ALREADY_LAUNCHED = loadVariable("rp.existing.launched");
        AUTO_EXEC_HANDLER_URL = loadVariable("rp.exec.handler.url");

        // Performing mandatory properties check;
//        checkMandatoryProperties(RP_ENDPOINT, RP_UUID, RP_LAUNCH, RP_PROJECT);
//        checkRPEnable();
//        checkRerunConfig();
//        checkGenerateFailedXml();

//        if (mandatoryPropCheck)
//            System.out.println(".............. Cloud Reporting Enabled ..............");
//        else
//            System.out.println(".............. Cloud Reporting Disabled ..............");

//        if (generateFailedXml)
//            System.out.println(".............. Generate Failed XML Enabled ..............");
//        else
//            System.out.println(".............. Generate Failed XML Disabled ..............");
    }


    private static String loadVariable(String vairableName) {
        String temp = System.getProperty(vairableName);
        if (null == temp || temp.isEmpty()) {
            try {
                temp = PropertyUtil.getInstance().getValue(vairableName);
            } catch (Throwable ignored) {
            }
        }
        System.out.println("PROPERTY LOADED " + vairableName + ": " + temp);
        return temp;
    }

//    private static void checkMandatoryProperties(String... properties) {
//        List<String> propToCheck = Arrays.asList(properties);
//        propToCheck.forEach(item -> {
//            if (null != item && !item.isEmpty()) {
//
//            } else
//                mandatoryPropCheck = false;
//        });
//    }
//
//    private static void checkRPEnable() {
//        Boolean flag = Boolean.valueOf(RP_ENABLE);
//        if (!flag)
//            mandatoryPropCheck = false;
//    }
//
//    private static void checkRerunConfig() {
//        Boolean flag = Boolean.valueOf(RP_RERUN);
//        if (flag) {
//            if (null == RP_RERUN_OF || RP_RERUN_OF.isEmpty()) {
//                mandatoryPropCheck = false;
//            }
//        }
//    }

//    private static void checkGenerateFailedXml(){
//        Boolean flag = Boolean.valueOf(GENERATE_FAILED_XML);
//        if(flag){
//            generateFailedXml = true;
//        }
//    }
}
