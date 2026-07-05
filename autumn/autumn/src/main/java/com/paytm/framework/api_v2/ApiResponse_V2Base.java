package com.paytm.framework.api_v2;

import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.response.ValidatableResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ankuragarwal
 *
 * ApiResponse_V2Base represents template of Rest API response
 */
public class ApiResponse_V2Base implements IApiResponse_V2 {

    private Response response = null;

    public ApiResponse_V2Base(Response response) {
        this.response = response;
    }

    @Override
    public ResponseBody body() {
        return this.response.body();
    }

    @Override
    public String content() {
        return this.response.contentType();
    }

    @Override
    public int statusCode() {
        return this.response.statusCode();
    }

    @Override
    public Response response() {
        return this.response;
    }

    @Override
    public Map<String, String> headers() {
        Map<String, String> s = new HashMap<>();
        this.response.headers().asList().
                forEach(header -> {
                    s.put(header.getName(), header.getValue());
                });
        return s;
    }

    public ValidatableResponse validatableResponse() {
        return this.response.then();
    }
}
