package com.paytm.dto.CloseOrder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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

}