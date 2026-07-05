package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * POST {@code /api/emi/all} — EMI options for the store-attendant flow.
 *
 * <p><b>Catalog item</b> (default): {@code items[]} includes {@code productId}, {@code brandId}, {@code categoryId},
 * {@code model}, synthetic {@code id} ({@code productId + "_id"}), {@code price}, {@code quantity}.
 *
 * <p><b>Amount-based offer</b>: minimal {@code items[]} — {@code id} (e.g. pgmid), {@code price}, {@code quantity} only;
 * enable via {@link Builder#amountBasedOffer(boolean)} {@code true} or {@link Builder#itemLineId(String)} /
 * {@link Builder#merchantAndLineId(String)}. {@code paymentDetails.transactionAmount} defaults to {@code price} when
 * unset ({@code 0}) in that mode.
 */
public class DealsEmiAll extends BaseApi {

    public static final String API_RELATIVE_PATH = "/api/emi/all";

    private static final String PATH = API_RELATIVE_PATH;

    private static final String DEFAULT_UA =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/146.0.7680.119 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private DealsEmiAll(Builder b) {
        setMethod(MethodType.POST);
        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(PATH);
        getRequestSpecBuilder().setContentType("application/json");
        getRequestSpecBuilder().addHeader("accept", "application/json, text/plain, */*");
        getRequestSpecBuilder().addHeader("accept-language", "en-IN,en-US;q=0.9,en;q=0.8");
        getRequestSpecBuilder().addHeader("user-agent", b.userAgent != null ? b.userAgent : DEFAULT_UA);
        getRequestSpecBuilder().addHeader("x-requested-with", "com.paytm.store_attendant");
        getRequestSpecBuilder().addHeader("origin", base);
        getRequestSpecBuilder().addHeader("sec-ch-ua",
                "\"Chromium\";v=\"146\", \"Not-A.Brand\";v=\"24\", \"Android WebView\";v=\"146\"");
        getRequestSpecBuilder().addHeader("sec-ch-ua-mobile", "?1");
        getRequestSpecBuilder().addHeader("sec-ch-ua-platform", "\"Android\"");
        getRequestSpecBuilder().addHeader("sec-fetch-site", "same-origin");
        getRequestSpecBuilder().addHeader("sec-fetch-mode", "cors");
        getRequestSpecBuilder().addHeader("sec-fetch-dest", "empty");
        getRequestSpecBuilder().addHeader("priority", "u=1, i");
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);
        getRequestSpecBuilder().addQueryParam("utm_source", b.utmSource);
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }
        if (b.cookie != null && !b.cookie.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", b.cookie);
        }
        if (b.sendCustomCookieHeader) {
            getRequestSpecBuilder().addHeader("custom-cookie", b.customCookieValue != null ? b.customCookieValue : "");
        }

        String bodyJson =
                b.rawBodyJson != null && !b.rawBodyJson.isEmpty() ? b.rawBodyJson : buildRequestBodyJson(b);
        getRequestSpecBuilder().setBody(bodyJson);
    }

    private static boolean useAmountBasedPayload(Builder b) {
        return b.amountBasedOffer || (b.itemLineId != null && !b.itemLineId.isEmpty());
    }

    private static String buildRequestBodyJson(Builder b) {
        JSONObject item = useAmountBasedPayload(b) ? buildAmountBasedItem(b) : buildCatalogItem(b);

        long txnAmount = resolveTransactionAmount(b);
        JSONObject paymentDetails = new JSONObject();
        paymentDetails.put("transactionAmount", txnAmount);

        JSONObject body = new JSONObject();
        body.put("items", new JSONArray().put(item));
        body.put("merchantId", b.merchantId);
        body.put("paymentDetails", paymentDetails);

        return body.toString();
    }

    private static JSONObject buildCatalogItem(Builder b) {
        JSONObject item = new JSONObject();
        item.put("id", b.productId + "_id");
        item.put("productId", b.productId);
        item.put("brandId", b.brandId);
        item.put("categoryId", b.categoryId);
        item.put("model", b.model);
        item.put("price", b.price);
        item.put("quantity", b.quantity);
        return item;
    }

    private static JSONObject buildAmountBasedItem(Builder b) {
        JSONObject item = new JSONObject();
        item.put("id", b.itemLineId);
        item.put("price", b.price);
        item.put("quantity", b.quantity);
        return item;
    }

    private static long resolveTransactionAmount(Builder b) {
        if (useAmountBasedPayload(b)) {
            return b.transactionAmount > 0 ? b.transactionAmount : b.price;
        }
        return b.transactionAmount;
    }

    /**
     * Referer for {@code /pgmid/{pgmid}/check-for-emi-plans} (guest / amount-based EMI check).
     */
    public static String refererCheckForEmiPlansGuest(String dealsBase, String pgmid) {
        String root = dealsBase.endsWith("/") ? dealsBase.substring(0, dealsBase.length() - 1) : dealsBase;
        return String.format(
                "%s/pgmid/%s/check-for-emi-plans?isPaytmLovesEmiFlow=true&utm_source=guest-user&prevScreen=store_page",
                root, pgmid);
    }

    public static final class Builder {
        private String baseUri;
        private String productId;
        private int brandId;
        private int categoryId;
        private String model;
        private long price;
        private int quantity = 1;
        private String merchantId;
        private long transactionAmount;
        /** When true, or when {@link #itemLineId} is set, body uses minimal {@code items[]} (amount-based flow). */
        private boolean amountBasedOffer;
        /** {@code items[].id} for amount-based payloads (often pgmid). */
        private String itemLineId;
        private String referer;
        private String userAgent;
        private String cookie;
        private boolean sendCustomCookieHeader;
        private String customCookieValue = "";
        private String rawBodyJson;
        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S26.04.02";
        private String requestId;
        private String utmSource = "login-via-username";

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder brandId(int brandId) {
            this.brandId = brandId;
            return this;
        }

        public Builder categoryId(int categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder price(long price) {
            this.price = price;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        /**
         * Root {@code paymentDetails.transactionAmount}. For amount-based payloads, when unset ({@code 0}), defaults to
         * {@link #price}.
         */
        public Builder transactionAmount(long transactionAmount) {
            this.transactionAmount = transactionAmount;
            return this;
        }

        /** Minimal line-item body (check-for-emi-plans / amount-based offers). */
        public Builder amountBasedOffer(boolean amountBasedOffer) {
            this.amountBasedOffer = amountBasedOffer;
            return this;
        }

        /** Sets {@code items[].id} for amount-based mode; also selects amount-based payload when non-empty. */
        public Builder itemLineId(String itemLineId) {
            this.itemLineId = itemLineId;
            return this;
        }

        /** Same pgmid for {@link #merchantId} and {@link #itemLineId}; enables amount-based payload. */
        public Builder merchantAndLineId(String pgmid) {
            this.merchantId = pgmid;
            this.itemLineId = pgmid;
            this.amountBasedOffer = true;
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

        public Builder cookie(String cookie) {
            this.cookie = cookie;
            return this;
        }

        public Builder customCookieHeader(boolean send) {
            this.sendCustomCookieHeader = send;
            return this;
        }

        public Builder customCookieValue(String value) {
            this.customCookieValue = value;
            return this;
        }

        /** Full JSON body; structured fields ignored when set. */
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

        public Builder utmSource(String utmSource) {
            this.utmSource = utmSource;
            return this;
        }

        public DealsEmiAll build() {
            boolean hasRaw = rawBodyJson != null && !rawBodyJson.isEmpty();
            boolean amountBased = !hasRaw && useAmountBasedPayload(this);

            if (!hasRaw) {
                if (merchantId == null || merchantId.isEmpty()) {
                    throw new IllegalStateException("merchantId is required");
                }
                if (amountBased) {
                    if (itemLineId == null || itemLineId.isEmpty()) {
                        throw new IllegalStateException(
                                "amount-based emi/all requires itemLineId (or use merchantAndLineId(pgmid))");
                    }
                    if (price <= 0) {
                        throw new IllegalStateException("price (paise) must be > 0");
                    }
                    if (quantity <= 0) {
                        throw new IllegalStateException("quantity must be > 0");
                    }
                } else {
                    if (productId == null || model == null) {
                        throw new IllegalStateException("productId, merchantId, and model are required for catalog emi/all");
                    }
                }
            }

            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            return new DealsEmiAll(this);
        }
    }
}
