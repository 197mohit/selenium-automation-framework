package com.paytm.dto.TransactionStatusV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class TransactionStatusV1DTO {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public TransactionStatusV1DTO setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public TransactionStatusV1DTO setBody(Body body) {
        this.body = body;
        return this;
    }

}