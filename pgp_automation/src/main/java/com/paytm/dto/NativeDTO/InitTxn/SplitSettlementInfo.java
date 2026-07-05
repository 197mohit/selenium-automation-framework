
package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "splitMethod",
    "splitInfo"
})
public class SplitSettlementInfo implements Serializable
{

    @JsonProperty("splitMethod")
    private String splitMethod;
    @JsonProperty("splitInfo")
    private SplitInfo[] splitInfo = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 1640298235749087721L;

    @JsonProperty("splitMethod")
    public String getSplitMethod() {
        return splitMethod;
    }

    @JsonProperty("splitMethod")
    public SplitSettlementInfo setSplitMethod(String splitMethod) {
        this.splitMethod = splitMethod;
        return this;
    }

    @JsonProperty("splitInfo")
    public SplitInfo[] getSplitInfo() {
        return splitInfo;
    }

    @JsonProperty("splitInfo")
    public SplitSettlementInfo setSplitInfo(SplitInfo[] splitInfo) {
        this.splitInfo = splitInfo;
        return this;
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
