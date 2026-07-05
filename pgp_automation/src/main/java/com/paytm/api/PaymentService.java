package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.EdcRequestDto.EdcRequest;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PaymentService extends BaseApi {

    // Expiry Date needs to be updated after every 6 months
   //  private static final String expiryDate = "2022-06-31";
    private static final String expiryDate = LocalDateTime.now().plusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    private static final String tpapExpiryDate = LocalDateTime.now().plusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"1\",\n" +
            "        \"requestTimestamp\": \"2\",\n" +
            "        \"channelId\": \"PLab\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"version\": \"5\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"QR_ORDER\",\n" +
            "        \"displayName\": \"Dynamic043\",\n" +
            "        \"productId\": \"425653500\",\n" +
            "        \"industryType\": \"retail\",\n" +
            "        \"contactPhoneNo\": \"9899267758\",\n" +
            "        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"additionalInfo\": {\n" +
            "                        \"merchantName\": \"test\",\n" +
            "                        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "                        \"paytmMerchantId\": \"{MERCHANT_ID}\"\n" +
            "                }\n" +
            "    }\n" +
            "}";

    String request1 = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"1\",\n" +
            "        \"requestTimestamp\": \"2\",\n" +
            "        \"channelId\": \"PLab\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"version\": \"5\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"QR_ORDER\",\n" +
            "        \"displayName\": \"Dynamic043\",\n" +
            "        \"productId\": \"425653500\",\n" +
            "        \"industryType\": \"retail\",\n" +
            "        \"contactPhoneNo\": \"9899267758\",\n" +
            "        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"posId\": \"Test123451\",\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"additionalInfo\": {\n" +
            "                        \"merchantName\": \"test\",\n" +
            "                        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "                        \"paytmMerchantId\": \"{MERCHANT_ID}\",\n" +
            "                        \"isEdcRequest\": \"{EDC_REQUEST}\",\n" +
            "                        \"PRODUCT_CODE\": \"51051000100000000047\",\n" +
            "                        \"businessType\": \"{BUSINESS_TYPE}\",\n" +
            "                        \"paymentOptionsAvailable\": \"{PAYMENT_OPTIONS_AVAILABLE}\"\n" +
            "                }\n" +
            "    }\n" +
            "}";

    String requestTpap= "{\"head\":{\"clientId\":\"C11\",\"requestTimestamp\":\"1738223262568\",\"signature\":\"{SIGNATURE}\",\"version\":\"V1\",\"channelId\":\"WEB\"},\"body\":{\"orderId\":\"{ORDER_ID}\",\"amount\":{TRANSACTION_AMOUNT},\"businessType\":\"UPI_QR_CODE\",\"posId\":\"97005471\",\"productId\":0,\"orderDetails\":\"true\",\"expiryDate\":\"{expiryDate}\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"source\":\"Searce\",\"PRODUCT_CODE\":\"51051000100000000001\",\"merchantName\":\"ALI Test-002\",\"ecrMerchantCallBackUrl\":null,\"merchantSubCategory\":\"Events\",\"posId\":\"97005471\",\"terminalLongitude\":\"77.391\",\"posYear\":\"2025\",\"linkBasedInvoicePayment\":\"false\",\"topupAndPay\":\"false\",\"merchantReferenceNo\":null,\"digiposEsnFlow\":\"false\",\"invoiceNumber\":\"000262\",\"terminalLatitude\":\"28.5355\",\"terminalAddress\":\"12/90,\",\"edcRequest\":\"false\",\"networkType\":\"4g\",\"merchantType\":null,\"timestamp\":\"1738222979084\",\"posTime\":\"131735\",\"clientId\":\"PAXA5025894\",\"isPostFactoTxn\":\"false\",\"transactionDateTime\":null,\"deviceVersion\":\"3.010.000\",\"udf1\":\"97005471\",\"isEdcRequest\":\"true\",\"isHDFCDigiPOSMerchant\":\"false\",\"deviceSerialNo\":\"ALI002a\",\"merchantTransId\":\"{ORDER_ID}\",\"paytmMerchantId\":\"{MERCHANT_ID}\",\"posDate\":\"0130\",\"merchantCategory\":\"Entertainment\",\"operatorType\":\"airtel\",\"simNo\":\"8991102305870114818\",\"extServiceId\":null,\"allowedTpaps\":\"{allowedTpap}\"},\"migrationStatusRequired\":true,\"extendedInfoRequired\":true,\"skipOrderCreation\":false}}";

    String bodyTpap="{\"orderId\":\"{ORDER_ID}\",\"amount\":{TRANSACTION_AMOUNT},\"businessType\":\"UPI_QR_CODE\",\"posId\":\"97005471\",\"productId\":0,\"orderDetails\":\"true\",\"expiryDate\":\"{expiryDate}\",\"imageRequired\":true,\"mid\":\"{MERCHANT_ID}\",\"additionalInfo\":{\"source\":\"Searce\",\"PRODUCT_CODE\":\"51051000100000000001\",\"merchantName\":\"ALI Test-002\",\"ecrMerchantCallBackUrl\":null,\"merchantSubCategory\":\"Events\",\"posId\":\"97005471\",\"terminalLongitude\":\"77.391\",\"posYear\":\"2025\",\"linkBasedInvoicePayment\":\"false\",\"topupAndPay\":\"false\",\"merchantReferenceNo\":null,\"digiposEsnFlow\":\"false\",\"invoiceNumber\":\"000262\",\"terminalLatitude\":\"28.5355\",\"terminalAddress\":\"12/90,\",\"edcRequest\":\"false\",\"networkType\":\"4g\",\"merchantType\":null,\"timestamp\":\"1738222979084\",\"posTime\":\"131735\",\"clientId\":\"PAXA5025894\",\"isPostFactoTxn\":\"false\",\"transactionDateTime\":null,\"deviceVersion\":\"3.010.000\",\"udf1\":\"97005471\",\"isEdcRequest\":\"true\",\"isHDFCDigiPOSMerchant\":\"false\",\"deviceSerialNo\":\"ALI002a\",\"merchantTransId\":\"{ORDER_ID}\",\"paytmMerchantId\":\"{MERCHANT_ID}\",\"posDate\":\"0130\",\"merchantCategory\":\"Entertainment\",\"operatorType\":\"airtel\",\"simNo\":\"8991102305870114818\",\"extServiceId\":null,\"allowedTpaps\":\"{allowedTpap}\"},\"migrationStatusRequired\":true,\"extendedInfoRequired\":true,\"skipOrderCreation\":false}";

    String body = "{\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"QR_ORDER\",\n" +
            "        \"displayName\": \"Dynamic043\",\n" +
            "        \"productId\": \"425653500\",\n" +
            "        \"industryType\": \"retail\",\n" +
            "        \"contactPhoneNo\": \"9899267758\",\n" +
            "        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"additionalInfo\": {\n" +
            "                        \"merchantName\": \"test\",\n" +
            "                        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "                        \"paytmMerchantId\": \"{MERCHANT_ID}\"\n" +
            "                }\n" +
            "    }";

    String requestwithsubwallet = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"1\",\n" +
            "        \"requestTimestamp\": \"2\",\n" +
            "        \"channelId\": \"PLab\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"version\": \"v1\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"QR_ORDER\",\n" +
            "        \"displayName\": \"Qr Test\",\n" +
            "        \"posId\": \"Test123451\",\n" +
            "        \"productId\": \"42563500\",\n" +
            "        \"contactPhoneNo\": \"9899267758\",\n" +
            "        \"orderDetails\": \"1563467\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": \"false\",\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"subwalletAmount\": { \"FOOD\": {foodamount}},\n" +
            "        \"additionalInfo\": {\n" +
            "            \"mercUnqRef\": \"TEST_MERC_REF\",\n" +
            "            \"isEdcRequest\": \"false\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

String BodyForSubwallet="{\n" +
        "        \"orderId\": \"{ORDER_ID}\",\n" +
        "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
        "        \"businessType\": \"QR_ORDER\",\n" +
        "        \"displayName\": \"Qr Test\",\n" +
        "        \"posId\": \"Test123451\",\n" +
        "        \"productId\": \"42563500\",\n" +
        "        \"contactPhoneNo\": \"9899267758\",\n" +
        "        \"orderDetails\": \"1563467\",\n" +
        "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
        "        \"imageRequired\": \"false\",\n" +
        "        \"mid\": \"{MERCHANT_ID}\",\n" +
        "        \"subwalletAmount\": { \"FOOD\": {foodamount}},\n" +
        "        \"additionalInfo\": {\n" +
        "            \"mercUnqRef\": \"TEST_MERC_REF\",\n" +
        "            \"isEdcRequest\": \"false\"\n" +
        "        }\n" +
        "    }";

    String requestShop = "{\n" +
            "    \"head\": {\n" +
            "        \"clientId\": \"1\",\n" +
            "        \"requestTimestamp\": \"2\",\n" +
            "        \"channelId\": \"PLab\",\n" +
            "        \"signature\": \"{SIGNATURE}\",\n" +
            "        \"version\": \"5\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"QR_ORDER\",\n" +
            "        \"displayName\": \"Dynamic043\",\n" +
            "        \"productId\": \"425653500\",\n" +
            "        \"industryType\": \"retail\",\n" +
            "        \"contactPhoneNo\": \"9899267758\",\n" +
            "        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"posId\": \"Test123451\",\n" +
            "        \"orderDetails\": \"1563467\",\n" +
            "        \"additionalInfo\": {\n" +
            "                        \"merchantName\": \"test\",\n" +
            "                        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "                        \"paytmMerchantId\": \"{MERCHANT_ID}\"\n" +
            "                }\n" +
            "    }\n" +
            "}";
    String bodyShop = "{\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"QR_ORDER\",\n" +
            "        \"displayName\": \"Dynamic043\",\n" +
            "        \"productId\": \"425653500\",\n" +
            "        \"industryType\": \"retail\",\n" +
            "        \"contactPhoneNo\": \"9899267758\",\n" +
            "        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"posId\": \"Test123451\",\n" +
            "        \"orderDetails\": \"1563467\",\n" +
            "        \"additionalInfo\": {\n" +
            "                        \"merchantName\": \"test\",\n" +
            "                        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "                        \"paytmMerchantId\": \"{MERCHANT_ID}\"\n" +
            "                }\n" +
            "    }";

    String body1 = "{\n" +
            "        \"orderId\": \"{ORDER_ID}\",\n" +
            "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
            "        \"businessType\": \"QR_ORDER\",\n" +
            "        \"displayName\": \"Dynamic043\",\n" +
            "        \"productId\": \"425653500\",\n" +
            "        \"industryType\": \"retail\",\n" +
            "        \"contactPhoneNo\": \"9899267758\",\n" +
            "        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "        \"expiryDate\": " + "\"" + expiryDate + " 19:30:00\",\n" +
            "        \"imageRequired\": true,\n" +
            "        \"mid\": \"{MERCHANT_ID}\",\n" +
            "        \"posId\": \"Test123451\",\n" +
            "        \"orderDetails\": \"1563467\",\n" +
            "        \"additionalInfo\": {\n" +
            "                        \"merchantName\": \"test\",\n" +
            "                        \"merchantTransId\": \"{ORDER_ID}\",\n" +
            "                        \"paytmMerchantId\": \"{MERCHANT_ID}\",n" +
            "                        \"isEdcRequest\": \"{EDC_REQUEST}\",\n" +
            "                        \"PRODUCT_CODE\": \"51051000100000000047\",\n" +
            "                        \"businessType\": \"{BUSINESS_TYPE}\",\n" +
            "                        \"paymentOptionsAvailable\": \"{PAYMENT_OPTIONS_AVAILABLE}\"\n" +
            "                }\n" +
            "    }";


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



public  String getRequestwithsubwallet()
{return  requestwithsubwallet;}

    public String getRequest()

    {return request;}

    public String getRequestShop()

    {return requestShop;}


    public  PaymentService setRequestWithSubWallet(Constants.MerchantType mid,String txnAmount,String OrderId, String foodamount)
    {

        BodyForSubwallet= BodyForSubwallet.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{foodamount}",foodamount);
        System.out.println("Body : " + BodyForSubwallet);

        String Signature = createChecksum(mid.getKey(),BodyForSubwallet);
        System.out.println("Signature : " + Signature);
        this.requestwithsubwallet = requestwithsubwallet.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SIGNATURE}",Signature).replace("{foodamount}",foodamount);
        System.out.println("Request Body  : " + requestwithsubwallet);
        return this;
    }

    public PaymentService setRequest(Constants.MerchantType mid,String txnAmount,String OrderId)
    {

        body = body.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId);
        System.out.println("Body : " + body);

        String Signature = createChecksum(mid.getKey(),body);
        System.out.println("Signature : " + Signature);
        this.request = request.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SIGNATURE}",Signature);
        System.out.println("Request Body  : " + request);
        return this;
    }

    public PaymentService setRequestShop(Constants.MerchantType mid,String txnAmount,String OrderId)
    {

        bodyShop = bodyShop.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId);
        System.out.println("Body : " + bodyShop);

        String Signature = createChecksum(mid.getKey(),bodyShop);
        System.out.println("Signature : " + Signature);
        this.requestShop = requestShop.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SIGNATURE}",Signature);
        System.out.println("Request Body  : " + requestShop);
        return this;
    }
    public PaymentService setRequest1(Constants.MerchantType mid,String txnAmount,String OrderId,String businessType,String paymentOptionsAvailable,String isEDCRequest)
    {

        body1 = Objects.requireNonNull(body1.replace("{MERCHANT_ID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", txnAmount).replace("{ORDER_ID}", OrderId).replace("{EDC_REQUEST}",isEDCRequest).replace("{BUSINESS_TYPE}",businessType).replace("{PAYMENT_OPTIONS_AVAILABLE}",paymentOptionsAvailable));
        System.out.println("Body : " + body1);

        String Signature = createChecksum(mid.getKey(),body1);
        System.out.println("Signature : " + Signature);
        this.request = request1.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{EDC_REQUEST}",isEDCRequest).replace("{BUSINESS_TYPE}",businessType).replace("{PAYMENT_OPTIONS_AVAILABLE}",paymentOptionsAvailable).replace("{SIGNATURE}",Signature);
        System.out.println("Request Body  : " + request);
        return this;
    }

    public PaymentService(Constants.MerchantType mid,String txnAmount,String OrderId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        setRequest(mid,txnAmount,OrderId);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public PaymentService(Constants.MerchantType mid,String txnAmount,String OrderId, List<String>allowedTpap) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        setRequest(mid,txnAmount,OrderId,allowedTpap);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public PaymentService setRequest(Constants.MerchantType mid,String txnAmount,String OrderId,List<String>allowedTpap)
    {

        String allowedTpapString = String.join(", ", allowedTpap);
        body = bodyTpap.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{allowedTpap}",allowedTpapString).replace("{expiryDate}",tpapExpiryDate);
        System.out.println("Body : " + body);

        String Signature = createChecksum(mid.getKey(),body);
        System.out.println("Signature : " + Signature);
        this.request = requestTpap.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",OrderId).replace("{SIGNATURE}",Signature).replace("{allowedTpap}",allowedTpapString).replace("{expiryDate}",tpapExpiryDate);
        System.out.println("Request Body  : " + requestTpap);
        return this;
    }

    public PaymentService(String txnAmount,String OrderId,Constants.MerchantType mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        setRequestShop(mid,txnAmount,OrderId);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        getRequestSpecBuilder().setBody(getRequestShop());
    }

    public PaymentService(EdcRequest edcRequest) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        getRequestSpecBuilder().setBody(edcRequest);
    }
    public PaymentService(Constants.MerchantType mid,String txnAmount,String OrderId,String foodamount) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        setRequestWithSubWallet(mid,txnAmount,OrderId,foodamount);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        getRequestSpecBuilder().setBody(getRequestwithsubwallet());
    }


    public PaymentService(Constants.MerchantType mid,String txnAmount,String OrderId,boolean isEDCRequest) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        setRequest(mid,txnAmount,OrderId);
        getRequestSpecBuilder().setBody(getRequest());

        this.setContext("body.posId", CommonHelpers.getRandomWithSize(5));
        this.setContext("body.orderDetails", CommonHelpers.getRandomWithSize(5));
        this.setContext("body.additionalInfo.isEdcRequest","true");
        this.setContext("body.additionalInfo.PRODUCT_CODE","51051000100000000047");

    }

    public PaymentService(Constants.MerchantType mid,String txnAmount,String OrderId,int posId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid", mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        setRequest(mid, txnAmount, OrderId);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        getRequestSpecBuilder().setBody(getRequest());
        this.setContext("body.posId", posId);
    }

    public PaymentService(Constants.MerchantType mid,String txnAmount,String OrderId,boolean isEDCRequest,String busynessType,String custId) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        setRequest(mid,txnAmount,OrderId);
        getRequestSpecBuilder().setBody(getRequest());

        this.setContext("body.posId", CommonHelpers.getRandomWithSize(5));
        this.setContext("body.orderDetails", CommonHelpers.getRandomWithSize(5));
        this.setContext("body.additionalInfo.isEdcRequest","true");
        this.setContext("body.additionalInfo.PRODUCT_CODE","51051000100000000047");
        this.setContext("body.businessType",busynessType);
        this.setContext("body.custId",custId);

    }

    public PaymentService(Constants.MerchantType mid,String txnAmount,String OrderId,String businessType,String paymentOptionsAvailable,String isEDCRequest) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.PAYMENT_SERVICE);
        setRequest1(mid,txnAmount,OrderId,businessType,paymentOptionsAvailable,isEDCRequest);
        getRequestSpecBuilder().setBody(getRequest());

        this.setContext("body.posId", CommonHelpers.getRandomWithSize(5));
        this.setContext("body.orderDetails", CommonHelpers.getRandomWithSize(5));
        this.setContext("body.additionalInfo.isEdcRequest",isEDCRequest);
        this.setContext("body.additionalInfo.PRODUCT_CODE","51051000100000000047");
        this.setContext("body.additionalInfo.paymentOptionsAvailable",paymentOptionsAvailable);
        this.setContext("body.businessType",businessType);

    }
}
