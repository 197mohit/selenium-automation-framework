//package com.paytm.listeners;
//
//import com.aventstack.extentreports.ExtentTest;
//import com.aventstack.extentreports.service.ExtentTestManager;
//import com.aventstack.extentreports.testng.listener.ExtentITestListenerAdapter;
//import com.jayway.restassured.module.jsv.JsonSchemaValidationException;
//import io.restassured.internal.http.HttpResponseDecorator;
//import io.restassured.internal.http.HttpResponseException;
//import io.restassured.internal.http.ResponseParseException;
//import io.restassured.path.json.exception.JsonPathException;
//import io.restassured.path.xml.exception.XmlPathException;
//import org.openqa.selenium.WebDriverException;
//import org.testng.ITestResult;
//
//import java.net.MalformedURLException;
//
//public class CustomExtentITestListenerAdapter extends ExtentITestListenerAdapter {
//    @Override
//    public synchronized void onTestFailure(ITestResult result) {
//        Throwable e = result.getThrowable();
//        ExtentTest test = ExtentTestManager.getTest(result);
//        String debugMsg = "";
//        boolean isExceptionHandled = false;
//        SeleniumExceptionHandler:
//        {
//            if (e instanceof WebDriverException) {
//                String supportUrl = "https://www.katalon.com/resources-center/blog/selenium-exceptions/";
//                if (e.getLocalizedMessage().contains("keys should be a string")) {
//                    debugMsg = "You have passed non-string value in input.";
//                }
//                debugMsg = debugMsg.concat("Visit <a target='_blank' href='" + supportUrl + "'>selenium exceptions</a> for more info.");
//                isExceptionHandled = true;
//            }
//        }
//        RestAssuredExceptionsHandler:
//        {
//            if (e instanceof ResponseParseException) {
//                debugMsg = "Response body is parsed unsuccessfully. This most often occurs when a server returns an error status code and sends a different content-type body from what was expected.";
//                HttpResponseDecorator res = ((ResponseParseException) e).getResponse();
//                test.debug("Content Type: " + res.getContentType());
//                test.debug("Status: " + res.getStatus());
//                test.debug("API Response: " + res);
//                isExceptionHandled = true;
//            }
//            else if (e instanceof HttpResponseException) {
//                HttpResponseDecorator res = ((HttpResponseException) e).getResponse();
//                test.debug("Content Type: " + res.getContentType());
//                test.debug("Headers: " + res.getAllHeaders());
//                test.debug("Query Params: " + res.getParams());
//                test.debug("Status: " + res.getStatus());
//                test.debug("API Response: " + res);
//                isExceptionHandled = true;
//            } else if (e instanceof JsonSchemaValidationException) {
//                debugMsg = "Something went wrong during JSON Schema validation";
//                isExceptionHandled = true;
//            } else if (e instanceof JsonPathException) {
//                debugMsg = "Something went wrong during JSON parsing";
//                isExceptionHandled = true;
//            } else if (e instanceof XmlPathException) {
//                debugMsg = "Something went wrong during XML parsing";
//                isExceptionHandled = true;
//            } else if (e instanceof MalformedURLException) {
//                debugMsg = "Something wrong with your API Url - Either no legal protocol could be found in specification string Or “the string could not be parsed.";
//                isExceptionHandled = true;
//            }
//        }
//        if (isExceptionHandled) {
//            test.debug(debugMsg);
//            e.setStackTrace(new StackTraceElement[]{});//to remove unwanted stacktrace which doesn't help in debugging
//            result.setStatus(3);//marking test as skip
//        }
//        super.onTestFailure(result);
//    }
//}
