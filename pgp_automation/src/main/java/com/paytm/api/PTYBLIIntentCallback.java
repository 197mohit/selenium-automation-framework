package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.NativeAPIResourcePath;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class PTYBLIIntentCallback extends BaseApi {
  String request = "{\"instaUrl\":\"https://pgp-qa12.paytm.in\",\"payeeVpa\":\"payeevpa@test\",\"payerVpa\":\"payervpa@test\",\"externalSerialNo\":\"5031223505008881665\",\"payerName\":\"Abhishek Verma\"}";


  public PTYBLIIntentCallback() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PPBL_URL);
    getRequestSpecBuilder().setBasePath(NativeAPIResourcePath.PTYBLI_INTENT_CALLBACK);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {return request;}


  public PTYBLIIntentCallback buildRequest(String payerName, String instaBaseUrl, String payeeVpa,
      String externalSerialNo,String payerVpa) {
    setContext("payerName", payerName);
    setContext("instaUrl", instaBaseUrl);
    setContext("payeeVpa", payeeVpa);
    setContext("externalSerialNo", externalSerialNo);
    setContext("payerVpa", payerVpa);
    return this;
  }

}