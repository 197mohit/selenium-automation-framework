package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AffordabilityDetails {
    @JsonProperty("settlement")
    private List<Settlement> settlement;

    public AffordabilityDetails() {}

    public List<Settlement> getSettlement() {
        return settlement;
    }

    public void setSettlement(List<Settlement> settlement) {
        this.settlement = settlement;
    }

    public static class Settlement {
        @JsonProperty("redemption_type")
        private String redemptionType;
        
        @JsonProperty("action_type")
        private String actionType;
        
        @JsonProperty("contriInfo")
        private ContriInfo contriInfo;

        public String getRedemptionType() {
            return redemptionType;
        }

        public void setRedemptionType(String redemptionType) {
            this.redemptionType = redemptionType;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public ContriInfo getContriInfo() {
            return contriInfo;
        }

        public void setContriInfo(ContriInfo contriInfo) {
            this.contriInfo = contriInfo;
        }
    }

    public static class ContriInfo {
        @JsonProperty("merchant")
        private double merchant;

        public double getMerchant() {
            return merchant;
        }

        public void setMerchant(double merchant) {
            this.merchant = merchant;
        }
    }
}