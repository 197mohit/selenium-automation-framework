package com.paytm.utils.merchant.util.exception.authException;

import io.restassured.response.Response;

/**
 * Created by deepakkumar on 3/4/18.
 */
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 2748158608178628770L;

    private Response apiResponse;


    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

    public AuthException(String message, Response apiResponse) {
        super(message);
        this.apiResponse = apiResponse;
    }

    public AuthException(String message, Throwable cause, Response apiResponse) {
        super(message, cause);
        this.apiResponse = apiResponse;
    }

    public Response getApiResponse() {
        return apiResponse;
    }
}
