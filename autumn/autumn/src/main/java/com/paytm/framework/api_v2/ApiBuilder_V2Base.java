package com.paytm.framework.api_v2;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ankuragarwal
 */
public class ApiBuilder_V2Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiBuilder_V2Base.class);

    private RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();

    private MethodType method;

    public MethodType getMethod() {
        return this.method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public RequestSpecBuilder getRequestSpecBuilder() {
        return this.requestSpecBuilder;
    }

    public Response execute() {
        RequestSpecification requestSpecification = this.requestSpecBuilder.build();
        RestAssured.defaultParser = Parser.JSON;
        RestAssuredConfig config = RestAssured.config();
        Response response;
        switch (this.method) {
            case GET:
                response = (Response) RestAssured.given().config(config).spec(requestSpecification).when().get();
                break;
            case POST:
                response = (Response) RestAssured.given().config(config).spec(requestSpecification).when().post();
                break;
            case PUT:
                response = (Response) RestAssured.given().config(config).spec(requestSpecification).when().put();
                break;
            case DELETE:
                response = (Response) RestAssured.given().config(config).spec(requestSpecification).when().delete();
                break;
            case PATCH:
                response = (Response) RestAssured.given().config(config).spec(requestSpecification).when().patch();
                break;
            default:
                throw new RuntimeException("API method not specified");
        }

        return response;
    }

    public static enum MethodType {
        POST,
        GET,
        PUT,
        DELETE,
        PATCH;

        private MethodType() {
        }
    }

}
