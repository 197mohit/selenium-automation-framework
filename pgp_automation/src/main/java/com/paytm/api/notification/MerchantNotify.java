package com.paytm.api.notification;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.Random;
public class MerchantNotify extends BaseApi {
    String reqMsgId = "AWSPG202306280001216820000893549400346";
    String merchantNotifyRequest = "{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"clientId\": \"notification-adapter\",\n" +
            "            \"function\": \"oldPG.settlement.settlementNotify\",\n" +
            "            \"reqTime\": \"2023-06-28T13:00:21+05:30\",\n" +
            "            \"version\": \"1.1.4\",\n" +
            "            \"reqMsgId\": \"AWSPG202306280001216820000893549400346\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"notificationStatus\": \"SUCCESS\",\n" +
            "            \"fromPg2\": true,\n" +
            "            \"retryCount\": 0,\n" +
            "            \"settleStrategy\": \"REALTIME_SETTLEMENT\",\n" +
            "            \"settleAmount\": \"10000\",\n" +
            "            \"merchantBankName\": \"INDIAN BANK\",\n" +
            "            \"bizType\": \"REALTIME_SETTLEMENT\",\n" +
            "            \"channel\": [\n" +
            "                \"SMS\",\n" +
            "                \"PUSH\",\n" +
            "                \"WHATSAPP\"\n" +
            "            ],\n" +
            "            \"errorCode\": \"SUCCESS\",\n" +
            "            \"isReversal\": null,\n" +
            "            \"settlementDate\": \"20230628\",\n" +
            "            \"source\": \"STS\",\n" +
            "            \"creationDate\": \"2023-06-28T12:48:38+05:30\",\n" +
            "            \"maskedAccountNumber\": \"7217**8811\",\n" +
            "            \"merchantSolutionType\": \"OFFLINE\",\n" +
            "            \"notifyMerchant\": true,\n" +
            "            \"utr\": \"354510923727\",\n" +
            "            \"modificationDate\": \"2023-06-28T12:48:38+05:30\",\n" +
            "            \"merchantId\": \"216820000893549400346\",\n" +
            "            \"requestId\": \"AWSPG202306280001216820000893549400346\",\n" +
            "            \"ifscCode\": \"IDIB000D038\",\n" +
            "            \"settleType\": \"onlineSettlement\",\n" +
            "            \"previousTxnStatus\": \"TXN_PENDING\",\n" +
            "            \"channelId\": \"PPBL_AUTO\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"af5d9b3c15e4a6c32b15e462e5c563178c4beaf559fe08b0bddbcd7d10656074\"\n" +
            "}";

    public MerchantNotify() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getMerchantNotifyRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.MERCHANT_NOTIFY);
    }

    public String getMerchantNotifyRequest() {
        return merchantNotifyRequest;
    }

    public void setSettleStrategy(String settleStrategy) {
        setContext("request.body.settleStrategy", settleStrategy);
    }

    public void setMerchantId(String merchantId) {
        setContext("request.body.merchantId", merchantId);
    }

    public void setSettleType(String settleType) {
        setContext("request.body.settleType", settleType);
    }

    public void setRequestId(String requestId) {
        setContext("request.body.requestId", requestId);
    }

    public void setNotificationStatus(String notificationStatus) {
        setContext("request.body.notificationStatus", notificationStatus);
    }

    public void setPreviousTxnStatus(String previousTxnStatus) {
        setContext("request.body.previousTxnStatus", previousTxnStatus);
    }

    public void setErrorCode(String errorCode) {
        setContext("request.body.errorCode", errorCode);
    }

    public void setChannel(ArrayList<String> channel) {
        setContext("request.body.channel", channel);
    }

    public void setReqMsgId() {
        reqMsgId = java.util.UUID.randomUUID().toString();
        setContext("request.head.reqMsgId", reqMsgId);
    }

    public String getReqMsgId() {
        return reqMsgId;
    }

    public void setbizType(String bizType) {
        setContext("request.body.bizType", bizType);
    }

    public void setretryCount(String retryCount) {
        setContext("request.body.retryCount", retryCount);
    }
}