package com.paytm.utils.merchant.api.pgp.merchant_status;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.util.TreeMap;

public class TransactionStatusList extends BaseApi {


    public TransactionStatusList(String pgpUrl, String mId, String orderId, String txnType) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST");
        String postData = generatePostData(mId, orderId, txnType);
        getRequestSpecBuilder().addParam("JsonData", postData);
    }

    public TransactionStatusList(String mId, String orderId, String txnType) {
        this(Constants.PGP_HOST, mId, orderId, txnType);
    }


    private String generatePostData(String mId, String orderId, String txnType) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("MID", mId);
        params.put("ORDERID", orderId);
        params.put("TXNTYPE", txnType);
        JSONObject obj = new JSONObject(params);
        return obj.toString();
    }

}
