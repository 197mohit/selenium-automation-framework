package com.paytm.utils.merchant.util.exception.walletException;

import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

/**
 * Created by deepakkumar on 3/4/18.
 */
public class WalletException extends RuntimeException {

    private static final long serialVersionUID = 1370591078363029363L;

    private Response apiResponse;

    private List<Map<String, Object>> dbResult;

    public WalletException(String message) {
        super(message);
    }

    public WalletException(Throwable cause) {
        super(cause);
    }

    public WalletException(String message, Throwable cause) {
        super(message, cause);
    }


    public WalletException(String message, Response apiResponse) {
        super(message);
        this.apiResponse = apiResponse;
    }


    public WalletException(String message, Throwable cause, Response apiResponse) {
        super(message, cause);
        this.apiResponse = apiResponse;
    }

    public WalletException(String message, List<Map<String, Object>> dbResult) {
        super(message);
        this.dbResult = dbResult;
    }

    public WalletException(String message, Throwable cause, List<Map<String, Object>> dbResult) {
        super(message, cause);
        this.dbResult = dbResult;
    }

    public Response getApiResponse() {
        return apiResponse;
    }

    public List<Map<String, Object>> getDbResult() {
        return dbResult;
    }
}
