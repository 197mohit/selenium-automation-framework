package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "promoCode",
        "applyAvailablePromo",
        "validatePromo",
        "promoAmount",
        "cartDetails"
})
public class SimplifiedPaymentOffers {

    @JsonProperty("promoCode")
    private String promoCode;
    @JsonProperty("applyAvailablePromo")
    private String applyAvailablePromo;
    @JsonProperty("validatePromo")
    private String validatePromo;
    @JsonProperty("promoAmount")
    private String promoAmount;
    @JsonProperty("cartDetails")
    private CartDetails cartDetails;

    @JsonProperty("items")
    private List<SimplifiedSubvention.Item> items = null;

    @JsonProperty("promoCode")
    public String getPromoCode() {
        return promoCode;
    }

    @JsonProperty("promoCode")
    public SimplifiedPaymentOffers setPromoCode(String promoCode) {
        this.promoCode = promoCode;
        return this;
    }

    @JsonProperty("applyAvailablePromo")
    public String getApplyAvailablePromo() {
        return applyAvailablePromo;
    }

    @JsonProperty("applyAvailablePromo")
    public SimplifiedPaymentOffers setApplyAvailablePromo(String applyAvailablePromo) {
        this.applyAvailablePromo = applyAvailablePromo;
        return this;
    }

    @JsonProperty("validatePromo")
    public String getValidatePromo() {
        return validatePromo;
    }

    @JsonProperty("validatePromo")
    public SimplifiedPaymentOffers setValidatePromo(String validatePromo) {
        this.validatePromo = validatePromo;
        return this;
    }

    @JsonProperty("promoAmount")
        public String getPromoAmount() {    return promoAmount;        }

    @JsonProperty("promoAmount")
    public SimplifiedPaymentOffers setPromoAmount(String promoAmount) {
        this.promoAmount = promoAmount;
        return this;
    }

    public SimplifiedPaymentOffers(String promoCode, String applyAvailablePromo, String validatePromo){
        this.promoCode = promoCode;
        this.applyAvailablePromo = applyAvailablePromo;
        this.validatePromo = validatePromo;
    }


    public SimplifiedPaymentOffers(String promoCode, String applyAvailablePromo, String validatePromo,String promoAmount){
        this.promoCode = promoCode;
        this.applyAvailablePromo = applyAvailablePromo;
        this.validatePromo = validatePromo;
        this.promoAmount = promoAmount;
    }

    public SimplifiedPaymentOffers(){
        this.promoCode = null;
        this.applyAvailablePromo = null;
        this.validatePromo = null;
    }
    public static class Items {
        @JsonProperty("id")
        private String id;
        @JsonProperty("promocode")
        private String promocode;
        @JsonProperty("amount")
        private String amount;
        @JsonProperty("productDetail")
        private ProductDetail productDetail;


        public Items(String id, String promocode, String amount, ProductDetail productDetail) {
            this.id = id;
            this.promocode = promocode;
            this.amount = amount;
            this.productDetail = productDetail;
        }
    }

    public static class ProductDetail {
        @JsonProperty("id")
        private String id;
        @JsonProperty("merchantId")
        private String merchantId;
        @JsonProperty("brandId")
        private String brandId;
        @JsonProperty("categoryIds")
        private List<String> categoryIds;


        public ProductDetail(String id, String merchantId, String brandId, List<String> categoryIds) {
            this.id = id;
            this.merchantId = merchantId;
            this.brandId = brandId;
            this.categoryIds = categoryIds;
        }
    }

    public static class CartDetails {
        @JsonProperty("items")
        private List<SimplifiedPaymentOffers.Items> items = null;
        public CartDetails(List<Items> items) {
            this.items = items;
        }

    }
    public SimplifiedPaymentOffers(String promoCode, String applyAvailablePromo, String validatePromo,CartDetails cartDetails){
        this.promoCode = promoCode;
        this.applyAvailablePromo = applyAvailablePromo;
        this.validatePromo = validatePromo;
        this.cartDetails = cartDetails;
    }
}