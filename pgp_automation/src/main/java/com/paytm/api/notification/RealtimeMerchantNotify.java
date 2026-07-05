package com.paytm.api.notification;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import java.util.ArrayList;


public class RealtimeMerchantNotify extends BaseApi {
    String request = "{ \"request\": { \"head\": { \"clientId\": \"notification-adapter\", \"function\": \"alipayplus.settlement.settlementNotify\", \"reqTime\": \"2023-05-30T17:09:42+05:30\", \"version\": \"1.1.4\", \"reqMsgId\": \"20230530010890000870352384717819317\" }, " +
            "\"body\": { \"notificationStatus\": \"{notificationStatus}\", \"fromPg2\": true, \"retryCount\": 3, \"settleStrategy\": \"REALTIME_SETTLEMENT\", \"settleAmount\": \"2000\", \"merchantBankName\": \"STATE BANK OF INDIA\", \"channel\":[ \"SMS\"], \"errorCode\": \"{errorCode}\", \"isReversal\": \"\", \"settlementDate\": \"20230703\", \"source\": \"STS\", \"creationDate\": \"2023-07-03T12:13:33+05:30\", \"maskedAccountNumber\": \"1330********3383\", \"merchantSolutionType\": \"OFFLINE\", \"notifyMerchant\": true, \"utr\": \"355016101705\", \"modificationDate\": \"2023-07-03T12:13:33+05:30\", \"merchantId\": \"{mid}\", \"requestId\": \"20230530010890000870352384717819317\", \"ifscCode\": \"SBIN0031091\", \"settleType\": \"onlineSettlement\", \"previousTxnStatus\": \"TXN_FAILED\", \"channelId\": \"PPBL_AUTO\" } }, \"signature\": \"ec09d90f6e8ce13ebfc8bd5a19d6854f3148576f461b796cfc20410f7bcdf3cf\" }";

    public RealtimeMerchantNotify setMID(String mid){
        setContext("request.body.merchantId",mid);
        return this;
    }

    public RealtimeMerchantNotify setErrorCode(String errorCode) {
        setContext("request.body.errorCode",errorCode);
        return this;
    }
    public RealtimeMerchantNotify setNotificationStatus(String notificationStatus) {
        setContext("request.body.notificationStatus",notificationStatus);
        return this;
    }
    public RealtimeMerchantNotify setChannel(ArrayList<String> channel) {
        setContext("request.body.channel",channel);
        return this;
    }
    public RealtimeMerchantNotify()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.REALTIME_SETTLEMENT_NOTIFY);
    }
    public String getRequest() {return request;}
}