package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "amountBasedSubvention",
        "amountBasedBankOffer",
        "items"
})
public class FetchUnifiedOffers {
    @JsonProperty("amountBasedSubvention")
    private Boolean amountBasedSubvention;
    @JsonProperty("amountBasedBankOffer")
    private Boolean amountBasedBankOffer;
    @JsonProperty("items")
    private List<Item> items = null;

    public FetchUnifiedOffers(Boolean amountBasedSubvention, Boolean amountBasedBankOffer, List<Item> items){
        this.amountBasedSubvention = amountBasedSubvention;
        this.amountBasedBankOffer = amountBasedBankOffer;
        this.items = items;
    }

    @JsonProperty("amountBasedSubvention")
    public Boolean getAmountBasedSubvention() {
        return amountBasedSubvention;
    }

    @JsonProperty("amountBasedSubvention")
    public void setAmountBasedSubvention(Boolean amountBasedSubvention) {
        this.amountBasedSubvention = amountBasedSubvention;
    }

    @JsonProperty("amountBasedBankOffer")
    public Boolean getAmountBasedBankOffer() {
        return amountBasedBankOffer;
    }

    @JsonProperty("amountBasedBankOffer")
    public void setAmountBasedBankOffer(Boolean amountBasedBankOffer) {
        this.amountBasedBankOffer = amountBasedBankOffer;
    }

    public static class Item {

        @JsonProperty("id")
        private String id;
        @JsonProperty("productId")
        private String productId;
        @JsonProperty("brandId")
        private String brandId;
        @JsonProperty("categoryId")
        private String categoryId;
        @JsonProperty("price")
        private String price;

        public Item(String id, String productId, String brandId, String categoryId, String price) {
            this.id = id;
            this.productId = productId;
            this.brandId = brandId;
            this.categoryId = categoryId;
            this.price = price;
        }
        @JsonProperty("id")
        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        @JsonProperty("productId")
        public String getProductId() {
            return productId;
        }

        @JsonProperty("productId")
        public void setProductId(String productId) {
            this.productId = productId;
        }

        @JsonProperty("brandId")
        public String getBrandId() {
            return brandId;
        }

        @JsonProperty("brandId")
        public void setBrandId(String brandId) {
            this.brandId = brandId;
        }

        @JsonProperty("categoryId")
        public String getCategoryId() {
            return categoryId;
        }

        @JsonProperty("categoryId")
        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        @JsonProperty("price")
        public String getPrice() {
            return price;
        }

        @JsonProperty("price")
        public void setPrice(String price) {
            this.price = price;
        }
    }

}
