package com.paytm.dto.processTransactionV1.response;

import com.fasterxml.jackson.annotation.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "finalTransactionAmount",
        "subventionPlanId",
        "itemOfferDetails"
})
public class EmiSubventionInfo {

    @JsonProperty("subventionPlanId")
    private String subventionPlanId = null;
    @JsonProperty("finalTransactionAmount")
    private FinalTransactionAmount finalTransactionAmount = null;
    @JsonProperty("itemOfferDetails")
    private List<ItemOfferDetails> itemOfferDetails = null;

    public EmiSubventionInfo(){

    }

    public EmiSubventionInfo(String subventionPlanId,  FinalTransactionAmount finalTransactionAmount, List<ItemOfferDetails> itemOfferDetails){
        this.subventionPlanId = subventionPlanId;
        this.finalTransactionAmount = finalTransactionAmount;
        this.itemOfferDetails = itemOfferDetails;
    }

    public EmiSubventionInfo(String subventionPlanId,  FinalTransactionAmount finalTransactionAmount){
        this.subventionPlanId = subventionPlanId;
        this.finalTransactionAmount = finalTransactionAmount;
    }

    @JsonProperty("subventionPlanId")
    public String getSubventionPlanId() { return subventionPlanId; };

    @JsonProperty("subventionPlanId")
    public void setSubventionPlanId(String subventionPlanId) { this.subventionPlanId = subventionPlanId; };

    @JsonProperty("finalTransactionAmount")
    public FinalTransactionAmount getfinalTransactionAmount() { return finalTransactionAmount; };

    @JsonProperty("finalTransactionAmount")
    public void setfinalTransactionAmount( FinalTransactionAmount finalTransactionAmount) { this.finalTransactionAmount = finalTransactionAmount; };

    @JsonProperty("itemOfferDetails")
    public List<ItemOfferDetails> getitemOfferDetails() { return itemOfferDetails; };

    @JsonProperty("itemOfferDetails")
    public void setitemOfferDetails(List<ItemOfferDetails> itemOfferDetails) { this.itemOfferDetails = itemOfferDetails; };



}
