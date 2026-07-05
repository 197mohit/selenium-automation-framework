package com.paytm.api.notification.directNQHNotify;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SubsCloseNotifyWOPayment extends BaseApi {
    String request = "{\n" +
            "    \"acquirementId\": \"20240430010860000992091885281656766\",\n" +
            "    \"merchantTransId\": \"PARCEL541264\",\n" +
            "    \"orderAmount\": {\n" +
            "        \"cent\": 200,\n" +
            "        \"currency\": \"INR\",\n" +
            "        \"currencyValue\": \"356\",\n" +
            "        \"currencyCode\": \"INR\",\n" +
            "        \"centFactor\": 100,\n" +
            "        \"amount\": 2.00\n" +
            "    },\n" +
            "    \"merchantId\": \"{mid}\",\n" +
            "    \"productCode\": \"51051000100000000004\",\n" +
            "    \"contractId\": \"305314405920770050\",\n" +
            "    \"createOrderTime\": \"2024-04-30T15:38:57.642+05:30\",\n" +
            "    \"buyerInfo\": {\n" +
            "        \"userId\": \"216810000000000036362\",\n" +
            "        \"externalUserId\": \"Test101\",\n" +
            "        \"externalUserType\": \"MERCHANT\",\n" +
            "        \"nickname\": \"Akshat Sharma\"\n" +
            "    },\n" +
            "    \"closedTime\": \"2024-04-30T15:42:07.920+05:30\",\n" +
            "    \"closeResult\": {\n" +
            "        \"resultStatus\": \"S\",\n" +
            "        \"resultCodeId\": \"00000000\",\n" +
            "        \"resultCode\": \"SUCCESS\",\n" +
            "        \"resultMsg\": \"SUCCESS\"\n" +
            "    },\n" +
            "    \"orderExtendInfo\": \"{\\\"totalTxnAmount\\\":\\\"2\\\",\\\"checkoutJsAppInvokePayment\\\":\\\"false\\\",\\\"CUST_ID\\\":\\\"Test101\\\",\\\"ROUTE\\\":\\\"PG2\\\",\\\"graceDays\\\":\\\"0\\\",\\\"pushDataToDynamicQR\\\":\\\"true\\\",\\\"communicationManager\\\":\\\"false\\\",\\\"standardBrandEmi\\\":\\\"false\\\",\\\"bankManagedInstantDiscount\\\":\\\"false\\\",\\\"merchantName\\\":\\\"oldPG\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"clientIP\\\":\\\"103.115.212.2\\\",\\\"alipayMerchantId\\\":\\\"qa5old66598369142433\\\",\\\"aoaSubsOnPgMid\\\":\\\"false\\\",\\\"payerDeviceId\\\":\\\"\\\",\\\"isBankManagedInstantDiscount\\\":\\\"false\\\",\\\"linkBasedNonInvoicePayment\\\":\\\"false\\\",\\\"offlineFlow\\\":\\\"false\\\",\\\"LPV_ROUTE\\\":\\\"PG2\\\",\\\"merchantTransId\\\":\\\"PARCEL541264\\\",\\\"custID\\\":\\\"Test101\\\",\\\"paytmMerchantId\\\":\\\"qa5old66598369142433\\\",\\\"subscriptionId\\\":\\\"295271\\\",\\\"cardTokenRequired\\\":\\\"false\\\",\\\"enhancedNative\\\":\\\"true\\\",\\\"preDebitRenewal\\\":\\\"false\\\",\\\"TXN_TOKEN\\\":\\\"23b391ceeb5848acb42db8afe0bb00ef1714471735570\\\",\\\"isEnhancedNative\\\":\\\"true\\\",\\\"phoneNo\\\":\\\"\\\",\\\"merchantUniqueReference\\\":\\\"\\\",\\\"workFlow\\\":\\\"checkout\\\",\\\"platformFallbackDisabled\\\":\\\"false\\\",\\\"storeCashEnabled\\\":\\\"false\\\",\\\"paymodeIdentifier\\\":\\\"99\\\",\\\"peonURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/peon\\\",\\\"theme\\\":\\\"merchant4\\\",\\\"promoCode\\\":\\\"\\\",\\\"email\\\":\\\"\\\",\\\"merchantOnPaytm\\\":\\\"false\\\",\\\"website\\\":\\\"retail\\\",\\\"callBackURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\\\",\\\"requestType\\\":\\\"NATIVE_SUBSCRIPTION_PAY\\\",\\\"merchantLimitEnabled\\\":\\\"false\\\",\\\"isStandardBrandEmi\\\":\\\"false\\\",\\\"fromAoaMerchant\\\":\\\"false\\\",\\\"merchantLimitUpdated\\\":\\\"false\\\",\\\"udf2\\\":\\\"\\\",\\\"subsRenewOrderAlreadyCreated\\\":\\\"false\\\",\\\"productCode\\\":\\\"51051000100000000004\\\",\\\"autoRenewal\\\":\\\"false\\\",\\\"autoRetry\\\":\\\"false\\\"}\",\n" +
            "    \"acquireMode\": \"DIRECTPAY\",\n" +
            "    \"closeReason\": \"User drop\",\n" +
            "    \"closeSource\": \"MERCHANT_CLOSE\",\n" +
            "    \"additionalMetaInfo\": {\n" +
            "        \"settleStrategy\": \"REALTIME\",\n" +
            "        \"instantSettlement\": \"false\",\n" +
            "        \"mpa\": \"501017003588046719\",\n" +
            "        \"pplusMid\": \"qa5old66598369142433\",\n" +
            "        \"merchantOfficialName\": \"oldPG11\",\n" +
            "        \"merchantLocalName\": \"oldPG\",\n" +
            "        \"pplusUserId\": \"216810000000000036362\"\n" +
            "    },\n" +
            "    \"orderModifyExtendInfo\": \"\"\n" +
            "}";

    public SubsCloseNotifyWOPayment()
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.CLOSE_NOTIFY);
        getRequestSpecBuilder().setBody(request);
    }

    public SubsCloseNotifyWOPayment setMID(String mid){
        setContext("merchantId",mid);
        return this;
    }

    public String getRequest() {return request;}
}
