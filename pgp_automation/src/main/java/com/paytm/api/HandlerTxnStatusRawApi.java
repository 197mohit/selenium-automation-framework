package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.TreeMap;

/**
 * API client for GET/POST HANDLER_INTERNAL/TXNSTATUS and HANDLER_INTERNAL/TXNSTATUSLIST
 * with customisable JsonData — used for branch-coverage tests where the request fields
 * need to deviate from the standard helpers.
 *
 * Covers controller paths:
 *   MerchantTxnStatusController.processMerchantTxnStatusGET()     — GET  HANDLER_INTERNAL/TXNSTATUS
 *   MerchantTxnStatusController.processMerchantTxnStatusPOST()    — POST HANDLER_INTERNAL/TXNSTATUS
 *   MerchantTxnStatusController.processMerchantTxnListStatusGET() — GET  HANDLER_INTERNAL/TXNSTATUSLIST
 *   MerchantTxnStatusController.processMerchantTxnListStatusPOST()— POST HANDLER_INTERNAL/TXNSTATUSLIST
 */
public class HandlerTxnStatusRawApi extends BaseApi {

    /**
     * @param endpoint  one of Constants.PGPAPIResourcePath.TXNSTATUS or TXN_STATUS_LIST
     * @param mid       merchant ID
     * @param orderId   order ID
     * @param checksum  CHECKSUMHASH value; pass "" for blank, or fake value for mismatch
     * @param asPost    true → POST, false → GET
     */
    public HandlerTxnStatusRawApi(String endpoint, String mid, String orderId,
                                  String checksum, boolean asPost) {
        String jsonData = "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId
                + "\",\"CHECKSUMHASH\":\"" + checksum + "\"}";

        setMethod(asPost ? MethodType.POST : MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endpoint);
        getRequestSpecBuilder().addParam("JsonData", jsonData);
    }

    /**
     * Build a valid-checksum request.
     */
    public HandlerTxnStatusRawApi(String endpoint, String mid, String orderId,
                                  String merchantKey, boolean asPost, boolean validChecksum) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);
        String checksum = validChecksum ? PGPUtil.getChecksum(merchantKey, treemap) : "INVALID_CHECKSUM";

        String jsonData = "{\"MID\":\"" + mid + "\",\"ORDERID\":\"" + orderId
                + "\",\"CHECKSUMHASH\":\"" + checksum + "\"}";

        setMethod(asPost ? MethodType.POST : MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endpoint);
        getRequestSpecBuilder().addParam("JsonData", jsonData);
    }
}
