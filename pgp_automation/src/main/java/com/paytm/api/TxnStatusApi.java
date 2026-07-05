package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.util.TreeMap;

public class TxnStatusApi extends BaseApi{
    private static final String BASE_PATH = "/merchant-status/getTxnStatus";

    public TxnStatusApi(final String mId, final String mKey, final String orderId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(BASE_PATH);
        String postData = generatePostData(mId, mKey, orderId);
        getRequestSpecBuilder().addParam("JsonData", postData);
    }


    private String generatePostData(final String mId, final String mKey, final String orderId) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("MID", mId);
        params.put("ORDERID", orderId);
        String checksum = PGPUtil.getChecksum(mKey, params);
        params.put("CHECKSUMHASH", checksum);
        JSONObject obj = new JSONObject(params);
        return obj.toString();
    }
}
