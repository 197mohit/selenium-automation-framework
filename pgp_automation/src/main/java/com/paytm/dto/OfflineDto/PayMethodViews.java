package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "merchantSavedInstruments",
        "addMoneySavedInstruments",
        "merchantPayMethods",
        "addMoneyPayMethods"
})
public class PayMethodViews {

    @JsonProperty("merchantSavedInstruments")
    private MerchantSavedInstruments merchantSavedInstruments;
    @JsonProperty("addMoneySavedInstruments")
    private AddMoneySavedInstruments addMoneySavedInstruments;
    @JsonProperty("merchantPayMethods")
    private List<MerchantPayMethod> merchantPayMethods = null;
    @JsonProperty("addMoneyPayMethods")
    private Object addMoneyPayMethods;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("merchantSavedInstruments")
    public MerchantSavedInstruments getMerchantSavedInstruments() {
        return merchantSavedInstruments;
    }

    @JsonProperty("merchantSavedInstruments")
    public void setMerchantSavedInstruments(MerchantSavedInstruments merchantSavedInstruments) {
        this.merchantSavedInstruments = merchantSavedInstruments;
    }

    @JsonProperty("addMoneySavedInstruments")
    public AddMoneySavedInstruments getAddMoneySavedInstruments() {
        return addMoneySavedInstruments;
    }

    @JsonProperty("addMoneySavedInstruments")
    public void setAddMoneySavedInstruments(AddMoneySavedInstruments addMoneySavedInstruments) {
        this.addMoneySavedInstruments = addMoneySavedInstruments;
    }

    @JsonProperty("merchantPayMethods")
    public List<MerchantPayMethod> getMerchantPayMethods() {
        return merchantPayMethods;
    }

    @JsonProperty("merchantPayMethods")
    public void setMerchantPayMethods(List<MerchantPayMethod> merchantPayMethods) {
        this.merchantPayMethods = merchantPayMethods;
    }

    @JsonProperty("addMoneyPayMethods")
    public Object getAddMoneyPayMethods() {
        return addMoneyPayMethods;
    }

    @JsonProperty("addMoneyPayMethods")
    public void setAddMoneyPayMethods(Object addMoneyPayMethods) {
        this.addMoneyPayMethods = addMoneyPayMethods;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}