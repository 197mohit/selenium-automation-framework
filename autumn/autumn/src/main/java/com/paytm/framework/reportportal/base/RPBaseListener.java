package com.paytm.framework.reportportal.base;

import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.contants.FeaturesEvaluator;
import com.paytm.framework.reportportal.contants.ItemStatusEnum;
import com.paytm.framework.reportportal.service.GenerateFailedXML;
import com.paytm.framework.reportportal.service.RPTestNGServiceImpl;
import org.testng.*;
import org.testng.internal.IResultListener2;
import org.testng.xml.XmlSuite;

import java.util.List;

public class RPBaseListener implements IExecutionListener, ISuiteListener, IResultListener2, IReporter {

    private RPTestNGServiceImpl rpTestNGService;

    public RPBaseListener(RPTestNGServiceImpl rpTestNGService) {
        this.rpTestNGService = rpTestNGService;
    }

    @Override
    public void onExecutionStart() {
        rpTestNGService.startLaunch();
    }

    @Override
    public void onExecutionFinish() {
        rpTestNGService.finishLaunch();
    }

    @Override
    public void onStart(ISuite suite) {
        rpTestNGService.startTestSuite(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        rpTestNGService.finishTestSuite(suite);
    }

    @Override
    public void onStart(ITestContext testContext) {
        rpTestNGService.startTest(testContext);
    }

    @Override
    public void onFinish(ITestContext testContext) {
        rpTestNGService.finishTest(testContext);
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        rpTestNGService.startTestMethod(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        rpTestNGService.finishTestMethod(ItemStatusEnum.PASSED, testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        rpTestNGService.sendReportPortalMsg(testResult);
        rpTestNGService.finishTestMethod(ItemStatusEnum.FAILED, testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        rpTestNGService.sendReportPortalMsg(testResult);
        rpTestNGService.finishTestMethod(ItemStatusEnum.SKIPPED, testResult);
    }

    @Override
    public void beforeConfiguration(ITestResult testResult) {
        rpTestNGService.startConfiguration(testResult);
    }

    @Override
    public void onConfigurationFailure(ITestResult testResult) {
        rpTestNGService.sendReportPortalMsg(testResult);
        rpTestNGService.finishTestMethod(ItemStatusEnum.FAILED, testResult);
    }

    @Override
    public void onConfigurationSuccess(ITestResult testResult) {
        rpTestNGService.finishTestMethod(ItemStatusEnum.PASSED, testResult);
    }

    @Override
    public void onConfigurationSkip(ITestResult testResult) {
        rpTestNGService.sendReportPortalMsg(testResult);
//        rpTestNGService.startConfiguration(testResult);
        rpTestNGService.finishTestMethod(ItemStatusEnum.SKIPPED, testResult);
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        switch (FeaturesEvaluator.FEATURE_GENERATE_FAILED_XML){
            case ENABLED:
                System.out.println("FAILED TESTS REPORT GENERATION STARTED ===========================");
                new GenerateFailedXML().generateReport(xmlSuites, suites, outputDirectory);
                break;
        }
//        if (ReporterConfig.generateFailedXml) {
//            System.out.println("FAILED TESTS REPORT GENERATION STARTED ===========================");
//            new GenerateFailedXML().generateReport(xmlSuites, suites, outputDirectory);
//        }
    }
}
