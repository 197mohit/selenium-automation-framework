package com.paytm.listeners;
import org.testng.*;
import org.testng.xml.XmlSuite;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassedReport implements IReporter  {
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<ITestResult> passedTests = new ArrayList<>();

        for (ISuite suite : suites) {
            Map<String, ISuiteResult> suiteResults = suite.getResults();
            for (ISuiteResult suiteResult : suiteResults.values()) {
                passedTests.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
            }
        }

        // Generate XML file containing passed test cases
        generateXml(passedTests, outputDirectory);
    }

    private void generateXml(List<ITestResult> passedTests, String outputDirectory) {
        StringBuilder xmlContent = new StringBuilder();
        xmlContent.append("<suite name=\"PassedTestSuite\">");

        Map<String, Map<String, List<String>>> testClassMethodsMap = new HashMap<>();
        Map<String, Map<String, String>> testParametersMap = new HashMap<>();

        for (ITestResult passedTest : passedTests) {
            ITestContext testContext = passedTest.getTestContext();
            Map<String, String> parameters = testContext.getCurrentXmlTest().getAllParameters();

            String testName = testContext.getCurrentXmlTest().getName();
            String className = passedTest.getTestClass().getName();
            String methodName = passedTest.getMethod().getMethodName();

            Map<String, List<String>> classMethodsMap = testClassMethodsMap.computeIfAbsent(testName, k -> new HashMap<>());
            List<String> methods = classMethodsMap.computeIfAbsent(className, k -> new ArrayList<>());
            methods.add(methodName);

            Map<String, String> testParams = testParametersMap.computeIfAbsent(testName, k -> new HashMap<>());
            for (Map.Entry<String, String> paramEntry : parameters.entrySet()) {
                String paramName = paramEntry.getKey();
                String paramValue = paramEntry.getValue();
                testParams.put(paramName, paramValue);
            }
        }

        for (Map.Entry<String, Map<String, List<String>>> testEntry : testClassMethodsMap.entrySet()) {
            String testName = testEntry.getKey();
            Map<String, List<String>> classMethodsMap = testEntry.getValue();
            Map<String, String> testParams = testParametersMap.get(testName);

            xmlContent.append("<test name=\"").append(testName).append("\"");
            if (testParams != null && !testParams.isEmpty()) {
                xmlContent.append(">");
                for (Map.Entry<String, String> paramEntry : testParams.entrySet()) {
                    String paramName = paramEntry.getKey();
                    String paramValue = paramEntry.getValue();
                    xmlContent.append("<parameter name=\"").append(paramName).append("\" value=\"").append(paramValue).append("\"/>");
                }
                xmlContent.append("</test>");
            } else {
                xmlContent.append("/>");
            }

            for (Map.Entry<String, List<String>> classEntry : classMethodsMap.entrySet()) {
                String className = classEntry.getKey();
                List<String> methods = classEntry.getValue();

                xmlContent.append("<class name=\"").append(className).append("\">");

                for (String methodName : methods) {
                    xmlContent.append("<methods>");
                    xmlContent.append("<include name=\"").append(methodName).append("\"/>");
                    xmlContent.append("</methods>");
                }

                xmlContent.append("</class>");
            }
        }

        xmlContent.append("</suite>");

        try {
            // Apply XML formatting
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(xmlContent.toString())));

            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            String formattedXml = stringWriter.toString();

            FileWriter writer = new FileWriter(outputDirectory + "/passeDTests.xml");
            writer.write(formattedXml);
            writer.close();
        } catch (IOException | TransformerException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

    }
    //not used
    private String getParametersAsString(Map<String, String> parameters) {
        StringBuilder parameterString = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            parameterString.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        if (parameterString.length() > 0) {
            parameterString.deleteCharAt(parameterString.length() - 1);
        }
        return parameterString.toString();
    }

}

