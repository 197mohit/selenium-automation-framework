package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * API client for GET/POST /merchant-status/getTxnStatus with a fully custom JsonData
 * query parameter.
 *
 * Used for negative/branch-coverage tests where the JsonData fields (MID, ORDERID,
 * CHECKSUMHASH) need to deviate from what the normal {@link TxnStatus} constructors
 * would produce — e.g., blank CHECKSUMHASH, unknown MID, or non-existent ORDERID.
 */
public class TxnStatusRawApi extends BaseApi {

    private static final String ENDPOINT = Constants.PGPAPIResourcePath.TXNSTATUS_CHECKSUM;

    /**
     * Build a GET request with fully custom JsonData query param.
     *
     * @param mid          MID value to embed in JsonData
     * @param orderId      ORDERID value to embed in JsonData
     * @param checksumHash CHECKSUMHASH value; pass {@code ""} to test blank-checksum branch,
     *                     or any fake string to test mismatch branch
     */
    public TxnStatusRawApi(String mid, String orderId, String checksumHash) {
        String jsonData = "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId
                + "\",\"CHECKSUMHASH\":\"" + checksumHash + "\"}";

        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        getRequestSpecBuilder().addParam("JsonData", jsonData);
    }

    /**
     * Build a POST request with fully custom JsonData query param.
     * Same semantic contract as the GET variant.
     */
    public TxnStatusRawApi(String mid, String orderId, String checksumHash, boolean asPost) {
        String jsonData = "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId
                + "\",\"CHECKSUMHASH\":\"" + checksumHash + "\"}";

        setMethod(asPost ? MethodType.POST : MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(ENDPOINT);
        getRequestSpecBuilder().addParam("JsonData", jsonData);
    }
}