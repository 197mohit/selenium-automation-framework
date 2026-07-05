
package com.paytm.dto.NativeDTO.CancelSubscription;

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
    "mid",
    "ssoToken",
    "subsId"
})
public class Body {

    public Body(String mid,String subsId,String ssoToken){
    this.mid=mid;
    this.subsId=subsId;
    this.ssoToken=ssoToken;
    }

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("ssoToken")
    private String ssoToken;
    @JsonProperty("subsId")
    private String subsId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(String mid) {
        this.mid = mid;
    }

    @JsonProperty("ssoToken")
    public String getSsoToken() {
        return ssoToken;
    }

    @JsonProperty("ssoToken")
    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    @JsonProperty("subsId")
    public String getSubsId() {
        return subsId;
    }

    @JsonProperty("subsId")
    public void setSubsId(String subsId) {
        this.subsId = subsId;
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
