package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Map;

public class DeveloperPaytmPageAPI extends BaseApi {

    public  DeveloperPaytmPageAPI(String url) {
        this.setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = this.getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.URLENC);
        requestSpecBuilder.setAccept(ContentType.URLENC);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        requestSpecBuilder.setBaseUri(LocalConfig.DEV_PAYTM_HOST);
        requestSpecBuilder.setBasePath(url);
    }

    public void getParameters(String xPath) throws XPathExpressionException {
        Response response=this.execute();
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expr = xpath.compile(xPath);
        Object result = expr.evaluate(response.asString());
    }

}
