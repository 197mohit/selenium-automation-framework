package com.paytm.dto.theia;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Request body for {@code /theia/api/v2/fetchMerchantInfo} (POST).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchMerchantInfoV2DTO {

    @JsonProperty("head")
    private Head head;

    @JsonProperty("body")
    private Body body;

    public Head getHead() {
        return head;
    }

    public FetchMerchantInfoV2DTO setHead(Head head) {
        this.head = head;
        return this;
    }

    public Body getBody() {
        return body;
    }

    public FetchMerchantInfoV2DTO setBody(Body body) {
        this.body = body;
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Head {

        @JsonProperty("version")
        private String version = "v2";

        @JsonProperty("requestId")
        private String requestId;

        @JsonProperty("requestTimestamp")
        private String requestTimestamp;

        @JsonProperty("channelId")
        private String channelId = "WEB";

        @JsonProperty("ssoToken")
        private String ssoToken;

        @JsonProperty("txnToken")
        private String txnToken;

        public String getVersion() {
            return version;
        }

        public Head setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getRequestId() {
            return requestId;
        }

        public Head setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public String getRequestTimestamp() {
            return requestTimestamp;
        }

        public Head setRequestTimestamp(String requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        public String getChannelId() {
            return channelId;
        }

        public Head setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public String getSsoToken() {
            return ssoToken;
        }

        public Head setSsoToken(String ssoToken) {
            this.ssoToken = ssoToken;
            return this;
        }

        public String getTxnToken() {
            return txnToken;
        }

        public Head setTxnToken(String txnToken) {
            this.txnToken = txnToken;
            return this;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Body {

        @JsonProperty("mid")
        private String mid;

        @JsonProperty("orderId")
        private String orderId;

        public String getMid() {
            return mid;
        }

        public Body setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public String getOrderId() {
            return orderId;
        }

        public Body setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }
    }

    /**
     * Builds a request DTO with the same head/body values as the legacy string-template API client.
     */
    public static class Builder {

        private final String mid;
        private final String orderId;
        private final String txnToken;
        private final String ssoToken;

        public Builder(String mid, String orderId, String txnToken, String ssoToken) {
            this.mid = mid;
            this.orderId = orderId;
            this.txnToken = txnToken;
            this.ssoToken = ssoToken;
        }

        public FetchMerchantInfoV2DTO build() {
            Head head = new Head()
                    .setVersion("v2")
                    .setRequestId(UUID.randomUUID().toString())
                    .setRequestTimestamp(String.valueOf(System.currentTimeMillis()))
                    .setChannelId("WEB")
                    .setSsoToken(ssoToken)
                    .setTxnToken(txnToken);

            Body body = new Body()
                    .setMid(mid)
                    .setOrderId(orderId);

            return new FetchMerchantInfoV2DTO()
                    .setHead(head)
                    .setBody(body);
        }
    }
}
