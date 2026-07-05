package com.paytm.framework.reporting.listeners;

import com.paytm.framework.reporting.Reporter;
import org.testng.IAlterSuiteListener;
import org.testng.xml.*;

import java.util.*;


/**
 * Implementations of this interface will gain access to the XmlSuite object and thus let users be able to alter a suite or a test based on their own needs. This listener can be added ONLY via the following two ways :
 * <listeners> tag in a suite file.
 * via Service loaders
 */
public abstract class DynamicRunner implements IAlterSuiteListener {
    XmlPackage testPackage=null;

    @Override
    public void alter(List<XmlSuite> suites) {
        this.testPackage = new XmlPackage(System.getProperty("package", suites.get(0).getParameter("package")));
        List<XmlSuite> suite=buildSuites(getTestCondition(),suites);
    }

    private List<XmlSuite> buildSuites(String testCondition,List<XmlSuite> suites){
        testCondition=testCondition.replace("\"","");
       String[] totalSuites= testCondition.split("\\+")[1].split(";");
       List<XmlSuite> xmlSuitesList=suites;

       for(String suite:totalSuites){
           XmlSuite xmlSuite = new XmlSuite();
           xmlSuite.setName(suite.split("@")[0]);
           xmlSuite.setParallel(XmlSuite.ParallelMode.TESTS);
           xmlSuite.setThreadCount(1);
           xmlSuite.setVerbose(1);

           xmlSuite.setListeners(getListener());
           try {
               xmlSuite.setTests(getTests(suite.split("@")[1],xmlSuite));
           } catch (Exception e) {
               Reporter.report.error("Couldn't buildSuites "+e.getMessage());
           }
           //xmlSuite.setParameters(setSuiteParameter(xmlSuite));
           //xmlSuite.setConfigFailurePolicy(XmlSuite.FailurePolicy.SKIP);
           xmlSuitesList.add(xmlSuite);
       }
        xmlSuitesList.remove(0);
        return xmlSuitesList;

    }

    private List<XmlTest> getTests(String testCondition,XmlSuite xmlSuite)throws Exception{
        List<XmlTest> xmlTestsList=new ArrayList<>();
        String[] totalTests= testCondition.split(",");
        for(String test:totalTests) {
            XmlTest xmlTest=new XmlTest(xmlSuite);
            xmlTest.setName(test.split("=")[0]);
            String selectionCriteria=test.split("=")[1];
            if(test.split("=")[1].split(":").length>1) {
                for (String s : test.split("=")[1].split(":")[1].split("_")) {
                    getTestParametersDefinition(s)
                            .forEach((key, value) ->
                                    xmlTest.addParameter(key, value));
                }
            }
            xmlTest.setXmlPackages(Collections.singletonList(testPackage));
            //XmlClass xmlClass=new XmlClass();
            //xmlClass.setName("scripts.Dummy");
            //xmlTest.setClasses(Arrays.asList(xmlClass));
            xmlTest.setVerbose(1);
            xmlTest.setMethodSelectors(getMethodSelector(selectionCriteria));
            xmlTestsList.add(xmlTest);
        }

        return xmlTestsList;
    }

    public abstract String getTestCondition();
    public abstract List<XmlMethodSelector> getMethodSelector(String selectionCriteria);
    public abstract List<String> getListener();
    public abstract HashMap<String,String> getTestParametersDefinition(String paramKey) throws Exception;

}
