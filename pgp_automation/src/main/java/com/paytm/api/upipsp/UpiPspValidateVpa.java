package com.paytm.api.upipsp;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.PGPAPIResourcePath;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class UpiPspValidateVpa extends BaseApi {

  String request = "{\"head\":{\"requestTimestamp\":\"currentEpochTimeStamp\",\"signature\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ2cGEiOiJwYXl0bS05NTY5MTc2MjFAcHR5YmwiLCJpc3MiOiJ0cyJ9.nf5i6343YMELRKsRQrBzDvN5WXfZt05D1UUepD2surQ\"},\"body\":{\"mid\":\"\",\"vpa\":\"paytm-956917621@ptybl\"}}";

  public UpiPspValidateVpa(String clientId) {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.UPI_PSP_VALIDATE_VPA);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().addHeader("clientId",clientId);
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {
    return request;
  }

  public UpiPspValidateVpa buildRequest(String payeeVpa, String mid, String requestTimeStamp,String JWT) {
    setContext("head.requestTimestamp", requestTimeStamp);
    setContext("head.signature", JWT);
    setContext("body.mid", mid);
    setContext("body.vpa", payeeVpa);
    return this;
  }

}
