package com.paytm.utils.merchant.api.pgp.refund;

import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.util.TreeMap;

public class RefundStatus extends BaseApi {


    public RefundStatus(String pgpUrl, String mId, String refId) {
        this(pgpUrl, createBody(mId, refId));
    }

    public RefundStatus(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/refund/HANDLER_INTERNAL/REFUND_STATUS");
        getRequestSpecBuilder().setBody(body);
    }

    private static String createBody(String mId, String refId) {
        TreeMap<String, String> body = new TreeMap<>();
        body.put("MID", mId);
        body.put("REFID", refId);
        JSONObject obj = new JSONObject(body);
        return obj.toString();
    }


}
