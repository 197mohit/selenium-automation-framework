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
 * POST {@code /api/payment/createQr} — create QR for brand / store flows (captures staging curl shape: UPI plan-summary
 * referrer, BRAND/OFFUS, {@code affordabilityEmiInfo.totalPayableAmount}, item line with {@code *_id} id).
 *
 * <p>Use {@link Builder#bodyJson(String)} for an exact replica of a captured payload; otherwise set structured fields on
 * {@link Builder}.
 */
public class DealsPaymentCreateQr extends BaseApi {

    public static final String API_RELATIVE_PATH = "/api/payment/createQr";

    private static final String BASE_PATH = API_RELATIVE_PATH;

    /** Matches captured store-attendant WebView UA (Chrome 143). Override via {@link Builder#userAgent(String)}. */
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/143.0.7499.35 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private static final String DEFAULT_SEC_CH_UA =
            "\"Android WebView\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"";

    private DealsPaymentCreateQr(Builder b) {
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
        getRequestSpecBuilder().addHeader("sec-ch-ua", b.secChUa != null && !b.secChUa.isEmpty() ? b.secChUa : DEFAULT_SEC_CH_UA);
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

    /** Single cart line ({@code productId + "_id"} default for {@code id}). */
    public static final class QrLineItem {
        private final String productId;
        private final int brandId;
        private final int categoryId;
        private final String model;
        /** Amount in smallest currency unit (e.g. paise per curl {@code price}). */
        private final long price;
        private final int quantity;
        /** When null, JSON {@code id} is {@code productId + "_id"}. */
        private final String lineId;

        public QrLineItem(
                String productId, int brandId, int categoryId, String model, long price, int quantity) {
            this(productId, brandId, categoryId, model, price, quantity, null);
        }

        public QrLineItem(
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
        /** Empty string matches captured curl header {@code custom-cookie: }. */
        private String customCookie = "";
        private String xRequestedWith = "com.paytm.store_attendant";
        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S26.01.07";
        private String requestId;

        private String mid;
        private String merchantId;
        private String tid;
        private String time;
        private String date;
        private String year;
        /** Gross / invoice amount string (curl sample {@code "810000"}). */
        private String amount;
        private String invoiceNumber;
        private String imeiNo;
        private String flowType = "BRAND";
        private final List<QrLineItem> lineItems = new ArrayList<>();
        private String merchantType = "OFFUS";
        private String phoneNo;
        private Long paymentDetailTransactionAmount;
        private String merchantRequestId;
        private String ecrIntegrationSource = "nonEcr";
        private String userLbsLatitude = "0.0";
        private String userLbsLongitude = "0.0";
        private String accuracy = "0.0";
        private String productBrandId;
        private String productBrandName;
        private String productProductId;
        private String productSkuCode;
        private String productBrandInvoiceNumber;
        private String bankInvoiceNumber;
        private String affordabilityCouponCode = "";
        private String affordabilityTemplateId = "tnctemplate1";
        private boolean affordabilityBankManagedDiscount;
        private Long affordabilityEffectiveAmount;
        private Long affordabilityLoanAmount;
        private Long affordabilityPayableAmount;
        private String affordabilityTotalPayableAmount;
        private String affordabilityProcessingFee = "0";
        private String affordabilityCustomerMobileNumber;
        private String affordabilityValidationCode = "";

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        /** When set, structured fields below are ignored. */
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

        /** Overrides default {@code sec-ch-ua} matching Chrome&nbsp;143 WebView curl. */
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

        /** When omitted in structured builds, defaults to {@link #mid(String)}. */
        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder tid(String tid) {
            this.tid = tid;
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

        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder imeiNo(String imeiNo) {
            this.imeiNo = imeiNo;
            return this;
        }

        public Builder flowType(String flowType) {
            this.flowType = flowType;
            return this;
        }

        public Builder addLineItem(QrLineItem item) {
            lineItems.add(Objects.requireNonNull(item));
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

        /** {@code paymentDetails.transactionAmount} as JSON number (curl sample {@code 1000000}). */
        public Builder paymentDetailTransactionAmount(long paymentDetailTransactionAmount) {
            this.paymentDetailTransactionAmount = paymentDetailTransactionAmount;
            return this;
        }

        public Builder merchantRequestId(String merchantRequestId) {
            this.merchantRequestId = merchantRequestId;
            return this;
        }

        public Builder ecrIntegrationSource(String ecrIntegrationSource) {
            this.ecrIntegrationSource = ecrIntegrationSource;
            return this;
        }

        public Builder userLbsLatitude(String userLbsLatitude) {
            this.userLbsLatitude = userLbsLatitude;
            return this;
        }

        public Builder userLbsLongitude(String userLbsLongitude) {
            this.userLbsLongitude = userLbsLongitude;
            return this;
        }

        public Builder accuracy(String accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public Builder productInfo(
                String brandId, String brandName, String productId, String skuCode, String brandInvoiceNumber) {
            this.productBrandId = brandId;
            this.productBrandName = brandName;
            this.productProductId = productId;
            this.productSkuCode = skuCode;
            this.productBrandInvoiceNumber = brandInvoiceNumber;
            return this;
        }

        public Builder bankInvoiceNumber(String bankInvoiceNumber) {
            this.bankInvoiceNumber = bankInvoiceNumber;
            return this;
        }

        public Builder affordabilityCouponCode(String affordabilityCouponCode) {
            this.affordabilityCouponCode = affordabilityCouponCode;
            return this;
        }

        public Builder affordabilityTemplateId(String affordabilityTemplateId) {
            this.affordabilityTemplateId = affordabilityTemplateId;
            return this;
        }

        public Builder affordabilityBankManagedDiscount(boolean affordabilityBankManagedDiscount) {
            this.affordabilityBankManagedDiscount = affordabilityBankManagedDiscount;
            return this;
        }

        public Builder affordabilityEffectiveAmount(Long affordabilityEffectiveAmount) {
            this.affordabilityEffectiveAmount = affordabilityEffectiveAmount;
            return this;
        }

        public Builder affordabilityLoanAmount(Long affordabilityLoanAmount) {
            this.affordabilityLoanAmount = affordabilityLoanAmount;
            return this;
        }

        public Builder affordabilityPayableAmount(Long affordabilityPayableAmount) {
            this.affordabilityPayableAmount = affordabilityPayableAmount;
            return this;
        }

        public Builder affordabilityTotalPayableAmount(String affordabilityTotalPayableAmount) {
            this.affordabilityTotalPayableAmount = affordabilityTotalPayableAmount;
            return this;
        }

        public Builder affordabilityProcessingFee(String affordabilityProcessingFee) {
            this.affordabilityProcessingFee = affordabilityProcessingFee;
            return this;
        }

        public Builder affordabilityCustomerMobileNumber(String affordabilityCustomerMobileNumber) {
            this.affordabilityCustomerMobileNumber = affordabilityCustomerMobileNumber;
            return this;
        }

        public Builder affordabilityValidationCode(String affordabilityValidationCode) {
            this.affordabilityValidationCode = affordabilityValidationCode;
            return this;
        }

        private String buildBodyJson() {
            String effMid = mid;
            String effMerchantId = merchantId != null ? merchantId : mid;
            JSONArray items = new JSONArray();
            for (QrLineItem li : lineItems) {
                items.put(li.toJSONObject());
            }
            JSONObject paymentDetails = new JSONObject();
            paymentDetails.put("transactionAmount", paymentDetailTransactionAmount);

            String extMerchantReq =
                    merchantRequestId != null && !merchantRequestId.isEmpty()
                            ? merchantRequestId
                            : "T" + System.currentTimeMillis();
            JSONObject extendInfo = new JSONObject();
            extendInfo.put("merchantRequestId", extMerchantReq);
            extendInfo.put("ecrIntegrationSource", ecrIntegrationSource);
            extendInfo.put("userLBSLatitude", userLbsLatitude);
            extendInfo.put("userLBSLongitude", userLbsLongitude);
            extendInfo.put("accuracy", accuracy);

            JSONObject productInfo = new JSONObject();
            productInfo.put("brandId", productBrandId);
            productInfo.put("brandName", productBrandName);
            productInfo.put("productId", productProductId);
            productInfo.put("skuCode", productSkuCode);
            productInfo.put("brandInvoiceNumber", productBrandInvoiceNumber);

            String affMobile =
                    affordabilityCustomerMobileNumber != null && !affordabilityCustomerMobileNumber.isEmpty()
                            ? affordabilityCustomerMobileNumber
                            : phoneNo;
            JSONObject affordabilityEmiInfo = new JSONObject();
            affordabilityEmiInfo.put("couponCode", affordabilityCouponCode);
            affordabilityEmiInfo.put("templateId", affordabilityTemplateId);
            affordabilityEmiInfo.put("originalPrice", JSONObject.NULL);
            affordabilityEmiInfo.put("isBankManagedDiscount", affordabilityBankManagedDiscount);
            affordabilityEmiInfo.put(
                    "effectiveAmount",
                    affordabilityEffectiveAmount == null ? JSONObject.NULL : affordabilityEffectiveAmount);
            affordabilityEmiInfo.put(
                    "loanAmount", affordabilityLoanAmount == null ? JSONObject.NULL : affordabilityLoanAmount);
            affordabilityEmiInfo.put(
                    "payableAmount", affordabilityPayableAmount == null ? JSONObject.NULL : affordabilityPayableAmount);
            affordabilityEmiInfo.put("totalPayableAmount", affordabilityTotalPayableAmount);
            affordabilityEmiInfo.put("processingFee", affordabilityProcessingFee);
            affordabilityEmiInfo.put("customerMobileNumber", affMobile);
            affordabilityEmiInfo.put("validationCode", affordabilityValidationCode);

            JSONObject root = new JSONObject();
            root.put("mid", effMid);
            root.put("tid", tid);
            root.put("time", time);
            root.put("date", date);
            root.put("year", year);
            root.put("amount", amount);
            root.put("invoiceNumber", invoiceNumber);
            root.put("imeiNo", imeiNo);
            root.put("flowType", flowType);
            root.put("items", items);
            root.put("merchantType", merchantType);
            root.put("phoneNo", phoneNo);
            root.put("merchantId", effMerchantId);
            root.put("paymentDetails", paymentDetails);
            root.put("extendInfo", extendInfo);
            root.put("productInfo", productInfo);
            root.put("bankInvoiceNumber", bankInvoiceNumber);
            root.put("affordabilityEmiInfo", affordabilityEmiInfo);
            return root.toString();
        }

        private void validateStructured() {
            if (mid == null || mid.isEmpty()) {
                throw new IllegalStateException("mid is required when bodyJson is not set");
            }
            if (tid == null
                    || time == null
                    || date == null
                    || year == null
                    || amount == null
                    || invoiceNumber == null
                    || imeiNo == null
                    || phoneNo == null
                    || bankInvoiceNumber == null
                    || affordabilityTotalPayableAmount == null) {
                throw new IllegalStateException(
                        "tid, time, date, year, amount, invoiceNumber, imeiNo, phoneNo, bankInvoiceNumber, "
                                + "affordabilityTotalPayableAmount are required when bodyJson is not set");
            }
            if (lineItems.isEmpty()) {
                throw new IllegalStateException("at least one addLineItem is required when bodyJson is not set");
            }
            if (paymentDetailTransactionAmount == null) {
                throw new IllegalStateException("paymentDetailTransactionAmount is required when bodyJson is not set");
            }
            if (productBrandId == null
                    || productBrandName == null
                    || productProductId == null
                    || productSkuCode == null
                    || productBrandInvoiceNumber == null) {
                throw new IllegalStateException("productInfo fields (use productInfo(...)) are required when bodyJson is not set");
            }
        }

        public DealsPaymentCreateQr build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            if (bodyJson == null || bodyJson.isEmpty()) {
                validateStructured();
            }
            return new DealsPaymentCreateQr(this);
        }
    }
}
