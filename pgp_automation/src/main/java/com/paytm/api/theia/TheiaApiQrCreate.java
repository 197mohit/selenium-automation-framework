package com.paytm.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TheiaApiQrCreate extends BaseApi {

    // Expiry Date needs to be updated after every 6 months
    //  private static final String expiryDate = "2022-06-31";
    private static final String expiryDate = LocalDateTime.now().plusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    String oneTimeChecksumRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ABC\",\n" +
            "        \"requestTimestamp\": \"1753092438064\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"version\": \"V1\",\n" +
            "        \"channelId\": \"WEB\"\n" +
            "    },\n" +
            "    \"body\": {\"orderId\":\"{ORDER_ID}\",\"amount\":\"{TRANSACTION_AMOUNT}\",\"businessType\":\"UPI_QR_CODE\",\"expiryDate\":" + "\"" + expiryDate + " 19:30:00\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"udf1\":\"26467159\",\"udf2\":\"abc@123!!!\",\"merchantName\":\"test\"}}\n" +
            "}";


    String subscriptionChecksumRequest =  "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ABC\",\n" +
            "        \"requestTimestamp\": \"1753092438064\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"version\": \"V1\",\n" +
            "        \"channelId\": \"WEB\"\n" +
            "    },\n" +
            "    \"body\": {\"orderId\":\"{ORDER_ID}\",\"amount\":\"{TRANSACTION_AMOUNT}\",\"businessType\":\"UPI_QR_CODE\",\"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"udf1\":\"26467159\",\"udf2\":\"abc@123!!!\",\"merchantName\":\"test\"},\"subscriptionQr\":\"true\",\"requestType\":\"{SUBSCRIPTION_REQUESTTYPE}\"}\n" +
            "}";

    String  oneTimeChecksumBody = "{\"orderId\":\"{ORDER_ID}\",\"amount\":\"{TRANSACTION_AMOUNT}\",\"businessType\":\"UPI_QR_CODE\",\"expiryDate\":" + "\"" + expiryDate + " 19:30:00\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"udf1\":\"26467159\",\"udf2\":\"abc@123!!!\",\"merchantName\":\"test\"}}";

    String subscriptionChecksumBody = "{\"orderId\":\"{{ORDER_ID}}\",\"amount\":\"{TRANSACTION_AMOUNT}\",\"businessType\":\"UPI_QR_CODE\",\"expiryDate\":" + "\"" + expiryDate + " 19:30:00\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"udf1\":\"26467159\",\"udf2\":\"abc@123!!!\",\"merchantName\":\"test\"},\"subscriptionQr\":\"true\",\"requestType\":\"{SUBSCRIPTION_REQUESTTYPE}\"}";

    String oneTimeTxnTokenRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"C11\",\n" +
            "        \"requestTimestamp\": \"1753092438064\",\n" +
            "        \"token\": \"{txnToken}\",\n" +
            "        \"tokenType\" : \"TXN_TOKEN\",\n" +
            "        \"version\": \"V1\",\n" +
            "        \"channelId\": \"WEB\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\":\"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"UPI_QR_CODE\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"additionalInfo\": {\n" +
            "            \"udf1\": \"26467159\",\n" +
            "            \"udf2\": \"abc@123!!!\",\n" +
            "            \"merchantName\": \"test\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    String subscriptionTxnTokenRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"C11\",\n" +
            "        \"requestTimestamp\": \"1753092438064\",\n" +
            "        \"token\": \"{txnToken}\",\n" +
            "        \"tokenType\" : \"TXN_TOKEN\",\n" +
            "        \"version\": \"V1\",\n" +
            "        \"channelId\": \"WEB\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\":\"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"UPI_QR_CODE\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"additionalInfo\": {\n" +
            "            \"udf1\": \"26467159\",\n" +
            "            \"udf2\": \"abc@123!!!\",\n" +
            "            \"merchantName\": \"test\"\n" +
            "        },\n" +
            "       \"subscriptionQr\" : \"true\",\n" +
            "        \"requestType\" : \"{SUBSCRIPTION_REQUESTTYPE}\"\n" +
            "    }\n" +
            "}";

    String oneTimeChecksumInvalidExpiryRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"ABC\",\n" +
            "        \"requestTimestamp\": \"1753092438064\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"version\": \"V1\",\n" +
            "        \"channelId\": \"WEB\"\n" +
            "    },\n" +
            "    \"body\": {\"orderId\":\"{ORDER_ID}\",\"amount\":\"200\",\"businessType\":\"UPI_QR_CODE\",\"expiryDate\":" + "\"" + expiryDate + " 19abc:30:00\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"udf1\":\"26467159\",\"udf2\":\"abc@123!!!\",\"merchantName\":\"test\"}}\n" +
            "}";

    String  oneTimeChecksumnvalidExpiryBody = "{\"orderId\":\"{ORDER_ID}\",\"amount\":\"200\",\"businessType\":\"UPI_QR_CODE\",\"expiryDate\":" + "\"" + expiryDate + " 19abc:30:00\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"udf1\":\"26467159\",\"udf2\":\"abc@123!!!\",\"merchantName\":\"test\"}}";


    private static String createChecksum(String merchantKey,String body) {
        String checksum = "";

        try {
            checksum = PGPUtil.getChecksum(merchantKey, body);
            return checksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getOneTimeChecksumRequest()
    {
        return oneTimeChecksumRequest;
    }

    public String getSubscriptionChecksumRequest()
    {
        return subscriptionChecksumRequest;
    }

    public String getOneTimeTxnTokenRequest()
    {
        return oneTimeTxnTokenRequest;
    }

    public String getSubscriptionTxnTokenRequest()
    {
        return subscriptionTxnTokenRequest;
    }

    public String getOneTimeChecksumInvalidExpiryRequest()
    {
        return oneTimeChecksumInvalidExpiryRequest;
    }

    public void SetOneTimeChecksumRequest(String OrderId, Constants.MerchantType mid,String txnAmount){

        oneTimeChecksumBody= oneTimeChecksumBody.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId);

        String Signature = createChecksum(mid.getKey(),oneTimeChecksumBody);

        oneTimeChecksumRequest = oneTimeChecksumRequest.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SIGNATURE}",Signature);

    }

    public void SetSubscriptionChecksumRequest(String OrderId, Constants.MerchantType mid,String txnAmount, String subscriptionRequestType){

        subscriptionChecksumBody= subscriptionChecksumBody.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SUBSCRIPTION_REQUESTTYPE}",subscriptionRequestType);

        String Signature = createChecksum(mid.getKey(),subscriptionChecksumBody);

        subscriptionChecksumRequest = subscriptionChecksumRequest.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SUBSCRIPTION_REQUESTTYPE}",subscriptionRequestType).replace("{SIGNATURE}",Signature);

    }

    public void SetOneTimeTxnTokenRequest(String OrderId, String mid,String txnAmount, String txnToken){

        oneTimeTxnTokenRequest = oneTimeTxnTokenRequest.replace("{MERCHANT_ID}",mid).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{txnToken}",txnToken);

    }

    public void SetSubscriptionTimeTxnTokenRequest(String OrderId, String mid,String txnAmount, String subscriptionRequestType, String txnToken){

        subscriptionTxnTokenRequest = subscriptionTxnTokenRequest.replace("{MERCHANT_ID}",mid).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SUBSCRIPTION_REQUESTTYPE}",subscriptionRequestType).replace("{txnToken}",txnToken);

    }

    public void SetOneTimeChecksumInvalidExpiryRequest(String OrderId, Constants.MerchantType mid){

        oneTimeChecksumnvalidExpiryBody= oneTimeChecksumnvalidExpiryBody.replace("{MERCHANT_ID}",mid.getId()).replace("{ORDER_ID}",OrderId);

        String Signature = createChecksum(mid.getKey(),oneTimeChecksumnvalidExpiryBody);

        oneTimeChecksumInvalidExpiryRequest = oneTimeChecksumInvalidExpiryRequest.replace("{MERCHANT_ID}",mid.getId()).replace("{ORDER_ID}",OrderId).replace("{SIGNATURE}",Signature);

    }

    public TheiaApiQrCreate( String OrderId, Constants.MerchantType mid,String txnAmount) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        SetOneTimeChecksumRequest(OrderId,mid,txnAmount);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.THEIA_API_V1_QR_CREATE);
        getRequestSpecBuilder().setBody(getOneTimeChecksumRequest());
    }

    public TheiaApiQrCreate( String OrderId, Constants.MerchantType mid,String txnAmount, String subscriptionRequestType) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        SetSubscriptionChecksumRequest(OrderId,mid,txnAmount,subscriptionRequestType);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.THEIA_API_V1_QR_CREATE);
        getRequestSpecBuilder().setBody(getSubscriptionChecksumRequest());
    }

    public TheiaApiQrCreate( String OrderId, String mid,String txnAmount, String txnToken) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        SetOneTimeTxnTokenRequest(OrderId,mid,txnAmount,txnToken);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.THEIA_API_V1_QR_CREATE);
        getRequestSpecBuilder().setBody(getOneTimeTxnTokenRequest());
    }

    public TheiaApiQrCreate( String OrderId, String mid,String txnAmount, String subscriptionRequestType,String txnToken) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        SetSubscriptionTimeTxnTokenRequest(OrderId,mid,txnAmount,subscriptionRequestType,txnToken);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.THEIA_API_V1_QR_CREATE);
        getRequestSpecBuilder().setBody(getSubscriptionTxnTokenRequest());
    }

    public TheiaApiQrCreate( String OrderId, Constants.MerchantType mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        SetOneTimeChecksumInvalidExpiryRequest(OrderId,mid);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.THEIA_API_V1_QR_CREATE);
        getRequestSpecBuilder().setBody(getOneTimeChecksumInvalidExpiryRequest());
    }

}

