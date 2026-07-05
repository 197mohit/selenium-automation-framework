package testListener;

import com.paytm.LocalConfig;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import scripts.LogCheckBash;

import java.util.Arrays;
import java.util.List;

public class UpdateXmlListener implements IAlterSuiteListener {

    /**
     * @param suites - The list of {@link XmlSuite}s that are part of the current execution.
     */
    @Override
    public void alter(List<XmlSuite> suites) {
        if(LocalConfig.PERFORM_LOGCHECK) {
            System.out.println("including_logcheck");
            addLogCheckBashSuite(suites);
        }
        else {
            System.out.println("Ignoring LogCheckBash");
        }
    }

    private void addLogCheckBashSuite(List<XmlSuite> suites) {
        try {
            XmlSuite logcheckSuite = new XmlSuite();
            logcheckSuite.setName("LogcheckSuite");
            XmlClass logClass = new XmlClass();
            logClass.setClass(LogCheckBash.class);
            XmlTest sampleTest = new XmlTest(logcheckSuite);
            sampleTest.setName("LogCheckBash Test");
            sampleTest.setClasses(Arrays.asList(logClass));
            suites.add(logcheckSuite);
        } catch (Exception e){
            System.out.println("addLogCheckBashSuite error: " + e);
        }
    }
}
