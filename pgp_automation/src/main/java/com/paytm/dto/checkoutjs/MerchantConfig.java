package com.paytm.dto.checkoutjs;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "root",
        "hidePaymodeLabel",
        "flow",
        "style",
        "jsFile",
        "data",
        "merchant",
        "mapClientMessage",
        "labels",
        "payMode",
        "handler",
        "emiSubvention"
})
public class MerchantConfig implements Serializable {

    @JsonProperty("root")
    public String root;
    @JsonProperty("hidePaymodeLabel")
    public Boolean hidePaymodeLabel;
    @JsonProperty("flow")
    public String flow;
    @JsonProperty("style")
    public Style style;
    @JsonProperty("jsFile")
    public String jsFile;
    @JsonProperty("data")
    public Data data;
    @JsonProperty("merchant")
    public Merchant merchant;
    @JsonProperty("mapClientMessage")
    public MapClientMessage mapClientMessage;
    @JsonProperty("labels")
    public Labels labels;
    @JsonProperty("payMode")
    public PayMode payMode;
    @JsonProperty("handler")
    public Handler handler;
    @JsonProperty("emiSubvention")
    public emiSubvention emiSubvention;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 780073490401638366L;

    public MerchantConfig setRoot(String root) {
        this.root = root;
        return this;
    }

    public MerchantConfig setHidePaymodeLabel(Boolean hidePaymodeLabel){
        this.hidePaymodeLabel = hidePaymodeLabel;
        return this;
    }

    public MerchantConfig setFlow(String flow) {
        this.flow = flow;
        return this;
    }

    public MerchantConfig setStyle(Style style) {
        this.style = style;
        return this;
    }

    public MerchantConfig setJsFile(String jsFile) {
        this.jsFile = jsFile;
        return this;
    }

    public MerchantConfig setData(Data data) {
        this.data = data;
        return this;
    }

    public MerchantConfig setMerchant(Merchant merchant) {
        this.merchant = merchant;
        return this;
    }

    public MerchantConfig setMapClientMessage(MapClientMessage mapClientMessage) {
        this.mapClientMessage = mapClientMessage;
        return this;
    }

    public MerchantConfig setLabels(Labels labels) {
        this.labels = labels;
        return this;
    }

    public MerchantConfig setPayMode(PayMode payMode) {
        this.payMode = payMode;
        return this;
    }

    public MerchantConfig setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public MerchantConfig setEmiSubvention(emiSubvention emiSubvention) {
        this.emiSubvention = emiSubvention;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }


    @JsonAnySetter
    public MerchantConfig setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        ObjectMapper m = new ObjectMapper();
        try {
            return m.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
