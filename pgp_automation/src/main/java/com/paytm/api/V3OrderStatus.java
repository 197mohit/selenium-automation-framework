package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PGPAPIResourcePath;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

public class V3OrderStatus extends BaseApi {

  String request = "{\n"
      + "    \"body\": {\n"
      + "        \"mid\": \"qa8mid29057279849424\",\n"
      + "        \"orderId\": \"PARCEL53908\"\n"
      + "    },\n"
      + "    \"head\": {\n"
      + "        \"signature\": \"\",\n"
      + "        \"channelId\": \"WEB\",\n"
      + "        \"version\": \"V1\",\n"
      + "        \"clientId\":\"C11\",\n"
      + "        \"requestTimestamp\": \"1589348522012\"\n"
      + "    }\n"
      + "}";

  public V3OrderStatus() {

    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.V3_ORDER_STATUS);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());

  }

  public String getRequest() {
    return request;
  }

  public V3OrderStatus buildRequest(MerchantType merchant, String orderId) {
    setContext("body.mid", merchant.getId());
    setContext("body.orderId", orderId);
    String bodyForChecksum = "{\"mid\":\""+merchant.getId()+"\",\"orderId\":\""+orderId+"\"}";
    System.out.println("Body for checksum is "+bodyForChecksum);
    String signature= PGPUtil.getChecksum(merchant.getKey(),bodyForChecksum);
    setContext("head.signature",signature);
    return this;
  }

}
