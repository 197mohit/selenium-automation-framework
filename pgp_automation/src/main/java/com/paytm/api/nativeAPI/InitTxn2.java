package com.paytm.api.nativeAPI;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InitTxn2 extends BaseApi {

    private static final String jsonPath = "/home/nikunjkumar/repos/pgp_automation/src/test/java/scripts/Native/Init.json";
    private static Map<String, String> defaultChanges = new HashMap<>();

    public InitTxn2(Builder builder) throws IOException, ParseException {
        this(builder.orderId, builder.mid, builder);
    }

    public InitTxn2(String orderId, String mid, Builder builder) throws IOException, ParseException {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);

        getRequestSpecBuilder().addQueryParam("mid", mid);
        getRequestSpecBuilder().addQueryParam("orderId", orderId);
        setBody(jsonPath, defaultChanges);
    }

    public static class Builder {
        private String mid;
        private String orderId;
        private String ssoToken;
        private Map<String, String> testSpecificChanges = new HashMap<>();

        public Builder(String ssoToken, MerchantType mid) {
            this.ssoToken = ssoToken;
            this.mid = mid.getId();
            this.orderId = CommonHelpers.generateOrderId();
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setSsoToken(String ssoToken) {
            this.ssoToken = ssoToken;
            return this;
        }

        public Builder setChannelId(String channelId) {
            defaultChanges.put("head.channelId", channelId);
            return this;
        }

        public Builder setTxnValue(String txnValue) {
            defaultChanges.put("body.txnAmount.value",txnValue);
            return this;
        }

        public InitTxn2 build() throws IOException, ParseException {
            defaultChanges.put("body.orderId", this.orderId);
            defaultChanges.put("body.mid", this.mid);
            defaultChanges.put("body.paytmSsoToken", this.ssoToken);

            if (testSpecificChanges != null) {
                defaultChanges.putAll(testSpecificChanges);
            }
            return new InitTxn2(this);
        }
    }

}

