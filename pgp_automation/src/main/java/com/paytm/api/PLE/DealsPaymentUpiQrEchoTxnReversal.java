package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

/**
 * POST {@code /api/payment/upiQr/echo/txnReversal} — UPI QR echo / reversal callback body (staging store-attendant
 * curl shape: SALE + QR mode, acquirement id, extendInfo, {@code riskExtendInfo}, nullable ISO fields {@code tc},
 * {@code tsi}, etc.).
 *
 * <p>Use {@link Builder#bodyJson(String)} for a verbatim capture; otherwise use the structured builder (defaults mirror
 * the sample: JSON {@code null} for omitted ISO fields, {@code reversalFlag=false}, {@code saleQrTxnApiResCode} {@code EOS_0000}).
 */
public class DealsPaymentUpiQrEchoTxnReversal extends BaseApi {

    public static final String API_RELATIVE_PATH = "/api/payment/upiQr/echo/txnReversal";

    private static final String BASE_PATH = API_RELATIVE_PATH;

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/143.0.7499.35 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private static final String DEFAULT_SEC_CH_UA =
            "\"Android WebView\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"";

    private DealsPaymentUpiQrEchoTxnReversal(Builder b) {
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
        private String stan;
        private String invoiceNumber;
        private String time;
        private String date;
        private String year;
        private String tc;
        private String tsi;
        private String tvr;
        private String txnStatus = "SUCCESS";
        private String txnType = "SALE";
        private String txnMode = "QR";
        private String acquirementId;
        private String orderId;
        private String extendInfoTxnType = "QR";
        private String extendInfoOrderId;
        private String reversalErrorMsg = "";
        private boolean reversalFlag;
        private String saleQrTxnApiResCode = "EOS_0000";
        private String extServiceId;
        private Object extraPaymentInfo;
        private String posConditionCode;
        private String posEntryMode;
        private String primaryAccountNr;
        private String processingCode;
        private String riskUserLbsLatitude = "";
        private String riskUserLbsLongitude = "";
        private String riskSource = "EMI_APP";
        private String riskAccuracy = "";
        private String riskTimestamp;

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

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder tid(String tid) {
            this.tid = tid;
            return this;
        }

        public Builder stan(String stan) {
            this.stan = stan;
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

        /** Root {@code tc}; omit from JSON meaning: leave as {@code null} when unset — use setter to send a value. */
        public Builder tc(String tc) {
            this.tc = tc;
            return this;
        }

        public Builder tsi(String tsi) {
            this.tsi = tsi;
            return this;
        }

        public Builder tvr(String tvr) {
            this.tvr = tvr;
            return this;
        }

        public Builder txnStatus(String txnStatus) {
            this.txnStatus = txnStatus;
            return this;
        }

        /** Root {@code txnType} (curl sample {@code SALE}). */
        public Builder txnType(String txnType) {
            this.txnType = txnType;
            return this;
        }

        public Builder txnMode(String txnMode) {
            this.txnMode = txnMode;
            return this;
        }

        public Builder acquirementId(String acquirementId) {
            this.acquirementId = acquirementId;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        /** {@code extendInfo.txnType} (curl sample {@code QR}). */
        public Builder extendInfoTxnType(String extendInfoTxnType) {
            this.extendInfoTxnType = extendInfoTxnType;
            return this;
        }

        /** Defaults to {@link #orderId(String)} when not set explicitly. */
        public Builder extendInfoOrderId(String extendInfoOrderId) {
            this.extendInfoOrderId = extendInfoOrderId;
            return this;
        }

        public Builder reversalErrorMsg(String reversalErrorMsg) {
            this.reversalErrorMsg = reversalErrorMsg != null ? reversalErrorMsg : "";
            return this;
        }

        public Builder reversalFlag(boolean reversalFlag) {
            this.reversalFlag = reversalFlag;
            return this;
        }

        public Builder saleQrTxnApiResCode(String saleQrTxnApiResCode) {
            this.saleQrTxnApiResCode = saleQrTxnApiResCode;
            return this;
        }

        public Builder extServiceId(String extServiceId) {
            this.extServiceId = extServiceId;
            return this;
        }

        public Builder extraPaymentInfo(Object extraPaymentInfo) {
            this.extraPaymentInfo = extraPaymentInfo;
            return this;
        }

        public Builder posConditionCode(String posConditionCode) {
            this.posConditionCode = posConditionCode;
            return this;
        }

        public Builder posEntryMode(String posEntryMode) {
            this.posEntryMode = posEntryMode;
            return this;
        }

        public Builder primaryAccountNr(String primaryAccountNr) {
            this.primaryAccountNr = primaryAccountNr;
            return this;
        }

        public Builder processingCode(String processingCode) {
            this.processingCode = processingCode;
            return this;
        }

        public Builder riskUserLbsLatitude(String riskUserLbsLatitude) {
            this.riskUserLbsLatitude = riskUserLbsLatitude != null ? riskUserLbsLatitude : "";
            return this;
        }

        public Builder riskUserLbsLongitude(String riskUserLbsLongitude) {
            this.riskUserLbsLongitude = riskUserLbsLongitude != null ? riskUserLbsLongitude : "";
            return this;
        }

        public Builder riskSource(String riskSource) {
            this.riskSource = riskSource;
            return this;
        }

        public Builder riskAccuracy(String riskAccuracy) {
            this.riskAccuracy = riskAccuracy != null ? riskAccuracy : "";
            return this;
        }

        /** {@code riskExtendInfo.timestamp} (string epoch ms); default {@code Long.toString(System.currentTimeMillis())}. */
        public Builder riskTimestamp(String riskTimestamp) {
            this.riskTimestamp = riskTimestamp;
            return this;
        }

        private static Object jsonNullable(String s) {
            return s == null ? JSONObject.NULL : s;
        }

        private String buildBodyJson() {
            String ord = orderId;
            String extendOrd =
                    extendInfoOrderId != null && !extendInfoOrderId.isEmpty() ? extendInfoOrderId : ord;
            JSONObject extendInfo = new JSONObject();
            extendInfo.put("txnType", extendInfoTxnType);
            extendInfo.put("orderId", extendOrd);
            extendInfo.put("reversalErrorMsg", reversalErrorMsg != null ? reversalErrorMsg : "");

            JSONObject riskExtendInfo = new JSONObject();
            riskExtendInfo.put("userLBSLatitude", riskUserLbsLatitude != null ? riskUserLbsLatitude : "");
            riskExtendInfo.put("userLBSLongitude", riskUserLbsLongitude != null ? riskUserLbsLongitude : "");
            riskExtendInfo.put("source", riskSource);
            riskExtendInfo.put("accuracy", riskAccuracy != null ? riskAccuracy : "");
            String ts =
                    riskTimestamp != null && !riskTimestamp.isEmpty()
                            ? riskTimestamp
                            : Long.toString(System.currentTimeMillis());
            riskExtendInfo.put("timestamp", ts);

            JSONObject root = new JSONObject();
            root.put("mid", mid);
            root.put("tid", tid);
            root.put("stan", stan);
            root.put("invoiceNumber", invoiceNumber);
            root.put("time", time);
            root.put("date", date);
            root.put("year", year);
            root.put("tc", jsonNullable(tc));
            root.put("tsi", jsonNullable(tsi));
            root.put("tvr", jsonNullable(tvr));
            root.put("txnStatus", txnStatus);
            root.put("txnType", txnType);
            root.put("txnMode", txnMode);
            root.put("acquirementId", acquirementId);
            root.put("orderId", ord);
            root.put("extendInfo", extendInfo);
            root.put("reversalFlag", reversalFlag);
            root.put("saleQrTxnApiResCode", saleQrTxnApiResCode);
            root.put("extServiceId", jsonNullable(extServiceId));
            root.put("extraPaymentInfo", extraPaymentInfo == null ? JSONObject.NULL : extraPaymentInfo);
            root.put("posConditionCode", jsonNullable(posConditionCode));
            root.put("posEntryMode", jsonNullable(posEntryMode));
            root.put("primaryAccountNr", jsonNullable(primaryAccountNr));
            root.put("processingCode", jsonNullable(processingCode));
            root.put("riskExtendInfo", riskExtendInfo);
            return root.toString();
        }

        private void validateStructured() {
            if (mid == null
                    || tid == null
                    || stan == null
                    || invoiceNumber == null
                    || time == null
                    || date == null
                    || year == null
                    || acquirementId == null
                    || orderId == null
                    || mid.isEmpty()
                    || tid.isEmpty()
                    || stan.isEmpty()
                    || invoiceNumber.isEmpty()
                    || time.isEmpty()
                    || date.isEmpty()
                    || year.isEmpty()
                    || acquirementId.isEmpty()
                    || orderId.isEmpty()) {
                throw new IllegalStateException(
                        "mid, tid, stan, invoiceNumber, time, date, year, acquirementId, orderId are required when "
                                + "bodyJson is not set");
            }
            if (txnStatus == null
                    || txnType == null
                    || txnMode == null
                    || saleQrTxnApiResCode == null
                    || extendInfoTxnType == null
                    || riskSource == null) {
                throw new IllegalStateException("txn/rescode/extend/risk defaults must not be cleared to null");
            }
        }

        public DealsPaymentUpiQrEchoTxnReversal build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            if (bodyJson == null || bodyJson.isEmpty()) {
                validateStructured();
            }
            return new DealsPaymentUpiQrEchoTxnReversal(this);
        }
    }
}
