package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;

import java.util.Arrays;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "customerId",
        "planId",
        "subventionAmount",
        "offerDetails",
        "items"
})
public class SimplifiedSubvention {

    @JsonProperty("customerId")
    private String customerId;
    @JsonProperty("planId")
    private String planId;
    @JsonProperty("subventionAmount")
    private String subventionAmount = null;
    @JsonProperty("offerDetails")
    private OfferDetails offerDetails = null;
    @JsonProperty("items")
    private List<Item> items = null;
    @JsonProperty("selectPlanOnCashierPage")
    private Boolean selectPlanOnCashierPage = false;

    public SimplifiedSubvention(String customerId, String planId, String subventionAmount, OfferDetails offerDetails){
        this.customerId = customerId;
        this.planId = planId;
        this.subventionAmount = subventionAmount;
        this.offerDetails = offerDetails;
    }

    public SimplifiedSubvention(String customerId, String planId, List<Item> items){
        this.customerId = customerId;
        this.planId = planId;
        this.items = items;
    }

    public SimplifiedSubvention(String customerId, String subventionAmount, Boolean selectPlanOnCashierPage){
        this.customerId= customerId;
        this.subventionAmount= subventionAmount;
        this.selectPlanOnCashierPage= selectPlanOnCashierPage;
    }


    @JsonProperty("customerId")
    public String getCustomerId() {
        return customerId;
    }

    @JsonProperty("customerId")
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @JsonProperty("planId")
    public String getPlanId() {
        return planId;
    }

    @JsonProperty("planId")
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    @JsonProperty("subventionAmount")
    public String getSubventionAmount() {
        return subventionAmount;
    }

    @JsonProperty("subventionAmount")
    public void setSubventionAmount(String subventionAmount) {
        this.subventionAmount = subventionAmount;
    }

    @JsonProperty("offerDetails")
    public OfferDetails getOfferDetails() {
        return offerDetails;
    }

    @JsonProperty("offerDetails")
    public void setOfferDetails(OfferDetails offerDetails) {
        this.offerDetails = offerDetails;
    }

    @JsonProperty("selectPlanOnCashierPage")
    public Boolean getselectPlanOnCashierPage() { return  selectPlanOnCashierPage; }

    @JsonProperty("selectPlanOnCashierPage")
    public void setselectPlanOnCashierPage(boolean selectPlanOnCashierPage) { this.selectPlanOnCashierPage = selectPlanOnCashierPage; }

    public static class Item {

        @JsonProperty("id")
        private String id;
        @JsonProperty("productId")
        private String productId;
        @JsonProperty("brandId")
        private String brandId;
        @JsonProperty("categoryList")
        private List<String> categoryList = null;
        @JsonProperty("quantity")
        private String quantity;
        @JsonProperty("price")
        private String price;
        @JsonProperty("verticalId")
        private String verticalId;
        @JsonProperty("isEmiEnabled")
        private Boolean isEmiEnabled;
        @JsonProperty("isStandardEmi")
        private Boolean isStandardEmi;
        @JsonProperty("offerDetails")
        private OfferDetails offerDetails;
        @JsonProperty("isPhysical")
        private Boolean isPhysical;
        @JsonProperty("discoverability")
        private String discoverability;
        @JsonProperty("model")
        private String model;
        @JsonProperty("merchantId")
        private String merchantId;

        public Item(String id, String productId, String brandId,List<String> categoryList, String quantity,
        String price, String verticalId, Boolean isEmiEnabled,Boolean isStandardEmi, OfferDetails offerDetails){
            this.id = id;
            this.productId = productId;
            this.model = productId;
            this.brandId = brandId;
            this.categoryList = categoryList;
            this.quantity = quantity;
            this.price = price;
            this.verticalId = verticalId;
            this.isEmiEnabled = isEmiEnabled;
            this.isStandardEmi= isStandardEmi;
            this.offerDetails = offerDetails;
        }

        public Item(String id, String productId, String brandId,List<String> categoryList, String quantity, String price){
            this.quantity = quantity;
            this.productId = productId;
            this.model = productId;
            this.price = price;
            this.brandId = brandId;
            this.categoryList = categoryList;
            this.id = id;
        }

        public Item() {
            this.id = "1234";
            this.productId = "27902";
            this.brandId = "table";
            this.categoryList = Arrays.asList("66781");
            this.price = "5";
            this.quantity = "1";
            this.verticalId = "";
            this.isEmiEnabled = true;
            this.isStandardEmi = false;
            this.model = "27902";
            this.offerDetails = new OfferDetails().setOfferId("123456");
        }
        public Item(String id, String productId, String brandId,List<String> categoryList, String quantity,
                    String price, String verticalId, Boolean isEmiEnabled,Boolean isStandardEmi, OfferDetails offerDetails,Boolean isPhysical,String discoverability,String model,String merchantId){
            this.id = id;
            this.productId = productId;
            this.brandId = brandId;
            this.categoryList = categoryList;
            this.quantity = quantity;
            this.price = price;
            this.verticalId = verticalId;
            this.isEmiEnabled = isEmiEnabled;
            this.isStandardEmi= isStandardEmi;
            this.offerDetails = offerDetails;
            this.isPhysical = isPhysical;
            this.discoverability = discoverability;
            this.model = model;
            this.merchantId = merchantId;
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

        @JsonProperty("categoryList")
        public List<String> getCategoryList() {
            return categoryList;
        }

        @JsonProperty("categoryList")
        public void setCategoryList(List<String> categoryList) {
            this.categoryList = categoryList;
        }

        @JsonProperty("quantity")
        public String getQuantity() {
            return quantity;
        }

        @JsonProperty("quantity")
        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        @JsonProperty("price")
        public String getPrice() {
            return price;
        }

        @JsonProperty("price")
        public void setPrice(String price) {
            this.price = price;
        }

        @JsonProperty("verticalId")
        public String getVerticalId() {
            return verticalId;
        }

        @JsonProperty("verticalId")
        public void setVerticalId(String verticalId) {
            this.verticalId = verticalId;
        }

        @JsonProperty("isStandardEmi")
        public Boolean getIsStandardEmi() {
            return isStandardEmi;
        }

        @JsonProperty("isStandardEmi")
        public void setIsStandardEmi(Boolean isStandardEmi) {
            this.isStandardEmi = isStandardEmi;
        }

        @JsonProperty("isEmiEnabled")
        public Boolean getIsEmiEnabled() {
            return isEmiEnabled;
        }

        @JsonProperty("isEmiEnabled")
        public void setIsEmiEnabled(Boolean isEmiEnabled) {
            this.isEmiEnabled = isEmiEnabled;
        }

        @JsonProperty("offerDetails")
        public OfferDetails getOfferDetails() {
            return offerDetails;
        }

        @JsonProperty("offerDetails")
        public void setOfferDetails(OfferDetails offerDetails) {
            this.offerDetails = offerDetails;
        }

        @JsonProperty("isPhysical")
        private Boolean getisPhysical() { return isPhysical; };

        @JsonProperty("isPhysical")
        private void getisPhysical(Boolean isPhysical) { this.isPhysical = isPhysical; };

        @JsonProperty("discoverability")
        private String getdiscoverability() { return  discoverability; };

        @JsonProperty("discoverability")
        private void getdiscoverability(String discoverability) { this.discoverability = discoverability; };

        @JsonProperty("model")
        private String getModel() { return  model; };

        @JsonProperty("model")
        public void setModel(String model) { this.model = model; };

        @JsonProperty("merchantId")
        private String getmerchantId() { return merchantId; };

        @JsonProperty("merchantId")
        private void setmerchantId(String merchantId) { this.merchantId = merchantId; };

    }

}