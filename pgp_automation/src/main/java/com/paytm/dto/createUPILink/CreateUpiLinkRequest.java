package com.paytm.dto.createUPILink;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.utils.merchant.util.PGPUtil;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "body"
})
public class CreateUpiLinkRequest {
    @JsonProperty("head")
    private Head head;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public CreateUpiLinkRequest(String mid, String merchantKey,  String txnToken, String aggMid, String accountNumber, String orderId) {
        this.head = new Head();
        this.body = new Body();
        body.setMid(mid);
        body.setTxnToken(txnToken);
        body.setAggMid(aggMid);
        body.setAccountNumber(accountNumber);
        body.setOrderId(orderId);
        body.setAmount("4");
        String checksum = createChecksum(merchantKey, body);
        head.setSignature(checksum);
    }

    private static String createChecksum(String merchantKey, Body body) {
        String checksum = "";
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(body);
            checksum = PGPUtil.getChecksum(merchantKey, jsonString);
            return checksum;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

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
