package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.TreeMap;

public class RefundApi extends BaseApi {
    @Deprecated
    public RefundApi(String mid, String orderId, String refId, String refundAmount, String txnId, String postConvFlag) {
        APIBuilder:
        {
            setMethod(BaseApi.MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.REFUND);
            if (postConvFlag == null || postConvFlag.isEmpty()) {
                getRequestSpecBuilder().addParam("JsonData", "{\"MID\": \"" + mid + "\", \"ORDERID\": \"" + orderId + "\", \"REFID\": \"" + refId + "\", \"CHECKSUM\": \"0\", \"COMMENTS\":\"Initiate Refund.....\", \"TXNTYPE\": \"REFUND\", \"REFUNDAMOUNT\": \"" + refundAmount + "\",\"TXNID\":\"" + txnId + "\"}");
            } else {
                getRequestSpecBuilder().addParam("JsonData", "{\"MID\": \"" + mid + "\", \"ORDERID\": \"" + orderId + "\", \"REFID\": \"" + refId + "\", \"CHECKSUM\": \"0\", \"COMMENTS\":\"Initiate Refund.....\", \"TXNTYPE\": \"" + postConvFlag + "\", \"REFUNDAMOUNT\": \"" + refundAmount + "\",\"TXNID\":\"" + txnId + "\"}");
            }
        }
    }

    public RefundApi(String mid, String merchantKey, String orderId, String refId, String refundAmount, String txnId, String postConvFlag) {
        TreeMap<String, String> treemap = new TreeMap<>();
        String checksum = "";
        APIBuilder:
        {
            setMethod(MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.REFUND);
            treemap.put("MID", mid);
            treemap.put("ORDERID", orderId);
            treemap.put("REFID", refId);
            treemap.put("COMMENTS", "COMMENTS");
            treemap.put("REFUNDAMOUNT", refundAmount);
            treemap.put("TXNID", txnId);
            if (postConvFlag == null || postConvFlag.isEmpty()) {
                treemap.put("TXNTYPE", "REFUND");
                checksum = PGPUtil.getChecksum(merchantKey, treemap);
                treemap.put("CHECKSUM", checksum);
            } else {
                treemap.put("TXNTYPE", postConvFlag);
                checksum = PGPUtil.getChecksum(merchantKey, treemap);
                treemap.put("CHECKSUM", checksum);
            }
            getRequestSpecBuilder().setBody(treemap);
        }
    }
}





