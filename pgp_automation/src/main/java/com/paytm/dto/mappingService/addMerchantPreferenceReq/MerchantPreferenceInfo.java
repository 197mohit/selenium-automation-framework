package com.paytm.dto.mappingService.addMerchantPreferenceReq;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "prefType",
        "prefStatus",
        "prefValue"
})
public class MerchantPreferenceInfo implements Serializable
{

    @JsonProperty("prefType")
    private String prefType;
    @JsonProperty("prefStatus")
    private String prefStatus;
    @JsonProperty("prefValue")
    private String prefValue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 4676462456046083413L;

    @JsonProperty("prefType")
    public String getPrefType() {
        return prefType;
    }

    @JsonProperty("prefType")
    public MerchantPreferenceInfo setPrefType(String prefType) {
        this.prefType = prefType;
        return this;
    }

    @JsonProperty("prefStatus")
    public String getPrefStatus() {
        return prefStatus;
    }

    @JsonProperty("prefStatus")
    public MerchantPreferenceInfo setPrefStatus(String prefStatus) {
        this.prefStatus = prefStatus;
        return this;
    }

    @JsonProperty("prefValue")
    public String getPrefValue() {
        return prefValue;
    }

    @JsonProperty("prefValue")
    public MerchantPreferenceInfo setPrefValue(String prefValue) {
        this.prefValue = prefValue;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public MerchantPreferenceInfo setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("prefType", prefType).append("prefStatus", prefStatus).append("prefValue", prefValue).append("additionalProperties", additionalProperties).toString();
    }

}