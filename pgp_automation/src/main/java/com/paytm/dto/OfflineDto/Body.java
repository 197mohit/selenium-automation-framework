package com.paytm.dto.OfflineDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anjukumari on 04/12/18
 */
public class Body {
    @JsonProperty("orderAmount")
    private OrderAmount orderAmount;
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("industryTypeId")
    private String industryTypeId;
    @JsonProperty("instrumentTypes")
    private List<String> instrumentTypes = null;
    @JsonProperty("savedInstrumentsTypes")
    private List<Object> savedInstrumentsTypes = null;
    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("postpaidOnboardingSupported")
    private String postpaidOnboardingSupported;

    @JsonProperty("postpaidOnboardingSupported")
    public String getPostpaidOnboardingSupported() {
        return postpaidOnboardingSupported;
    }

    @JsonProperty("postpaidOnboardingSupported")
    public void setPostpaidOnboardingSupported(String postpaidOnboardingSupported) {
        this.postpaidOnboardingSupported = postpaidOnboardingSupported;
    }

    @JsonProperty("orderAmount")
    public OrderAmount getOrderAmount() {
        return orderAmount;
    }

    @JsonProperty("orderAmount")
    public void setOrderAmount(OrderAmount orderAmount) {
        this.orderAmount = orderAmount;
    }

    @JsonProperty("deviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("deviceId")
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @JsonProperty("channelId")
    public String getChannelId() {
        return channelId;
    }

    @JsonProperty("channelId")
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @JsonProperty("industryTypeId")
    public String getIndustryTypeId() {
        return industryTypeId;
    }

    @JsonProperty("industryTypeId")
    public void setIndustryTypeId(String industryTypeId) {
        this.industryTypeId = industryTypeId;
    }

    @JsonProperty("instrumentTypes")
    public List<String> getInstrumentTypes() {
        return instrumentTypes;
    }

    @JsonProperty("instrumentTypes")
    public void setInstrumentTypes(List<String> instrumentTypes) {
        this.instrumentTypes = instrumentTypes;
    }

    @JsonProperty("savedInstrumentsTypes")
    public List<Object> getSavedInstrumentsTypes() {
        return savedInstrumentsTypes;
    }

    @JsonProperty("savedInstrumentsTypes")
    public void setSavedInstrumentsTypes(List<Object> savedInstrumentsTypes) {
        this.savedInstrumentsTypes = savedInstrumentsTypes;
    }

    @JsonProperty("extendInfo")
    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    @JsonProperty("extendInfo")
    public void setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
    }

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    @JsonProperty("signature")
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Body() {
        this.orderAmount = new OrderAmount();
        this.deviceId = "96507880";
        this.channelId = "WEB";
        this.industryTypeId = "USR";
        List<String> ins =  new ArrayList<String>();
        ins.add("ALL");
        this.instrumentTypes = ins;
        List<Object> saveDetail =  new ArrayList<Object>();
        saveDetail.add("ALL");
        this.savedInstrumentsTypes = saveDetail;
        this.extendInfo = new ExtendInfo();
        this.signature = "";
    }
}
