package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.api.coft.saveCard.SavedCardByMidCustId;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class JustPayCallback extends BaseApi {

  String request = "{\n"
      + "    \"callbackUrl\": \"https://pgp-qa12.paytm.in/instaproxy/bankresponse/PTYES/UPI/callback\",\n"
      + "    \"payeeVpa\": \"paytm.ud956915885@pty\",\n"
      + "    \"npciTxnId\": \"PTMCR8K10276F765GH2399081856\",\n"
      + "    \"transactionAmount\": \"99.10\",\n"
      + "    \"payerAcType\":\"SOD\",\n"
      + "    \"payerIfsc\":\"AABD0000011\",\n"
      + "    \"payerName\":\"ABC\",\n"
      + "    \"payerVpa\":\"8840500363@ypay\",\n"
      + "    \"type\":\"MERCHANT_CREDITED_VIA_PAY\"\n"
      + "}";

  public JustPayCallback() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.MOCK_HOST);
    getRequestSpecBuilder().setBasePath(Constants.InstaProxyService.JUSTPAY_CALLBACK_API);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {
    return request;
  }

  public JustPayCallback buildRequest(String payeeVpa, String npciTxnId, String transactionAmount,
      String payerAcType, String payerIfsc,String payerName,String payerVpa, String type,String PaymentInstrument) {
    setContext("callbackUrl", LocalConfig.PGP_HOST+Constants.InstaProxyService.JUSTPAY_CALLBACK);
    setContext("payeeVpa", payeeVpa);
    setContext("npciTxnId", npciTxnId);
    setContext("transactionAmount", transactionAmount);
    setContext("payerAcType", payerAcType);
    setContext("payerIfsc", payerIfsc);
    setContext("payerName",payerName);
    setContext("payerVpa",payerVpa);
    setContext("type",type);
    setContext("paymentInstrument",PaymentInstrument);
    return this;
  }

}