package com.paytm.dto.NativeDTO.getEMIDetails.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder ({
        "head",
        "body"
})
public class GetEMIDetailsRequest {

    @JsonProperty(value = "head")
    private Head head;
    @JsonProperty("body")
    private Body body;


    public GetEMIDetailsRequest(){}
    public GetEMIDetailsRequest(String signature,String mid) {
        this.head =new Head(signature);
        this.body = new Body(mid);
    }

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public GetEMIDetailsRequest setHead(Head head) {
        this.head = head;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public GetEMIDetailsRequest setBody(Body body) {
        this.body = body;
        return this;
    }



}
