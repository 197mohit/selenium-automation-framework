package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApi.MethodType;
import io.restassured.http.ContentType;

public class GlobalConfig extends BaseApi {

  public GlobalConfig(String config)
  {
    setMethod(MethodType.GET);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    String basePath = Constants.MappingService.GLOBAL_CONFIG+"/"+config;
    getRequestSpecBuilder().setBasePath(basePath);
  }


}
