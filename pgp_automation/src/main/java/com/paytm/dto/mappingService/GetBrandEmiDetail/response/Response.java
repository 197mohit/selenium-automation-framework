package com.paytm.dto.mappingService.GetBrandEmiDetail.response;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "emiBrandSubventionPlans"
})
public class Response {
    @JsonProperty("emiBrandSubventionPlans")
    private List<EmiBrandSubventionPlan> emiBrandSubventionPlans = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("emiBrandSubventionPlans")
    public List<EmiBrandSubventionPlan> getEmiBrandSubventionPlans() {
        return emiBrandSubventionPlans;
    }

    @JsonProperty("emiBrandSubventionPlans")
    public void setEmiBrandSubventionPlans(List<EmiBrandSubventionPlan> emiBrandSubventionPlans) {
        this.emiBrandSubventionPlans = emiBrandSubventionPlans;
    }

    public Response withEmiBrandSubventionPlans(List<EmiBrandSubventionPlan> emiBrandSubventionPlans) {
        this.emiBrandSubventionPlans = emiBrandSubventionPlans;
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

    public Response withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
