package com.paytm.dto.orderPayUnipsp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "header",
        "body"
})
public class OrderPayUnipspDTO {

    @JsonProperty("header")
    private Header header;
    @JsonProperty("body")
    private Body body;

    @JsonProperty("header")
    public Header getHeader() {
        return header;
    }

    @JsonProperty("header")
    public OrderPayUnipspDTO setHeader(Header header) {
        this.header = header;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public OrderPayUnipspDTO setBody(Body body) {
        this.body = body;
        return this;
    }

}