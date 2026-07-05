
package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "savedCards",
        "savedVPAs",
        "sarvatraVpa",
        "sarvatraUserProfile"
})
public class AddMoneySavedInstruments {

    @JsonProperty("savedCards")
    private List<Object> savedCards = null;
    @JsonProperty("savedVPAs")
    private List<Object> savedVPAs = null;
    @JsonProperty("sarvatraVpa")
    private Object sarvatraVpa;
    @JsonProperty("sarvatraUserProfile")
    private Object sarvatraUserProfile;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("savedCards")
    public List<Object> getSavedCards() {
        return savedCards;
    }

    @JsonProperty("savedCards")
    public void setSavedCards(List<Object> savedCards) {
        this.savedCards = savedCards;
    }

    @JsonProperty("savedVPAs")
    public List<Object> getSavedVPAs() {
        return savedVPAs;
    }

    @JsonProperty("savedVPAs")
    public void setSavedVPAs(List<Object> savedVPAs) {
        this.savedVPAs = savedVPAs;
    }

    @JsonProperty("sarvatraVpa")
    public Object getSarvatraVpa() {
        return sarvatraVpa;
    }

    @JsonProperty("sarvatraVpa")
    public void setSarvatraVpa(Object sarvatraVpa) {
        this.sarvatraVpa = sarvatraVpa;
    }

    @JsonProperty("sarvatraUserProfile")
    public Object getSarvatraUserProfile() {
        return sarvatraUserProfile;
    }

    @JsonProperty("sarvatraUserProfile")
    public void setSarvatraUserProfile(Object sarvatraUserProfile) {
        this.sarvatraUserProfile = sarvatraUserProfile;
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