package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

public class SMSPrimary extends BaseApi {
    private Response response;

    public SMSPrimary(String orderId) {
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
        getRequestSpecBuilder().setBasePath(Constants.Mockbank.SMS_PRIMARY);
        getRequestSpecBuilder().addParam("orderId", orderId);
    }

    public Response executeUntilGetResponse()
    {

        Response response = null;
        int pollCount=0;
        while (pollCount<3) {
            pollCount++;
            response = this.execute();
            this.response = response;
            if (response.getStatusCode() == 200) {
                break;
            }
            try {
                 Thread.sleep(10 * 1000);

            } catch (InterruptedException e) {
            }
        }
        Assertions.assertThat(response.getStatusCode()).as("Status Code mismatch").isEqualTo(200);
        return this.response;
    }


    public Response response() {
        return this.response;
    }


}
