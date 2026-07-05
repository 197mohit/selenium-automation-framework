package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.json.JSONObject;

/**
 * POST {@code /api/user/sendOtp} — send OTP for PLE EMI / store-attendant flows. Uses {@code clientid}, header {@code mid},
 * and JWT {@code token} per captured curl ({@code phoneNo}, {@code receiverType}, amounts, {@code brandName},
 * {@code timeStamp} in body).
 *
 * <p>Use {@link Builder#bodyJson(String)} for a verbatim payload; otherwise set structured fields.
 */
public class DealsUserSendOtp extends BaseApi {

    public static final String API_RELATIVE_PATH = "/api/user/sendOtp";

    private static final String BASE_PATH = API_RELATIVE_PATH;

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/143.0.7499.35 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private static final String DEFAULT_SEC_CH_UA =
            "\"Android WebView\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"";

    private DealsUserSendOtp(Builder b) {
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

        if (b.clientid != null && !b.clientid.isEmpty()) {
            getRequestSpecBuilder().addHeader("clientid", b.clientid);
        }
        if (b.mid != null && !b.mid.isEmpty()) {
            getRequestSpecBuilder().addHeader("mid", b.mid);
        }
        if (b.token != null && !b.token.isEmpty()) {
            getRequestSpecBuilder().addHeader("token", b.token);
        }

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

        /** Request header {@code clientid}; curl uses {@code EMI_APP}. */
        private String clientid = "EMI_APP";
        /** Request header {@code mid} — merchant PG MID. */
        private String mid;
        /** Request header JWT {@code token}. */
        private String token;

        private String phoneNo;
        private String receiverType = "SMS";
        private String originalAmount;
        private String payableAmount;
        private String brandName;
        /** Body field {@code timeStamp} (epoch seconds string per sample). */
        private String timeStamp;

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

        public Builder clientid(String clientid) {
            this.clientid = clientid;
            return this;
        }

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder phoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
            return this;
        }

        public Builder receiverType(String receiverType) {
            this.receiverType = receiverType;
            return this;
        }

        public Builder originalAmount(String originalAmount) {
            this.originalAmount = originalAmount;
            return this;
        }

        public Builder payableAmount(String payableAmount) {
            this.payableAmount = payableAmount;
            return this;
        }

        public Builder brandName(String brandName) {
            this.brandName = brandName;
            return this;
        }

        public Builder timeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        private String buildBodyJson() {
            JSONObject root = new JSONObject();
            root.put("phoneNo", phoneNo);
            root.put("receiverType", receiverType);
            root.put("originalAmount", originalAmount);
            root.put("payableAmount", payableAmount);
            root.put("brandName", brandName);
            root.put("timeStamp", timeStamp);
            return root.toString();
        }

        private void validateStructured() {
            if (phoneNo == null
                    || originalAmount == null
                    || payableAmount == null
                    || brandName == null
                    || timeStamp == null
                    || phoneNo.isEmpty()
                    || originalAmount.isEmpty()
                    || payableAmount.isEmpty()
                    || brandName.isEmpty()
                    || timeStamp.isEmpty()) {
                throw new IllegalStateException(
                        "phoneNo, originalAmount, payableAmount, brandName, timeStamp are required when bodyJson "
                                + "is not set");
            }
            if (mid == null || mid.isEmpty()) {
                throw new IllegalStateException("mid header is required when bodyJson is not set");
            }
            if (token == null || token.isEmpty()) {
                throw new IllegalStateException("token header is required when bodyJson is not set");
            }
            if (receiverType == null || receiverType.isEmpty()) {
                throw new IllegalStateException("receiverType must not be empty");
            }
        }

        public DealsUserSendOtp build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            if (bodyJson == null || bodyJson.isEmpty()) {
                validateStructured();
            }
            return new DealsUserSendOtp(this);
        }
    }
}
