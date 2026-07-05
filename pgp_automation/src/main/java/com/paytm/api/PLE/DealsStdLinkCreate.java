package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

/**
 * POST Deals standard (CC / non‑EMI) payment link —
 * {@code /api/link/pgmid/{pgmid}/createLink?flowType=STD_LINK&globalTS=…&buildVersion=…&requestId=…}.
 *
 * <p>Wires headers and trimmed JSON body to match store‑attendant WebView curl ({@code custom-cookie},
 * Chrome 143 {@code sec-ch-ua}). Use {@link Builder#cookie(String)} when {@code Cookie} header is required.
 *
 * <p>Does not add {@code utm_source}; this is intentional for parity with staging captures where that query key is absent.
 */
public class DealsStdLinkCreate extends BaseApi {

    /** Value sent as {@code flowType}; API contract for minimal create‑link payloads. */
    public static final String FLOW_TYPE_STD_LINK = "STD_LINK";

    /** Path relative to Deals base URI: {@code /api/link/pgmid/{pgmid}/createLink}. */
    private static final String PATH_PREFIX = "/api/link/pgmid/";
    private static final String PATH_SUFFIX = "/createLink";

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/143.0.7499.35 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private static final String DEFAULT_SEC_CH_UA =
            "\"Android WebView\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"";

    private DealsStdLinkCreate(Builder b) {
        setMethod(MethodType.POST);

        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(PATH_PREFIX + b.pgmid + PATH_SUFFIX);

        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("content-type", "application/json");
        getRequestSpecBuilder().addHeader("accept", "application/json, text/plain, */*");
        getRequestSpecBuilder().addHeader("accept-language", "en-IN,en-US;q=0.9,en;q=0.8");
        getRequestSpecBuilder().addHeader("priority", "u=1, i");
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("origin", base);

        getRequestSpecBuilder().addQueryParam("flowType", FLOW_TYPE_STD_LINK);
        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);

        String ua =
                (b.userAgent != null && !b.userAgent.isEmpty()) ? b.userAgent : DEFAULT_USER_AGENT;
        getRequestSpecBuilder().addHeader("user-agent", ua);
        getRequestSpecBuilder().addHeader("x-requested-with", b.xRequestedWith);
        getRequestSpecBuilder().addHeader("sec-ch-ua-mobile", "?1");
        getRequestSpecBuilder().addHeader("sec-ch-ua-platform", "\"Android\"");
        String secChUa =
                (b.secChUa != null && !b.secChUa.isEmpty()) ? b.secChUa : DEFAULT_SEC_CH_UA;
        getRequestSpecBuilder().addHeader("sec-ch-ua", secChUa);
        getRequestSpecBuilder().addHeader("sec-fetch-site", "same-origin");
        getRequestSpecBuilder().addHeader("sec-fetch-mode", "cors");
        getRequestSpecBuilder().addHeader("sec-fetch-dest", "empty");
        getRequestSpecBuilder().addHeader("custom-cookie", "");

        if (b.cookie != null && !b.cookie.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", b.cookie);
        }
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }

        String body =
                (b.rawBodyJson != null && !b.rawBodyJson.isEmpty())
                        ? b.rawBodyJson
                        : buildStdLinkBody(b);
        getRequestSpecBuilder().setBody(body);
    }

    private static String buildStdLinkBody(Builder b) {
        JSONObject root = new JSONObject();
        root.put("price", b.price);
        root.put("mobileNumber", b.mobileNumber);
        root.put("linkDescription", b.linkDescription);
        root.put("invoiceNumber", b.invoiceNumber);
        root.put("cashierMobileNumber", b.cashierMobileNumber);
        if (b.description != null && !b.description.isEmpty()) {
            root.put("description", b.description);
        }
        return root.toString();
    }

    public static final class Builder {
        private String baseUri;
        private String pgmid;
        private String rawBodyJson;

        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S25.12.26";
        private String requestId;

        private String cookie;
        private String referer;
        private String userAgent;
        private String secChUa;
        private String xRequestedWith = "com.paytm.store_attendant";

        private String price;
        private String mobileNumber;
        private String linkDescription;
        private String invoiceNumber;
        private String cashierMobileNumber;
        private String description;

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder pgmid(String pgmid) {
            this.pgmid = pgmid;
            return this;
        }

        /** Overrides structured fields when non‑empty JSON string is set (curl parity / experiments). */
        public Builder rawBodyJson(String rawBodyJson) {
            this.rawBodyJson = rawBodyJson;
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

        /** Overrides default Chrome 143 store‑webview {@code sec-ch-ua} triple. */
        public Builder secChUa(String secChUa) {
            this.secChUa = secChUa;
            return this;
        }

        public Builder xRequestedWith(String xRequestedWith) {
            this.xRequestedWith = xRequestedWith;
            return this;
        }

        public Builder price(String price) {
            this.price = price;
            return this;
        }

        public Builder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public Builder linkDescription(String linkDescription) {
            this.linkDescription = linkDescription;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder cashierMobileNumber(String cashierMobileNumber) {
            this.cashierMobileNumber = cashierMobileNumber;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public DealsStdLinkCreate build() {
            if (pgmid == null || pgmid.isEmpty()) {
                throw new IllegalStateException("pgmid is required");
            }
            boolean hasRaw = rawBodyJson != null && !rawBodyJson.trim().isEmpty();
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            if (!hasRaw
                    && (price == null
                            || mobileNumber == null
                            || linkDescription == null
                            || invoiceNumber == null
                            || cashierMobileNumber == null)) {
                throw new IllegalStateException(
                        "Unless rawBodyJson is set: price, mobileNumber, linkDescription, invoiceNumber, "
                                + "cashierMobileNumber are required (optional: description)");
            }
            return new DealsStdLinkCreate(this);
        }
    }
}
