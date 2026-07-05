package com.paytm.api.Deals;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.swagger.models.auth.In;

import java.io.UnsupportedEncodingException;

public class InitiateTransaction extends BaseApi {

    private String orderId;
    private String bankOfferRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"clientId\": \"cart\",\n" +
            "        \"requestTimestamp\": \"2023-01-11T10:47:14.247Z\",\n" +
            "        \"token\": \"{{jwt}}\",\n" +
            "        \"tokenType\": \"JWT\",\n" +
            "        \"version\": \"v1\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"affordabilityDetails\": {\n" +
            "            \"settlement\": [\n" +
            "                {\n" +
            "                    \"action_type\": \"applyDiscount\",\n" +
            "                    \"contriInfo\": {\n" +
            "                        \"bank\": 0,\n" +
            "                        \"brand\": 0,\n" +
            "                        \"merchant\": 10,\n" +
            "                        \"platform\": 0,\n" +
            "                        \"user\": 0\n" +
            "                    },\n" +
            "                    \"redemption_type\": \"discount\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"affordabilityInfo\": {\n" +
            "            \"paymentFlow\": \"DEAL_FLOW\"\n" +
            "        },\n" +
            "        \"paymentOffersAppliedV2\": {\n" +
            "            \"verificationCode\": \"790878270\",\n" +
            "            \"promoCode\": \"DEALSQATEST11\",\n" +
            "            \"promoContext\": {\n" +
            "                \"cart\": \"{\\\"items\\\":{\\\"1349568965421\\\":{\\\"price\\\":{payableAmount},\\\"product\\\":{\\\"id\\\":1234580979,\\\"merchant_id\\\":63795704,\\\"brand_id\\\":571366,\\\"vertical_id\\\":1218,\\\"category_ids\\\":[166091]}}}}\"\n" +
            "            },\n" +
            "            \"savings\": [\n" +
            "                {\n" +
            "                    \"savings\": \"10.00\",\n" +
            "                    \"redemptionType\": \"discount\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"callbackUrl\": \"https://cart-staging.paytm.com/payment/status\",\n" +
            "        \"cardTokenRequired\": \"true\",\n" +
            "        \"custId\": 1000885542,\n" +
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
            "        \"posId\": \"97001370\",\n"+
            "        \"merchantRequestId\": \"9310312\",\n"+
            "        \"mercUnqRef\": \"9310312\",\n"+
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
            "                    \"phoneNumber\": \"\"\n" +
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
            "        \"peonUrl\": \"https://cart-staging.paytm.com/dev1/payment/g/paytmnew/recon\",\n" +
            "        \"addMoneyFeeAppliedOnWallet\": false,\n" +
            "        \"isNativeAddMoney\": false,\n" +
            "        \"offlineFlow\": false,\n" +
            "        \"orderId\": \"{{orderId}}\",\n" +
            "        \"payableAmount\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": 180\n" +
            "        },\n" +
            "        \"requestType\": \"Payment\",\n" +
            "        \"shippingInfo\": [],\n" +
            "        \"txnAmount\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": \"190\"\n" +
            "        },\n" +
            "        \"userInfo\": {\n" +
            "            \"custId\": 1000885542,\n" +
            "            \"email\": \"\",\n" +
            "            \"firstName\": \"\",\n" +
            "            \"lastName\": \"\",\n" +
            "            \"mobile\": 5656144567\n" +
            "        },\n" +
            "        \"websiteName\": \"retail\"\n" +
            "    }\n" +
            "}";

    private String dealsRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"clientId\": \"cart\",\n" +
            "        \"requestTimestamp\": \"2023-01-11T10:47:14.247Z\",\n" +
            "        \"token\": \"{{jwt}}\",\n" +
            "        \"tokenType\": \"JWT\",\n" +
            "        \"version\": \"v1\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"affordabilityDetails\": {\n" +
            "            \"settlement\": [\n" +
            "                {\n" +
            "                    \"action_type\": \"applyDiscount\",\n" +
            "                    \"contriInfo\": {\n" +
            "                        \"bank\": 0,\n" +
            "                        \"brand\": 0,\n" +
            "                        \"merchant\": 10,\n" +
            "                        \"platform\": 0,\n" +
            "                        \"user\": 0\n" +
            "                    },\n" +
            "                    \"redemption_type\": \"discount\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"affordabilityInfo\": {\n" +
            "            \"paymentFlow\": \"DEAL_FLOW\"\n" +
            "        },\n" +
            "        \"callbackUrl\": \"https://cart-staging.paytm.com/payment/status\",\n" +
            "        \"cardTokenRequired\": \"true\",\n" +
            "        \"custId\": 1000885542,\n" +
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
            "        \"posId\": \"97001370\",\n"+
            "        \"merchantRequestId\": \"9310312\",\n"+
            "        \"mercUnqRef\": \"9310312\",\n"+
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
            "                    \"phoneNumber\": \"\"\n" +
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
            "        \"peonUrl\": \"https://cart-staging.paytm.com/dev1/payment/g/paytmnew/recon\",\n" +
            "        \"addMoneyFeeAppliedOnWallet\": false,\n" +
            "        \"isNativeAddMoney\": false,\n" +
            "        \"offlineFlow\": false,\n" +
            "        \"orderId\": \"{{orderId}}\",\n" +
            "        \"payableAmount\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": 190\n" +
            "        },\n" +
            "        \"requestType\": \"Payment\",\n" +
            "        \"shippingInfo\": [],\n" +
            "        \"txnAmount\": {\n" +
            "            \"currency\": \"INR\",\n" +
            "            \"value\": \"190\"\n" +
            "        },\n" +
            "        \"userInfo\": {\n" +
            "            \"custId\": 1000885542,\n" +
            "            \"email\": \"\",\n" +
            "            \"firstName\": \"\",\n" +
            "            \"lastName\": \"\",\n" +
            "            \"mobile\": 5656144567\n" +
            "        },\n" +
            "        \"websiteName\": \"retail\"\n" +
            "    }\n" +
            "}";

    String cart = "{\"items\":{\"1349568965421\":{\"price\":{amount},\"product\":{\"id\":1234580979,\"merchant_id\":63795704,\"brand_id\":571366,\"vertical_id\":1218,\"category_ids\":[166091]}}}}";

    public String getCart() {
        return cart;
    }

    public void setCart(String cart) {
        this.cart = cart;
    }

    public String getBankOfferRequest() {
        return bankOfferRequest;
    }

    public void setBankOfferRequest(String bankOfferRequest) {
        this.bankOfferRequest = bankOfferRequest;
    }

    public String getDealsRequest() {
        return dealsRequest;
    }

    public void setDealsRequest(String dealsRequest) {
        this.dealsRequest = dealsRequest;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public InitiateTransaction() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);
    }

    public InitiateTransaction dealsbuildRequest(String mid, String amount, String sso) throws UnsupportedEncodingException {
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
        getRequestSpecBuilder().setBody(getDealsRequest());
        setContext("body.payableAmount.value", amount);
        return this;
    }

    public InitiateTransaction dealsBankOfferbuildRequest(String mid, String amount, String payableAmount,String sso, String promoCode, String verificationCode) throws UnsupportedEncodingException {
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
        setBankOfferRequest(getBankOfferRequest().replace("{payableAmount}",payableAmount));
        getRequestSpecBuilder().setBody(getBankOfferRequest());
        return this;
    }
}
