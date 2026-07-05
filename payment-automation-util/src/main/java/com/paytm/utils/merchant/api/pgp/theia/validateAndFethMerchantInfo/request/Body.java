package com.paytm.utils.merchant.api.pgp.theia.validateAndFethMerchantInfo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mids"
})
public class Body {

    @JsonProperty("mids")
    private List<String> mids = null;

    @JsonProperty("mids")
    public List<String> getMids() {
        return mids;
    }

    @JsonProperty("mids")
    public void setMids(List<String> mids) {
        this.mids = mids;
    }

}
