package com.paytm.utils.merchant.api.pgp.refund;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.util.TreeMap;

public class RefundStatusSecured extends BaseApi {

    public RefundStatusSecured(String pgpUrl, String mId, String refId, String checksumHash) {
        this(pgpUrl, createBody(mId, refId, checksumHash));
    }

    public RefundStatusSecured(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/refund/HANDLER_INTERNAL/getRefundStatus");
        getRequestSpecBuilder().setBody(body);
    }


    private static String createBody(String mId, String refId, String checksumHash){
        TreeMap<String, String> body = new TreeMap<>();
        body.put("MID", mId);
        body.put("REFID", refId);
        body.put("CHECKSUMHASH", checksumHash);
        JSONObject obj = new JSONObject(body);
        return obj.toString();
    }


}
