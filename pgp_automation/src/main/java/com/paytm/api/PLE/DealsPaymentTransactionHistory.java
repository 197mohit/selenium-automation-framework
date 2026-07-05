package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/** GET paginated payment transaction history — {@code /api/payment/transaction/history?mid=...}. */
public class DealsPaymentTransactionHistory extends BaseApi {

    private static final String BASE_PATH = "/api/payment/transaction/history";
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/146.0.7680.119 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private DealsPaymentTransactionHistory(Builder b) {
        setMethod(MethodType.GET);
        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(BASE_PATH);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("accept", "application/json, text/plain, */*");
        getRequestSpecBuilder().addHeader("accept-language", "en-IN,en-US;q=0.9,en;q=0.8");
        getRequestSpecBuilder().addHeader("priority", "u=1, i");
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
        getRequestSpecBuilder().addQueryParam("mid", b.mid);
        getRequestSpecBuilder().addQueryParam("pageNum", String.valueOf(b.pageNum));
        getRequestSpecBuilder().addQueryParam("startDate", b.startDate);
        getRequestSpecBuilder().addQueryParam("endDate", b.endDate);
        getRequestSpecBuilder().addQueryParam("nextPageNum", String.valueOf(b.nextPageNum));
        getRequestSpecBuilder().addQueryParam("size", String.valueOf(b.size));
        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);
        getRequestSpecBuilder().addQueryParam("utm_source", b.utmSource);
        if (b.cookie != null && !b.cookie.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", b.cookie);
        }
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }
    }

    public static final class Builder {
        private String baseUri;
        private String mid;
        private int pageNum;
        private int nextPageNum;
        private int size = 10;
        private String startDate;
        private String endDate;
        private String referer;
        private String cookie;
        private String userAgent;
        private String xRequestedWith = "com.paytm.store_attendant";
        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S26.04.02";
        private String requestId;
        private String utmSource = "login-via-username";

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder pageNum(int pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder nextPageNum(int nextPageNum) {
            this.nextPageNum = nextPageNum;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder startDate(String startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(String endDate) {
            this.endDate = endDate;
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

        public DealsPaymentTransactionHistory build() {
            if (mid == null || startDate == null || endDate == null) {
                throw new IllegalStateException("mid, startDate, and endDate are required");
            }
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            return new DealsPaymentTransactionHistory(this);
        }
    }
}
