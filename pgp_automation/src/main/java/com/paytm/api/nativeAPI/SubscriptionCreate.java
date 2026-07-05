package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SubscriptionCreate extends BaseApi {


    public static BaseApi Default(String mId, String mKey, String orderId, String ssoToken, String txnAmt) {
        return new SubscriptionCreate(
                new InitTxnDTO.Builder(mId, mKey, ssoToken)
                        .setOrderId(orderId)
                        .setTxnValue(txnAmt)
                        .setSubscriptionPaymentMode("")
                        .setSubscriptionAmountType("VARIABLE")
                        .setSubscriptionMaxAmount("10")
                        .setSubscriptionFrequency("1")
                        .setSubscriptionFrequencyUnit("MONTH")
                        .setSubscriptionGraceDays("5")
                        .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                        .setRequestType("NATIVE_SUBSCRIPTION")
                        .build()
        );
    }


    public SubscriptionCreate(InitTxnDTO initTxnDTO) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_SUBSCRIPTION);
        getRequestSpecBuilder().addQueryParam("mid", initTxnDTO.getBody().getMid());
        getRequestSpecBuilder().addQueryParam("orderId", initTxnDTO.getBody().getOrderId());
        getRequestSpecBuilder().setBody(initTxnDTO);
    }

}
