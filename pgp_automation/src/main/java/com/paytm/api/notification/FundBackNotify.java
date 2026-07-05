package com.paytm.api.notification;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FundBackNotify extends BaseApi {
    String fnRequest="{\n" +
            "  \"request\": {\n" +
            "    \"head\": {\n" +
            "      \"clientId\": \"2016030715243903536806\",\n" +
            "      \"function\": \"oldpg.acquiring.order.fundBackNotify\",\n" +
            "      \"reqTime\": \"2023-05-30T12:10:11+05:30\",\n" +
            "      \"sourceService\": \"PG-2.0-acquiring\",\n" +
            "      \"version\": \"1.1.4\",\n" +
            "      \"reqMsgId\": \"202305300110100008702766848119688112023-05-30T12:10:11+05:30\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "      \"acquirementId\": \"20230530011010000870276684811968811\",\n" +
            "      \"retryCount\": 0,\n" +
            "      \"destination\": \"TO_SOURCE\",\n" +
            "      \"createOrderTime\": \"2023-05-30T12:08:50+05:30\",\n" +
            "      \"resultInfo\": {\n" +
            "        \"resultStatus\": \"S\",\n" +
            "        \"resultCode\": \"SUCCESS\",\n" +
            "        \"resultCodeId\": \"00000000\",\n" +
            "        \"resultMsg\": \"SUCCESS\"\n" +
            "      },\n" +
            "      \"orderExtendInfo\": \"{\\\"totalTxnAmount\\\":\\\"62514\\\",\\\"checkoutJsAppInvokePayment\\\":\\\"false\\\",\\\"CUST_ID\\\":\\\"1124719589\\\",\\\"ROUTE\\\":\\\"PG2\\\",\\\"graceDays\\\":\\\"0\\\",\\\"PAYTM_USER_ID\\\":\\\"1124719589\\\",\\\"pushDataToDynamicQR\\\":\\\"false\\\",\\\"communicationManager\\\":\\\"false\\\",\\\"merchantName\\\":\\\"Pay*******\\\",\\\"isMerchantLimitUpdatedForPay\\\":\\\"false\\\",\\\"mccCode\\\":\\\"Recharge\\\",\\\"linkBasedInvoicePayment\\\":\\\"false\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"isMerchantLimitEnabledForPay\\\":\\\"false\\\",\\\"userMobile\\\":\\\"9972953111\\\",\\\"clientIP\\\":\\\"122.172.84.239\\\",\\\"issuingBankId\\\":\\\"ICICI\\\",\\\"alipayMerchantId\\\":\\\"216820000180333466515\\\",\\\"aoaSubsOnPgMid\\\":\\\"false\\\",\\\"linkBasedNonInvoicePayment\\\":\\\"false\\\",\\\"offlineFlow\\\":\\\"false\\\",\\\"merchantTransId\\\":\\\"21155530559\\\",\\\"custID\\\":\\\"1124719589\\\",\\\"paytmMerchantId\\\":\\\"qa12aj94651776155721\\\",\\\"paymentRequestFlow\\\":\\\"nativeJsonRequest\\\",\\\"cardTokenRequired\\\":\\\"true\\\",\\\"enhancedNative\\\":\\\"false\\\",\\\"cardHash\\\":\\\"1001&93aa910a1c100e99aa37ea38430961720aa66b25492c1609009bae7d95aa2aff\\\",\\\"cardIndexNo\\\":\\\"1001&93aa910a1c100e99aa37ea38430961720aa66b25492c1609009bae7d95aa2aff\\\",\\\"issuingBankName\\\":\\\"ICICI Bank\\\",\\\"preDebitRenewal\\\":\\\"false\\\",\\\"originalPrice\\\":\\\"625.14\\\",\\\"TXN_TOKEN\\\":\\\"9aa1ed7130004911a6911dc037e270b11685428729644\\\",\\\"isEnhancedNative\\\":\\\"false\\\",\\\"phoneNo\\\":\\\"9972953111\\\",\\\"platformFallbackDisabled\\\":\\\"false\\\",\\\"paymodeIdentifier\\\":\\\"12\\\",\\\"cobrandedCustomDisplayName\\\":\\\"ICICI Bank\\\",\\\"peonURL\\\":\\\"https://automation-pg-ext.paytm.in/mockbank/peon\\\",\\\"promoCode\\\":\\\"\\\",\\\"userEmail\\\":\\\"jinnikatari@gmail.com\\\",\\\"merchantOnPaytm\\\":\\\"true\\\",\\\"website\\\":\\\"PaytmMarketPlace\\\",\\\"callBackURL\\\":\\\"https://cart.paytm.com/payment/status\\\",\\\"requestType\\\":\\\"NATIVE\\\",\\\"merchantLimitEnabled\\\":\\\"false\\\",\\\"subwalletWithdrawMaxAmountDetails\\\":\\\"{\\\\\\\"FOOD\\\\\\\":0}\\\",\\\"fromAoaMerchant\\\":\\\"false\\\",\\\"merchantLimitUpdated\\\":\\\"false\\\",\\\"udf1\\\":\\\"oms_order\\\",\\\"subsRenewOrderAlreadyCreated\\\":\\\"false\\\",\\\"merchantKybId\\\":\\\"A0jp9z34x9397\\\",\\\"productCode\\\":\\\"51051000100000000001\\\",\\\"autoRenewal\\\":\\\"false\\\",\\\"autoRetry\\\":\\\"false\\\"}\",\n" +
            "      \"buyerInfo\": {\n" +
            "        \"externalUserType\": \"BUYER\",\n" +
            "        \"externalUserId\": \"1124719589\",\n" +
            "        \"nickname\": \"JIN*********\",\n" +
            "        \"userId\": \"1124719589\"\n" +
            "      },\n" +
            "      \"fundBackReason\": \"ORDER_IS_CLOSED\",\n" +
            "      \"orderAmount\": {\n" +
            "        \"currency\": \"INR\",\n" +
            "        \"value\": \"356\"\n" +
            "      },\n" +
            "      \"productCode\": \"51051000100000000001\",\n" +
            "      \"merchantId\": \"qa12aj94651776155721\",\n" +
            "      \"contractId\": \"302284901106161666\",\n" +
            "      \"merchantTransId\": \"21155530559\",\n" +
            "      \"fundBackChannelInfoList\": [\n" +
            "        {\n" +
            "          \"maskedBankAccountNumber\": \"************6009\",\n" +
            "          \"rrnCode\": null,\n" +
            "          \"amount\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": \"62514\"\n" +
            "          },\n" +
            "          \"issuingBankName\": null,\n" +
            "          \"cardScheme\": null,\n" +
            "          \"payMethod\": null,\n" +
            "          \"addAndPay\": false,\n" +
            "          \"maskedCardNumber\": \"************6009\",\n" +
            "          \"maskedEcomToken\": null,\n" +
            "          \"virtualPaymentAddress\": null,\n" +
            "          \"status\": null,\n" +
            "          \"prepaidCard\": null,\n" +
            "          \"issuerBankInstCode\": null,\n" +
            "          \"refundTurnAroundTime\": null,\n" +
            "          \"userMobileNo\": \"9972953111\",\n" +
            "          \"ifscCode\": \"TOKEN\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  \"signature\": \"935060f6ac949643f9b0d822b08bd29c9996ab7527dc073695a36955f423e4e6\"\n" +
            "}";


    public FundBackNotify setAcquirementID(String acquirementID){
        setContext("request.body.acquirementId",acquirementID);
        return this;
    }
    public FundBackNotify setIssuingBank(String issuingBank){
        setContext("request.body.fundBackChannelInfoList[0].issuingBankName",issuingBank);
        return this;
    }
    public FundBackNotify setCardScheme(String cardScheme){
        setContext("request.body.fundBackChannelInfoList[0].cardScheme",cardScheme);
        return this;
    }
    public FundBackNotify setPayMethod(String payMethod){
        setContext("request.body.fundBackChannelInfoList[0].payMethod",payMethod);
        return this;
    }

    public FundBackNotify setVirtualPaymentAddress(String virtualPaymentAddress){
        setContext("request.body.fundBackChannelInfoList[0].virtualPaymentAddress",virtualPaymentAddress);
        return this;
    }
    public FundBackNotify setResultStatus(String resultStatus){
        setContext("request.body.resultInfo.resultStatus",resultStatus);
        return this;
    }
    public FundBackNotify setResultCode(String resultCode){
        setContext("request.body.resultInfo.resultCode",resultCode);
        return this;
    }
    public FundBackNotify setResultCodeID(String resultCodeID){
        setContext("request.body.resultInfo.resultCodeId",resultCodeID);
        return this;
    }
    public FundBackNotify setResultMsg(String resultMsg){
        setContext("request.body.resultInfo.resultMsg",resultMsg);
        return this;
    }

    public FundBackNotify setMerchantTransId(String merchantTransId){
        setContext("request.body.merchantTransId",merchantTransId);
        return this;
    }
    public FundBackNotify setFundBackReason(String fundBackReason){
        setContext("request.body.fundBackReason",fundBackReason);
        return this;
    }
    public FundBackNotify setMerchantID(String merchantID){
        setContext("request.body.merchantId",merchantID);
        return this;
    }

    public FundBackNotify() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getFundbackNotifyRequest());
        getRequestSpecBuilder().setBasePath(Constants.NotificationService.FUNDBACK_NOTIFY);
    }
    public String getFundbackNotifyRequest() {return fnRequest;}
}
