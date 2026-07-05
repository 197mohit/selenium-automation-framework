package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

/** POST {@code /api/emi/shareInvoiceViaSms} — body includes {@code pgmid} (not in path). */
public class DealsShareInvoiceViaSms extends BaseApi {

    /** For curl logging / tests — path under Deals base. */
    public static final String API_PATH = "/api/emi/shareInvoiceViaSms";

    private static final String BASE_PATH = API_PATH;
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/146.0.7680.177 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private DealsShareInvoiceViaSms(Builder b) {
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
                "sec-ch-ua", "\"Chromium\";v=\"146\", \"Not-A.Brand\";v=\"24\", \"Android WebView\";v=\"146\"");
        getRequestSpecBuilder().addHeader("sec-fetch-site", "same-origin");
        getRequestSpecBuilder().addHeader("sec-fetch-mode", "cors");
        getRequestSpecBuilder().addHeader("sec-fetch-dest", "empty");
        getRequestSpecBuilder().addHeader("custom-cookie", "");
        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);
        if (b.utmSource != null && !b.utmSource.isEmpty()) {
            getRequestSpecBuilder().addQueryParam("utm_source", b.utmSource);
        }
        if (b.cookie != null && !b.cookie.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", b.cookie);
        }
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }
        JSONObject body = new JSONObject();
        body.put("pgmid", b.pgmid);
        body.put("orderId", b.orderId);
        body.put("phoneNo", b.phoneNo);
        body.put("bankName", b.bankName);
        if (b.transactionAmountRupees != null) {
            body.put("transactionAmount", b.transactionAmountRupees);
        } else {
            body.put("transactionAmount", b.transactionAmount);
        }
        body.put("transactionDate", b.transactionDate);
        body.put("paymentMethod", b.paymentMethod);
        body.put("merchantName", b.merchantName);
        getRequestSpecBuilder().setBody(body.toString());
    }

    public static final class Builder {
        private String baseUri;
        private String pgmid;
        private String orderId;
        private String phoneNo;
        private String bankName;
        private String transactionAmount;
        /** When set, JSON body uses a numeric value (e.g. {@code 5890}) as in store-attendant curl. */
        private Integer transactionAmountRupees;
        private String transactionDate;
        private String paymentMethod;
        private String merchantName;
        private String referer;
        private String cookie;
        private String userAgent;
        private String xRequestedWith = "com.paytm.store_attendant";
        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S26.04.23";
        private String requestId;
        private String utmSource;

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder pgmid(String pgmid) {
            this.pgmid = pgmid;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder phoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
            return this;
        }

        public Builder bankName(String bankName) {
            this.bankName = bankName;
            return this;
        }

        public Builder transactionAmount(String transactionAmount) {
            this.transactionAmount = transactionAmount;
            this.transactionAmountRupees = null;
            return this;
        }

        /**
         * Whole rupees as JSON number (e.g. {@code 5890}) — matches {@code /api/emi/shareInvoiceViaSms} curl; takes
         * precedence over {@link #transactionAmount(String)}.
         */
        public Builder transactionAmountRupeesInt(int amountRupees) {
            this.transactionAmountRupees = amountRupees;
            return this;
        }

        public Builder transactionDate(String transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder merchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public Builder referer(String referer) {
            this.referer = referer;
            return this;
        }

        public Builder cookie(String cookie) {
            this.cookie = cookie;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder xRequestedWith(String xRequestedWith) {
            this.xRequestedWith = xRequestedWith;
            return this;
        }

        public Builder buildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
            return this;
        }

        public Builder globalTs(long globalTs) {
            this.globalTs = globalTs;
            return this;
        }

        public Builder utmSource(String utmSource) {
            this.utmSource = utmSource;
            return this;
        }

        public DealsShareInvoiceViaSms build() {
            if (pgmid == null || orderId == null) {
                throw new IllegalStateException("pgmid and orderId are required");
            }
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            return new DealsShareInvoiceViaSms(this);
        }
    }
}
