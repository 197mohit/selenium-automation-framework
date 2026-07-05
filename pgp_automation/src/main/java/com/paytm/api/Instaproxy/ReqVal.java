package com.paytm.api.Instaproxy;

import com.paytm.LocalConfig;
import com.paytm.api.coft.saveCard.DeleteCardByMidCustId;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.InstaProxyService;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApi.MethodType;
import io.restassured.http.ContentType;

public class ReqVal extends BaseApi {
  String request = "{\"vpa\":\"paytm.us102343476v@pty\",\"type\":\"V\"}";

  public ReqVal buildRequest(String vpa,String type)
  {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(InstaProxyService.REQ_VAL.replace("{bankcode}","PTYES"));
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    setContext("vpa",vpa);
    setContext("type",type);
    getRequestSpecBuilder().setBody(getRequest());
    return this;
  }

  public String getRequest() {
    return request;
  }

}
