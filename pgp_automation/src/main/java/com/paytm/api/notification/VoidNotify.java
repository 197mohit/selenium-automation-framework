package com.paytm.api.notification;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class VoidNotify extends BaseApi {
    String vnRequest="{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"clientId\": \"notification-adapter\",\n" +
            "            \"function\": \"oldpg.acquiring.order.voidNotify\",\n" +
            "            \"reqTime\": \"2023-05-30T12:36:56+05:30\",\n" +
            "            \"version\": \"1.1.4\",\n" +
            "            \"reqMsgId\": \"202305300509487028375295210700986112023-05-30T12:36:56+05:30\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"fromPg2\": true,\n" +
            "            \"acquirementId\": \"20230607010890000873236257407818167\",\n" +
            "            \"voidApplyTime\": \"2023-05-30T12:36:55+05:30\",\n" +
            "            \"voidId\": \"20230607010890000873236257407818167\",\n" +
            "            \"voidSource\": \"MERCHANT\",\n" +
            "            \"voidReason\": \"\",\n" +
            "            \"retryCount\": 0,\n" +
            "            \"orderStatus\": \"CLOSED\",\n" +
            "            \"extendInfo\": \"{\\\"topupAndPay\\\":\\\"false\\\",\\\"merchantGuid\\\":\\\"qa8pcr90843930543632\\\",\\\"looperExtendInfo\\\":\\\"{\\\\\\\"callbackUrl\\\\\\\":\\\\\\\"rmi://10.188.105.48:13385/looperCallback\\\\\\\",\\\\\\\"localCacheKey\\\\\\\":\\\\\\\"QUERY_VOID_6c0732c4051347929c6a52bd5ebb44fb10548apsouth1computeinternal\\\\\\\"}\\\"}\",\n" +
            "            \"voidResult\": {\n" +
            "                \"resultStatus\": \"S\",\n" +
            "                \"resultCode\": \"SUCCESS\",\n" +
            "                \"resultCodeId\": \"00000000\",\n" +
            "                \"resultMsg\": \"SUCCESS\"\n" +
            "            },\n" +
            "            \"orderAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"11115\"\n" +
            "            },\n" +
            "            \"voidedTime\": \"2023-06-07T12:36:56+05:30\",\n" +
            "            \"merchantId\": \"qa8pcr90843930543632\",\n" +
            "            \"merchantTransId\": \"20230607010890000873236257407818167\",\n" +
            "            \"voidAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"11115\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"c0e9d31aae9e3a1b56f5ebbee14e9d58aef983ab1fefbab2c055646df1d36053\"\n" +
            "}";

    public VoidNotify setVoidId(String voidId){
        setContext("request.body.voidId",voidId);
        return this;
    }
    public VoidNotify setvoidSource(String voidSource){
        setContext("request.body.voidSource",voidSource);
        return this;
    }
    public VoidNotify setvoidReason(String voidReason){
        setContext("request.body.voidId",voidReason);
        return this;
    }
    public VoidNotify setOrderStatus(String orderStatus){
        setContext("request.body.orderStatus",orderStatus);
        return this;
    }

    public VoidNotify setMerchantTransID(String merchantTransID){
        setContext("request.body.merchantTransId",merchantTransID);
        return this;
    }
    public VoidNotify setResultStatus(String resultStatus){
        setContext("request.body.voidResult.resultStatus",resultStatus);
        return this;
    }
    public VoidNotify setResultCode(String resultCode){
        setContext("request.body.voidResult.resultCode",resultCode);
        return this;
    }
    public VoidNotify setResultCodeID(String resultCodeID){
        setContext("request.body.voidResult.resultCodeId",resultCodeID);
        return this;
    }
    public VoidNotify setResultMsg(String resultMsg){
        setContext("request.body.voidResult.resultMsg",resultMsg);
        return this;
    }
    public VoidNotify setMID(String mid){
        String extendInfo ="{\"topupAndPay\":\"false\",\"merchantGuid\":\"{PAYTM_MID}\",\"looperExtendInfo\":\"{\\\"callbackUrl\\\":\\\"rmi://10.188.105.48:13385/looperCallback\\\",\\\"localCacheKey\\\":\\\"QUERY_VOID_6c0732c4051347929c6a52bd5ebb44fb10548apsouth1computeinternal\\\"}\"}";
        String ex= extendInfo.replace("{PAYTM_MID}",mid);
        setContext("request.body.extendInfo",ex);
        setContext("request.body.merchantId",mid);
        return this;
    }
    public VoidNotify setAcquirementID(String acquirementID){
        setContext("request.body.acquirementID",acquirementID);
        return this;
    }

    public VoidNotify() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getVoidNotifyRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.VOID_NOTIFY);
    }
    public String getVoidNotifyRequest() {return vnRequest;}
}
