package com.paytm.utils.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deepakkumar on 22/10/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateMerchant {

    @JsonProperty("CUST_ID")
    private String custid;
    @JsonProperty("SF_CALLBACK_URL")
    private String sfCallbackUrl;
    @JsonProperty("IP_ADDR")
    private String ipAddr;
    @JsonProperty("createMerReq")
    private CreateMerRequest createMerRequest;
    @JsonProperty("configureMbid")
    private ConfigureMBID configureMbid;
    @JsonProperty("configVelocity")
    private ConfigVelocity configVelocity;
    @JsonProperty("configureMerchantCommission")
    private ConfigureMerchantCommission configureMerchantCommission;

    public CreateMerchant setConfigureMbidAndInstrument(ConfigureMbidAndInstrument configureMbidAndInstrument) {
        this.configureMbidAndInstrument = configureMbidAndInstrument;
        return this;
    }

    @JsonProperty("configureMbidAndInstrument")
    private  ConfigureMbidAndInstrument configureMbidAndInstrument;

    public String getIpAddr() {
        return ipAddr;
    }

    public CreateMerchant setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
        return this;
    }

    public String getCustid() {
        return custid;
    }

    public CreateMerchant setCustid(String custid) {
        this.custid = custid;
        return this;
    }

    public String getSfCallbackUrl() {
        return sfCallbackUrl;
    }

    public CreateMerchant setSfCallbackUrl(String sfCallbackUrl) {
        this.sfCallbackUrl = sfCallbackUrl;
        return this;
    }

    public CreateMerRequest getCreateMerRequest() {
        return createMerRequest;
    }

    public CreateMerchant setCreateMerRequest(CreateMerRequest createMerRequest) {
        this.createMerRequest = createMerRequest;
        return this;
    }

    public ConfigureMBID getConfigureMbid() {
        return configureMbid;
    }

    public CreateMerchant setConfigureMbid(ConfigureMBID configureMbid) {
        this.configureMbid = configureMbid;
        return this;
    }

    public ConfigVelocity getConfigVelocity() {
        return configVelocity;
    }

    public CreateMerchant setConfigVelocity(ConfigVelocity configVelocity) {
        this.configVelocity = configVelocity;
        return this;
    }

    public ConfigureMerchantCommission getConfigureMerchantCommission() {
        return configureMerchantCommission;
    }

    public ConfigureMbidAndInstrument getConfigureMbidAndInstrument() {
        return configureMbidAndInstrument;
    }

    public CreateMerchant setConfigureMerchantCommission(ConfigureMerchantCommission configureMerchantCommission) {
        this.configureMerchantCommission = configureMerchantCommission;
        return this;
    }

}
