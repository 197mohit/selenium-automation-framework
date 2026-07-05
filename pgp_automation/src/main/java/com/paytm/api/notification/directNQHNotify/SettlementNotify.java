package com.paytm.api.notification.directNQHNotify;

import com.paytm.LocalConfig;
import com.paytm.api.notification.RealtimeMerchantNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.ArrayList;

public class SettlementNotify extends BaseApi {
    String request = "{\n" +
            "    \"requestId\": \"AWSPG202404190001CZmudv69434926402313\",\n" +
            "    \"notifyMerchant\": true,\n" +
            "    \"merchantId\": \"{mid}\",\n" +
            "    \"settleAmount\": \"2000\",\n" +
            "    \"ifscCode\": \"UTIB0000198\",\n" +
            "    \"settleType\": \"onlineSettlement\",\n" +
            "    \"creationDate\": \"2024-04-19T12:16:18+05:30\",\n" +
            "    \"utr\": \"PB0024083077\",\n" +
            "    \"notificationStatus\": \"{notificationStatus}\",\n" +
            "    \"previousTxnStatus\": \"{previousTxnStatus}\",\n" +
            "    \"isReversal\": null,\n" +
            "    \"merchantBankName\": \"STATE BANK OF INDIA\",\n" +
            "    \"maskedAccountNumber\": \"1330********3383\",\n" +
            "    \"errorCode\": \"SUCCESS\",\n" +
            "    \"settlementDate\": \"20240419\",\n" +
            "    \"retryCount\": 0,\n" +
            "    \"source\": \"STS\",\n" +
            "    \"settleStrategy\": \"REALTIME_SETTLEMENT\",\n" +
            "    \"channelId\": \"OCL_AXIS_NEFT\",\n" +
            "    \"modificationDate\": \"2024-04-19T12:17:14+05:30\",\n" +
            "    \"merchantSolutionType\": \"OFFLINE\",\n" +
            "    \"channel\": [\n" +
            "        \"PUSH\"\n" +
            "    ],\n" +
            "    \"billDateTime\": \"202404191200\",\n" +
            "    \"colAmt\": \"43700\",\n" +
            "    \"settleBy\": \"05:00 PM\",\n" +
            "    \"slo\": \"000\"\n" +
            "}";

    public SettlementNotify setMID(String mid){
        setContext("merchantId",mid);
        return this;
    }

    public SettlementNotify setChannel(ArrayList<String> channel) {
        setContext("channel",channel);
        return this;
    }

    public SettlementNotify setNotificationStatus(String notificationStatus) {
        setContext("notificationStatus",notificationStatus);
        return this;
    }

    public SettlementNotify setPreviousTxnStatus(String previousTxnStatus) {
        setContext("previousTxnStatus",previousTxnStatus);
        return this;
    }

    public SettlementNotify setErrorCode(String errorCode) {
        setContext("errorCode",errorCode);
        return this;
    }

    public SettlementNotify()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.SETTLE_NOTIFY);
    }
    public String getRequest() {return request;}


}
