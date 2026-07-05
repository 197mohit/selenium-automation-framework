package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "merchantGoodsId",
            "merchantShippingId",
            "snapshotUrl",
            "description",
            "category",
            "quantity",
            "unit",
            "price",
            "productSku",
            "productName",
            "productCode",
            "hsnCode",
            "extendInfo"
    })
    public class Good {

        @JsonProperty("merchantGoodsId")
        private String merchantGoodsId;
        @JsonProperty("merchantShippingId")
        private String merchantShippingId;
        @JsonProperty("snapshotUrl")
        private String snapshotUrl;
        @JsonProperty("description")
        private String description;
        @JsonProperty("category")
        private String category;
        @JsonProperty("quantity")
        private String quantity;
        @JsonProperty("unit")
        private String unit;
        @JsonProperty("price")
        private Price price;
        @JsonProperty("productSku")
        private String productSku;
        @JsonProperty("productName")
        private String productName;
        @JsonProperty("productCode")
        private String productCode;
        @JsonProperty("hsnCode")
        private String hsnCode;
        @JsonProperty("extendInfo")
        private ExtendInfo extendInfo;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        @JsonProperty("merchantGoodsId")
        public String getMerchantGoodsId() {
            return merchantGoodsId;
        }

        @JsonProperty("merchantGoodsId")
        public void setMerchantGoodsId(String merchantGoodsId) {
            this.merchantGoodsId = merchantGoodsId;
        }

        @JsonProperty("merchantShippingId")
        public String getMerchantShippingId() {
            return merchantShippingId;
        }

        @JsonProperty("merchantShippingId")
        public void setMerchantShippingId(String merchantShippingId) {
            this.merchantShippingId = merchantShippingId;
        }

        @JsonProperty("snapshotUrl")
        public String getSnapshotUrl() {
            return snapshotUrl;
        }

        @JsonProperty("snapshotUrl")
        public void setSnapshotUrl(String snapshotUrl) {
            this.snapshotUrl = snapshotUrl;
        }

        @JsonProperty("description")
        public String getDescription() {
            return description;
        }

        @JsonProperty("description")
        public void setDescription(String description) {
            this.description = description;
        }

        @JsonProperty("category")
        public String getCategory() {
            return category;
        }

        @JsonProperty("category")
        public void setCategory(String category) {
            this.category = category;
        }

        @JsonProperty("quantity")
        public String getQuantity() {
            return quantity;
        }

        @JsonProperty("quantity")
        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        @JsonProperty("unit")
        public String getUnit() {
            return unit;
        }

        @JsonProperty("unit")
        public void setUnit(String unit) {
            this.unit = unit;
        }

        @JsonProperty("price")
        public Price getPrice() {
            return price;
        }

        @JsonProperty("price")
        public void setPrice(Price price) {
            this.price = price;
        }

        @JsonProperty("productSku")
        public String getProductSku() {
            return productSku;
        }

        @JsonProperty("productSku")
        public void setProductSku(String productSku) {
            this.productSku = productSku;
        }

        @JsonProperty("productName")
        public String getProductName() {
            return productName;
        }

        @JsonProperty("productName")
        public void setProductName(String productName) {
            this.productName = productName;
        }

        @JsonProperty("productCode")
        public String getProductCode() {
            return productCode;
        }

        @JsonProperty("productCode")
        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        @JsonProperty("hsnCode")
        public String getHsnCode() {
            return hsnCode;
        }

        @JsonProperty("hsnCode")
        public void setHsnCode(String hsnCode) {
            this.hsnCode = hsnCode;
        }

        @JsonProperty("extendInfo")
        public ExtendInfo getExtendInfo() {
            return extendInfo;
        }

        @JsonProperty("extendInfo")
        public void setExtendInfo(ExtendInfo extendInfo) {
            this.extendInfo = extendInfo;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }


        public Good() {
            this.unit = "Kg";
            this.category = "travelling/subway";
            this.price = new Price();
            this.merchantShippingId = "564314314574327545";
            this.merchantGoodsId = "24525635625623";
            this.description = "Women Summer Dress New White Lace Sleeveless";
            this.snapshotUrl = "[http://snap.url.com ]";
            this.quantity = "3.2";
            this.extendInfo = new ExtendInfo();
        }
}
