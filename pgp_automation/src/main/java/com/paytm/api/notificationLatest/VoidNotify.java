package com.paytm.api.notificationLatest;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class VoidNotify extends BaseApi{
    String vnRequest = "{\"" +
            "   orderStatus\":\"CLOSED\",\"" +
            "   merchantRequestId\":\"e158d0345b904682822b0d8f83ac4eb615111apsouth1computeinternal\",\"" +
            "   voidId\":\"20240607050970058693809974722571527\",\"" +
            "   voidSource\":\"MERCHANT\",\"" +
            "   voidReason\":\"edc-update-rec-void\",\"" +
            "   voidAmount\":{\"value\":16000,\"currency\":\"INR\"},\"" +
            "   voidApplyTime\":\"2024-06-07T16:05:48.559+05:30\",\"" +
            "   voidedTime\":\"2024-06-07T16:08:03.632+05:30\",\"" +
            "   extendInfo\":\"{" +
            "                \\\"totalTxnAmount\\\":\\\"160.0\\\",\\\"ROUTE\\\":\\\"PG2\\\",\\\"shiftNumber\\\":\\\"0002\\\",\\\"confirmTimeOut\\\":\\\"2592000\\\",\\\"accuracy\\\":\\\"1.687667\\\",\\\"source\\\":\\\"gps\\\",\\\"standardBrandEmi\\\":\\\"false\\\",\\\"bankManagedInstantDiscount\\\":\\\"false\\\",\\\"PRODUCT_CODE\\\":\\\"51051000100000000047\\\",\\\"merchantName\\\":\\\"Ambika Petroleum\\\",\\\"mode\\\":\\\"UPI_QR_CODE\\\",\\\"merchantSubCategory\\\":\\\"Nayara Energy\\\",\\\"terminalLongitude\\\":\\\"72.864418\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"posYear\\\":\\\"2024\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"TXN_AMOUNT\\\":\\\"160.0\\\",\\\"terminalLatitude\\\":\\\"21.207075\\\",\\\"REQUEST_TYPE\\\":\\\"DYNAMIC_QR\\\",\\\"edcRequest\\\":\\\"true\\\",\\\"subventionCreated\\\":\\\"false\\\",\\\"alipayMerchantId\\\":\\\"216820000663778053277\\\",\\\"isPostFactoTxn\\\":\\\"false\\\",\\\"isBankManagedInstantDiscount\\\":\\\"false\\\",\\\"bankOfferApplied\\\":\\\"false\\\",\\\"isProductBlocked\\\":\\\"false\\\",\\\"isEdcRequest\\\":\\\"true\\\",\\\"isHDFCDigiPOSMerchant\\\":\\\"false\\\",\\\"looperExtendInfo\\\":\\\"{\\\\\\\"callbackUrl\\\\\\\":\\\\\\\"rmi://ip-10-191-5-111:13385/looperCallback\\\\\\\",\\\\\\\"localCacheKey\\\\\\\":\\\\\\\"QUERY_VOID_2ad93b6b0251411b9edc5ef472913afd15111apsouth1computeinternal\\\\\\\"}\\\",\\\"" +
            "                    merchantTransId\\\":\\\"2024060716053007326711071547\\\",\\\"" + "paytmMerchantId\\\":\\\"qa14me67864128884642\\\",\\\"" + "posDate\\\":\\\"0607\\\",\\\"merchantCategory\\\":\\\"Gas and Petrol\\\",\\\"" + "orderAlreadyCreated\\\":\\\"true\\\",\\\"" + "usrLBSLongitude\\\":\\\"" + "72.78065136666667\\\",\\\"" + "productBlocked\\\":\\\"false\\\",\\\"" + "clientConfirmWaitTime\\\":\\\"1717756534542\\\",\\\"" +
            "                    reversalErrorMsg\\\":\\\"QR txn, back button pressed.\\\",\\\"" + "usrLBSLatitude\\\":\\\"21.228657750000004\\\",\\\"ecrIntegrationSource\\\":\\\"nonEcr\\\",\\\"" + "posId\\\":\\\"11071547\\\",\\\"" + "merchantUniqueReference\\\":\\\"true\\\",\\\"" + "isVoidReversal\\\":\\\"true\\\",\\\"" +
            "                    invoiceNumber\\\":\\\"073267\\\",\\\"" + "terminalAddress\\\":\\\"Dandi Road,Surat\\\",\\\"" + "isEdcReversalBasedFundbackEnabled\\\":\\\"true\\\",\\\"" + "networkType\\\":\\\"Wifi\\\",\\\"" + "timestamp\\\":\\\"1717720178847\\\",\\\"" + "posTime\\\":\\\"160530\\\",\\\"" + "paytmTid\\\":\\\"11071547\\\",\\\"" + "qrCodeId\\\":\\\"3230976000X1717756534075\\\",\\\"" +
            "                    callBackURL\\\":\\\"https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=2024060716053007326711071547\\\",\\\"" + "clientId\\\":\\\"INGDX8020922\\\",\\\"" + "merchantLimitEnabled\\\":\\\"false\\\",\\\"" +
            "                    brandValidationEnabled\\\":\\\"false\\\",\\\"" + "requestType\\\":\\\"DYNAMIC_QR\\\",\\\"" + "isStandardBrandEmi\\\":\\\"false\\\",\\\"" + "merchantLimitUpdated\\\":\\\"false\\\",\\\"" + "deviceVersion\\\":\\\"2.038.000\\\",\\\"" + "udf1\\\":\\\"11071547\\\",\\\"" + "productCode\\\":\\\"51051000100000000047\\\",\\\"" + "brandEmiPayInFullWithCashback\\\":\\\"false\\\",\\\"" + "deviceSerialNo\\\":\\\"225UCD8D2947\\\",\\\"isSubventionCreated\\\":\\\"false\\\",\\\"isBankOfferApplied\\\":\\\"false\\\",\\\"operatorType\\\":\\\"airtel\\\",\\\"isBrandEmiPayInFullWithCashback\\\":\\\"false\\\",\\\"loyaltyTxn\\\":\\\"false\\\",\\\"simNo\\\":\\\"8991102105546754157F\\\"" +
            "                }\",\"" +
            "   voidResult\":{" +
            "              \"resultStatus\":\"S\"," +
            "              \"resultCodeId\":\"00000000\"," +
            "              \"resultCode\":\"SUCCESS\"," +
            "              \"resultMsg\":\"SUCCESS\" " +
            "                },\"" +
            "   acquirementId\":\"20240607011050000005869320456721527\",\"" +
            "   merchantTransId\":\"2024060716053007326711071547\",\"" +
            "   orderAmount\":{\"value\":16000,\"currency\":\"INR\"},\"" +
            "   merchantId\":\"qa14me67864128884642\",\"" +
            "   productCode\":\"51051000100000000047\",\"" +
            "   contractId\":\"302233209961800707\",\"" +
            "   createOrderTime\":\"2024-06-07T16:05:34.151+05:30\"}";


    public VoidNotify setVoidId(String voidId){
        setContext("voidId",voidId);
        return this;
    }
    public VoidNotify setvoidSource(String voidSource){
        setContext("voidSource",voidSource);
        return this;
    }
    public VoidNotify setvoidReason(String voidReason){
        setContext("voidId",voidReason);
        return this;
    }
    public VoidNotify setAcquirementId(String acquirementId){
        setContext("acquirementId",acquirementId);
        return this;
    }
    public VoidNotify setOrderStatus(String orderStatus){
        setContext("orderStatus",orderStatus);
        return this;
    }
    public VoidNotify setmerchantTransId(String merchantTransId){
        setContext("merchantTransId",merchantTransId);
        return this;
    }
    public VoidNotify setmerchantId(String merchantId){
        setContext("merchantId",merchantId);
        return this;
    }
    public VoidNotify setResultStatus(String resultStatus){
        setContext("voidResult.resultStatus",resultStatus);
        return this;
    }
    public VoidNotify setResultCode(String resultCode){
        setContext("voidResult.resultCode",resultCode);
        return this;
    }
    public VoidNotify setResultCodeID(String resultCodeID){
        setContext("voidResult.resultCodeId",resultCodeID);
        return this;
    }
    public VoidNotify setResultMsg(String resultMsg){
        setContext("voidResult.resultMsg",resultMsg);
        return this;
    }


    public VoidNotify()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.VOID_NOTIFY);
    }

    public String getRequest() {return vnRequest;}

}
