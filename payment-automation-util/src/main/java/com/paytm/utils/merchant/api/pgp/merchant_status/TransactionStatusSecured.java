package com.paytm.utils.merchant.api.pgp.merchant_status;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import org.json.JSONObject;

import java.util.TreeMap;

public class TransactionStatusSecured extends BaseApi {


    public TransactionStatusSecured(String pgpUrl, String mId, String mKey, String orderId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(pgpUrl);
        getRequestSpecBuilder().setBasePath("/merchant-status/getTxnStatus");
        String postData = generatePostData(mId, mKey, orderId);
        getRequestSpecBuilder().addParam("JsonData", postData);
    }

    public TransactionStatusSecured(String mId, String mKey, String orderId) {
        this(Constants.PGP_HOST, mId, mKey, orderId);
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
