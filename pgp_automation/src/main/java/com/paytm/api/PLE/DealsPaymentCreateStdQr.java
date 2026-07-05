package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

/**
 * POST {@code /api/payment/createStdQr} — standard QR for collect-payment / UPI style flows (staging curl: amount,
 * orderDetails, imageRequired, {@code additionalInfo.merchantName} / {@code paytmMerchantId}, migration flags).
 *
 * <p>Use {@link Builder#bodyJson(String)} for a verbatim capture; otherwise configure the structured builder.
 */
public class DealsPaymentCreateStdQr extends BaseApi {

    public static final String API_RELATIVE_PATH = "/api/payment/createStdQr";

    private static final String BASE_PATH = API_RELATIVE_PATH;

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/143.0.7499.35 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private static final String DEFAULT_SEC_CH_UA =
            "\"Android WebView\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"";

    private DealsPaymentCreateStdQr(Builder b) {
        setMethod(MethodType.POST);
        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(BASE_PATH);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("content-type", "application/json");
        getRequestSpecBuilder().addHeader("accept", "application/json, text/plain, */*");
        getRequestSpecBuilder().addHeader("accept-language", "en-IN,en-US;q=0.9,en;q=0.8");
        getRequestSpecBuilder().addHeader("priority", "u=1, i");
        getRequestSpecBuilder().addHeader("origin", base);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        String ua = b.userAgent != null && !b.userAgent.isEmpty() ? b.userAgent : DEFAULT_USER_AGENT;
        getRequestSpecBuilder().addHeader("user-agent", ua);
        getRequestSpecBuilder().addHeader("x-requested-with", b.xRequestedWith);
        getRequestSpecBuilder().addHeader("sec-ch-ua-mobile", "?1");
        getRequestSpecBuilder().addHeader("sec-ch-ua-platform", "\"Android\"");
        getRequestSpecBuilder().addHeader(
                "sec-ch-ua", b.secChUa != null && !b.secChUa.isEmpty() ? b.secChUa : DEFAULT_SEC_CH_UA);
        getRequestSpecBuilder().addHeader("sec-fetch-site", "same-origin");
        getRequestSpecBuilder().addHeader("sec-fetch-mode", "cors");
        getRequestSpecBuilder().addHeader("sec-fetch-dest", "empty");
        getRequestSpecBuilder().addHeader("custom-cookie", b.customCookie != null ? b.customCookie : "");

        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);
        if (b.cookie != null && !b.cookie.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", b.cookie);
        }
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }

        String body = b.bodyJson != null && !b.bodyJson.isEmpty() ? b.bodyJson : b.buildBodyJson();
        getRequestSpecBuilder().setBody(body);
    }

    public static final class Builder {
        private String baseUri;
        private String bodyJson;
        private String cookie;
        private String referer;
        private String userAgent;
        private String secChUa;
        private String customCookie = "";
        private String xRequestedWith = "com.paytm.store_attendant";
        private long globalTs = System.currentTimeMillis();
        /** Sample capture uses {@code S25.12.26}. */
        private String buildVersion = "S25.12.26";
        private String requestId;

        private String amount;
        private boolean orderDetails = true;
        private boolean imageRequired = true;
        private String mid;
        private String description = "UPI Payment";
        private String additionalMerchantName = "Emi Store Testing";
        private String additionalPaytmMerchantId;
        private boolean migrationStatusRequired = true;
        private boolean skipOrderCreation;

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder bodyJson(String bodyJson) {
            this.bodyJson = bodyJson;
            return this;
        }

        public Builder cookie(String cookie) {
            this.cookie = cookie;
            return this;
        }

        public Builder referer(String referer) {
            this.referer = referer;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder secChUa(String secChUa) {
            this.secChUa = secChUa;
            return this;
        }

        public Builder customCookieHeader(String customCookie) {
            this.customCookie = customCookie;
            return this;
        }

        public Builder xRequestedWith(String xRequestedWith) {
            this.xRequestedWith = xRequestedWith;
            return this;
        }

        public Builder globalTs(long globalTs) {
            this.globalTs = globalTs;
            return this;
        }

        public Builder buildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder orderDetails(boolean orderDetails) {
            this.orderDetails = orderDetails;
            return this;
        }

        public Builder imageRequired(boolean imageRequired) {
            this.imageRequired = imageRequired;
            return this;
        }

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder additionalMerchantName(String additionalMerchantName) {
            this.additionalMerchantName = additionalMerchantName;
            return this;
        }

        /** Defaults to {@link #mid(String)} when not set explicitly. */
        public Builder additionalPaytmMerchantId(String additionalPaytmMerchantId) {
            this.additionalPaytmMerchantId = additionalPaytmMerchantId;
            return this;
        }

        public Builder migrationStatusRequired(boolean migrationStatusRequired) {
            this.migrationStatusRequired = migrationStatusRequired;
            return this;
        }

        public Builder skipOrderCreation(boolean skipOrderCreation) {
            this.skipOrderCreation = skipOrderCreation;
            return this;
        }

        private String buildBodyJson() {
            String paytmMid =
                    additionalPaytmMerchantId != null && !additionalPaytmMerchantId.isEmpty()
                            ? additionalPaytmMerchantId
                            : mid;
            JSONObject additionalInfo = new JSONObject();
            additionalInfo.put("merchantName", additionalMerchantName);
            additionalInfo.put("paytmMerchantId", paytmMid);

            JSONObject root = new JSONObject();
            root.put("amount", amount);
            root.put("orderDetails", orderDetails);
            root.put("imageRequired", imageRequired);
            root.put("mid", mid);
            root.put("description", description != null ? description : "UPI Payment");
            root.put("additionalInfo", additionalInfo);
            root.put("migrationStatusRequired", migrationStatusRequired);
            root.put("skipOrderCreation", skipOrderCreation);
            return root.toString();
        }

        private void validateStructured() {
            if (amount == null || amount.isEmpty() || mid == null || mid.isEmpty()) {
                throw new IllegalStateException("amount and mid are required when bodyJson is not set");
            }
        }

        public DealsPaymentCreateStdQr build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            if (bodyJson == null || bodyJson.isEmpty()) {
                validateStructured();
            }
            return new DealsPaymentCreateStdQr(this);
        }
    }
}
