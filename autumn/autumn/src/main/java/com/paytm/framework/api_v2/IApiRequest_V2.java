package com.paytm.framework.api_v2;

import io.restassured.http.ContentType;

import java.util.Map;

/**
 * @author ankuragarwal
 *
 * Defines basic struture of API, which needs to defined while creating API
 *
 * @param <T>
 */
public interface IApiRequest_V2<T> {

    /**
     * Implementation can used like: <br>
     * - method can return the request as Java DTO <br>
     * <p>
     * - method can also used to set the base request param setup <br>
     * <p>
     * - Priority of setting request param is 1 and can override other value of same key. <br>
     * - Use setContext on this method only when KEY:VALUE is constant
     *
     * @return Generic
     */
    T requestBody();

    ApiBuilder_V2Base.MethodType apiMethod();

    Map headers();

    String basePath();

    String baseUrl();

    ContentType contentType();

    Map<String, ?> queryParam();

    T build();

}
