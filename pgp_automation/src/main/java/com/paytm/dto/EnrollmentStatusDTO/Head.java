package com.paytm.dto.EnrollmentStatusDTO;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "requestId",
        "token",
        "tokenType"
})
public class Head {

@JsonProperty("requestId")
private String requestId;
@JsonProperty("token")
private String token;
@JsonProperty("tokenType")
private String tokenType;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("requestId")
public String getRequestId() {
        return requestId;
        }

@JsonProperty("requestId")
public Head setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
        }

@JsonProperty("token")
public String getToken() {
        return token;
        }

@JsonProperty("token")
public Head setToken(String token) {
        this.token = token;
        return this;
        }

@JsonProperty("tokenType")
public String getTokenType() {
        return tokenType;
        }

@JsonProperty("tokenType")
public Head setTokenType(String tokenType) {
        this.tokenType = tokenType;
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

        public Head(){

        }
        public Head(String requestId, String token, String tokenType){
        this.requestId=requestId;
        this.token=token;
        this.tokenType=tokenType;

        }

        }
