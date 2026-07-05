package com.paytm.api.notification;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class PaymentNotify extends BaseApi {
    
    String paymentNotifyRequest = "{\n" +
            "    \"acquirementId\": \"20250731011810000157653193875178098\",\n" +
            "    \"merchantTransId\": \"PARCEL883808\",\n" +
            "    \"orderAmount\": {\n" +
            "        \"cent\": 200,\n" +
            "        \"currency\": \"INR\",\n" +
            "        \"currencyValue\": \"356\",\n" +
            "        \"currencyCode\": \"INR\",\n" +
            "        \"centFactor\": 100,\n" +
            "        \"amount\": 2.00\n" +
            "    },\n" +
            "    \"merchantId\": \"qa14id54760874792742\",\n" +
            "    \"productCode\": \"51051000100000000101\",\n" +
            "    \"contractId\": \"504083273349660672\",\n" +
            "    \"createOrderTime\": \"2025-07-31T12:20:29.511+05:30\",\n" +
            "    \"buyerInfo\": {\n" +
            "        \"userId\": \"216810000000000028282\",\n" +
            "        \"externalUserId\": \"Test101\",\n" +
            "        \"externalUserType\": \"MERCHANT\",\n" +
            "        \"nickname\": \"\"\n" +
            "    },\n" +
            "    \"acquireMode\": \"DIRECTPAY\",\n" +
            "    \"orderStatus\": \"SUCCESS\",\n" +
            "    \"payId\": \"202507313311000001000003610000007473704312832\",\n" +
            "    \"createdTime\": \"2025-07-31T12:20:29.511+05:30\",\n" +
            "    \"paidTime\": \"2025-07-31T12:21:05.220+05:30\",\n" +
            "    \"pdEventCode\": \"12128004\",\n" +
            "    \"paymentView\": {\n" +
            "        \"cashierRequestId\": \"2025073174a933f3ae3a24604b09942b34420d808theia54c9797bb6ldld72828003\",\n" +
            "        \"paidTime\": \"2025-07-31T12:21:04.981+05:30\",\n" +
            "        \"payOptionInfos\": [\n" +
            "            {\n" +
            "                \"payMethod\": \"CREDIT_CARD\",\n" +
            "                \"payAmount\": {\n" +
            "                    \"cent\": 200,\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"currencyValue\": \"356\",\n" +
            "                    \"currencyCode\": \"INR\",\n" +
            "                    \"centFactor\": 100,\n" +
            "                    \"amount\": 2.00\n" +
            "                },\n" +
            "                \"transAmount\": {\n" +
            "                    \"cent\": 200,\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"currencyValue\": \"356\",\n" +
            "                    \"currencyCode\": \"INR\",\n" +
            "                    \"centFactor\": 100,\n" +
            "                    \"amount\": 2.00\n" +
            "                },\n" +
            "                \"assetType\": \"\",\n" +
            "                \"chargeAmount\": {\n" +
            "                    \"cent\": 0,\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"currencyValue\": \"356\",\n" +
            "                    \"currencyCode\": \"INR\",\n" +
            "                    \"centFactor\": 100,\n" +
            "                    \"amount\": 0.00\n" +
            "                },\n" +
            "                \"extendInfo\": \"{\\\"mbid\\\":\\\"70007981\\\",\\\"cardScheme\\\":\\\"VISA\\\",\\\"rrnCode\\\":\\\"777001387346699\\\",\\\"feeCurrency\\\":\\\"INR\\\",\\\"cardCacheToken\\\":\\\"073120251221001009000829940978172894465189463\\\",\\\"disableRefund\\\":\\\"false\\\",\\\"routePayId\\\":\\\"202507319109133888443432963610\\\",\\\"payMethod\\\":\\\"CREDIT_CARD\\\",\\\"externalExtendInfo\\\":\\\"{\\\\\\\"totalTxnAmount\\\\\\\":\\\\\\\"200\\\\\\\",\\\\\\\"ROUTE\\\\\\\":\\\\\\\"PG2\\\\\\\",\\\\\\\"checkoutJsAppInvokePayment\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"CUST_ID\\\\\\\":\\\\\\\"Test101\\\\\\\",\\\\\\\"graceDays\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"binNumber\\\\\\\":\\\\\\\"476995\\\\\\\",\\\\\\\"pushDataToDynamicQR\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"communicationManager\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"standardBrandEmi\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isActive\\\\\\\":\\\\\\\"true\\\\\\\",\\\\\\\"bankManagedInstantDiscount\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantName\\\\\\\":\\\\\\\"VishalG Garg\\\\\\\",\\\\\\\"isMerchantLimitUpdatedForPay\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"mccCode\\\\\\\":\\\\\\\"Retail\\\\\\\",\\\\\\\"linkBasedInvoicePayment\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"topupAndPay\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isMerchantLimitEnabledForPay\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"clientIP\\\\\\\":\\\\\\\"103.115.212.2\\\\\\\",\\\\\\\"merchantMcc\\\\\\\":\\\\\\\"Retail\\\\\\\",\\\\\\\"aoaSubsOnPgMid\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isBankManagedInstantDiscount\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"linkBasedNonInvoicePayment\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"offlineFlow\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantTransId\\\\\\\":\\\\\\\"PARCEL883808\\\\\\\",\\\\\\\"custID\\\\\\\":\\\\\\\"Test101\\\\\\\",\\\\\\\"paytmMerchantId\\\\\\\":\\\\\\\"qa14id54760874792742\\\\\\\",\\\\\\\"oldPGMerchantId\\\\\\\":\\\\\\\"qa14id54760874792742\\\\\\\",\\\\\\\"paymentRequestFlow\\\\\\\":\\\\\\\"nativeJsonRequest\\\\\\\",\\\\\\\"cardTokenRequired\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"enhancedNative\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"cardIndexNo\\\\\\\":\\\\\\\"1001\\\\\\\\u00267badbff795aa34353149c0fd7323f30e5a8df9b40fa0018b4f765c8d490a5ee2\\\\\\\",\\\\\\\"issuingBankName\\\\\\\":\\\\\\\"HDFC Bank\\\\\\\",\\\\\\\"preDebitRenewal\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"cardName\\\\\\\":\\\\\\\"VISA\\\\\\\",\\\\\\\"acquiringMCC\\\\\\\":\\\\\\\"5812\\\\\\\",\\\\\\\"TXN_TOKEN\\\\\\\":\\\\\\\"ebf116969362434ebd6bd31b4a2554021753944629280\\\\\\\",\\\\\\\"isEnhancedNative\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"CARD_TYPE\\\\\\\":\\\\\\\"CREDIT_CARD\\\\\\\",\\\\\\\"prepaidCard\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantUniqueReference\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"workFlow\\\\\\\":\\\\\\\"checkout\\\\\\\",\\\\\\\"platformFallbackDisabled\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"storeCashEnabled\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"paymodeIdentifier\\\\\\\":\\\\\\\"12\\\\\\\",\\\\\\\"cobrandedCustomDisplayName\\\\\\\":\\\\\\\"HDFC Bank\\\\\\\",\\\\\\\"peonURL\\\\\\\":\\\\\\\"https://automation-pg-ext.paytm.in/mockbank/peon\\\\\\\",\\\\\\\"promoCode\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"originalCardHash\\\\\\\":\\\\\\\"6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58\\\\\\\",\\\\\\\"merchantOnPaytm\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"bankCode\\\\\\\":\\\\\\\"HDFC\\\\\\\",\\\\\\\"website\\\\\\\":\\\\\\\"retail\\\\\\\",\\\\\\\"callBackURL\\\\\\\":\\\\\\\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\\\\\\\",\\\\\\\"requestType\\\\\\\":\\\\\\\"NATIVE\\\\\\\",\\\\\\\"merchantLimitEnabled\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isStandardBrandEmi\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"fromAoaMerchant\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantLimitUpdated\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"transactionVia\\\\\\\":\\\\\\\"ALT_ID\\\\\\\",\\\\\\\"udf2\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"corporateCard\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"subsRenewOrderAlreadyCreated\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantKybId\\\\\\\":\\\\\\\"B0bxd13mee5190\\\\\\\",\\\\\\\"productCode\\\\\\\":\\\\\\\"51051000100000000101\\\\\\\",\\\\\\\"isIndian\\\\\\\":\\\\\\\"true\\\\\\\",\\\\\\\"autoRenewal\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"autoRetry\\\\\\\":\\\\\\\"false\\\\\\\"}\\\",\\\"instCode\\\":\\\"HDFC\\\",\\\"assetLast4Ref\\\":\\\"7926\\\",\\\"serviceId\\\":\\\"PAYMENT3D\\\",\\\"gmtSend\\\":\\\"2025-07-31 12:21:05\\\",\\\"uniqueCardReference\\\":\\\"1001&7badbff795aa34353149c0fd7323f30e5a8df9b40fa0018b4f765c8d490a5ee2\\\",\\\"thirdResultInfo\\\":\\\"{\\\\\\\"resultStatus\\\\\\\":\\\\\\\"01\\\\\\\",\\\\\\\"resultCode\\\\\\\":\\\\\\\"SUCCESS\\\\\\\",\\\\\\\"resultCodeId\\\\\\\":\\\\\\\"NA\\\\\\\",\\\\\\\"resultMsg\\\\\\\":\\\\\\\"CAPTURED\\\\\\\"}\\\",\\\"serviceInstId\\\":\\\"HDFC\\\",\\\"authCode\\\":\\\"123456\\\",\\\"referenceNo\\\":\\\"777001387346699\\\",\\\"feeRateFactors\\\":\\\"{\\\\\\\"solutionWiseMdr\\\\\\\":\\\\\\\"API\\\\\\\",\\\\\\\"instId\\\\\\\":\\\\\\\"VISAW7IN\\\\\\\",\\\\\\\"serviceInstId\\\\\\\":\\\\\\\"HDFCC1IN\\\\\\\",\\\\\\\"schemeCardVariantCategory\\\\\\\":\\\\\\\"OTHERS\\\\\\\"}\\\",\\\"payChannelApi\\\":\\\"VISAW7IN_CREDIT_CARD_PAYMENT3D\\\",\\\"cashierExtendInfo\\\":\\\"{\\\\\\\"isDefaultPayerUserId\\\\\\\":\\\\\\\"true\\\\\\\",\\\\\\\"externalUserId\\\\\\\":\\\\\\\"Test101\\\\\\\"}\\\",\\\"issuerBank\\\":\\\"HDFC Bank\\\",\\\"bankAbbr\\\":\\\"HDFC\\\",\\\"issuerBankInnerInstCode\\\":\\\"HDFCC1IN\\\",\\\"isDomestic\\\":\\\"true\\\",\\\"assetType\\\":\\\"ALT_TOKEN\\\",\\\"externalEventCode\\\":\\\"HDFCC1IN02.pg.router.paytm.bankcard.payment.request@1.1.2\\\",\\\"feeAmount\\\":\\\"0\\\",\\\"instId\\\":\\\"VISA\\\",\\\"bizPatternId\\\":\\\"BANKCARD\\\",\\\"maskedAssetNo\\\":\\\"************0336\\\",\\\"cardPrefixHash\\\":\\\"3fb2d11eb1e05d10eb9167057afead33\\\",\\\"schemeCardVariantCategory\\\":\\\"OTHERS\\\",\\\"payOption\\\":\\\"CREDIT_CARD_VISA\\\",\\\"refServiceInstId\\\":\\\"HDFCC1IN\\\",\\\"instOfficialName\\\":\\\"HDFC Bank\\\",\\\"bankResponseCode\\\":\\\"ResultInfo(resultCodeId=NA, resultStatus=01, resultCode=SUCCESS, resultMsg=CAPTURED)\\\",\\\"issuerBankCode\\\":\\\"HDFC\\\",\\\"webFormId\\\":\\\"608c31d9-e1d2-4743-9b18-9bc44a51f435\\\",\\\"externalSerialNo\\\":\\\"5073133388844343297\\\"}\",\n" +
            "                \"payOptionBillExtendInfo\": \"{\\\"totalTxnAmount\\\":\\\"200\\\",\\\"ROUTE\\\":\\\"PG2\\\",\\\"checkoutJsAppInvokePayment\\\":\\\"false\\\",\\\"CUST_ID\\\":\\\"Test101\\\",\\\"graceDays\\\":\\\"0\\\",\\\"binNumber\\\":\\\"476995\\\",\\\"pushDataToDynamicQR\\\":\\\"false\\\",\\\"communicationManager\\\":\\\"false\\\",\\\"standardBrandEmi\\\":\\\"false\\\",\\\"isActive\\\":\\\"true\\\",\\\"bankManagedInstantDiscount\\\":\\\"false\\\",\\\"merchantName\\\":\\\"VishalG Garg\\\",\\\"isMerchantLimitUpdatedForPay\\\":\\\"false\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"isMerchantLimitEnabledForPay\\\":\\\"false\\\",\\\"clientIP\\\":\\\"103.115.212.2\\\",\\\"merchantMcc\\\":\\\"Retail\\\",\\\"aoaSubsOnPgMid\\\":\\\"false\\\",\\\"isBankManagedInstantDiscount\\\":\\\"false\\\",\\\"linkBasedNonInvoicePayment\\\":\\\"false\\\",\\\"offlineFlow\\\":\\\"false\\\",\\\"merchantTransId\\\":\\\"PARCEL883808\\\",\\\"custID\\\":\\\"Test101\\\",\\\"paytmMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"oldPGMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"paymentRequestFlow\\\":\\\"nativeJsonRequest\\\",\\\"cardTokenRequired\\\":\\\"false\\\",\\\"enhancedNative\\\":\\\"false\\\",\\\"cardIndexNo\\\":\\\"1001&7badbff795aa34353149c0fd7323f30e5a8df9b40fa0018b4f765c8d490a5ee2\\\",\\\"issuingBankName\\\":\\\"HDFC Bank\\\",\\\"preDebitRenewal\\\":\\\"false\\\",\\\"cardName\\\":\\\"VISA\\\",\\\"acquiringMCC\\\":\\\"5812\\\",\\\"TXN_TOKEN\\\":\\\"ebf116969362434ebd6bd31b4a2554021753944629280\\\",\\\"isEnhancedNative\\\":\\\"false\\\",\\\"CARD_TYPE\\\":\\\"CREDIT_CARD\\\",\\\"prepaidCard\\\":\\\"false\\\",\\\"merchantUniqueReference\\\":\\\"\\\",\\\"workFlow\\\":\\\"checkout\\\",\\\"platformFallbackDisabled\\\":\\\"false\\\",\\\"storeCashEnabled\\\":\\\"false\\\",\\\"paymodeIdentifier\\\":\\\"12\\\",\\\"cobrandedCustomDisplayName\\\":\\\"HDFC Bank\\\",\\\"peonURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/peon\\\",\\\"promoCode\\\":\\\"\\\",\\\"originalCardHash\\\":\\\"6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58\\\",\\\"merchantOnPaytm\\\":\\\"false\\\",\\\"bankCode\\\":\\\"HDFC\\\",\\\"website\\\":\\\"retail\\\",\\\"callBackURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\\\",\\\"requestType\\\":\\\"NATIVE\\\",\\\"merchantLimitEnabled\\\":\\\"false\\\",\\\"isStandardBrandEmi\\\":\\\"false\\\",\\\"fromAoaMerchant\\\":\\\"false\\\",\\\"merchantLimitUpdated\\\":\\\"false\\\",\\\"transactionVia\\\":\\\"ALT_ID\\\",\\\"udf2\\\":\\\"\\\",\\\"corporateCard\\\":\\\"false\\\",\\\"subsRenewOrderAlreadyCreated\\\":\\\"false\\\",\\\"merchantKybId\\\":\\\"B0bxd13mee5190\\\",\\\"productCode\\\":\\\"51051000100000000101\\\",\\\"isIndian\\\":\\\"true\\\",\\\"autoRenewal\\\":\\\"false\\\",\\\"autoRetry\\\":\\\"false\\\"}\",\n" +
            "                \"payChannelInfo\": {\n" +
            "                    \"payOption\": \"CREDIT_CARD_VISA\"\n" +
            "                },\n" +
            "                \"feeRateFactorsInfo\": \"{\\\"solutionWiseMdr\\\":\\\"API\\\",\\\"instId\\\":\\\"VISAW7IN\\\",\\\"serviceInstId\\\":\\\"HDFCC1IN\\\",\\\"schemeCardVariantCategory\\\":\\\"OTHERS\\\"}\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"payRequestExtendInfo\": \"{\\\"totalTxnAmount\\\":\\\"200\\\",\\\"ROUTE\\\":\\\"PG2\\\",\\\"checkoutJsAppInvokePayment\\\":\\\"false\\\",\\\"CUST_ID\\\":\\\"Test101\\\",\\\"graceDays\\\":\\\"0\\\",\\\"binNumber\\\":\\\"476995\\\",\\\"pushDataToDynamicQR\\\":\\\"false\\\",\\\"communicationManager\\\":\\\"false\\\",\\\"standardBrandEmi\\\":\\\"false\\\",\\\"isActive\\\":\\\"true\\\",\\\"bankManagedInstantDiscount\\\":\\\"false\\\",\\\"merchantName\\\":\\\"VishalG Garg\\\",\\\"isMerchantLimitUpdatedForPay\\\":\\\"false\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"isMerchantLimitEnabledForPay\\\":\\\"false\\\",\\\"clientIP\\\":\\\"103.115.212.2\\\",\\\"aoaSubsOnPgMid\\\":\\\"false\\\",\\\"isBankManagedInstantDiscount\\\":\\\"false\\\",\\\"linkBasedNonInvoicePayment\\\":\\\"false\\\",\\\"offlineFlow\\\":\\\"false\\\",\\\"merchantTransId\\\":\\\"PARCEL883808\\\",\\\"custID\\\":\\\"Test101\\\",\\\"paytmMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"oldPGMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"paymentRequestFlow\\\":\\\"nativeJsonRequest\\\",\\\"cardTokenRequired\\\":\\\"false\\\",\\\"enhancedNative\\\":\\\"false\\\",\\\"cardIndexNo\\\":\\\"1001&7badbff795aa34353149c0fd7323f30e5a8df9b40fa0018b4f765c8d490a5ee2\\\",\\\"issuingBankName\\\":\\\"HDFC Bank\\\",\\\"preDebitRenewal\\\":\\\"false\\\",\\\"cardName\\\":\\\"VISA\\\",\\\"TXN_TOKEN\\\":\\\"ebf116969362434ebd6bd31b4a2554021753944629280\\\",\\\"isEnhancedNative\\\":\\\"false\\\",\\\"CARD_TYPE\\\":\\\"CREDIT_CARD\\\",\\\"prepaidCard\\\":\\\"false\\\",\\\"merchantUniqueReference\\\":\\\"\\\",\\\"workFlow\\\":\\\"checkout\\\",\\\"platformFallbackDisabled\\\":\\\"false\\\",\\\"storeCashEnabled\\\":\\\"false\\\",\\\"paymodeIdentifier\\\":\\\"12\\\",\\\"cobrandedCustomDisplayName\\\":\\\"HDFC Bank\\\",\\\"peonURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/peon\\\",\\\"promoCode\\\":\\\"\\\",\\\"originalCardHash\\\":\\\"6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58\\\",\\\"merchantOnPaytm\\\":\\\"false\\\",\\\"bankCode\\\":\\\"HDFC\\\",\\\"website\\\":\\\"retail\\\",\\\"callBackURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\\\",\\\"requestType\\\":\\\"NATIVE\\\",\\\"merchantLimitEnabled\\\":\\\"false\\\",\\\"isStandardBrandEmi\\\":\\\"false\\\",\\\"fromAoaMerchant\\\":\\\"false\\\",\\\"merchantLimitUpdated\\\":\\\"false\\\",\\\"transactionVia\\\":\\\"ALT_ID\\\",\\\"udf2\\\":\\\"\\\",\\\"corporateCard\\\":\\\"false\\\",\\\"subsRenewOrderAlreadyCreated\\\":\\\"false\\\",\\\"merchantKybId\\\":\\\"B0bxd13mee5190\\\",\\\"productCode\\\":\\\"51051000100000000101\\\",\\\"isIndian\\\":\\\"true\\\",\\\"autoRenewal\\\":\\\"false\\\",\\\"autoRetry\\\":\\\"false\\\"}\",\n" +
            "        \"extendInfo\": \"{\\\"riskResultInfo\\\":\\\"{\\\\\\\"riskResult\\\\\\\":\\\\\\\"ACCEPT\\\\\\\",\\\\\\\"riskInfoCodes\\\\\\\":[\\\\\\\"SUCCESS_TRUST\\\\\\\"],\\\\\\\"riskExtendedInfo\\\\\\\":{\\\\\\\"RISK_ENABLED\\\\\\\":\\\\\\\"TRUE\\\\\\\"}}\\\",\\\"cashierExtendInfo\\\":\\\"{\\\\\\\"cashierTransType\\\\\\\":\\\\\\\"ACQUIRING\\\\\\\",\\\\\\\"redirectUrl\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"isHybrid\\\\\\\":\\\\\\\"false\\\\\\\"}\\\",\\\"transPayId\\\":\\\"\\\",\\\"terminalType\\\":\\\"WEB\\\",\\\"merchantSolutionType\\\":\\\"ONLINE\\\",\\\"noSettle\\\":\\\"false\\\",\\\"dtc\\\":\\\"\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"retryMergeFlow\\\":\\\"true\\\",\\\"idtBizOrderId\\\":\\\"20250731011810000157653193875178098\\\",\\\"paytmMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"externalExtendInfo\\\":\\\"{\\\\\\\"totalTxnAmount\\\\\\\":\\\\\\\"200\\\\\\\",\\\\\\\"ROUTE\\\\\\\":\\\\\\\"PG2\\\\\\\",\\\\\\\"checkoutJsAppInvokePayment\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"CUST_ID\\\\\\\":\\\\\\\"Test101\\\\\\\",\\\\\\\"graceDays\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"binNumber\\\\\\\":\\\\\\\"476995\\\\\\\",\\\\\\\"pushDataToDynamicQR\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"communicationManager\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"standardBrandEmi\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isActive\\\\\\\":\\\\\\\"true\\\\\\\",\\\\\\\"bankManagedInstantDiscount\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantName\\\\\\\":\\\\\\\"VishalG Garg\\\\\\\",\\\\\\\"isMerchantLimitUpdatedForPay\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"mccCode\\\\\\\":\\\\\\\"Retail\\\\\\\",\\\\\\\"linkBasedInvoicePayment\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"topupAndPay\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isMerchantLimitEnabledForPay\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"clientIP\\\\\\\":\\\\\\\"103.115.212.2\\\\\\\",\\\\\\\"aoaSubsOnPgMid\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isBankManagedInstantDiscount\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"linkBasedNonInvoicePayment\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"offlineFlow\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantTransId\\\\\\\":\\\\\\\"PARCEL883808\\\\\\\",\\\\\\\"custID\\\\\\\":\\\\\\\"Test101\\\\\\\",\\\\\\\"paytmMerchantId\\\\\\\":\\\\\\\"qa14id54760874792742\\\\\\\",\\\\\\\"oldPGMerchantId\\\\\\\":\\\\\\\"qa14id54760874792742\\\\\\\",\\\\\\\"paymentRequestFlow\\\\\\\":\\\\\\\"nativeJsonRequest\\\\\\\",\\\\\\\"cardTokenRequired\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"enhancedNative\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"cardIndexNo\\\\\\\":\\\\\\\"1001\\\\\\\\u00267badbff795aa34353149c0fd7323f30e5a8df9b40fa0018b4f765c8d490a5ee2\\\\\\\",\\\\\\\"issuingBankName\\\\\\\":\\\\\\\"HDFC Bank\\\\\\\",\\\\\\\"preDebitRenewal\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"cardName\\\\\\\":\\\\\\\"VISA\\\\\\\",\\\\\\\"TXN_TOKEN\\\\\\\":\\\\\\\"ebf116969362434ebd6bd31b4a2554021753944629280\\\\\\\",\\\\\\\"isEnhancedNative\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"CARD_TYPE\\\\\\\":\\\\\\\"CREDIT_CARD\\\\\\\",\\\\\\\"prepaidCard\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantUniqueReference\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"workFlow\\\\\\\":\\\\\\\"checkout\\\\\\\",\\\\\\\"platformFallbackDisabled\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"storeCashEnabled\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"paymodeIdentifier\\\\\\\":\\\\\\\"12\\\\\\\",\\\\\\\"cobrandedCustomDisplayName\\\\\\\":\\\\\\\"HDFC Bank\\\\\\\",\\\\\\\"peonURL\\\\\\\":\\\\\\\"https://automation-pg-ext.paytm.in/mockbank/peon\\\\\\\",\\\\\\\"promoCode\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"originalCardHash\\\\\\\":\\\\\\\"6c0a4bf0a234745f977a0ef9bdaa08180ffa831cceaf40c637da3d251265fd58\\\\\\\",\\\\\\\"merchantOnPaytm\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"bankCode\\\\\\\":\\\\\\\"HDFC\\\\\\\",\\\\\\\"website\\\\\\\":\\\\\\\"retail\\\\\\\",\\\\\\\"callBackURL\\\\\\\":\\\\\\\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\\\\\\\",\\\\\\\"requestType\\\\\\\":\\\\\\\"NATIVE\\\\\\\",\\\\\\\"merchantLimitEnabled\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"isStandardBrandEmi\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"fromAoaMerchant\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantLimitUpdated\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"transactionVia\\\\\\\":\\\\\\\"ALT_ID\\\\\\\",\\\\\\\"udf2\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"corporateCard\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"subsRenewOrderAlreadyCreated\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"merchantKybId\\\\\\\":\\\\\\\"B0bxd13mee5190\\\\\\\",\\\\\\\"productCode\\\\\\\":\\\\\\\"51051000100000000101\\\\\\\",\\\\\\\"isIndian\\\\\\\":\\\\\\\"true\\\\\\\",\\\\\\\"autoRenewal\\\\\\\":\\\\\\\"false\\\\\\\",\\\\\\\"autoRetry\\\\\\\":\\\\\\\"false\\\\\\\"}\\\",\\\"oldPGMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"chargeTarget\\\":\\\"PAYER\\\",\\\"paymentStatus\\\":\\\"SUCCESS\\\"}\",\n" +
            "        \"revoked\": false\n" +
            "    },\n" +
            "    \"orderExtendInfo\": \"{\\\"totalTxnAmount\\\":\\\"200\\\",\\\"EDC_ID\\\":\\\"edc_1\\\",\\\"checkoutJsAppInvokePayment\\\":\\\"false\\\",\\\"CUST_ID\\\":\\\"Test101\\\",\\\"ROUTE\\\":\\\"PG2\\\",\\\"graceDays\\\":\\\"0\\\",\\\"pushDataToDynamicQR\\\":\\\"false\\\",\\\"communicationManager\\\":\\\"false\\\",\\\"standardBrandEmi\\\":\\\"false\\\",\\\"bankManagedInstantDiscount\\\":\\\"false\\\",\\\"merchantName\\\":\\\"VishalG Garg\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"clientIP\\\":\\\"103.115.212.2\\\",\\\"aoaSubsOnPgMid\\\":\\\"false\\\",\\\"payerDeviceId\\\":\\\"\\\",\\\"isBankManagedInstantDiscount\\\":\\\"false\\\",\\\"linkBasedNonInvoicePayment\\\":\\\"false\\\",\\\"offlineFlow\\\":\\\"false\\\",\\\"LPV_ROUTE\\\":\\\"PG2\\\",\\\"merchantTransId\\\":\\\"PARCEL883808\\\",\\\"custID\\\":\\\"Test101\\\",\\\"paytmMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"oldPGMerchantId\\\":\\\"qa14id54760874792742\\\",\\\"cardTokenRequired\\\":\\\"false\\\",\\\"enhancedNative\\\":\\\"true\\\",\\\"preDebitRenewal\\\":\\\"false\\\",\\\"TXN_TOKEN\\\":\\\"ebf116969362434ebd6bd31b4a2554021753944629280\\\",\\\"isEnhancedNative\\\":\\\"true\\\",\\\"phoneNo\\\":\\\"\\\",\\\"merchantUniqueReference\\\":\\\"MERCUNIQ_1\\\",\\\"workFlow\\\":\\\"checkout\\\",\\\"platformFallbackDisabled\\\":\\\"false\\\",\\\"storeCashEnabled\\\":\\\"false\\\",\\\"paymodeIdentifier\\\":\\\"99\\\",\\\"peonURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/peon\\\",\\\"theme\\\":\\\"merchant4\\\",\\\"promoCode\\\":\\\"\\\",\\\"email\\\":\\\"\\\",\\\"merchantOnPaytm\\\":\\\"false\\\",\\\"website\\\":\\\"retail\\\",\\\"callBackURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\\\",\\\"requestType\\\":\\\"NATIVE_PAY\\\",\\\"merchantLimitEnabled\\\":\\\"false\\\",\\\"isStandardBrandEmi\\\":\\\"false\\\",\\\"fromAoaMerchant\\\":\\\"false\\\",\\\"merchantLimitUpdated\\\":\\\"false\\\",\\\"udf2\\\":\\\"\\\",\\\"subsRenewOrderAlreadyCreated\\\":\\\"false\\\",\\\"merchantKybId\\\":\\\"B0bxd13mee5190\\\",\\\"productCode\\\":\\\"51051000100000000101\\\",\\\"autoRenewal\\\":\\\"false\\\",\\\"autoRetry\\\":\\\"false\\\"}\",\n" +
            "    \"extendInfo\": \"{\\\"currentTxnCount\\\":\\\"0\\\"}\",\n" +
            "    \"merchantName\": \"Vishal\",\n" +
            "    \"payResult\": {\n" +
            "        \"resultStatus\": \"S\",\n" +
            "        \"resultCodeId\": \"00000000\",\n" +
            "        \"resultCode\": \"SUCCESS\",\n" +
            "        \"resultMsg\": \"SUCCESS\"\n" +
            "    },\n" +
            "    \"timeoutExtendInfo\": \"[{\\\"timeoutType\\\":\\\"EXPIRY_TIMEOUT\\\",\\\"disabled\\\":\\\"false\\\",\\\"timeoutInSeconds\\\":\\\"300.0\\\",\\\"absoluteTimeout\\\":\\\"2025-07-31T12:25:29.511+05:30\\\"},{\\\"timeoutType\\\":\\\"INACTIVE_TIMEOUT\\\",\\\"disabled\\\":\\\"false\\\",\\\"timeoutInSeconds\\\":\\\"600.0\\\",\\\"absoluteTimeout\\\":\\\"2025-07-31T12:30:29.511+05:30\\\"}]\",\n" +
            "    \"additionalMetaInfo\": {\n" +
            "        \"settleStrategy\": \"BY_TIME_CYCLE\",\n" +
            "        \"instantSettlement\": \"false\",\n" +
            "        \"mpa\": \"501078008990987867\",\n" +
            "        \"isPayAttempted\": \"T\",\n" +
            "        \"pplusMid\": \"qa14id54760874792742\",\n" +
            "        \"merchantOfficialName\": \"Vishal\",\n" +
            "        \"merchantLocalName\": \"VishalG Garg\",\n" +
            "        \"pplusUserId\": \"216810000000000028282\"\n" +
            "    },\n" +
            "    \"merchantSolutionType\": \"ONLINE\",\n" +
            "    \"payConfirmFlowType\": \"\"\n" +
            "}";

    // Setter methods for modifying request parameters
    public PaymentNotify setAcquirementId(String acquirementId) {
        setContext("acquirementId", acquirementId);
        return this;
    }

    public PaymentNotify setMerchantTransId(String merchantTransId) {
        setContext("merchantTransId", merchantTransId);
        return this;
    }

    public PaymentNotify setMerchantId(String merchantId) {
        setContext("merchantId", merchantId);
        return this;
    }

    public PaymentNotify setOrderAmount(int cent, String currency, String currencyValue, String currencyCode, int centFactor, double amount) {
        setContext("orderAmount.cent", cent);
        setContext("orderAmount.currency", currency);
        setContext("orderAmount.currencyValue", currencyValue);
        setContext("orderAmount.currencyCode", currencyCode);
        setContext("orderAmount.centFactor", centFactor);
        setContext("orderAmount.amount", amount);
        return this;
    }

    public PaymentNotify setProductCode(String productCode) {
        setContext("productCode", productCode);
        return this;
    }

    public PaymentNotify setContractId(String contractId) {
        setContext("contractId", contractId);
        return this;
    }

    public PaymentNotify setOrderStatus(String orderStatus) {
        setContext("orderStatus", orderStatus);
        return this;
    }

    public PaymentNotify setPayId(String payId) {
        setContext("payId", payId);
        return this;
    }

    public PaymentNotify setBuyerInfo(String userId, String externalUserId, String externalUserType, String nickname) {
        setContext("buyerInfo.userId", userId);
        setContext("buyerInfo.externalUserId", externalUserId);
        setContext("buyerInfo.externalUserType", externalUserType);
        setContext("buyerInfo.nickname", nickname);
        return this;
    }

    public PaymentNotify setAcquireMode(String acquireMode) {
        setContext("acquireMode", acquireMode);
        return this;
    }

    public PaymentNotify setPdEventCode(String pdEventCode) {
        setContext("pdEventCode", pdEventCode);
        return this;
    }

    public PaymentNotify setMerchantName(String merchantName) {
        setContext("merchantName", merchantName);
        return this;
    }

    public PaymentNotify setPayResult(String resultStatus, String resultCodeId, String resultCode, String resultMsg) {
        setContext("payResult.resultStatus", resultStatus);
        setContext("payResult.resultCodeId", resultCodeId);
        setContext("payResult.resultCode", resultCode);
        setContext("payResult.resultMsg", resultMsg);
        return this;
    }

    public PaymentNotify setMerchantSolutionType(String merchantSolutionType) {
        setContext("merchantSolutionType", merchantSolutionType);
        return this;
    }

    public PaymentNotify setEdcId(String edcId) {
        // Simple string replacement for EDC_ID
        paymentNotifyRequest = paymentNotifyRequest.replace("edc_1", edcId);
        return this;
    }

    public PaymentNotify setMerchantUniqueReference(String merchantUniqueReference) {
        // Simple string replacement for merchantUniqueReference
        paymentNotifyRequest = paymentNotifyRequest.replace("MERCUNIQ_1", merchantUniqueReference);
        return this;
    }

    public PaymentNotify() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getPaymentNotifyRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.PAYMENT_NOTIFY);
    }

    public PaymentNotify(String edcId, String merchantUniqueReference) {
        // Set custom values for EDC_ID and merchantUniqueReference
        setEdcId(edcId);
        setMerchantUniqueReference(merchantUniqueReference);
        
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getPaymentNotifyRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.PAYMENT_NOTIFY);
    }

    public String getPaymentNotifyRequest() {
        return paymentNotifyRequest;
    }
} 