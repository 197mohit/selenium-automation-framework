package com.paytm.api.coft.PTS;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * @Author mayankbharshiv
 * @Date 04/01/22 11:52 AM
 * @Version 1.0
 */
public class FetchTokenDetails extends BaseApi {


    public FetchTokenDetails(String mid, String tokenIndexNumber,String requestId) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.FETCH_TOKEN_DETAILS);
        getRequestSpecBuilder().addPathParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("tokenIndexNumber", tokenIndexNumber);
        getRequestSpecBuilder().addQueryParam("requestId", requestId);
        getRequestSpecBuilder().addHeader("version", "v1");
        getRequestSpecBuilder().addHeader("requestTimestamp", "1544614590000");
        getRequestSpecBuilder().addHeader("requestId", "merchre000001");
        getRequestSpecBuilder().addHeader("signature", "d7186f022c5a44cc8915bf74e4e8c9f61534487392716");
        getRequestSpecBuilder().addHeader("signatureType", "CHECKSUM");
        getRequestSpecBuilder().addHeader("clientId", "c");
        getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    }
}
