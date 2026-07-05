package com.paytm.api.StoreCash;

import com.paytm.LocalConfig;
import com.paytm.api.Deals.InitiateTransaction;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;
import io.swagger.models.auth.In;

import java.io.UnsupportedEncodingException;

public class MLPInitiateTransaction  extends BaseApi
{
    private String orderId;
    private String MLPInitTxnRequest ="{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"clientId\": \"cart\",\n" +
            "        \"requestTimestamp\": \"2023-01-11T10:47:14.247Z\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"{{sso}}\",\n" +
            "        \"version\": \"v1\",\n" +
            "        \"signature\": \"{{signature}}\"\n" +
            "    },\n" +
            "    \"body\": {\"affordabilityInfo\":{\"paymentFlow\":\"DEAL_FLOW\"},\"callbackUrl\":\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\",\"cardTokenRequired\":\"true\",\"custId\":1107232315,\"disablePaymentMode\":[{\"banks\":[],\"channels\":[],\"mode\":\"PAY_AT_COUNTER\"},{\"banks\":[],\"channels\":[],\"mode\":\"COD\"},{\"banks\":[],\"channels\":[],\"mode\":\"ESCROW\"}],\"paytmSsoToken\":\"{{sso}}\",\"enablePaymentMode\":[],\"extendInfo\":{\"subwalletAmount\":{\"FOOD\":\"0\"},\"udf1\":\"oms_order\",\"storeCashIssuableAmount\":\"120.00\"},\"goods\":[{\"category\":\"Paytm Rewards/166091/paytmrewards/1218\",\"description\":\"BLR Brewing Co.\",\"extendInfo\":{\"areaCode\":\"\",\"operator\":\"\",\"phoneNumber\":\"\"},\"merchantGoodsId\":1234580979,\"merchantShippingId\":1349568965421,\"price\":{\"currency\":\"INR\",\"value\":100000},\"quantity\":1,\"snapshotUrl\":\"\"}],\"mid\":\"{{mid}}\",\"native\":true,\"peonUrl\":\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\",\"addMoneyFeeAppliedOnWallet\":false,\"isNativeAddMoney\":false,\"offlineFlow\":false,\"orderId\":\"{{orderId}}\",\"payableAmount\":{\"currency\":\"INR\",\"value\":\"{{payableAmountValue}}\"},\"requestType\":\"Payment\",\"shippingInfo\":[],\"txnAmount\":{\"currency\":\"INR\",\"value\":\"{{amount}}\"},\"userInfo\":{\"custId\":1107232315,\"email\":\"\",\"firstName\":\"\",\"lastName\":\"\",\"mobile\":5656144567},\"websiteName\":\"MarketplaceBeta\"}\n" +
            "}";

    String MLPBankOfferRequest="{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"clientId\": \"C11\",\n" +
            "        \"requestTimestamp\": \"2023-01-11T10:47:14.247Z\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"{{sso}}\",\n" +
            "        \"version\": \"v1\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"paymentOffersAppliedV2\": {\n" +
            "            \"verificationCode\": \"-1778747602\",\n" +
            "            \"promoCode\": \"TEST02\",\n" +
            "            \"promoContext\": {\n" +
            "                \"cart\": \"{\\\"items\\\":{\\\"1349568965421\\\":{\\\"price\\\":25,\\\"product\\\":{\\\"id\\\":1234580979,\\\"merchant_id\\\":63795704,\\\"brand_id\\\":571366,\\\"vertical_id\\\":1218,\\\"category_ids\\\":[166091]}}}}\"\n" +
            "            },\n" +
            "            \"savings\": [\n" +
            "                {\n" +
            "                    \"savings\": \"2.75\",\n" +
            "                    \"redemptionType\": \"discount\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"affordabilityInfo\": {\n" +
            "            \"paymentFlow\": \"DEAL_FLOW\"\n" +
            "        },\n" +
            "        \"callbackUrl\": \"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\",\n" +
            "        \"cardTokenRequired\": \"true\",\n" +
            "        \"custId\": 1001840919,\n" +
            "        \"disablePaymentMode\": [\n" +
            "            {\n" +
            "                \"banks\": [],\n" +
            "                \"channels\": [],\n" +
            "                \"mode\": \"PAY_AT_COUNTER\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"banks\": [],\n" +
            "                \"channels\": [],\n" +
            "                \"mode\": \"COD\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"banks\": [],\n" +
            "                \"channels\": [],\n" +
            "                \"mode\": \"ESCROW\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paytmSsoToken\": \"{{sso}}\",\n" +
            "        \"enablePaymentMode\": [],\n" +
            "        \"extendInfo\": {\n" +
            "            \"subwalletAmount\": {\n" +
            "                \"FOOD\": \"0\"\n" +
            "            },\n" +
            "            \"udf1\": \"oms_order\"\n" +
            "        },\n" +
            "        \"goods\": [\n" +
            "            {\n" +
            "                \"category\": \"Paytm Rewards/166091/paytmrewards/1218\",\n" +
            "                \"description\": \"BLR Brewing Co.\",\n" +
            "                \"extendInfo\": {\n" +
            "                    \"areaCode\": \"\",\n" +
            "                    \"operator\": \"\",\n" +
            "                    \"phoneNumber\": \"\",\n" +
            "                    \"storeCashIssuableAmount\": \"120.00\"\n" +
            "\n" +
            "                },\n" +
            "                \"merchantGoodsId\": 1234580979,\n" +
            "                \"merchantShippingId\": 1349568965421,\n" +
            "                \"price\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": 100000\n" +
            "                },\n" +
            "                \"quantity\": 1,\n" +
            "                \"snapshotUrl\": \"https://catalog-staging.paytm.com/v1/mobile/product/1234580979?visibility_origin=offline&merchant_id=63795704\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"mid\": \"{{mid}}\",\n" +
            "        \"native\": true,\n" +
            "        \"peonUrl\": \"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\",\n" +
            "        \"addMoneyFeeAppliedOnWallet\": false,\n" +
            "        \"isNativeAddMoney\": false,\n" +
            "        \"offlineFlow\": false,\n" +
            "        \"orderId\": \"{{orderId}}\",\n" +
            "        \"payableAmount\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": 23.25\n" +
            "        },\n" +
            "        \"requestType\": \"Payment\",\n" +
            "        \"shippingInfo\": [],\n" +
            "        \"txnAmount\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": \"26\"\n" +
            "        },\n" +
            "        \"userInfo\": {\n" +
            "            \"custId\": 1001840919,\n" +
            "            \"email\": \"\",\n" +
            "            \"firstName\": \"\",\n" +
            "            \"lastName\": \"\",\n" +
            "            \"mobile\": 5656144567\n" +
            "        },\n" +
            "        \"websiteName\": \"MarketplaceBeta\"\n" +
            "    }\n" +
            "}";

    String cart = "{\"items\":{\"1349568965421\":{\"price\":{amount},\"product\":{\"id\":1234580979,\"merchant_id\":63795704,\"brand_id\":571366,\"vertical_id\":1218,\"category_ids\":[166091]}}}}";

    public String getCart() {
        return cart;
    }

    public void setCart(String cart) {
        this.cart = cart;
    }

    public String getMLPInitTxnRequest() {
        return MLPInitTxnRequest;
    }

    public void setMLPInitTxnRequest(String MLPInitTxnRequest) {
        this.MLPInitTxnRequest = MLPInitTxnRequest;
    }

    public String getMLPBankOfferRequest() {
        return MLPBankOfferRequest;
    }

    public void setMLPBankOfferRequest(String MLPBankOfferRequest) {
        this.MLPBankOfferRequest = MLPBankOfferRequest;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public MLPInitiateTransaction() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);
    }

    public MLPInitiateTransaction withoutOfferBuildRequest(String mid, String amount, String sso) throws UnsupportedEncodingException
    {
        Constants.MerchantType merchant= Constants.MerchantType.STORE_CASH;
        String orderId = CommonHelpers.generateOrderId();
//        String jwtToken = PGPHelpers.createTokenForDealsTxn(mid,orderId,amount);
        setOrderId(orderId);
        setContext("head.token", sso);
        setContext("body.mid", mid);
        setContext("body.orderId", orderId);
        setContext("body.txnAmount.value", amount);
        setContext("body.paytmSsoToken", sso);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        setContext("body.payableAmount.value", amount);
        setContext("head.signature",getChecksum(merchant,amount,sso,orderId));
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBody(getMLPInitTxnRequest());
        return this;
    }

    public MLPInitiateTransaction bankOfferbuildRequest(String mid, String amount, String payableAmount,String sso, String promoCode, String verificationCode) throws UnsupportedEncodingException
    {
        String orderId = CommonHelpers.generateOrderId();
        String jwtToken = PGPHelpers.createTokenForDealsTxn(mid,orderId,amount);
        setOrderId(orderId);
        setContext("head.token", jwtToken);
        setContext("body.mid", mid);
        setContext("body.orderId", orderId);
        setContext("body.txnAmount.value", amount);
        setContext("body.paytmSsoToken", sso);
        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        setContext("body.payableAmount.value", payableAmount);
        setContext("body.paymentOffersAppliedV2.promoCode", promoCode);
        setContext("body.paymentOffersAppliedV2.verificationCode", verificationCode);
        setMLPBankOfferRequest(getMLPBankOfferRequest().replace("{payableAmount}",payableAmount));
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBody(getMLPBankOfferRequest());
        return this;
    }

    public String getChecksum(Constants.MerchantType merchant,String amount,String sso, String orderId)
    {
        String body = getMLPInitTxnRequest().replace("{{mid}}",merchant.getId())
                .replace("{{sso}}",sso)
                .replace("{{orderId}}",orderId)
                .replace("{{amount}}",amount)
                .replace("{{payableAmountValue}}",amount);
        body= body.substring(body.indexOf("body")+7,body.length()-1);
        body=body.trim();
        String checksum = PGPUtil.getChecksum(merchant.getKey(), body);
        return checksum;
    }
}
