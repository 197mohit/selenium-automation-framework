package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Alipay;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class MidMiscInfo extends BaseApi {

  public MidMiscInfo(String mid,String bin)
  {
    setMethod(MethodType.GET);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PG2_BASE_URL);
    String basePath = Alipay.MID_MISC_INFO+mid;
    getRequestSpecBuilder().addQueryParam("typeName","BIN_IDENTIFIER_"+mid+"_"+bin);
    getRequestSpecBuilder().addQueryParam("typeValue",bin);
    getRequestSpecBuilder().setBasePath(basePath);
    getRequestSpecBuilder().addHeader("Client-Id","Theia");
  }

}
