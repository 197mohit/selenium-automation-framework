
package com.paytm.dto.NativeDTO.RenewSubscription.Response;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "resultInfo",
    "txnId"
})
public class Body {

    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;
    @JsonProperty("txnId")
    private String txnId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("resultInfo")
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    @JsonProperty("resultInfo")
    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @JsonProperty("txnId")
    public String getTxnId() {
        return txnId;
    }

    @JsonProperty("txnId")
    public void setTxnId(String txnId) {
        this.txnId = txnId;
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
