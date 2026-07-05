package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;

import static org.awaitility.Awaitility.with;

public class MasterRefundStatusApi extends BaseApi {
    public MasterRefundStatusApi(String mid, String refId) {
        APIBuilder:
        {
            setMethod(MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.MASTER_REFUND_STATUS);
            getRequestSpecBuilder().addParam("JsonData", "{\"MID\": \"" + mid + "\", \"REFID\": \"" + refId + "\"}");
        }
    }
}
