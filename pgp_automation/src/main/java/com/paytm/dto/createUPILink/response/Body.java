package com.paytm.dto.createUPILink.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultCode",
        "resultCodeId",
        "resultMsg",
        "deepLink",
        "orderId",
        "transId",
        "cashierRequestId"
})
public class Body {

    @JsonProperty("resultCode")
    private String resultCode;
    @JsonProperty("resultCodeId")
    private String resultCodeId;
    @JsonProperty("resultMsg")
    private String resultMsg;
    @JsonProperty("deepLink")
    private String deepLink;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("transId")
    private String transId;
    @JsonProperty("cashierRequestId")
    private String cashierRequestId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("resultCode")
    public String getResultCode() {
        return resultCode;
    }

    @JsonProperty("resultCode")
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @JsonProperty("resultCodeId")
    public String getResultCodeId() {
        return resultCodeId;
    }

    @JsonProperty("resultCodeId")
    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    @JsonProperty("resultMsg")
    public String getResultMsg() {
        return resultMsg;
    }

    @JsonProperty("resultMsg")
    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

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

    @JsonProperty("transId")
    public String getTransId() {
        return transId;
    }

    @JsonProperty("transId")
    public void setTransId(String transId) {
        this.transId = transId;
    }

    @JsonProperty("cashierRequestId")
    public String getCashierRequestId() {
        return cashierRequestId;
    }

    @JsonProperty("cashierRequestId")
    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
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
