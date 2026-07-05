package com.paytm.dto.GetPaymentStatusResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultInfo"
})
public class FeeRateFactors {

    @JsonProperty("corporateCard")
    private String corporateCard;
    @JsonProperty("ecomToken")
    private String ecomToken;

    public String getCorporateCard(){return corporateCard;}

    public void setCorporateCard(String corporateCard){this.corporateCard = corporateCard;}

    public String getEcomToken() {
        return ecomToken;
    }

    public void setEcomToken(String ecomToken) {
        this.ecomToken = ecomToken;
    }
}