package com.paytm.dto.NativeDTO.getEMIDetails.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "brandCode",
        "brandEmiDetailInfo"
})
public class BrandEmiDetails {

    @JsonProperty("brandCode")
    private String brandCode;
    @JsonProperty("brandEmiDetailInfo")
    private List<BrandEmiDetailInfo> brandEmiDetailInfo = null;

    @JsonProperty("brandCode")
    public String getBrandCode() {
        return brandCode;
    }

    @JsonProperty("brandCode")
    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public BrandEmiDetails withBrandCode(String brandCode) {
        this.brandCode = brandCode;
        return this;
    }

    @JsonProperty("brandEmiDetailInfo")
    public List<BrandEmiDetailInfo> getBrandEmiDetailInfo() {
        return brandEmiDetailInfo;
    }

    @JsonProperty("brandEmiDetailInfo")
    public void setBrandEmiDetailInfo(List<BrandEmiDetailInfo> brandEmiDetailInfo) {
        this.brandEmiDetailInfo = brandEmiDetailInfo;
    }

    public BrandEmiDetails withBrandEmiDetailInfo(List<BrandEmiDetailInfo> brandEmiDetailInfo) {
        this.brandEmiDetailInfo = brandEmiDetailInfo;
        return this;
    }
}
