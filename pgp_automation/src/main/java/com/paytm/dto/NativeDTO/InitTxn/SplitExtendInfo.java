
package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mutualFundFeedInfo"
})
public class SplitExtendInfo implements Serializable {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("mutualFundFeedInfo")
    private AdditionalInfo mutualFundFeedInfo;

    @JsonProperty("mutualFundFeedInfo")
    public AdditionalInfo getMutualFundFeedInfo() {
        return mutualFundFeedInfo;
    }

    @JsonProperty("mutualFundFeedInfo")
    public SplitExtendInfo setMutualFundFeedInfo(AdditionalInfo mutualFundFeedInfo) {
        this.mutualFundFeedInfo = mutualFundFeedInfo;
        return this;
    }
}
