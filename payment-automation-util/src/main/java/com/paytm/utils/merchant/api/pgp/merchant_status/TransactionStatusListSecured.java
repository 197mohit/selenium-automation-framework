package com.paytm.utils.merchant.api.pgp.merchant_status;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.util.TreeMap;

public class TransactionStatusListSecured extends BaseApi {


    public TransactionStatusListSecured(String pgpUrl, String mId, String orderId, String txnType, String checksumHash) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/merchant-status/getTxnListStatus");
        String postData = generatePostData(mId, orderId, txnType, checksumHash);
        getRequestSpecBuilder().addParam("JsonData", postData);
    }

    public TransactionStatusListSecured(String mId, String orderId, String txnType, String checksumHash) {
        this(Constants.PGP_HOST, mId, orderId, txnType, checksumHash);
    }


    private static String generatePostData(String mId, String orderId, String txnType, String checksumHash) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("MID", mId);
        params.put("ORDERID", orderId);
        params.put("TXNTYPE", txnType);
        params.put("CHECKSUMHASH", checksumHash);
        JSONObject obj = new JSONObject(params);
        return obj.toString();
    }

}
