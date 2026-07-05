package com.paytm.dto.emiSubvention.ApiV1Validate.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "offerDetails"
})
public class Item extends com.paytm.dto.emiSubvention.ApiV1Banks.request.Item {

    public Item() {
        super();
        this.offerDetails = new OfferDetails().setOfferId("123456");
    }

    @JsonProperty("offerDetails")
    private OfferDetails offerDetails;

    @JsonProperty("offerDetails")
    public OfferDetails getOfferDetails() {
        return offerDetails;
    }

    @JsonProperty("offerDetails")
    public void setOfferDetails(OfferDetails offerDetails) {
        this.offerDetails = offerDetails;
    }

}
