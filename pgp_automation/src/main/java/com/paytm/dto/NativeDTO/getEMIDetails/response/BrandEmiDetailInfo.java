package com.paytm.dto.NativeDTO.getEMIDetails.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "channelCode",
        "channelName",
        "emiType",
        "iconUrl",
        "emiChannelInfos",
        "multiItemEmiSupported"
})
public class BrandEmiDetailInfo {

    @JsonProperty("channelCode")
    private String channelCode;
    @JsonProperty("channelName")
    private String channelName;
    @JsonProperty("emiType")
    private String emiType;
    @JsonProperty("iconUrl")
    private String iconUrl;
    @JsonProperty("emiChannelInfos")
    private List<EmiChannelInfo> emiChannelInfos = null;
    @JsonProperty("multiItemEmiSupported")
    private Boolean multiItemEmiSupported;

    @JsonProperty("channelCode")
    public String getChannelCode() {
        return channelCode;
    }

    @JsonProperty("channelCode")
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public BrandEmiDetailInfo withChannelCode(String channelCode) {
        this.channelCode = channelCode;
        return this;
    }

    @JsonProperty("channelName")
    public String getChannelName() {
        return channelName;
    }

    @JsonProperty("channelName")
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public BrandEmiDetailInfo withChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    @JsonProperty("emiType")
    public String getEmiType() {
        return emiType;
    }

    @JsonProperty("emiType")
    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public BrandEmiDetailInfo withEmiType(String emiType) {
        this.emiType = emiType;
        return this;
    }

    @JsonProperty("iconUrl")
    public String getIconUrl() {
        return iconUrl;
    }

    @JsonProperty("iconUrl")
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public BrandEmiDetailInfo withIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    @JsonProperty("emiChannelInfos")
    public List<EmiChannelInfo> getEmiChannelInfos() {
        return emiChannelInfos;
    }

    @JsonProperty("emiChannelInfos")
    public void setEmiChannelInfos(List<EmiChannelInfo> emiChannelInfos) {
        this.emiChannelInfos = emiChannelInfos;
    }

    public BrandEmiDetailInfo withEmiChannelInfos(List<EmiChannelInfo> emiChannelInfos) {
        this.emiChannelInfos = emiChannelInfos;
        return this;
    }

    @JsonProperty("multiItemEmiSupported")
    public Boolean getMultiItemEmiSupported() {
        return multiItemEmiSupported;
    }

    @JsonProperty("multiItemEmiSupported")
    public void setMultiItemEmiSupported(Boolean multiItemEmiSupported) {
        this.multiItemEmiSupported = multiItemEmiSupported;
    }

    public BrandEmiDetailInfo withMultiItemEmiSupported(Boolean multiItemEmiSupported) {
        this.multiItemEmiSupported = multiItemEmiSupported;
        return this;
    }
}
