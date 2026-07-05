package com.paytm.dto.NativeDTO.OfferApply;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class PromoContext {
    @JsonProperty("cart")
    private String cart;
    
    @JsonProperty("affordabilityInfo")
    private String affordabilityInfo;

    public PromoContext() {}

    public PromoContext(String cart, String affordabilityInfo) {
        this.cart = cart;
        this.affordabilityInfo = affordabilityInfo;
    }

    public String getCart() {
        return cart;
    }

    public void setCart(String cart) {
        this.cart = cart;
    }

    public String getAffordabilityInfo() {
        return affordabilityInfo;
    }

    public void setAffordabilityInfo(String affordabilityInfo) {
        this.affordabilityInfo = affordabilityInfo;
    }
}