package com.paytm.dto.EnrollmentStatusDTO;
import com.fasterxml.jackson.annotation.*;
import com.google.inject.internal.util.$Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "appId",
        "custId",
        "accountDataList"
})
public class Body{

@JsonProperty("appId")
private String appId;
@JsonProperty("custId")
private String custId;
@JsonProperty("accountDataList")
private List<AccountDataList> accountDataList =null;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("appId")
public String getAppId(){
        return appId;
        }

@JsonProperty("appId")
public Body setAppId(String appId) {
        this.appId = appId;
        return this;
        }

@JsonProperty("custId")
public String getCustId() {
        return custId;
        }

@JsonProperty("custId")
public Body setCustId(String custId) {
        this.custId = custId;
        return  this;
        }

@JsonProperty("accountDataList")
public List<AccountDataList> getAccountDataList() {
        return accountDataList;
        }

@JsonProperty("accountDataList")
public Body setAccountDataList(List<AccountDataList> accountDataList) {
        this.accountDataList = accountDataList;
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
        public Body(){

        }

        public <accountDataList> Body(String appId, String custId, List <accountDataList> accountDataLists){
    this.appId=appId;
    this.custId=custId;
    this.accountDataList= accountDataList;

        }

        }
