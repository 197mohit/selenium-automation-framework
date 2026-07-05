package com.paytm.api.notification;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;


public class arnNotify extends BaseApi {
    String request = "{\n" +
            "  \"request\": {\n" +
            "    \"head\": {\n" +
            "      \"sourceService\": \"PG_ACQUIRING\",\n" +
            "      \"requestCreationTime\": \"2023-05-24T06:30:33.740857Z\",\n" +
            "      \"requestUniqueId\": \"20230523010910000867731352060456223\",\n" +
            "      \"businessFlowKey\": \"PAYMENT_SUCCESS_ARN_UPDATE\",\n" +
            "      \"shadowRequest\": false\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "      \"merchantId\": \"{mid}\",\n" +
            "      \"acquirementId\": \"20230523010910000867731352060456223\",\n" +
            "      \"orderId\": \"{orderId}\",\n" +
            "      \"txnAmount\": {\n" +
            "        \"value\": \"20000\",\n" +
            "        \"currency\": \"INR\"\n" +
            "      },\n" +
            "      \"txnDate\": \"2023-05-23T11:34:36+05:30\",\n" +
            "      \"gatewayName\": \"HDFC\",\n" +
            "      \"payMethod\": \"EMI\",\n" +
            "      \"bankName\": \"HDFC Bank\",\n" +
            "      \"arn\": \"74332742343234219725530\",\n" +
            "      \"refundAmount\": {\n" +
            "        \"value\": null,\n" +
            "        \"currency\": null\n" +
            "      },\n" +
            "      \"rrn\": \"777001350560678\",\n" +
            "      \"bankTxnId\": \"777001350560678\",\n" +
            "      \"currency\": \"INR\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"signature\": \"826655ce349a3ee74f5f2e135a753c5c3b19651cb882c86fe1b3e42b9d4fc472\"\n" +
            "}";
    String refundarnrequest = "{\"request\": {\n" +
            "        \"head\": {\n" +
            "            \"sourceService\": \"PG_ACQUIRING\",\n" +
            "            \"requestCreationTime\": \"2023-05-04T07:13:07.520Z\",\n" +
            "            \"requestUniqueId\": \"1233455685695\",\n" +
            "            \"businessFlowKey\": \"REFUND_SUCCESS_ARN_UPDATE\",\n" +
            "            \"shadowRequest\": false\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"merchantId\": \"{mid}\",\n" +
            "            \"acquirementId\": \"1233455685695\",\n" +
            "            \"orderId\": \"{orderId}\",\n" +
            "            \"txnAmount\": {\n" +
            "                \"value\": \"103540\",\n" +
            "                \"currency\": \"INR\"\n" +
            "            },\n" +
            "            \"txnDate\": \"2023-05-04T12:45:38+05:30\",\n" +
            "            \"gatewayName\": \"HDFC\",\n" +
            "            \"payMethod\": \"DEBIT_CARD\",\n" +
            "            \"bankName\": \"Axis Bank\",\n" +
            "            \"arn\": \"12343453535\",\n" +
            "            \"refundAmount\": {\n" +
            "                \"value\": \"103540\",\n" +
            "                \"currency\": \"INR\"\n" +
            "            },\n" +
            "            \"refId\": \"1233455685695\",\n" +
            "            \"refundId\": \"1233455685695\",\n" +
            "            \"refundDate\": \"2023-05-04T12:45:38+05:30\",\n" +
            "            \"rrn\": \"938934932\",\n" +
            "            \"bankTxnId\": \"483938923829BankTxnId\",\n" +
            "            \"currency\": \"INR\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"71df7459cf330b9517253d24a70d4e7e0981bedc477f97d9edf492be3ba1a014\"\n" +
            "}";

    public void txnarnNotify(String mid,String orderId){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.ARN_NOTIFY);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setContext("request.body.merchantId", mid);
        setContext("request.body.orderId", orderId);
        execute();
    }
    public String getRequest() {return request;}
    public void refundArnNotify(String mid,String orderId){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.ARN_NOTIFY);
        getRequestSpecBuilder().setBody(getRefundRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setContext("request.body.merchantId", mid);
        setContext("request.body.orderId", orderId);
        execute();
    }
    public String getRefundRequest() {return refundarnrequest;}
}