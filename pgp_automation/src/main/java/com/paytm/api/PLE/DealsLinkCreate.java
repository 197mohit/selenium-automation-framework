package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

/**
 * POST {@code /api/link/pgmid/{pgmid}/createLink} on Deals (store attendant / WebView flow).
 * Pass {@link Builder#cookie(String)} when session cookies are required.
 *
 * <p>{@link Builder#productId(String)} is optional for amount-based / bank-offer links where the JSON omits {@code
 * productId}.
 */
public class DealsLinkCreate extends BaseApi {

    private static final String PATH_PREFIX = "/api/link/pgmid/";
    private static final String PATH_SUFFIX = "/createLink";
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/146.0.7680.119 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private DealsLinkCreate(Builder b) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("content-type", "application/json");
        getRequestSpecBuilder().addHeader("accept", "application/json, text/plain, */*");
        getRequestSpecBuilder().addHeader("accept-language", "en-IN,en-US;q=0.9,en;q=0.8");
        getRequestSpecBuilder().addHeader("priority", "u=1, i");
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());

        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(PATH_PREFIX + b.pgmid + PATH_SUFFIX);
        getRequestSpecBuilder().addHeader("origin", base);

        getRequestSpecBuilder().addQueryParam("flowType", b.flowType);
        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);
        getRequestSpecBuilder().addQueryParam("utm_source", b.utmSource);

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
        if (b.cookie != null && !b.cookie.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", b.cookie);
        }
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }

        String bodyJson = b.bodyJson != null && !b.bodyJson.isEmpty() ? b.bodyJson : b.buildBodyJson();
        getRequestSpecBuilder().setBody(bodyJson);
    }

    public static final class Builder {
        private String baseUri;
        private String bodyJson;
        private String pgmid;
        private String flowType = "EMI_APP";
        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S26.04.02";
        private String requestId;
        private String utmSource = "login-via-username";
        private String cookie;
        private String referer;
        private String userAgent;
        private String xRequestedWith = "com.paytm.store_attendant";

        private Object createLinkConfig = JSONObject.NULL;
        private String mobileNumber;
        private String invoiceNumber;
        private String linkDescription;
        private String issuingBank;
        private String productId;
        private String price;
        private String payMethod;
        private String tenure;
        private String imeiNo;
        private String cashierMobileNumber;

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        /** Full JSON body; when set, structured fields are ignored. */
        public Builder bodyJson(String bodyJson) {
            this.bodyJson = bodyJson;
            return this;
        }

        public Builder pgmid(String pgmid) {
            this.pgmid = pgmid;
            return this;
        }

        public Builder flowType(String flowType) {
            this.flowType = flowType;
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

        public Builder utmSource(String utmSource) {
            this.utmSource = utmSource;
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

        public Builder xRequestedWith(String xRequestedWith) {
            this.xRequestedWith = xRequestedWith;
            return this;
        }

        /** Use {@code null} for JSON null; default is null in payload. */
        public Builder createLinkConfig(Object createLinkConfig) {
            this.createLinkConfig = createLinkConfig == null ? JSONObject.NULL : createLinkConfig;
            return this;
        }

        public Builder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder linkDescription(String linkDescription) {
            this.linkDescription = linkDescription;
            return this;
        }

        public Builder issuingBank(String issuingBank) {
            this.issuingBank = issuingBank;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder price(String price) {
            this.price = price;
            return this;
        }

        public Builder payMethod(String payMethod) {
            this.payMethod = payMethod;
            return this;
        }

        public Builder tenure(String tenure) {
            this.tenure = tenure;
            return this;
        }

        public Builder imeiNo(String imeiNo) {
            this.imeiNo = imeiNo;
            return this;
        }

        public Builder cashierMobileNumber(String cashierMobileNumber) {
            this.cashierMobileNumber = cashierMobileNumber;
            return this;
        }

        private String buildBodyJson() {
            JSONObject root = new JSONObject();
            root.put("createLinkConfig", createLinkConfig);
            root.put("mobileNumber", mobileNumber);
            root.put("invoiceNumber", invoiceNumber);
            root.put("linkDescription", linkDescription);
            root.put("issuingBank", issuingBank);
            if (productId != null && !productId.isEmpty()) {
                root.put("productId", productId);
            }
            root.put("price", price);
            root.put("payMethod", payMethod);
            root.put("tenure", tenure);
            root.put("imeiNo", imeiNo);
            root.put("cashierMobileNumber", cashierMobileNumber);
            return root.toString();
        }

        public DealsLinkCreate build() {
            if (pgmid == null || pgmid.isEmpty()) {
                throw new IllegalStateException("pgmid is required");
            }
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            boolean hasRaw = bodyJson != null && !bodyJson.isEmpty();
            if (!hasRaw) {
                if (mobileNumber == null
                        || invoiceNumber == null
                        || linkDescription == null
                        || issuingBank == null
                        || price == null
                        || payMethod == null
                        || tenure == null
                        || imeiNo == null
                        || cashierMobileNumber == null) {
                    throw new IllegalStateException(
                            "Either bodyJson or all of mobileNumber, invoiceNumber, linkDescription, issuingBank, "
                                    + "price, payMethod, tenure, imeiNo, cashierMobileNumber are required "
                                    + "(productId optional — omit for amount-based / bank-offer link)");
                }
            }
            return new DealsLinkCreate(this);
        }
    }
}
