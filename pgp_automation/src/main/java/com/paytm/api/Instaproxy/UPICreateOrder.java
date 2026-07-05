package com.paytm.api.Instaproxy;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.InstaProxyService;
import com.paytm.framework.api.BaseApi;
import com.paytm.pg.crypto.AesEncryption;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UPICreateOrder extends BaseApi {

  static final private Log localLogger = LogFactory.getLog(UPICreateOrder.class);

  public enum PayerInstrument {
    SAVINGS,
    CREDIT,
    SOD,
    LITE,

  }


  String request = "{\"payerPaymentInstrument\":\"{payerPaymentInstrument}\",\"creditCardInfo\":{\"binNumber\":null,\"creditAccountReferenceNumber\":\"XXXXX5878\",\"cardType\":\"\"},\"txnAmount\":{txnAmount},\"payerVpa\":\"{payerVpa}\",\"payeeVpa\":\"{payeeVpa}\",\"orderId\":\"{orderId}\",\"npciTxnId\":\"{npciTxnId}\",\"payerMobileNo\":null,\"payerIFSC\":\"AABD0000011\",\"extendInfo\":{\"additionalInfo\":\"\"},\"additionalInfo\":\"\",\"payerName\":\"{payerName}\",\"payerPSP\":\"\",\"type\":\"\",\"udf1\":\"\",\"udf2\":\"\",\"udf3\":\"\",\"udf4\":\"\",\"udf5\":\"\",\"issuingBank\":\"\",\"createTimestamp\":{createTimestamp}}";


  public String getRequest() {
    return request;
  }

  public UPICreateOrder buildRequest(PayerInstrument payerInstrument, String txnAmount,
      String payerVpa, String payeeVpa, String orderId, String npciTxnId, String payerName,
      String createTimestamp) {
    request = request.replace("{payerPaymentInstrument}", payerInstrument.name())
        .replace("{txnAmount}", txnAmount)
        .replace("{payerVpa}", payerVpa)
        .replace("{payeeVpa}", payeeVpa)
        .replace("{orderId}", orderId)
        .replace("{npciTxnId}", npciTxnId)
        .replace("{payerName}", payerName)
        .replace("{createTimestamp}", createTimestamp);
    localLogger.info("Raw request is" + getRequest().toString());
    return this;
  }

  public String encryptRequest(String rawRequest) {
    AesEncryption aesEncryption = new AesEncryption();
    String encryptedRequest;
    try {
      encryptedRequest = aesEncryption.encrypt(rawRequest, LocalConfig.INSTA_CREATE_ORDER_KEY);
      localLogger.info("Encrypted request is " + encryptedRequest);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return encryptedRequest;
  }

  @Override
  public Response execute() {
    return RestAssured.given()
        .log().everything()
        .contentType(ContentType.TEXT)
        .body(encryptRequest(getRequest()))
        .when()
        .post(LocalConfig.PGP_HOST + InstaProxyService.CREATE_ORDER);
  }

}
