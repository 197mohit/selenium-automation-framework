package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * POST {@code /api/emi/offerApply} — apply EMI / UPI-style offer on Deals (staging curl: {@code OFFUS},
 * {@code items[]}, {@code tid}, {@code phoneNo}, {@code paymentDetails.transactionAmount}).
 *
 * <p>Query string matches capture: {@code globalTS}, {@code buildVersion}, {@code requestId} only (no {@code utm_source}
 * in sample). Use {@link Builder#bodyJson(String)} for a verbatim payload.
 */
public class DealsEmiOfferApply extends BaseApi {

    public static final String API_RELATIVE_PATH = "/api/emi/offerApply";

    private static final String PATH = API_RELATIVE_PATH;

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/143.0.7499.35 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private static final String DEFAULT_SEC_CH_UA =
            "\"Android WebView\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"";

    private DealsEmiOfferApply(Builder b) {
        setMethod(MethodType.POST);
        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(PATH);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
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

    /** One {@code items[]} line (default {@code id} = {@code productId + "_id"}). */
    public static final class OfferLineItem {
        private final String productId;
        private final int brandId;
        private final int categoryId;
        private final String model;
        private final long price;
        private final int quantity;
        private final String lineId;

        public OfferLineItem(
                String productId, int brandId, int categoryId, String model, long price, int quantity) {
            this(productId, brandId, categoryId, model, price, quantity, null);
        }

        public OfferLineItem(
                String productId,
                int brandId,
                int categoryId,
                String model,
                long price,
                int quantity,
                String lineId) {
            this.productId = Objects.requireNonNull(productId);
            this.brandId = brandId;
            this.categoryId = categoryId;
            this.model = Objects.requireNonNull(model);
            this.price = price;
            this.quantity = quantity;
            this.lineId = lineId;
        }

        JSONObject toJSONObject() {
            JSONObject o = new JSONObject();
            o.put("id", lineId != null && !lineId.isEmpty() ? lineId : productId + "_id");
            o.put("productId", productId);
            o.put("brandId", brandId);
            o.put("categoryId", categoryId);
            o.put("model", model);
            o.put("price", price);
            o.put("quantity", quantity);
            return o;
        }
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

        private String merchantType = "OFFUS";
        private String phoneNo;
        private String merchantId;
        private String tid;
        private Long paymentDetailTransactionAmount;
        private final List<OfferLineItem> lineItems = new ArrayList<>();

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

        public Builder merchantType(String merchantType) {
            this.merchantType = merchantType;
            return this;
        }

        public Builder phoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder tid(String tid) {
            this.tid = tid;
            return this;
        }

        /** {@code paymentDetails.transactionAmount} as JSON number. */
        public Builder paymentDetailTransactionAmount(long paymentDetailTransactionAmount) {
            this.paymentDetailTransactionAmount = paymentDetailTransactionAmount;
            return this;
        }

        public Builder addLineItem(OfferLineItem item) {
            lineItems.add(Objects.requireNonNull(item));
            return this;
        }

        private String buildBodyJson() {
            JSONArray items = new JSONArray();
            for (OfferLineItem li : lineItems) {
                items.put(li.toJSONObject());
            }
            JSONObject paymentDetails = new JSONObject();
            paymentDetails.put("transactionAmount", paymentDetailTransactionAmount);

            JSONObject root = new JSONObject();
            root.put("items", items);
            root.put("merchantType", merchantType);
            root.put("phoneNo", phoneNo);
            root.put("merchantId", merchantId);
            root.put("tid", tid);
            root.put("paymentDetails", paymentDetails);
            return root.toString();
        }

        private void validateStructured() {
            if (phoneNo == null
                    || merchantId == null
                    || tid == null
                    || merchantType == null
                    || phoneNo.isEmpty()
                    || merchantId.isEmpty()
                    || tid.isEmpty()
                    || merchantType.isEmpty()) {
                throw new IllegalStateException(
                        "merchantType, phoneNo, merchantId, tid are required when bodyJson is not set");
            }
            if (lineItems.isEmpty()) {
                throw new IllegalStateException("at least one addLineItem is required when bodyJson is not set");
            }
            if (paymentDetailTransactionAmount == null) {
                throw new IllegalStateException("paymentDetailTransactionAmount is required when bodyJson is not set");
            }
        }

        public DealsEmiOfferApply build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            if (bodyJson == null || bodyJson.isEmpty()) {
                validateStructured();
            }
            return new DealsEmiOfferApply(this);
        }
    }
}
