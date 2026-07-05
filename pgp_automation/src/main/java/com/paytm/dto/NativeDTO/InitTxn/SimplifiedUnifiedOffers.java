package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SimplifiedUnifiedOffers {

    @JsonProperty("promoDetails")
    private PromoDetails promoDetails;
    @JsonProperty("subventionDetails")
    private SubventionDetails subventionDetails;
    @JsonProperty("items")
    private List<Items> items;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();
    @JsonProperty("deviceIMEI")
    private String deviceIMEI;
    @JsonProperty("source")
    private String source;

    public SimplifiedUnifiedOffers(PromoDetails promoDetails, SubventionDetails subventionDetails, List<Items> items){
        this.promoDetails=promoDetails;
        this.subventionDetails=subventionDetails;
        this.items=items;
    }
    public SimplifiedUnifiedOffers(PromoDetails promoDetails){
        this.promoDetails=promoDetails;
    }
    public SimplifiedUnifiedOffers(SubventionDetails subventionDetails){this.subventionDetails=subventionDetails;

    }
    public SimplifiedUnifiedOffers(SubventionDetails subventionDetails, PromoDetails promoDetails){
        this.subventionDetails=subventionDetails;
        this.promoDetails=promoDetails;
    }
    public SimplifiedUnifiedOffers(SubventionDetails subventionDetails,List<Items> items){
        this.subventionDetails=subventionDetails;
        this.items=items;
    }
    public SimplifiedUnifiedOffers(PromoDetails promoDetails,List<Items> items){
        this.promoDetails=promoDetails;
        this.items=items;
    }


    @JsonProperty("promoDetails")
    public PromoDetails getPromoDetails() {
        return promoDetails;
    }

    @JsonProperty("promoDetails")
    public void setPromoDetails(PromoDetails promoDetails) {
        this.promoDetails = promoDetails;
    }

    @JsonProperty("subventionDetails")
    public SubventionDetails getSubventionDetails() {
        return subventionDetails;
    }

    @JsonProperty("subventionDetails")
    public void setSubventionDetails(SubventionDetails subventionDetails) {
        this.subventionDetails = subventionDetails;
    }

    @JsonProperty("items")
    public List<Items> getItem() {
        return items;
    }

    @JsonProperty("items")
    public void setItem(List<Items> items) {
        this.items = items;
    }
    @JsonProperty("deviceIMEI")
    public String getDeviceIMEI() {
        return deviceIMEI;
    }
    @JsonProperty("deviceIMEI")
    public void setDeviceIMEI(String deviceIMEI) {
        this.deviceIMEI = deviceIMEI;
    }
    @JsonProperty("source")
    public String getSource() {
        return source;
    }
    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    public static class PromoDetails {

        @JsonProperty("promoCode")
        private List<String> promoCode;
        @JsonProperty("applyAvailablePromo")
        private String applyAvailablePromo="";
        @JsonProperty("isAmountBasedBankOffer")
        private String isAmountBasedBankOffer="";
        @JsonProperty("offerId")
        private String offerId="";
        @JsonProperty("validatePromo")
        private String validatePromo="";

        @JsonProperty("promoAmount")
        private String promoAmount="";

        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public PromoDetails(List<String> promoCode, String applyAvailablePromo, String validatePromo, String isAmountBasedBankOffer, String offerId ){
            this.promoCode=promoCode;
            this.applyAvailablePromo=applyAvailablePromo;
            this.validatePromo=validatePromo;
            this.isAmountBasedBankOffer=isAmountBasedBankOffer;
            this.offerId=offerId;
        }
        public PromoDetails(List<String> promoCode, String applyAvailablePromo, String validatePromo, String isAmountBasedBankOffer, String offerId , String promoAmount){
            this.promoCode=promoCode;
            this.applyAvailablePromo=applyAvailablePromo;
            this.validatePromo=validatePromo;
            this.isAmountBasedBankOffer=isAmountBasedBankOffer;
            this.offerId=offerId;
            this.promoAmount=promoAmount;
        }

        @JsonProperty("promoCode")
        public List<String> getPromoCode() {
            return promoCode;
        }

        @JsonProperty("promoCode")
        public void setPromoCode(List<String> promoCode) {
            this.promoCode = promoCode;
        }
        @JsonProperty("ValidatePromo")
        public String getValidatePromo() {
            return validatePromo;
        }

        @JsonProperty("ValidatePromo")
        public void setValidatePromo(String validatePromo) {
            this.validatePromo = validatePromo;
        }

        @JsonProperty("applyAvailablePromo")
        public String getApplyAvailablePromo() {
            return applyAvailablePromo;
        }

        @JsonProperty("applyAvailablePromo")
        public void setApplyAvailablePromo(String applyAvailablePromo) {
            this.applyAvailablePromo = applyAvailablePromo;
        }

        @JsonProperty("isAmountBasedBankOffer")
        public String getIsAmountBasedBankOffer() {
            return isAmountBasedBankOffer;
        }

        @JsonProperty("isAmountBasedBankOffer")
        public void setIsAmountBasedBankOffer(String isAmountBasedBankOffer) {
            this.isAmountBasedBankOffer = isAmountBasedBankOffer;
        }

        @JsonProperty("offerId")
        public String getOfferId() {
            return offerId;
        }

        @JsonProperty("offerId")
        public void setOfferId(String offerId) {
            this.offerId = offerId;
        }
        @JsonProperty("promoAmount")
        public String getPromoAmount() {
            return promoAmount;
        }
        @JsonProperty("promoAmount")
        public void setPromoAmount(String promoAmount) {
            this.promoAmount = promoAmount;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }


    public static class SubventionDetails {

        @JsonProperty("pgPlanId")
        private String pgPlanId;
        @JsonProperty("isAmountBasedSubvention")
        private String isAmountBasedSubvention;
        @JsonProperty("subventionAmount")
        private String subventionAmount;
        @JsonProperty("offerId")
        private String offerId;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public SubventionDetails(String isAmountBasedSubvention, String subventionAmount, String offerId,String pgPlanId){
            this.isAmountBasedSubvention=isAmountBasedSubvention;
            this.subventionAmount=subventionAmount;
            this.offerId=offerId;
            this.pgPlanId=pgPlanId;
        }

        @JsonProperty("pgPlanId")
        public String getPgPlanId() {
            return pgPlanId;
        }

        @JsonProperty("pgPlanId")
        public void setPgPlanId(String pgPlanId) {
            this.pgPlanId = pgPlanId;
        }

        @JsonProperty("isAmountBasedSubvention")
        public String getIsAmountBasedSubvention() {
            return isAmountBasedSubvention;
        }

        @JsonProperty("isAmountBasedSubvention")
        public void setIsAmountBasedSubvention(String isAmountBasedSubvention) {
            this.isAmountBasedSubvention = isAmountBasedSubvention;
        }

        @JsonProperty("subventionAmount")
        public String getSubventionAmount() {
            return subventionAmount;
        }

        @JsonProperty("subventionAmount")
        public void setSubventionAmount(String subventionAmount) {
            this.subventionAmount = subventionAmount;
        }

        @JsonProperty("offerId")
        public String getOfferId() {
            return offerId;
        }

        @JsonProperty("offerId")
        public void setOfferId(String offerId) {
            this.offerId = offerId;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Items {

        @JsonProperty("id")
        private String id;
        @JsonProperty("productId")
        private String productId;
        @JsonProperty("brandId")
        private String brandId;
        @JsonProperty("price")
        private Double price;
        @JsonProperty("categoryId")
        private String categoryId;
        @JsonProperty("offerDetails")
        private OfferDetails offerDetails;
        @JsonProperty("model")
        private String model;
        @JsonProperty("discoverability")
        private String discoverability;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();


        public  Items(String id, String productId, String brandId, Double price,String categoryId){
            this.id=id;
            this.productId=productId;
            this.brandId=brandId;
            this.categoryId=categoryId;
            this.price=price;
        }
        public  Items(String id, String productId, String brandId, Double price,String categoryId,String emiOfferDetails, List<OfferDetails.BankOfferDetails> bankOfferDetails){
            this.id=id;
            this.productId=productId;
            this.brandId=brandId;
            this.categoryId=categoryId;
            this.price=price;
            this.offerDetails= new OfferDetails(emiOfferDetails,bankOfferDetails);
        }
        public  Items(String id, String productId, String brandId,String categoryId,String model,String discoverability,Double price){
            this.id=id;
            this.productId=productId;
            this.brandId=brandId;
            this.categoryId=categoryId;
            this.price=price;
            this.model=model;
            this.discoverability=discoverability;
        }

        @JsonProperty("id")
        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
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

        @JsonProperty("price")
        public Double getPrice() {
            return price;
        }

        @JsonProperty("price")
        public void setPrice(Double price) {
            this.price = price;
        }
        @JsonProperty("offerDetails")
        public OfferDetails getOfferDetails() {
            return offerDetails;
        }

        @JsonProperty("offerDetails")
        public void setOfferDetails(OfferDetails offerDetails) {
            this.offerDetails = offerDetails;
        }
        @JsonProperty("model")
        public String getModel() {
            return model;
        }

        @JsonProperty("model")
        public void setModel(String model) {
            this.model = model;
        }

        @JsonProperty("discoverability")
        public String getDiscoverability() {
            return discoverability;
        }

        @JsonProperty("discoverability")
        public void setDiscoverability(String discoverability) {
            this.discoverability = discoverability;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OfferDetails {
        @JsonProperty("emiOfferDetails")
        private EmiOfferDetails emiOfferDetails;
        @JsonProperty("bankOfferDetails")
        private List<BankOfferDetails> bankOfferDetails;

        public OfferDetails(String emiOfferDetails, List<BankOfferDetails> bankOfferDetails) {
            if (emiOfferDetails != null) {
                this.emiOfferDetails = new EmiOfferDetails(emiOfferDetails);
            }
            if (bankOfferDetails != null) {
                this.bankOfferDetails = bankOfferDetails;
            }
        }

        @JsonProperty("emiOfferDetails")
        public EmiOfferDetails getEmiOfferDetails() {
            return emiOfferDetails;
        }

        @JsonProperty("emiOfferDetails")
        public void setEmiOfferDetails(EmiOfferDetails emiOfferDetails) {
            this.emiOfferDetails = emiOfferDetails;
        }

        @JsonProperty("bankOfferDetails")
        public List<BankOfferDetails> getBankOfferDetails() {
            return bankOfferDetails;
        }

        @JsonProperty("bankOfferDetails")
        public void setBankOfferDetails(List<BankOfferDetails> bankOfferDetails) {
            this.bankOfferDetails = bankOfferDetails;
        }


        public static class EmiOfferDetails {
            @JsonProperty("offerId")
            private String offerId;

            public EmiOfferDetails(String offerId) {
                this.offerId = offerId;
            }
            @JsonProperty("offerId")
            public String getOfferId() {
                return offerId;
            }

            @JsonProperty("offerId")
            public void setOfferId(String offerId) {
                this.offerId = offerId;
            }
        }


        public static class BankOfferDetails {
            @JsonProperty("offerId")
            private String offerId;

            public BankOfferDetails(String offerId) {
                this.offerId = offerId;
            }
            @JsonProperty("offerId")
            public String getOfferId() {
                return offerId;
            }

            @JsonProperty("offerId")
            public void setOfferId(String offerId) {
                this.offerId = offerId;
            }
        }

    }
}