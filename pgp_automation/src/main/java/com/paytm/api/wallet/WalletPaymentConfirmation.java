package com.paytm.api.wallet;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class WalletPaymentConfirmation extends BaseApi {
//    private final String CONFIRMATION_URL = "/wallet-web/paymentConfirmation";


    public WalletPaymentConfirmation confirmation(String TxnID) {
        String body ="{\"alipayTransId\": \""+TxnID+"\",\"status\": \"SUCCESS\"}";

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.WALLET_HOST);
        getRequestSpecBuilder().setBasePath(Constants.WalletAPIResourcePath.CONFIRMATION_URL);
        getRequestSpecBuilder().setBody(body);

        return this;
    }
}