package com.paytm.api.upipsp;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.PGPAPIResourcePath;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class UPIPspPaymentStatus extends BaseApi {

  String request = "{\"head\":{\"clientId\":\"YES_BANK\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0cyIsInBheWVlVnBhIjoicGF5dG0udWQ5NTY5MTU4ODZAcHR5IiwibnBjaVR4bklkIjoiMjAyNDQxNjE1NDY0NTY2N2FiMTQifQ.y7Nt4qwWhbia3SiOqabGUtRM6DWxZ2W9P_15u4kYP-A\",\"requestTimestamp\":\"1713250015204\"},\"body\":{\"payeeVpa\":\"paytm.ud956915886@pty\",\"npciTxnId\":\"2024416154645667ab14\",\"createTimestamp\":\"1713355792271\"}}";

  public UPIPspPaymentStatus() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.UPI_PSP_PROCESSOR_PAYMENT_STATUS);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {
    return request;
  }

  public UPIPspPaymentStatus buildRequest(String payeeVpa, String npciTxnId, String createTimestamp,
      String tokenType, String JWT) {
    setContext("head.tokenType", tokenType);
    setContext("head.signature", JWT);
    setContext("body.payeeVpa", payeeVpa);
    setContext("body.npciTxnId", npciTxnId);
    setContext("body.createTimestamp", createTimestamp);
    return this;
  }
}
