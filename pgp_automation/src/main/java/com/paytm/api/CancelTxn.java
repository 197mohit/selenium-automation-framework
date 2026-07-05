package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

    public class CancelTxn extends BaseApi {


    public CancelTxn(Constants.MerchantType mid,String OrderId,String txnToken) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().addParam("MID",mid.getId());
        getRequestSpecBuilder().addParam("ORDER_ID",OrderId);
        getRequestSpecBuilder().addParam("txnToken",txnToken);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CANCEL_TXN);
    }


        public CancelTxn(Constants.MerchantType mid,String OrderId,String txnToken,String token) {
            setMethod(MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.URLENC);
            getRequestSpecBuilder().setAccept(ContentType.URLENC);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().addParam("MID",mid.getId());
            getRequestSpecBuilder().addParam("ORDER_ID",OrderId);
            getRequestSpecBuilder().addParam("txnToken",txnToken);
            getRequestSpecBuilder().addParam("token",txnToken);
            getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CANCEL_TXN);
        }
}
