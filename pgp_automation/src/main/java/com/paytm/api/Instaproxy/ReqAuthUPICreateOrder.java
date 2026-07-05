package com.paytm.api.Instaproxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Predicate;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.InstaProxyService;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder;
import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pg.crypto.AesEncryption;
import com.paytm.pg.crypto.CryptoUtils;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.StringJoiner;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.api.SoftAssertions;

public class ReqAuthUPICreateOrder extends BaseApi {

  static final private Log localLogger = LogFactory.getLog(ReqAuthUPICreateOrder.class);

  public enum PayerInstrument {
    SAVINGS,
    CREDIT,
    SOD,
    UPI_LITE,
    CREDITLINE,
    CREDITLINE01,
    CREDITLINE02,
    VOUCHER,

  }


  String request = "{\"payerPaymentInstrument\":\"{payerPaymentInstrument}\",\"creditCardInfo\":{\"binNumber\":null,\"creditAccountReferenceNumber\":\"XXXXX5878\",\"cardType\":\"\"},\"txnAmount\":{txnAmount},\"payerVpa\":\"{payerVpa}\",\"payeeVpa\":\"{payeeVpa}\",\"orderId\":\"{orderId}\",\"npciTxnId\":\"{npciTxnId}\",\"payerMobileNo\":null,\"payerIFSC\":\"AABD0000011\",\"extendInfo\":{\"additionalInfo\":\"\"},\"additionalInfo\":\"\",\"payerName\":\"{payerName}\",\"payerPSP\":\"\",\"type\":\"\",\"udf1\":\"\",\"udf2\":\"\",\"udf3\":\"\",\"udf4\":\"\",\"udf5\":\"\",\"issuingBank\":\"\",\"createTimestamp\":{createTimestamp}}";
  String requestV2="{\"payerPaymentInstrument\":\"{payerPaymentInstrument}\",\"creditCardInfo\":{\"binNumber\":null,\"creditAccountReferenceNumber\":\"\",\"cardType\":\"\"},\"txnAmount\":{txnAmount},\"payerVpa\":\"{payerVpa}\",\"payeeVpa\":\"{payeeVpa}\",\"orderId\":\"{orderId}\",\"npciTxnId\":\"{npciTxnId}\",\"mobileNo\":null,\"extendInfo\":{\"additionalInfo\":\"comment:YES PAY NEXT Transaction\"},\"additionalInfo\":\"comment:YES PAY NEXT Transaction\",\"payerName\":\"{payerName}\",\"type\":\"\",\"udf1\":\"\",\"udf2\":\"\",\"udf3\":\"\",\"udf4\":\"\",\"udf5\":\"\",\"issuingBank\":\"\",\"createTimestamp\":{createTimestamp},\"riskExtendInfo\":{\"purposeCode\":\"44\",\"initiationMode\":\"19\",\"payeeVpa\":\"{payeeVpa}\",\"payerIfsc\":\"AABD0000011\",\"payerName\":\"ABC\",\"payerAccountType\":\"{payerAccountType}\"},\"mid\":\"{mid}\",\"requestType\":\"SEAMLESS_3D_FORM\",\"custId\":null,\"payerPsp\":\"ypay\",\"settlementType\":\"{settlementType}\",\"subscriptionId\":null,\"upiOrderTimeOutInSeconds\":null,\"payerIfsc\":\"{payerIfsc}\",\"requestMsgId\":\"{npciTxnId}\"}";
  String requestV2WithError="{\"payerPaymentInstrument\":\"{payerPaymentInstrument}\",\"creditCardInfo\":{\"binNumber\":null,\"creditAccountReferenceNumber\":\"\",\"cardType\":\"\"},\"txnAmount\":{txnAmount},\"payerVpa\":\"{payerVpa}\",\"payeeVpa\":\"{payeeVpa}\",\"orderId\":\"{orderId}\",\"npciTxnId\":\"{npciTxnId}\",\"mobileNo\":null,\"extendInfo\":{\"additionalInfo\":\"comment:YES PAY NEXT Transaction\"},\"additionalInfo\":\"comment:YES PAY NEXT Transaction\",\"payerName\":\"{payerName}\",\"type\":\"\",\"udf1\":\"\",\"udf2\":\"\",\"udf3\":\"\",\"udf4\":\"\",\"udf5\":\"\",\"issuingBank\":\"\",\"createTimestamp\":{createTimestamp},\"riskExtendInfo\":{\"purposeCode\":\"44\",\"initiationMode\":\"19\",\"payeeVpa\":\"{payeeVpa}\",\"payerIfsc\":\"AABD0000011\",\"payerName\":\"ABC\",\"payerAccountType\":\"{payerAccountType}\"},\"mid\":\"{mid}\",\"requestType\":\"SEAMLESS_3D_FORM\",\"custId\":null,\"payerPsp\":\"ypay\",\"settlementType\":\"{settlementType}\",\"subscriptionId\":null,\"upiOrderTimeOutInSeconds\":null,\"payerIfsc\":\"{payerIfsc}\",\"requestMsgId\":\"{npciTxnId}\",\"status\":\"{status}\",\"errorCode\":\"{errorCode}\",\"errorMessage\":\"{errorMessage}\"}";

  // AI-Generated: 2025-01-27 - V3 request template with orderRequestReceivedAtPg, paymentAggregator and mid fields
  String requestV3="{\"payerPaymentInstrument\":\"{payerPaymentInstrument}\",\"creditCardInfo\":{\"binNumber\":null,\"creditAccountReferenceNumber\":\"\",\"cardType\":\"\"},\"txnAmount\":{txnAmount},\"payerVpa\":\"{payerVpa}\",\"payeeVpa\":\"{payeeVpa}\",\"orderId\":\"{orderId}\",\"npciTxnId\":\"{npciTxnId}\",\"mobileNo\":null,\"extendInfo\":{\"additionalInfo\":\"comment:YES PAY NEXT Transaction\"},\"additionalInfo\":\"comment:YES PAY NEXT Transaction\",\"payerName\":\"{payerName}\",\"type\":\"\",\"udf1\":\"\",\"udf2\":\"\",\"udf3\":\"\",\"udf4\":\"\",\"udf5\":\"\",\"issuingBank\":\"\",\"createTimestamp\":{createTimestamp},\"riskExtendInfo\":{\"purposeCode\":\"44\",\"initiationMode\":\"19\",\"payeeVpa\":\"{payeeVpa}\",\"payerIfsc\":\"AABD0000011\",\"payerName\":\"ABC\",\"payerAccountType\":\"{payerAccountType}\"},\"mid\":\"{mid}\",\"requestType\":\"SEAMLESS_3D_FORM\",\"custId\":null,\"payerPsp\":\"ypay\",\"settlementType\":\"{settlementType}\",\"subscriptionId\":null,\"upiOrderTimeOutInSeconds\":null,\"payerIfsc\":\"{payerIfsc}\",\"requestMsgId\":\"{npciTxnId}\",\"orderRequestReceivedAtPg\":\"{orderRequestReceivedAtPg}\",\"paymentAggregator\":\"{paymentAggregator}\"}";

  public String getRequest() {
    return request;
  }

  public String getRequestV2() {
    return requestV2;
  }

  public String getRequestV2WithError() {
    return requestV2WithError;
  }

  public String getRequestV3() {
    return requestV3;
  }

  public ReqAuthUPICreateOrder buildRequest(PayerInstrument payerInstrument, String txnAmount,
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
    String query= "INSERT INTO PGP_QA_UTILS.justpay_txn_data (npciTxnId,orderId,amount,payerVpa,payeeVpa,payerName,paymentInstrument) VALUES ('" + npciTxnId + "','" + orderId + "','" + txnAmount + "','" + payerVpa + "','" + payeeVpa + "','" + payerName + "','" + payerInstrument.name() + "')";

    System.out.println("query is: "+query);
    OfflineTxnDataCreate offlineTxnDataCreate= new OfflineTxnDataCreate();
    offlineTxnDataCreate.buildRequest("PTYES",query);
    JsonPath offlineTxnDataCreateResponse = offlineTxnDataCreate.execute().jsonPath();
    SoftAssertions softAssertions= new SoftAssertions();
    softAssertions.assertThat(offlineTxnDataCreateResponse.getString("status")).isEqualTo("S");
    softAssertions.assertThat(offlineTxnDataCreateResponse.getString("responseMessage")).isEqualTo("Order Added in table");
    softAssertions.assertAll();
    return this;
  }

  public ReqAuthUPICreateOrder buildRequestV2(String payerInstrument, String txnAmount,
      String payerVpa, String payeeVpa, String orderId, String npciTxnId, String payerName,
      String createTimestamp,String payerIfsc,String payerAccountType,String mid,String settlementType) {
    localLogger.info("Building request for create order"+ payerInstrument+"_"+txnAmount+"_"+payerVpa+"_"+payeeVpa+"_"+orderId+"_"+npciTxnId+"_"+payerName+"_"+createTimestamp+"_"+payerIfsc+"_"+payerAccountType+"_"+mid+"_"+settlementType);
    requestV2 = requestV2.replace("{payerPaymentInstrument}", payerInstrument)
        .replace("{txnAmount}", txnAmount)
        .replace("{payerVpa}", payerVpa)
        .replace("{payeeVpa}", payeeVpa)
        .replace("{orderId}", orderId)
        .replace("{npciTxnId}", npciTxnId)
        .replace("{payerName}", payerName)
        .replace("{createTimestamp}", createTimestamp)
        .replace("{payerIfsc}",payerIfsc)
        .replace("{payerAccountType}",payerAccountType)
        .replace("{mid}",mid)
        .replace("{settlementType}",settlementType)
        .replace("{requestMsgId}",npciTxnId);


    localLogger.info("Raw request is" + getRequestV2().toString());
    return this;
  }

  public ReqAuthUPICreateOrder buildRequestV2WithError(String payerInstrument, String txnAmount,
      String payerVpa, String payeeVpa, String orderId, String npciTxnId, String payerName,
      String createTimestamp,String payerIfsc,String payerAccountType,String mid,String settlementType,
      String status, String errorCode, String errorMessage) {
    localLogger.info("Building request for create order with error"+ payerInstrument+"_"+txnAmount+"_"+payerVpa+"_"+payeeVpa+"_"+orderId+"_"+npciTxnId+"_"+payerName+"_"+createTimestamp+"_"+payerIfsc+"_"+payerAccountType+"_"+mid+"_"+settlementType+"_"+status+"_"+errorCode+"_"+errorMessage);

    // Handle null values to prevent NullPointerException
    String safeStatus = status != null ? status : "";
    String safeErrorCode = errorCode != null ? errorCode : "";
    String safeErrorMessage = errorMessage != null ? errorMessage : "";

    requestV2WithError = requestV2WithError.replace("{payerPaymentInstrument}", payerInstrument)
        .replace("{txnAmount}", txnAmount)
        .replace("{payerVpa}", payerVpa)
        .replace("{payeeVpa}", payeeVpa)
        .replace("{orderId}", orderId)
        .replace("{npciTxnId}", npciTxnId)
        .replace("{payerName}", payerName)
        .replace("{createTimestamp}", createTimestamp)
        .replace("{payerIfsc}",payerIfsc)
        .replace("{payerAccountType}",payerAccountType)
        .replace("{mid}",mid)
        .replace("{settlementType}",settlementType)
        .replace("{requestMsgId}",npciTxnId)
        .replace("{status}", safeStatus)
        .replace("{errorCode}", safeErrorCode)
        .replace("{errorMessage}", safeErrorMessage);

    localLogger.info("Raw request with error is" + getRequestV2WithError().toString());
    return this;
  }

  // AI-Generated: 2025-01-27 - V3 build request method with orderRequestReceivedAtPg, paymentAggregator and mid fields
  public ReqAuthUPICreateOrder buildRequestV3(String payerInstrument, String txnAmount,
      String payerVpa, String payeeVpa, String orderId, String npciTxnId, String payerName,
      String createTimestamp, String payerIfsc, String payerAccountType, String mid,
      String settlementType, String orderRequestReceivedAtPg, String paymentAggregator) {
    localLogger.info("Building request for create order V3: " + payerInstrument + "_" + txnAmount + "_" +
        payerVpa + "_" + payeeVpa + "_" + orderId + "_" + npciTxnId + "_" + payerName + "_" +
        createTimestamp + "_" + payerIfsc + "_" + payerAccountType + "_" + mid + "_" +
        settlementType + "_" + orderRequestReceivedAtPg + "_" + paymentAggregator);

    requestV3 = requestV3.replace("{payerPaymentInstrument}", payerInstrument)
        .replace("{txnAmount}", txnAmount)
        .replace("{payerVpa}", payerVpa)
        .replace("{payeeVpa}", payeeVpa)
        .replace("{orderId}", orderId)
        .replace("{npciTxnId}", npciTxnId)
        .replace("{payerName}", payerName)
        .replace("{createTimestamp}", createTimestamp)
        .replace("{payerIfsc}", payerIfsc)
        .replace("{payerAccountType}", payerAccountType)
        .replace("{mid}", mid)
        .replace("{settlementType}", settlementType)
        .replace("{requestMsgId}", npciTxnId)
        .replace("{orderRequestReceivedAtPg}", orderRequestReceivedAtPg)
        .replace("{paymentAggregator}", paymentAggregator);

    localLogger.info("Raw request V3 is: " + getRequestV3());
    return this;
  }

  // AI-Generated: 2025-01-27 - Execute V3 request with proper JWT signature
  public Response executeV3(String bankCode) {
    try {
      String requestBody = getRequestV3();

      // Log the request body for debugging
      localLogger.info("Request body for hash generation: " + requestBody);

      // Generate SHA256 hash of request body
      String bodyHash = CryptoUtils.getSHA256(requestBody);

      // Log the generated hash for debugging
      localLogger.info("Generated hash: " + bodyHash);

      // Create JWT token with SHA256 hash as data
      java.util.Map<String, String> jwtClaims = new java.util.HashMap<>();
      jwtClaims.put("data", bodyHash);
      String jwtToken = PGPHelpers.createJsonWebToken(jwtClaims, PGPHelpers.ISSUER.ts, LocalConfig.PG_JWT_KEY);

      // Log the JWT token for debugging
      localLogger.info("Generated JWT token: " + jwtToken);

      // Execute request with JWT signature
      RestAssuredConfig config = new CurlLoggingRestAssuredConfigBuilder().build();
      return RestAssured.given()
          .config(config)
          .contentType(ContentType.JSON)
          .header("signature", jwtToken)
          .body(requestBody)
          .when()
          .post(LocalConfig.PGP_HOST + InstaProxyService.REQ_AUTH_CREATE_ORDER_V3.replace("{bankcode}", bankCode));
    } catch (Exception e) {
      throw new RuntimeException("Failed to execute V3 request with JWT signature", e);
    }
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

  public Response execute(String bankCode) {
    RestAssuredConfig config = new CurlLoggingRestAssuredConfigBuilder().build();
    return RestAssured.given()
        .config(config)
        .contentType(ContentType.TEXT)
        .body(encryptRequest(getRequest()))
        .when()
        .post(LocalConfig.PGP_HOST + InstaProxyService.REQ_AUTH_CREATE_ORDER.replace("{bankcode}",bankCode));
  }
  public Response executeV2(String bankCode) {
    RestAssuredConfig config = new CurlLoggingRestAssuredConfigBuilder().build();
    return RestAssured.given()
        .config(config)
        .contentType(ContentType.TEXT)
        .body(encryptRequest(getRequestV2()))
        .when()
        .post(LocalConfig.PGP_HOST + InstaProxyService.REQ_AUTH_CREATE_ORDER_V2.replace("{bankcode}", bankCode));
  }

  public Response executeV2WithError(String bankCode) {
    RestAssuredConfig config = new CurlLoggingRestAssuredConfigBuilder().build();
    return RestAssured.given()
        .config(config)
        .contentType(ContentType.TEXT)
        .body(encryptRequest(getRequestV2WithError()))
        .when()
        .post(LocalConfig.PGP_HOST + InstaProxyService.REQ_AUTH_CREATE_ORDER_V2.replace("{bankcode}", bankCode));
  }


}
