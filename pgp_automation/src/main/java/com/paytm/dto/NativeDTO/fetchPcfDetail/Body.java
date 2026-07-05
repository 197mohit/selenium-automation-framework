

package com.paytm.dto.NativeDTO.fetchPcfDetail;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anjukumari on 14/05/19
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid",
        "txnAmount",
        "payMethods"
})
public class Body {

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("txnAmount")
    private String txnAmount;

    @JsonProperty("payMethods")
    private List<PayMethod> payMethods = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("payMethods")
    public List<PayMethod> getPayMethods() {
        return payMethods;
    }

    @JsonProperty("payMethods")
    public Body setPayMethods(List<PayMethod> payMethods) {
        this.payMethods = payMethods;
        return this;
    }

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("txnAmount")
    public String getTxnAmount() {
        return txnAmount;
    }

    @JsonProperty("txnAmount")
    public Body setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public Body setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}