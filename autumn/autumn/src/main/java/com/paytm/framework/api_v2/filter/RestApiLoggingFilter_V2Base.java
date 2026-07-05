package com.paytm.framework.api_v2.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.qameta.allure.Step;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ankuragarwal
 *
 * RestApiLoggingFilter_V2Base holds logging of rest api.
 */
public class RestApiLoggingFilter_V2Base implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiLoggingFilter_V2Base.class);
    private FilterableRequestSpecification requestSpec;
    private FilterableResponseSpecification responseSpec;
    private StringBuilder reqResp;
    private FilterContext ctx;

    public RestApiLoggingFilter_V2Base() {
    }


    @Step("{0}")
    private Response printStep(String path) {
        reqResp = new StringBuilder();
        logBuilder(reqResp, "API : " + this.requestSpec.getBaseUri() + path);
        logRequest();

        Response response = this.ctx.next(this.requestSpec, this.responseSpec);

        logResponse(response);

        LOGGER.info(reqResp.toString());
        return response;
    }

    private void logBuilder(StringBuilder obj, String input) {
        obj.append(input);
        obj.append(System.lineSeparator());
    }

    private void logRequest() {
        this.requestSpec.getHeaders().asList()
                .forEach(header -> {
                    logBuilder(reqResp, "[REQUEST] Header name: " + header.getName() + " ---> value: " + header.getValue());
                });
        this.requestSpec.getFormParams()
                .forEach((k, v) -> {
                    logBuilder(reqResp, "formParam: " + k + " ---> value: " + v);
                });
        this.requestSpec.getQueryParams()
                .forEach((k, v) -> {
                    logBuilder(reqResp, "queryParam: " + k + " ---> value: " + v);
                });
        if (null == this.requestSpec.getBody()) {
        } else {
            try {
                Gson GSON = new GsonBuilder().setPrettyPrinting().create();
                String prettyJsonString = GSON.toJson(JsonParser.parseString(this.requestSpec.getBody()));
                logBuilder(reqResp, prettyJsonString);
            } catch (Exception e) {
                logBuilder(reqResp, "Error occurred while logging request");
                LOGGER.error("Error occurred while logging request", e);
            }
        }
    }

    private void logResponse(Response response) {
        logBuilder(reqResp, "[RESPONSE] status code: " + response.statusCode());
        String responseString = "";
        try {
            responseString = response.jsonPath().prettify();
            logBuilder(reqResp, "[RESPONSE]: " + responseString);
        } catch (Exception var5) {
            LOGGER.error("ERROR in generating prettyJson", var5.getMessage());
            logBuilder(reqResp, "[RESPONSE]: " + response.body().asString());
        }
        response.headers().asList()
                .forEach(header -> {
                    logBuilder(reqResp, "[RESPONSE] Header name: " + header.getName() + " ---> value: " + header.getValue());
                });
    }


    @Override
    public Response filter(FilterableRequestSpecification filterableRequestSpecification, FilterableResponseSpecification filterableResponseSpecification, FilterContext filterContext) {

        this.requestSpec = filterableRequestSpecification;
        this.responseSpec = filterableResponseSpecification;
        this.ctx = filterContext;
        String endpoint = requestSpec.getBasePath();
        Response response = this.printStep(endpoint);
        return response;
    }
}
