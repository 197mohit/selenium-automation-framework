package com.paytm.dto.processTransactionV1;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "tipAmount",
})

public class TipDetails {
    @JsonProperty("tipAmount")
    private TipAmount tipAmount;

    @JsonProperty("tipAmount")
    public TipAmount getTipAmount() {
        return tipAmount;
    }

    @JsonProperty("tipAmount")
    public TipDetails setTipAmount(TipAmount tipAmount) {
        this.tipAmount = tipAmount;
        return this;
    }

}
