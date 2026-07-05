package com.paytm.utils.merchant.api.pgp.merchant_status;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.util.TreeMap;

public class TransactionStatus extends BaseApi {


    public TransactionStatus(String pgpUrl, String mId, String orderId) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/merchant-status/HANDLER_INTERNAL/TXNSTATUS");
        String postData = generatePostData(mId, orderId);
        getRequestSpecBuilder().addParam("JsonData", postData);
    }


    private String generatePostData(final String mId, final String orderId) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("MID", mId);
        params.put("ORDERID", orderId);
        JSONObject obj = new JSONObject(params);
        return obj.toString();
    }

}
