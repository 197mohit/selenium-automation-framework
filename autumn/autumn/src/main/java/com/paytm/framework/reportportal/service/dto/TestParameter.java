package com.paytm.framework.reportportal.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "key",
        "value"
})
public class TestParameter implements Serializable
{

    @JsonProperty("key")
    private String key;
    @JsonProperty("value")
    private String value;
    private final static long serialVersionUID = -4608105276606953297L;

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

    public TestParameter withKey(String key) {
        if(key.length() > 128)
            key = key.substring(0, 127);
        key = key.trim().isEmpty() ? "EMPTY_KEY" : key.trim();
        this.key = key;
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

    public TestParameter withValue(String value) {
        if(value.length() > 128)
            value = value.substring(0, 127);
        value = value.trim().isEmpty() ? "EMPTY_VALUE" : value.trim();
        this.value = value;
        return this;
    }

}