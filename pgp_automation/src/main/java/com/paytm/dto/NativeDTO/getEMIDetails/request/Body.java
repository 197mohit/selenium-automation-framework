package com.paytm.dto.NativeDTO.getEMIDetails.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mid"
})
public class Body {


    public Body(String mid){
        this.mid=mid;
    }
    public Body(String mid, String productCode,List<String> brandCode) {
      //  super();
        this.mid=mid;
        this.productCode = productCode;
        this.brandCode=brandCode;
    }
    public Body(String mid,List<String>brandCode)
    {
        this.mid=mid;
        this.brandCode=brandCode;
    }
    @JsonProperty("mid")
    private String mid;
    @JsonProperty("productCode")
    private String productCode;
    @JsonProperty("brandCode")
    private List<String> brandCode = null;

    @JsonProperty("mid")
    public String getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public Body setMid(String mid) {
        this.mid = mid;
        return this;
    }

    @JsonProperty("productCode")
    public String getProductCode() {
        return productCode;
    }

    @JsonProperty("productCode")
    public Body setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    @JsonProperty("brandCode")
    public List<String> getBrandCode() {
        return brandCode;
    }

    @JsonProperty("brandCode")
    public Body setBrandCode(List<String> brandCode) {
        this.brandCode = brandCode;
        return this;
    }



}
