package com.paytm.api.paymentService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SendPaymentRequestApi extends BaseApi {

String request="{\n" +
        "    \"head\": {\n" +
        "        \"version\": \"v1\",\n" +
        "        \"requestId\": \"sdfsfsd\",\n" +
        "        \"requestTimestamp\": \"\",\n" +
        "        \"clientId\": \"C11\",\n" +
        "        \"signature\": \"{SIGNATURE}\"\n" +
        "    },\n" +
        "    \"body\": {\n" +
        "        \"merchantOrderId\": \"{ORDER_ID}\",\n" +
        "        \"mid\": \"{MERCHANT_ID}\",\n" +
        "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
        "        \"subwalletAmount\": {\n" +
        "            \"FOOD\": {foodAmount}\n" +
        "        },\n" +
        "        \"expiry\": \"{EXPIRY}\",\n" +
        "        \"userPhoneNo\": \"5555000017\",\n" +
        "        \"posId\": \"1234112\",\n" +
        "        \"comment\": \"hi\"\n" +
        "    }\n" +
        "}";

String body="{\n" +
        "        \"merchantOrderId\": \"{ORDER_ID}\",\n" +
        "        \"mid\": \"{MERCHANT_ID}\",\n" +
        "        \"amount\": \"{TRANSACTION_AMOUNT}\",\n" +
        "        \"subwalletAmount\": {\n" +
        "            \"FOOD\": {foodAmount}\n" +
        "        },\n" +
        "        \"expiry\": \"{EXPIRY}\",\n" +
        "        \"userPhoneNo\": \"5555000017\",\n" +
        "        \"posId\": \"1234112\",\n" +
        "        \"comment\": \"hi\"\n" +
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

    public String getRequest()

    {return request;}

    public SendPaymentRequestApi setRequest(Constants.MerchantType mid, String txnAmount, String orderId, String foodAmount)
    {
        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime then = now.plusDays(7);
        System.out.println("date is....."+then.format(format));
        body= body.replace("{MERCHANT_ID}",mid.getId())
                .replace("{TRANSACTION_AMOUNT}",txnAmount)
                .replace("{ORDER_ID}",orderId)
                .replace("{foodAmount}",foodAmount)
                .replace("{EXPIRY}", then.format(format));
        System.out.println("Body : " + body);

        String Signature = createChecksum(mid.getKey(),body);
        System.out.println("Signature : " + Signature);
        this.request = request.replace("{MERCHANT_ID}",mid.getId()).replace("{TRANSACTION_AMOUNT}",txnAmount).replace("{ORDER_ID}",orderId).replace("{SIGNATURE}",Signature).replace("{foodAmount}",foodAmount);
        System.out.println("Request Body  : " + body);
        return this;
    }

    public SendPaymentRequestApi(Constants.MerchantType mid,String txnAmount,String orderId,String foodAmount) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("mid",mid.getId());
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        setRequest(mid,txnAmount,orderId,foodAmount);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.Send_Payment_Request);
        getRequestSpecBuilder().setBody(getRequest());
    }

}
