package com.paytm.dto.instaproxy.upipayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// AI-Generated: 2026-04-07 - DTO: Instaproxy PG2 UPI payment request — mbidConfigurationDetails
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpiMbidConfigurationDetails {

    @JsonProperty("mbId")
    private String mbId;
    @JsonProperty("key")
    private String key;
    @JsonProperty("parameter")
    private String parameter;
    @JsonProperty("instantSettlement")
    private Boolean instantSettlement;

    public UpiMbidConfigurationDetails() {
    }

    private UpiMbidConfigurationDetails(Builder b) {
        this.mbId = b.mbId;
        this.key = b.key;
        this.parameter = b.parameter;
        this.instantSettlement = b.instantSettlement;
    }

    public String getMbId() {
        return mbId;
    }

    public void setMbId(String mbId) {
        this.mbId = mbId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Boolean getInstantSettlement() {
        return instantSettlement;
    }

    public void setInstantSettlement(Boolean instantSettlement) {
        this.instantSettlement = instantSettlement;
    }

    public static class Builder {
        private String mbId = "qa12id40010813237542";
        private String key = "";
        private String parameter = "MERCHANT_VPA=paytm.d956934823@ptyesb";
        private Boolean instantSettlement = false;

        public Builder setMbId(String mbId) {
            this.mbId = mbId;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setParameter(String parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder setInstantSettlement(Boolean instantSettlement) {
            this.instantSettlement = instantSettlement;
            return this;
        }

        public UpiMbidConfigurationDetails build() {
            return new UpiMbidConfigurationDetails(this);
        }
    }
}
