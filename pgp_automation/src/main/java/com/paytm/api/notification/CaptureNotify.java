package com.paytm.api.notification;
import com.paytm.LocalConfig;
import com.paytm.api.merchant.migration.CreateMerchant;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
public class CaptureNotify extends BaseApi {
    String cnRequest="{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"clientId\": \"notification-adapter\",\n" +
            "            \"function\": \"oldpg.acquiring.order.captureNotify\",\n" +
            "            \"reqTime\": \"2023-05-30T13:05:07+05:30\",\n" +
            "            \"version\": \"1.1.4\",\n" +
            "            \"reqMsgId\": \"202305300108700008702920496253815732023-05-30T13:05:07+05:30\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"fromPg2\": true,\n" +
            "            \"acquirementId\": \"20230607010890000873236257407818167\",\n" +
            "            \"captureResult\": {\n" +
            "                \"resultStatus\": \"S\",\n" +
            "                \"resultCode\": \"SUCCESS\",\n" +
            "                \"resultCodeId\": \"00000000\",\n" +
            "                \"resultMsg\": \"SUCCESS\"\n" +
            "            },\n" +
            "            \"retryCount\": 0,\n" +
            "            \"orderStatus\": \"SUCCESS\",\n" +
            "            \"extendInfo\": \"{\\\"totalTxnAmount\\\":\\\"100000\\\",\\\"productBlocked\\\":\\\"false\\\",\\\"clientConfirmWaitTime\\\":\\\"0\\\",\\\"standardBrandEmi\\\":\\\"false\\\",\\\"bankManagedInstantDiscount\\\":\\\"false\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"isEdcReversalBasedFundbackEnabled\\\":\\\"false\\\",\\\"edcRequest\\\":\\\"true\\\",\\\"subventionCreated\\\":\\\"false\\\",\\\"SUPPORT_PARTIAL_CAPTURE\\\":\\\"false\\\",\\\"alipayMerchantId\\\":\\\"qa8pcr90843930543632\\\",\\\"paytmTid\\\":\\\"10721036\\\",\\\"requestType\\\":\\\"CAPTURE\\\",\\\"merchantLimitEnabled\\\":\\\"false\\\",\\\"brandValidationEnabled\\\":\\\"false\\\",\\\"isStandardBrandEmi\\\":\\\"false\\\",\\\"isBankManagedInstantDiscount\\\":\\\"false\\\",\\\"bankOfferApplied\\\":\\\"false\\\",\\\"merchantLimitUpdated\\\":\\\"false\\\",\\\"isProductBlocked\\\":\\\"false\\\",\\\"isEdcRequest\\\":\\\"true\\\",\\\"productCode\\\":\\\"51051000100000000146\\\",\\\"brandEmiPayInFullWithCashback\\\":\\\"false\\\",\\\"isSubventionCreated\\\":\\\"false\\\",\\\"looperExtendInfo\\\":\\\"{\\\\\\\"callbackUrl\\\\\\\":\\\\\\\"rmi://10.191.5.164:13385/looperCallback\\\\\\\" ,\\\\\\\"localCacheKey\\\\\\\":\\\\\\\"QUERY_CAPTURE_8a8a063c0b58467a903d201db75fb9aa15164apsouth1computeinternal\\\\\\\" }\\\",\\\"merchantTransId\\\":\\\"20230607010890000873236257407818167\\\",\\\"paytmMerchantId\\\":\\\"qa8pcr90843930543632\\\",\\\"isBankOfferApplied\\\":\\\"false\\\",\\\"isBrandEmiPayInFullWithCashback\\\":\\\"false\\\"}\",\n" +
            "            \"captureAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"100000\"\n" +
            "            },\n" +
            "            \"orderAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"100000\"\n" +
            "            },\n" +
            "            \"merchantId\": \"{MID}\",\n" +
            "            \"merchantTransId\": \"20230607010890000873236257407818167\",\n" +
            "            \"captureApplyTime\": \"2023-05-30T13:05:07+05:30\",\n" +
            "            \"captureId\": \"20230530010870000870292049625381573\",\n" +
            "            \"capturedTime\": \"2023-05-30T13:05:07+05:30\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"94751289211d6ec1c678b86b40613342fd305fa6d36339e3c08730ee5da81c2f\"\n" +
            "}";

    public CaptureNotify setAcquirementID(String acquirementID){
        setContext("request.body.acquirementID",acquirementID);
        return this;
    }
    public CaptureNotify setResultStatus(String resultStatus){
        setContext("request.body.captureResult.resultStatus",resultStatus);
        return this;
    }
    public CaptureNotify setResultCode(String resultCode){
        setContext("request.body.captureResult.resultCode",resultCode);
        return this;
    }
    public CaptureNotify setResultCodeID(String resultCodeID){
        setContext("request.body.captureResult.resultCodeId",resultCodeID);
        return this;
    }
    public CaptureNotify setResultMsg(String resultMsg){
        setContext("request.body.captureResult.resultMsg",resultMsg);
        return this;
    }
    public CaptureNotify setOrderStatus(String orderStatus){
        setContext("request.body.orderStatus",orderStatus);
        return this;
    }
    public CaptureNotify setMerchantTransID(String merchantTransID){
        setContext("request.body.merchantTransId",merchantTransID);
        return this;
    }
    public CaptureNotify setCaptureID(String captureID){
        setContext("request.body.captureId",captureID);
        return this;
    }
    public CaptureNotify setMID(String mid){
        String extendInfo ="{\"totalTxnAmount\":\"100000\",\"productBlocked\":\"false\",\"clientConfirmWaitTime\":\"0\",\"standardBrandEmi\":\"false\",\"bankManagedInstantDiscount\":\"false\",\"linkBasedInvoicePayment\":\"false\",\"topupAndPay\":\"false\",\"isEdcReversalBasedFundbackEnabled\":\"false\",\"edcRequest\":\"true\",\"subventionCreated\":\"false\",\"SUPPORT_PARTIAL_CAPTURE\":\"false\",\"alipayMerchantId\":\"{ALIPAY_MID}\",\"paytmTid\":\"10721036\",\"requestType\":\"CAPTURE\",\"merchantLimitEnabled\":\"false\",\"brandValidationEnabled\":\"false\",\"isStandardBrandEmi\":\"false\",\"isBankManagedInstantDiscount\":\"false\",\"bankOfferApplied\":\"false\",\"merchantLimitUpdated\":\"false\",\"isProductBlocked\":\"false\",\"isEdcRequest\":\"true\",\"productCode\":\"51051000100000000146\",\"brandEmiPayInFullWithCashback\":\"false\",\"isSubventionCreated\":\"false\",\"looperExtendInfo\":\"{\\\"callbackUrl\\\":\\\"rmi://10.191.5.164:13385/looperCallback\\\" ,\\\"localCacheKey\\\":\\\"QUERY_CAPTURE_8a8a063c0b58467a903d201db75fb9aa15164apsouth1computeinternal\\\" }\",\"merchantTransId\":\"20230607010890000873236257407818167\",\"paytmMerchantId\":\"{PAYTM_MID}\",\"isBankOfferApplied\":\"false\",\"isBrandEmiPayInFullWithCashback\":\"false\"}";
        String ex= extendInfo.replace("{ALIPAY_MID}",mid).replace("{PAYTM_MID}",mid);
        setContext("request.body.extendInfo",ex);
        setContext("request.body.merchantId",mid);
        return this;
    }
    public CaptureNotify() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getCaptureNotifyRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.CAPTURE_NOTIFY);
    }
    public String getCaptureNotifyRequest() {return cnRequest;}
}
