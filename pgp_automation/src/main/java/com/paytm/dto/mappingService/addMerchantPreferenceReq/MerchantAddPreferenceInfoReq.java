package com.paytm.dto.mappingService.addMerchantPreferenceReq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "merchantId",
        "merchantPreferenceInfos"
})
public class MerchantAddPreferenceInfoReq implements Serializable
{

    @JsonProperty("merchantId")
    private String merchantId;
    @JsonProperty("merchantPreferenceInfos")
    private List<MerchantPreferenceInfo> merchantPreferenceInfos = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 4620389140981565363L;

    private MerchantAddPreferenceInfoReq(Builder builder) {
        this.merchantId = builder.merchantId;
        this.merchantPreferenceInfos = Arrays.asList(
                new MerchantPreferenceInfo().setPrefStatus(builder.prefStatus)
                .setPrefType(builder.prefType)
                .setPrefValue(builder.prefValue)
        );
    }
    
    public static class Builder {
        private String merchantId;
        private String prefType;
        private String prefStatus;
        private String prefValue;

        public Builder(String merchantId, String prefType, String prefStatus, String prefValue) {
            this.merchantId = merchantId;
            this.prefStatus = prefStatus;
            this.prefType = prefType;
            this.prefValue = prefValue;
        }

        public Builder setMerchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder setPrefType(String prefType) {
            this.prefType = prefType;
            return this;
        }

        public Builder setPrefStatus(String prefStatus) {
            this.prefStatus = prefStatus;
            return this;
        }

        public Builder setPrefValue(String prefValue) {
            this.prefValue = prefValue;
            return this;
        }

        public MerchantAddPreferenceInfoReq build() {
            return new MerchantAddPreferenceInfoReq(this);
        }
    }

    public String getMerchantId() {
        return merchantId;
    }

    public List<MerchantPreferenceInfo> getMerchantPreferenceInfos() {
        return merchantPreferenceInfos;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}
