package com.paytm.dto.OrderStatusV2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class OrderStatusV2DTO {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public OrderStatusV2DTO setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public OrderStatusV2DTO setBody(Body body) {
        this.body = body;
        return this;
    }

}