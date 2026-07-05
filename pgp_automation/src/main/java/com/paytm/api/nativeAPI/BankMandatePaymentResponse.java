package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class BankMandatePaymentResponse extends BaseApi {

    String body = "{\n" +
            " \"status\": \"SUCCESSFUL\" ,\n" +
            " \"corporateTxnId\":\"{CTID}\",\n" +
            " \"bankRefNo\": \"3636646\" ,\n" +
            " \"rrn\": \"16266363\" ,\n" +
            " \"transactionDate\": \"1557812586556\" ,\n" +
            "}";

    public String getRequest() {
        return body;
    }

    public BankMandatePaymentResponse setRequest(String corporateTxnId) {
        this.body = body.replace("{CTID}", corporateTxnId);
        return this;
    }

    public BankMandatePaymentResponse(String corporateTxnId) {

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.MANDATE_PAYMENT_RESP);
        getRequestSpecBuilder().addHeader("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJQQVlUTSIsImNvcnBvcmF0ZVR4bklkIjoiUmVxMTIzIn0.BcXijuP6W-blXFyXigz-qNOxV5KKtrbpESPjfiF1jHY");
        setRequest(corporateTxnId);
        getRequestSpecBuilder().setBody(body);
    }
}