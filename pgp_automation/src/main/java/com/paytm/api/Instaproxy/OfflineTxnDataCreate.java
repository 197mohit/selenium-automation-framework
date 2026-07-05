package com.paytm.api.Instaproxy;

import com.paytm.LocalConfig;
import com.paytm.framework.api.BaseApi;

public class OfflineTxnDataCreate extends BaseApi {
  String request = "{\"bankCode\":\"PTYES\",\"queryString\":\"\"}";

  public OfflineTxnDataCreate buildRequest(String bankCode,String queryString)
  {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType("application/json");
    getRequestSpecBuilder().setAccept("application/json");
    getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
    getRequestSpecBuilder().setBasePath("/mockbank/api/v1/offline/txn/data/create");
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    setContext("bankCode",bankCode);
    setContext("queryString",queryString);
    getRequestSpecBuilder().setBody(getRequest());
    return this;
  }
  public String getRequest() {
    return request;
  }
}
