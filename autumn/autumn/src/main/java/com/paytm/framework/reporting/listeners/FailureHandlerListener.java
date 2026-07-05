package com.paytm.framework.reporting.listeners;

import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.contants.FeaturesEvaluator;
import com.paytm.framework.reportportal.service.GenerateFailedXML;
import org.apache.commons.io.FileUtils;
import org.testng.*;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import java.io.*;
import java.util.*;

public class FailureHandlerListener implements IReporter {
    public final String MASTER_TESTNG_FAILED_XML =
            FeaturesEvaluator.FEATURE_GENERATE_FAILED_XML.equals(FeaturesEvaluator.FEATURE_VALUE.ENABLED)
                    ? "master-" + GenerateFailedXML.TESTNG_FAILED_XML : "master-testng-failed.xml";
    public final String TESTNG_FAILED_XML =
            FeaturesEvaluator.FEATURE_GENERATE_FAILED_XML.equals(FeaturesEvaluator.FEATURE_VALUE.ENABLED)
                    ? GenerateFailedXML.TESTNG_FAILED_XML : "testng-failed.xml";

    @Override
    public void generateReport(
            List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        System.out.println("generateReport outputDirectory: " + outputDirectory);
        XmlSuite masterFailedSuite = new XmlSuite();
        masterFailedSuite.setName("master-testng-failed");
        List<String> failedSuiteLists = new ArrayList<>();
        if (suites.size() > 1) {
            for (int i = 0; i < suites.size(); i++) {
                if (null == suites.get(i).getXmlSuite().getParentSuite()) {
                    continue;
                }
                Map<String, ISuiteResult> results = suites.get(i).getResults();
                for (Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
                    ISuiteResult suiteResult = entry.getValue();
                    ITestContext testContext = suiteResult.getTestContext();

                    if ((null != testContext.getFailedTests().getAllResults()
                            || null != testContext.getSkippedTests().getAllResults())
                            && (testContext.getFailedTests().getAllResults().size() > 0
                            || testContext.getSkippedTests().getAllResults().size() > 0)) {
                        failedSuiteLists.add(suites.get(i).getOutputDirectory() + "/" + TESTNG_FAILED_XML);
                        break;
                    }
                }
            }
            if (failedSuiteLists.size() > 0) {
                masterFailedSuite.setSuiteFiles(failedSuiteLists);
                Utils.writeUtf8File(outputDirectory, MASTER_TESTNG_FAILED_XML, masterFailedSuite.toXml());
            }
        } else {
            File source = new File(outputDirectory + "/" + TESTNG_FAILED_XML);
            if (source.exists()) {
                File dest = new File(outputDirectory + "/" + MASTER_TESTNG_FAILED_XML);
                try {
                    FileUtils.copyFile(source, dest);
                } catch (IOException e) {
                    Reporter.log("Couldn't generateReport "+e.getMessage());
                }
            } else {
                System.out.println("source file not available at: " + source.getPath());
            }
        }
    }
}
