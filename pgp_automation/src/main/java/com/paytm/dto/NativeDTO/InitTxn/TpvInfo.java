package com.paytm.dto.NativeDTO.InitTxn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "accountNumber",
        "ifscCode",
        "status",
        "bankName",
        "accountHolderName",
        "accountType",
        "nbin"
})
@Generated("jsonschema2pojo")
public class TpvInfo {

    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("ifscCode")
    private String ifscCode;
    @JsonProperty("status")
    private String status;
    @JsonProperty("bankName")
    private String bankName;
    @JsonProperty("accountHolderName")
    private String accountHolderName;
    @JsonProperty("accountType")
    private String accountType;
    @JsonProperty("nbin")
    private String nbin;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


List<TpvInfo> tpvInfos;

    @JsonProperty("accountNumber")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("accountNumber")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonProperty("ifscCode")
    public String getIfscCode() {
        return ifscCode;
    }

    @JsonProperty("ifscCode")
    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("bankName")
    public String getBankName() {
        return bankName;
    }

    @JsonProperty("bankName")
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @JsonProperty("accountHolderName")
    public String getAccountHolderName() {
        return accountHolderName;
    }

    @JsonProperty("accountHolderName")
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    @JsonProperty("accountType")
    public String getAccountType() {
        return accountType;
    }

    @JsonProperty("accountType")
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    @JsonProperty("nbin")
    public String getNbin() {
        return nbin;
    }

    @JsonProperty("nbin")
    public void setNbin(String nbin) {
        this.nbin = nbin;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    public TpvInfo(String accountNumber, String ifscCode, String accountHolderName, String bankName, String status, String accountType, String nbin) {
    this.accountNumber=accountNumber;
    this.ifscCode=ifscCode;
    this.accountHolderName=accountHolderName;
    this.bankName=bankName;
    this.status=status;
    this.accountType=accountType;
    this.nbin=nbin;

    }
}