package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.MockbankUPIIntentPayDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

public class MockbankUPIIntentPayAPI extends BaseApi {

    private final String endPoint = "/mockbank/generate/upi/intent/pay";

    public MockbankUPIIntentPayAPI(MockbankUPIIntentPayDTO mockbankUPIIntentPayDTO) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBody(mockbankUPIIntentPayDTO);
    }

}
