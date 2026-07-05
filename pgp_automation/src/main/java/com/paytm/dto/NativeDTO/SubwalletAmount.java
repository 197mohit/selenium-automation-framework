
package com.paytm.dto.NativeDTO;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "FOOD",
    "GIFT"
})
public class SubwalletAmount {

    @JsonProperty("FOOD")
    private String fOOD;
    @JsonProperty("GIFT")
    private String gIFT;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("FOOD")
    public String getFOOD() {
        return fOOD;
    }

    @JsonProperty("FOOD")
    public void setFOOD(String fOOD) {
        this.fOOD = fOOD;
    }

    @JsonProperty("GIFT")
    public String getGIFT() {
        return gIFT;
    }

    @JsonProperty("GIFT")
    public void setGIFT(String gIFT) {
        this.gIFT = gIFT;
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
