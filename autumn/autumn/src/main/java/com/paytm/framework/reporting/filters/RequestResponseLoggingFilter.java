package com.paytm.framework.reporting.filters;

import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reportportal.ReporterConfig;
import io.qameta.allure.Step;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Filter to print API in allure report
 */
public class RequestResponseLoggingFilter implements Filter {

    private FilterableRequestSpecification requestSpec;
    private FilterableResponseSpecification responseSpec;
    private FilterContext ctx;

    @Step("{0}")
    private Response printStep(String path) {
        Response response = this.ctx.next(this.requestSpec, this.responseSpec);
        String responseString = "";
        try {
            responseString = response.jsonPath().prettyPrint();
        } catch (Exception ex) { }
        Reporter.report.step(responseString);
        return response;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
        this.ctx = ctx;

        String endpoint = requestSpec.getBasePath();
        String baseUri = requestSpec.getBaseUri();
        if(baseUri.contains(ReporterConfig.RP_ENDPOINT)){
            return this.ctx.next(this.requestSpec, this.responseSpec);
        }
        Response response = printStep(endpoint);
        return response;
    }
}
