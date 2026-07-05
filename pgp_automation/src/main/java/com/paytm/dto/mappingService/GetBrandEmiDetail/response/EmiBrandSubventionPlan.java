package com.paytm.dto.mappingService.GetBrandEmiDetail.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "brandCode",
        "brandName",
        "plans"
})
public class EmiBrandSubventionPlan {

    @JsonProperty("brandCode")
    private String brandCode;
    @JsonProperty("brandName")
    private String brandName;
    @JsonProperty("plans")
    private List<Plan> plans = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("brandCode")
    public String getBrandCode() {
        return brandCode;
    }

    @JsonProperty("brandCode")
    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public EmiBrandSubventionPlan withBrandCode(String brandCode) {
        this.brandCode = brandCode;
        return this;
    }

    @JsonProperty("brandName")
    public String getBrandName() {
        return brandName;
    }

    @JsonProperty("brandName")
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public EmiBrandSubventionPlan withBrandName(String brandName) {
        this.brandName = brandName;
        return this;
    }

    @JsonProperty("plans")
    public List<Plan> getPlans() {
        return plans;
    }

    @JsonProperty("plans")
    public void setPlans(List<Plan> plans) {
        this.plans = plans;
    }

    public EmiBrandSubventionPlan withPlans(List<Plan> plans) {
        this.plans = plans;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public EmiBrandSubventionPlan withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
