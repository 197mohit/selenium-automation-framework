
package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mid",
    "amount",
    "partnerId",
    "extendInfo",
    "additionalInfo",
    "mutualFundFeedInfo"
})
public class SplitInfo implements Serializable
{

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("amount")
    private Amount amount;
    @JsonProperty("partnerId")
    private String partnerId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("extendInfo")
    private SplitExtendInfo extendInfo;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("additionalInfo")
    private AdditionalInfo additionalInfo;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("mutualFundFeedInfo")
    private MutualFundFeedInfo mutualFundFeedInfo;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = -1931585440075667715L;

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public SplitInfo setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("amount")
    public Amount getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public SplitInfo setAmount(Amount amount) {
        this.amount = amount;
        return this;
    }

    @JsonProperty("partnerId")
    public String getPartnerId() {
        return partnerId;
    }
    @JsonProperty("partnerId")
    public SplitInfo setPartnerId(String partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    @JsonProperty("extendInfo")
    public SplitExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public SplitInfo setExtendInfo(SplitExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }

    @JsonProperty("additionalInfo")
    public AdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }

    @JsonProperty("additionalInfo")
    public SplitInfo setAdditionalInfo(AdditionalInfo additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    @JsonProperty("mutualFundFeedInfo")
    public MutualFundFeedInfo getMutualFundFeedInfo() {
        return mutualFundFeedInfo;
    }

    @JsonProperty("mutualFundFeedInfo")
    public SplitInfo setMutualFundFeedInfo(MutualFundFeedInfo mutualFundFeedInfo) {
        this.mutualFundFeedInfo = mutualFundFeedInfo;
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
