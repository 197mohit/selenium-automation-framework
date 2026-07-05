package com.paytm.utils.merchant.dto.refund.refundStatusV1DTO.request;


import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class RefundStatusV1Req {


    public RefundStatusV1Req(String mid, String orderId, String refId) {
        this.setBody(
                new Body()
                .setMid(mid)
                .setOrderId(orderId)
                .setRefId(refId));
        this.setHead(
                new Head()
                .setChannelId("")
                .setVersion("v1")
                .setRequestTimestamp("Time")
                .setChannelId("WEB")
                .setSignature("")
        );
    }

    public RefundStatusV1Req(String mid, String orderId, String refId, String tokenType, String token) {
        this.setBody(
                new Body()
                        .setMid(mid)
                        .setOrderId(orderId)
                        .setRefId(refId));
        this.setHead(
                new Head()
                        .setClientId("client123")
                        .setVersion("v1")
                        .setRequestTimestamp("Time")
                        .setChannelId("WEB")
                        .setTokenType(tokenType)
                        .setToken(token)
        );
    }

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


}
