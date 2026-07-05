package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.NativeAPIResourcePath;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class AxisIntentCallBack extends BaseApi {

  String request = "{\"callbackUrl\":\"https://pgp-ite.paytm.in/instaproxy/bankresponse/AXIF/UPI/RESPONSE\",\"merchChanId\":\"NEHAMADANMADANPRDAPP0068131\",\"creditVpa\":\"paytms.1000db@axis\",\"esn\":\"4040323209772025\",\"transactionAmount\":\"2.0\"}";


  public AxisIntentCallBack() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PPBL_URL);
    getRequestSpecBuilder().setBasePath(NativeAPIResourcePath.AXIS_INTENT_CALLBACK);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {return request;}


  public AxisIntentCallBack buildRequest(String mid, String callbackUrl, String creditVpa,
      String esn,String transactionAmount) {
    setContext("merchChanId", mid);
    setContext("callbackUrl", callbackUrl);
    setContext("creditVpa", creditVpa);
    setContext("esn", esn);
    setContext("transactionAmount", transactionAmount);
    return this;
  }

}
