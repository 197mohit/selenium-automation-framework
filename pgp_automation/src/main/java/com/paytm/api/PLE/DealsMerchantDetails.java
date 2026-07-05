package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/** GET {@code /api/merchant/details} for a pgmid (Deals store-attendant / EMI_APP WebView). */
public class DealsMerchantDetails extends BaseApi {

    private static final String PATH = "/api/merchant/details";
    private static final String DEFAULT_UA =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/146.0.7680.119 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private DealsMerchantDetails(Builder b) {
        setMethod(MethodType.GET);
        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(PATH);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("accept", "application/json, text/plain, */*");
        getRequestSpecBuilder().addHeader("accept-language", "en-IN,en-US;q=0.9,en;q=0.8");
        getRequestSpecBuilder().addHeader("user-agent", b.userAgent != null ? b.userAgent : DEFAULT_UA);
        getRequestSpecBuilder().addHeader("x-requested-with", "com.paytm.store_attendant");
        getRequestSpecBuilder().addHeader("sec-ch-ua",
                "\"Chromium\";v=\"146\", \"Not-A.Brand\";v=\"24\", \"Android WebView\";v=\"146\"");
        getRequestSpecBuilder().addHeader("sec-ch-ua-mobile", "?1");
        getRequestSpecBuilder().addHeader("sec-ch-ua-platform", "\"Android\"");
        getRequestSpecBuilder().addHeader("sec-fetch-site", "same-origin");
        getRequestSpecBuilder().addHeader("sec-fetch-mode", "cors");
        getRequestSpecBuilder().addHeader("sec-fetch-dest", "empty");
        getRequestSpecBuilder().addHeader("priority", "u=1, i");
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addQueryParam("pgmid", b.pgmid);
        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);
        getRequestSpecBuilder().addQueryParam("utm_source", b.utmSource);
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }
    }

    public static final class Builder {
        private String baseUri;
        private String pgmid;
        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S26.04.06";
        private String requestId;
        private String utmSource = "login-via-username";
        private String referer;
        private String userAgent;

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder pgmid(String pgmid) {
            this.pgmid = pgmid;
            return this;
        }

        public Builder buildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
            return this;
        }

        public Builder utmSource(String utmSource) {
            this.utmSource = utmSource;
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

        public DealsMerchantDetails build() {
            if (pgmid == null || pgmid.isEmpty()) {
                throw new IllegalStateException("pgmid is required");
            }
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            return new DealsMerchantDetails(this);
        }
    }
}
