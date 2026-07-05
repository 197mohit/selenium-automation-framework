package com.paytm.dto.NativeDTO.UpdateTransaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.apphelpers.PGPHelpers;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class UpdateTransactionDTO {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;

    public UpdateTransactionDTO setChecksum(String merchantKey) {
        String checksum = PGPHelpers.getNativeChecksum(merchantKey, this.getBody());
        this.getHead().setSignature(checksum);
        return this;
    }

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public UpdateTransactionDTO setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public UpdateTransactionDTO setBody(Body body) {
        this.body = body;
        return this;
    }

}