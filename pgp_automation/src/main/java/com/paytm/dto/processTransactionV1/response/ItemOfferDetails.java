package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "offerId"
})
public class ItemOfferDetails {
    @JsonProperty("id")
    public String id = null;
    @JsonProperty("offerId")
    public String offerId = null;



    @JsonProperty("id")
    public String getid() { return id; }

    @JsonProperty("id")
    public void setid(String id) { this.id = id; }

    @JsonProperty("offerId")
    public String getofferId() { return offerId; }

    @JsonProperty("offerId")
    public void setofferId(String offerId) { this.offerId = offerId; }
}
