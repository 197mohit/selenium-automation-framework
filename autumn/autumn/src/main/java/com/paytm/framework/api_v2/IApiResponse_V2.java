package com.paytm.framework.api_v2;

import io.restassured.response.Response;

import java.util.Map;

/**
 * @author ankuragarwal
 * @param <T>
 */
public interface IApiResponse_V2<T> {

    T body();

    String content();

    int statusCode();

    Response response();

    Map headers();

}
