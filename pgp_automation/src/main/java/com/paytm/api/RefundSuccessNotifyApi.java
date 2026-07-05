package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class RefundSuccessNotifyApi extends BaseApi {
    String request= "{\n" +
            "    \"request\": {\n" +
            "        \"head\": {\n" +
            "            \"version\": \"1.1.4\",\n" +
            "            \"function\": \"alipayplus.acquiring.refund.refundSuccessNotify\",\n" +
            "            \"clientId\": \"2016030715243903536806\",\n" +
            "            \"reqTime\": \"2021-12-01T14:53:12+05:30\",\n" +
            "            \"reqMsgId\": \"c5da49cd-2c68-4550-8192-e59e243af1d7\"\n" +
            "        },\n" +
            "        \"body\": {\n" +
            "            \"acquirementId\": \"20211126111212800110168972970463450\",\n" +
            "            \"buyerInfo\": {\n" +
            "                \"externalUserId\": \"6183cc804c76383daafa2556\",\n" +
            "                \"externalUserType\": \"MERCHANT\",\n" +
            "                \"nickname\": \"\",\n" +
            "                \"userId\": \"216810000000000097972\"\n" +
            "            },\n" +
            "            \"contractId\": \"2019021451051010016808997314433\",\n" +
            "            \"createOrderTime\": \"2021-11-26T20:57:52+05:30\",\n" +
            "            \"destination\": \"TO_SOURCE\",\n" +
            "            \"extendInfo\": \"{\\\"destinationAccountSource\\\":\\\"MERCHANT\\\",\\\"BANK_TXN_ID\\\":\\\"133031341729\\\",\\\"subwalletWithdrawMaxAmountDetails\\\":\\\"{}\\\",\\\"ISSUING_BANK_NAME\\\":\\\"\\\",\\\"returnChargeToPayee\\\":\\\"false\\\",\\\"merchantName\\\":\\\"Urban Company\\\",\\\"originalPaymentMode\\\":\\\"UPI\\\",\\\"originalTransactionAmount\\\":\\\"94700\\\",\\\"externalUserId\\\":\\\"6183cc804c76383daafa2556\\\",\\\"merchantRefId\\\":\\\"de21712a5e6c44ec8a33716333c47e42\\\",\\\"merchantTransId\\\":\\\"urbanclap-35809423-1\\\",\\\"TXN_DATE\\\":\\\"Fri Nov 26 20:57:52 IST 2021\\\",\\\"WALLET_AMOUNT\\\":\\\"\\\",\\\"NON_WALLET_AMOUNT\\\":\\\"94700\\\",\\\"paytmMerchantId\\\":\\\"URBTEC47050103592955\\\"}\",\n" +
            "            \"merchantId\": \"216820000008088818851\",\n" +
            "            \"merchantName\": \"{\\\"lastName\\\":\\\"Urban Company\\\"}\",\n" +
            "            \"merchantRequestId\": \"de21712a5e6c44ec8a33716333c47e42\",\n" +
            "            \"merchantTransId\": \"urbanclap-35809423-1\",\n" +
            "            \"offset\": true,\n" +
            "            \"orderAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"94700\"\n" +
            "            },\n" +
            "            \"orderExtendInfo\": \"{\\\"totalTxnAmount\\\":\\\"94700\\\",\\\"checkoutJsAppInvokePayment\\\":\\\"false\\\",\\\"CUST_ID\\\":\\\"6183cc804c76383daafa2556\\\",\\\"preDebitRenewal\\\":\\\"false\\\",\\\"graceDays\\\":\\\"0\\\",\\\"isEnhancedNative\\\":\\\"false\\\",\\\"pushDataToDynamicQR\\\":\\\"false\\\",\\\"communicationManager\\\":\\\"false\\\",\\\"phoneNo\\\":\\\"9494508603\\\",\\\"merchantName\\\":\\\"Urban Company\\\",\\\"prepaidCard\\\":\\\"false\\\",\\\"mccCode\\\":\\\"Retail92\\\",\\\"merchantUniqueReference\\\":\\\"NA\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"clientIP\\\":\\\"13.126.232.13\\\",\\\"paymodeIdentifier\\\":\\\"99\\\",\\\"additionalInfo\\\":\\\"\\\",\\\"peonURL\\\":\\\"https://api.juspay.in/v2/pay/webhooks/urbanclap/paytm_v2\\\",\\\"alipayMerchantId\\\":\\\"216820000277849301433\\\",\\\"email\\\":\\\"dummyemail@urbanclap.com\\\",\\\"merchantOnPaytm\\\":\\\"false\\\",\\\"website\\\":\\\"DEFAULT\\\",\\\"callBackURL\\\":\\\"https://api.juspay.in/v2/pay/response/urbanclap/eulkZKaKNqsKZVd9S6w\\\",\\\"requestType\\\":\\\"NATIVE\\\",\\\"merchantLimitEnabled\\\":\\\"false\\\",\\\"fromAoaMerchant\\\":\\\"false\\\",\\\"udf3\\\":\\\"NA\\\",\\\"merchantLimitUpdated\\\":\\\"false\\\",\\\"udf1\\\":\\\"NA\\\",\\\"udf2\\\":\\\"NA\\\",\\\"corporateCard\\\":\\\"false\\\",\\\"subsRenewOrderAlreadyCreated\\\":\\\"false\\\",\\\"linkBasedNonInvoicePayment\\\":\\\"false\\\",\\\"merchantKybId\\\":\\\"A0jpa5608d288\\\",\\\"offlineFlow\\\":\\\"false\\\",\\\"productCode\\\":\\\"51051000100000000001\\\",\\\"merchantTransId\\\":\\\"urbanclap-35809423-1\\\",\\\"custID\\\":\\\"6183cc804c76383daafa2556\\\",\\\"paytmMerchantId\\\":\\\"URBTEC47050103592955\\\",\\\"autoRenewal\\\":\\\"false\\\",\\\"autoRetry\\\":\\\"false\\\",\\\"cardTokenRequired\\\":\\\"false\\\",\\\"enhancedNative\\\":\\\"false\\\"}\",\n" +
            "            \"productCode\": \"51051000100000000001\",\n" +
            "            \"issuerBankInstCode\":\"PNB\",\n" +
            "            \"refundAmount\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"64800\"\n" +
            "            },\n" +
            "            \"refundApplyTime\": \"2021-12-01T14:53:10+05:30\",\n" +
            "            \"refundDetailInfoList\": [\n" +
            "                {\n" +
            "                    \"channelInfoList\": [\n" +
            "                        {\n" +
            "                            \"addAndPay\": false,\n" +
            "                            \"amount\": {\n" +
            "                                \"currency\": \"INR\",\n" +
            "                                \"value\": \"64800\"\n" +
            "                            },\n" +
            "                            \"payMethod\": \"UPI\",\n" +
            "                            \"refundTurnAroundTime\": \"2021-12-02T02:53:12+05:30\",\n" +
            "                            \"rrnCode\": \"133555442695\",\n" +
            "                            \"status\": \"SUCCESS\",\n" +
            "                            \"virtualPaymentAddress\": \"ananth.mar*****1@okicici\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"destination\": \"TO_SOURCE\",\n" +
            "                    \"refundDetailAmount\": {\n" +
            "                        \"currency\": \"INR\",\n" +
            "                        \"value\": \"64800\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ],\n" +
            "            \"refundFundChannelInfo\": [\n" +
            "                {\n" +
            "                    \"addAndPay\": false,\n" +
            "                    \"amount\": {\n" +
            "                        \"currency\": \"INR\",\n" +
            "                        \"value\": \"64800\"\n" +
            "                    },\n" +
            "                    \"assetTool\": \"EXTINSTPAY\",\n" +
            "                    \"feeRateFactorsInfo\": \"{}\",\n" +
            "                    \"payMethod\": \"UPI\",\n" +
            "                    \"sourceAssetTool\": \"EXTINSTPAY\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"refundId\": \"20211201111212801300168975379057625\",\n" +
            "            \"refundSuccessResult\": {\n" +
            "                \"resultCode\": \"SUCCESS\",\n" +
            "                \"resultCodeId\": \"00000000\",\n" +
            "                \"resultMsg\": \"SUCCESS\",\n" +
            "                \"resultStatus\": \"S\"\n" +
            "            },\n" +
            "            \"refundSuccessTime\": \"2021-12-01T14:53:12+05:30\",\n" +
            "            \"returnChargeToPayer\": false,\n" +
            "            \"rrnCode\": \"133555442695\",\n" +
            "            \"totalSuccessfulRefundedAmountOnOrder\": {\n" +
            "                \"currency\": \"INR\",\n" +
            "                \"value\": \"64800\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"signature\": \"54105c29113449c13f0a1c6f95c4cddbd59775ad9a9bf043601ba52d0b8f35a7\"\n" +
            "}";

    public RefundSuccessNotifyApi(String mid){
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.REFUND_SUCCESS_NOTIFY);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setContext("request.head.clientId", mid);
    }

    public String getRequest() {return request;}
}
