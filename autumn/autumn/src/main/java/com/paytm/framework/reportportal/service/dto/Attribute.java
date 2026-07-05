package com.paytm.framework.reportportal.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "key",
        "system",
        "value"
})
public class Attribute implements Serializable
{

    @JsonProperty("key")
    private String key;
    @JsonProperty("system")
    private Boolean system;
    @JsonProperty("value")
    private String value;
    private final static long serialVersionUID = 2494509518109485406L;

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        if(key.length() > 128)
            key = key.substring(0, 127);
        key = key.trim().isEmpty() ? "EMPTY_KEY" : key.trim();
        this.key = key;
    }

    public Attribute withKey(String key) {
        if(key.length() > 128)
            key = key.substring(0, 127);
        key = key.trim().isEmpty() ? "EMPTY_KEY" : key.trim();
        this.key = key;
        return this;
    }

    @JsonProperty("system")
    public Boolean getSystem() {
        return system;
    }

    @JsonProperty("system")
    public void setSystem(Boolean system) {
        this.system = system;
    }

    public Attribute withSystem(Boolean system) {
        system = null != system && system;
        this.system = system;
        return this;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        if(value.length() > 128)
            value = value.substring(0, 127);
        value = value.trim().isEmpty() ? "EMPTY_VALUE" : value.trim();
        this.value = value;
    }

    public Attribute withValue(String value) {
        if(value.length() > 128)
            value = value.substring(0, 127);
        value = value.trim().isEmpty() ? "EMPTY_VALUE" : value.trim();
        this.value = value;
        return this;
    }

}