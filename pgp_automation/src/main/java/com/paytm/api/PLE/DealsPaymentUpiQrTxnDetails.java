package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

/**
 * POST {@code /api/payment/upiQr/txnDetails} — UPI QR transaction details lookup (minimal body: mid, tid,
 * invoiceNumber, time, date, year, {@code extendInfo.orderId}). Matches store-attendant staging curl.
 *
 * <p>Use {@link Builder#bodyJson(String)} to send a captured payload verbatim; otherwise {@link Builder} builds the
 * same JSON shape as the curl sample.
 */
public class DealsPaymentUpiQrTxnDetails extends BaseApi {

    public static final String API_RELATIVE_PATH = "/api/payment/upiQr/txnDetails";

    private static final String BASE_PATH = API_RELATIVE_PATH;

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/143.0.7499.35 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private static final String DEFAULT_SEC_CH_UA =
            "\"Android WebView\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"";

    private DealsPaymentUpiQrTxnDetails(Builder b) {
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
        private String buildVersion = "S26.01.07";
        private String requestId;

        private String mid;
        private String tid;
        private String invoiceNumber;
        private String time;
        private String date;
        private String year;
        private String extendInfoOrderId;

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        /** When set, structured fields are ignored. */
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

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder tid(String tid) {
            this.tid = tid;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder time(String time) {
            this.time = time;
            return this;
        }

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder year(String year) {
            this.year = year;
            return this;
        }

        /** Maps to {@code extendInfo.orderId}. */
        public Builder extendInfoOrderId(String extendInfoOrderId) {
            this.extendInfoOrderId = extendInfoOrderId;
            return this;
        }

        private String buildBodyJson() {
            JSONObject extendInfo = new JSONObject();
            extendInfo.put("orderId", extendInfoOrderId);

            JSONObject root = new JSONObject();
            root.put("mid", mid);
            root.put("tid", tid);
            root.put("invoiceNumber", invoiceNumber);
            root.put("time", time);
            root.put("date", date);
            root.put("year", year);
            root.put("extendInfo", extendInfo);
            return root.toString();
        }

        private void validateStructured() {
            if (mid == null
                    || tid == null
                    || invoiceNumber == null
                    || time == null
                    || date == null
                    || year == null
                    || extendInfoOrderId == null
                    || mid.isEmpty()
                    || tid.isEmpty()
                    || invoiceNumber.isEmpty()
                    || time.isEmpty()
                    || date.isEmpty()
                    || year.isEmpty()
                    || extendInfoOrderId.isEmpty()) {
                throw new IllegalStateException(
                        "mid, tid, invoiceNumber, time, date, year, extendInfoOrderId are required when bodyJson is not set");
            }
        }

        public DealsPaymentUpiQrTxnDetails build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            if (bodyJson == null || bodyJson.isEmpty()) {
                validateStructured();
            }
            return new DealsPaymentUpiQrTxnDetails(this);
        }
    }
}
