/**
 * @author :- Samar Aswal
 * @desc :- This a API structer of close order V2 API
 */

package com.paytm.dto.CloseOrderV2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class CloseOrderDTO {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public CloseOrderDTO setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public CloseOrderDTO setBody(Body body) {
        this.body = body;
        return this;
    }

    public CloseOrderDTO(Builder builder){
        this.body = new Body()
                .setMid(builder.mid)
                .setOrderId(builder.orderId)
                .setForceClose(builder.forceClose);
        this.head = new Head()
                .setToken(builder.token)
                .setTokenType(builder.tokenType);
    }

    public static class Builder {
        private String mid;
        private String orderId;
        private String version;
        private String token;
        private String tokenType;
        private String requestTimestamp;
        private String forceClose;

        public Builder() {
            this.version = "v2";
            this.requestTimestamp = Long.toString(System.currentTimeMillis());
            this.mid = null;
            this.orderId = null;
            this.tokenType = null;
            this.token = null;
            this.forceClose = "false";
        }

        public String getMid() {
            return mid;
        }


        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }


        public String getOrderId() {
            return orderId;
        }


        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public String getToken() {
            return token;
        }


        public Builder setToken(String token) {
            this.token = token;
            return this;
        }


        public String getTokenType() {
            return tokenType;
        }


        public Builder setTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public String getForceClose() {
            return forceClose;
        }

        public Builder setForceClose(String forceClose) {
            this.forceClose = forceClose;
            return this;
        }

        public CloseOrderDTO build() {
            CloseOrderDTO closeOrderDTO = new CloseOrderDTO(this);
            return closeOrderDTO;
        }
    }

}