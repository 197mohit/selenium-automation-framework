package com.paytm.dto.EnrollmentStatusDTO;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "deviceId",
        "cardAlias",
        "bin"
})

public class AccountDataList {

@JsonProperty("deviceId")
private String deviceId;
@JsonProperty("cardAlias")
private String cardAlias;
@JsonProperty("bin")
private String bin;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("deviceId")
public String getDeviceId() {
        return deviceId;
        }

@JsonProperty("deviceId")
public AccountDataList setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
        }

@JsonProperty("cardAlias")
public String getCardAlias() {
        return cardAlias;
        }

@JsonProperty("cardAlias")
public AccountDataList setCardAlias(String cardAlias) {
        this.cardAlias = cardAlias;
        return this;
        }

@JsonProperty("bin")
public String getBin() {
        return bin;
        }

@JsonProperty("bin")
public AccountDataList setBin(String bin) {
        this.bin = bin;
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

        public  AccountDataList(String deviceId, String cardAlias, String bin){
    this.deviceId=deviceId;
    this.cardAlias=cardAlias;
    this.bin=bin;
        }

        }
