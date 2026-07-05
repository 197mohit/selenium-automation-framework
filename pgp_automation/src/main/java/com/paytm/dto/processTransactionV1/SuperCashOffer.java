package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "campaignId",
})
public class  SuperCashOffer {

    @JsonProperty("campaignId")
    private String campaignId;

    @JsonProperty("campaignId")
    public String getcampaignId() {
        return campaignId;
    }

    @JsonProperty("campaignId")
    public  SuperCashOffer setcampaignId(String campaignId) {
        this.campaignId = campaignId;
        return this;
    }
}
