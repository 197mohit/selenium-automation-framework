package com.paytm.dto.NativeDTO;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anjukumari on 22/10/18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Body {

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Body(String key, String val){
        Map<String, Object> map = new HashMap<>();
        map.put(key, val);
        this.additionalProperties = map;
    }

    public Body(Map<String, Object> map){
        this.additionalProperties = map;
    }

}
