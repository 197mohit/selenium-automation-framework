package com.paytm.listeners;

import org.apache.commons.io.FileUtils;
import org.testng.*;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MasterReport implements IReporter  {
    public final String MASTER_TESTNG_FAILED_XML="master-testng-failed.xml";
    public final String TESTNG_FAILED_XML="testng-failed.xml";

    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        System.out.println("generateReport outputDirectory: " + outputDirectory);
        XmlSuite masterFailedSuite = new XmlSuite();
        masterFailedSuite.setName("master-testng-failed");
        List<String> failedSuiteLists = new ArrayList();
        if (suites.size() > 1) {
            label56:
            for(int i = 0; i < suites.size(); ++i) {
                if (null != ((ISuite)suites.get(i)).getXmlSuite().getParentSuite()) {
                    Map<String, ISuiteResult> results = ((ISuite)suites.get(i)).getResults();
                    Iterator var8 = results.entrySet().iterator();

                    ITestContext testContext;
                    do {
                        do {
                            if (!var8.hasNext()) {
                                continue label56;
                            }

                            Map.Entry<String, ISuiteResult> entry = (Map.Entry)var8.next();
                            ISuiteResult suiteResult = (ISuiteResult)entry.getValue();
                            testContext = suiteResult.getTestContext();
                        } while(null == testContext.getFailedTests().getAllResults() && null == testContext.getSkippedTests().getAllResults());
                    } while(testContext.getFailedTests().getAllResults().size() <= 0 && testContext.getSkippedTests().getAllResults().size() <= 0);

                    String var10001 = ((ISuite)suites.get(i)).getOutputDirectory();
                    failedSuiteLists.add(var10001 + "/" + this.TESTNG_FAILED_XML);
                }
            }

            if (failedSuiteLists.size() > 0) {
                masterFailedSuite.setSuiteFiles(failedSuiteLists);
                masterFailedSuite.addListener("com.paytm.listeners.MasterReport");
                masterFailedSuite.addListener("com.paytm.listeners.ScreenListener");
                masterFailedSuite.addListener("com.paytm.listeners.StatusCounterListener");
                Utils.writeUtf8File(outputDirectory, this.MASTER_TESTNG_FAILED_XML, masterFailedSuite.toXml());
            }
        } else {
            File source = new File(outputDirectory + "/" + this.TESTNG_FAILED_XML);
            if (source.exists()) {
                File dest = new File(outputDirectory + "/" + this.MASTER_TESTNG_FAILED_XML);

                try {
                    FileUtils.copyFile(source, dest);
                } catch (IOException var12) {
                    Reporter.log("Couldn't generateReport " + var12.getMessage());
                }
            } else {
                System.out.println("source file not available at: " + source.getPath());
            }
        }

    }
}
