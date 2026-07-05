package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ankuragarwal on 29/10/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoCodeData {

    private String promoCode;
    private String promoCodeMsg;
    private String promoCodeTypeName;
    private boolean promoCodeValid;
    private String promoMsg;

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromoCodeMsg() {
        return promoCodeMsg;
    }

    public void setPromoCodeMsg(String promoCodeMsg) {
        this.promoCodeMsg = promoCodeMsg;
    }

    public String getPromoCodeTypeName() {
        return promoCodeTypeName;
    }

    public void setPromoCodeTypeName(String promoCodeTypeName) {
        this.promoCodeTypeName = promoCodeTypeName;
    }

    public boolean isPromoCodeValid() {
        return promoCodeValid;
    }

    public void setPromoCodeValid(boolean promoCodeValid) {
        this.promoCodeValid = promoCodeValid;
    }

    public String getPromoMsg() {
        return promoMsg;
    }

    public void setPromoMsg(String promoMsg) {
        this.promoMsg = promoMsg;
    }
}
