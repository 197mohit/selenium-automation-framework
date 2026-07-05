package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "deepLink",
        "orderId",
        "cashierRequestId",
        "transId"
})
public class DeepLinkInfo {

    @JsonProperty("deepLink")
    private String deepLink;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("cashierRequestId")
    private String cashierRequestId;
    @JsonProperty("transId")
    private String transId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("deepLink")
    public String getDeepLink() {
        return deepLink;
    }

    @JsonProperty("deepLink")
    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("cashierRequestId")
    public String getCashierRequestId() {
        return cashierRequestId;
    }

    @JsonProperty("cashierRequestId")
    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    @JsonProperty("transId")
    public String getTransId() {
        return transId;
    }

    @JsonProperty("transId")
    public void setTransId(String transId) {
        this.transId = transId;
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