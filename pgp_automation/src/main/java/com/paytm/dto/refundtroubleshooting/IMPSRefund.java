
package com.paytm.dto.refundtroubleshooting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;






@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class IMPSRefund {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    /**
     * No args constructor for use in serialization
     */
    public IMPSRefund() {
    }

    /**
     * @param body
     * @param head
     */
    public IMPSRefund(Head head, Body body) {
        super();
        this.head = head;
        this.body = body;
    }

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    public IMPSRefund withHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

    public IMPSRefund withBody(Body body) {
        this.body = body;
        return this;
    }

}
