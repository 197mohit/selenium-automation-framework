package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "totalInstantDiscount",
        "totalCashbackAmount",
        "offerBreakup",
        "totalTransactionAmount",
        "totalPaytmCashbackAmount",
        "cartOfferDetail",
        "cartDetails"
})
public class PaymentOffersApplied {

    @JsonProperty("totalInstantDiscount")
    private String totalInstantDiscount;
    @JsonProperty("totalCashbackAmount")
    private Object totalCashbackAmount;
    @JsonProperty("offerBreakup")
    private List<OfferBreakup> offerBreakup;
    @JsonProperty("totalTransactionAmount")
    private String totalTransactionAmount;

    @JsonProperty("totalPaytmCashbackAmount")
    private String totalPaytmCashbackAmount;
    @JsonProperty("cartOfferDetail")
    private CartOfferDetail cartOfferDetail;
    @JsonProperty("cartDetails")
    private CartDetails cartDetails;


    public PaymentOffersApplied(HashMap<String, Object> paymentOffersAppliedResponse){
        List<HashMap<String, Object>> offerbreakuplist = (List<HashMap<String, Object>>) paymentOffersAppliedResponse.get("offerBreakup");
        if (paymentOffersAppliedResponse.get("cartOfferDetail") != null ||  paymentOffersAppliedResponse.get("cartDetails") != null) {
               this.cartOfferDetail = new CartOfferDetail((HashMap<String, Object>) paymentOffersAppliedResponse.get("cartOfferDetail"));
                this.cartDetails = new CartDetails((HashMap<String, Object>) paymentOffersAppliedResponse.get("cartDetails"));
        }
        for(int i = 0; i < offerbreakuplist.size() ; i++){
            HashMap<String, Object> offer;
            List<OfferBreakup> offerBreakupList1 = new ArrayList<>();
            offer = offerbreakuplist.get(i);
            offerBreakupList1.add(new OfferBreakup(offer));
            this.offerBreakup = offerBreakupList1;
        }
        this.totalCashbackAmount = paymentOffersAppliedResponse.get("totalCashbackAmount");
        this.totalInstantDiscount = (String) paymentOffersAppliedResponse.get("totalInstantDiscount");
        this.totalTransactionAmount = (String) paymentOffersAppliedResponse.get("totalTransactionAmount");
        this.totalPaytmCashbackAmount= (String) paymentOffersAppliedResponse.get("totalPaytmCashbackAmount");
    }

    @JsonProperty("totalInstantDiscount")
    public String getTotalInstantDiscount() {
        return totalInstantDiscount;
    }

    @JsonProperty("totalInstantDiscount")
    public PaymentOffersApplied setTotalInstantDiscount(String totalInstantDiscount) {
        this.totalInstantDiscount = totalInstantDiscount;
        return this;
    }

    @JsonProperty("totalCashbackAmount")
    public Object getTotalCashbackAmount() {
        return totalCashbackAmount;
    }

    @JsonProperty("totalCashbackAmount")
    public PaymentOffersApplied setTotalCashbackAmount(Object totalCashbackAmount) {
        this.totalCashbackAmount = totalCashbackAmount;
        return this;
    }

    @JsonProperty("offerBreakup")
    public List<OfferBreakup> getOfferBreakupList() {
        return offerBreakup;
    }

    @JsonProperty("offerBreakup")
    public PaymentOffersApplied setOfferBreakupList(List<OfferBreakup> offerBreakup) {
        this.offerBreakup = offerBreakup;
        return this;
    }

    @JsonProperty("totalTransactionAmount")
    public String getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    @JsonProperty("totalTransactionAmount")
    public PaymentOffersApplied setTotalTransactionAmount(String totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
        return this;
    }

    public String getTotalPaytmCashbackAmount() {
        return totalPaytmCashbackAmount;
    }

    public PaymentOffersApplied setTotalPaytmCashbackAmount(String totalPaytmCashbackAmount) {
        this.totalPaytmCashbackAmount = totalPaytmCashbackAmount;
        return this;
    }

    public CartOfferDetail getCartOfferDetail() {
        return cartOfferDetail;
    }

    public PaymentOffersApplied setCartOfferDetail(CartOfferDetail cartOfferDetail) {
        this.cartOfferDetail = cartOfferDetail;
        return this;
    }

    public CartDetails getCartDetails() {
        return cartDetails;
    }

    public PaymentOffersApplied setCartDetails(CartDetails cartDetails) {
        this.cartDetails = cartDetails;
        return this;
    }

    public static class OfferBreakup{
        @JsonProperty("promocodeApplied")
        private String promocodeApplied;
        @JsonProperty("promotext")
        private String promotext;
        @JsonProperty("instantDiscount")
        private String instantDiscount;
        @JsonProperty("cashbackAmount")
        private Object cashbackAmount;
        @JsonProperty("payMethod")
        private String payMethod;
        @JsonProperty("promoVisibility")
        private String promoVisibility;
        @JsonProperty("responseCode")
        private String responseCode;
        @JsonProperty("transactionAmount")
        private String transactionAmount;
        @JsonProperty("paytmCashbackAmount")
        private String paytmCashbackAmount;

        public OfferBreakup(HashMap<String, Object> offer){
            this.promocodeApplied = offer.get("promocodeApplied").toString();
            this.promotext = offer.get("promotext").toString();
            this.instantDiscount = (String) offer.get("instantDiscount");
            if(offer.get("cashbackAmount") != null) {
                this.cashbackAmount = offer.get("cashbackAmount").toString();
            }
            else
                this.cashbackAmount = null;
            this.payMethod = offer.get("payMethod").toString();
            this.promoVisibility = offer.get("promoVisibility").toString();
            if(offer.get("responseCode")!=null) {
                this.responseCode = offer.get("responseCode").toString();
            }else {
                this.responseCode=null;
            }
            this.transactionAmount = offer.get("transactionAmount").toString();
            if(offer.get("paytmCashbackAmount")!=null) {
                this.paytmCashbackAmount = offer.get("paytmCashbackAmount").toString();
            }else {
                this.paytmCashbackAmount = null;
            }
        }

        @JsonProperty("promocodeApplied")
        public String getPromocodeApplied() {
            return promocodeApplied;
        }

        @JsonProperty("promocodeApplied")
        public OfferBreakup setPromocodeApplied(String promocodeApplied) {
            this.promocodeApplied = promocodeApplied;
            return this;
        }

        @JsonProperty("promotext")
        public String getPromotext() {
            return promotext;
        }

        @JsonProperty("promotext")
        public OfferBreakup setPromotext(String promotext) {
            this.promotext = promotext;
            return this;
        }

        @JsonProperty("instantDiscount")
        public String getInstantDiscount() {
            return instantDiscount;
        }

        @JsonProperty("instantDiscount")
        public OfferBreakup setInstantDiscount(String instantDiscount) {
            this.instantDiscount = instantDiscount;
            return this;
        }

        @JsonProperty("cashbackAmount")
        public Object getCashbackAmount() {
            return cashbackAmount;
        }

        @JsonProperty("cashbackAmount")
        public OfferBreakup setCashbackAmount(Object cashbackAmount) {
            this.cashbackAmount = cashbackAmount;
            return this;
        }

        @JsonProperty("payMethod")
        public String getPayMethod() {
            return payMethod;
        }

        @JsonProperty("payMethod")
        public OfferBreakup setPayMethod(String payMethod) {
            this.payMethod = payMethod;
            return this;
        }

        @JsonProperty("promoVisibility")
        public String getPromoVisibility() {
            return promoVisibility;
        }

        @JsonProperty("promoVisibility")
        public OfferBreakup setPromoVisibility(String promoVisibility) {
            this.promoVisibility = promoVisibility;
            return this;
        }

        @JsonProperty("responseCode")
        public String getResponseCode() {
            return responseCode;
        }

        @JsonProperty("responseCode")
        public OfferBreakup setResponseCode(String responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        @JsonProperty("transactionAmount")
        public String getTransactionAmount() {
            return transactionAmount;
        }

        @JsonProperty("transactionAmount")
        public OfferBreakup setTransactionAmount(String transactionAmount) {
            this.transactionAmount = transactionAmount;
            return this;
        }

        @JsonProperty("paytmCashbackAmount")
        public String getPaytmCashbackAmount() {
            return paytmCashbackAmount;
        }

        @JsonProperty("paytmCashbackAmount")
        public OfferBreakup setPaytmCashbackAmount(String paytmCashbackAmount) {
            this.paytmCashbackAmount = paytmCashbackAmount;
            return this;
        }
    }
    public class CartDetails {

        @JsonProperty("items")
        private List<Item__1> items;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public CartDetails(HashMap<String, Object> cartDetails){
            List<HashMap<String, Object>> itemsList = (List<HashMap<String, Object>>) cartDetails.get("items");
            List<Item__1> items1 = new ArrayList<>();
            for(int i = 0; i < itemsList.size() ; i++){
                HashMap<String, Object> item;
                item = itemsList.get(i);
                items1.add(new Item__1(item));
            }
            this.items = items1;
        } 
        
        @JsonProperty("items")
        public List<Item__1> getItems() {
            return items;
        }

        @JsonProperty("items")
        public CartDetails setItems(List<Item__1> items) {
            this.items = items;
            return this;
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
    public class CartOfferDetail {

        @JsonProperty("itemOffers")
        private List<ItemOffer> itemOffers;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public CartOfferDetail(HashMap<String, Object> cartOfferDetail) {
            List<HashMap<String , Object>> itemOffers1 = (List<HashMap<String, Object>>) cartOfferDetail.get("itemOffers");
            for(int i = 0; i <itemOffers1.size() ; i++){
                HashMap<String, Object> itemOffer;
                List<ItemOffer> itemOffers = new ArrayList<>();
                itemOffer=itemOffers1.get(i);
                itemOffers.add(new ItemOffer(itemOffer));
                this.itemOffers = itemOffers;
            }
        }

        @JsonProperty("itemOffers")
        public List<ItemOffer> getItemOffers() {
            return itemOffers;
        }

        @JsonProperty("itemOffers")
        public void setItemOffers(List<ItemOffer> itemOffers) {
            this.itemOffers = itemOffers;
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
    public class ItemOffer {

        @JsonProperty("promocode")
        private String promocode;
        @JsonProperty("items")
        private List<Item> items;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public ItemOffer(HashMap<String, Object> itemOffer) {
            this.promocode = itemOffer.get("promocode").toString();
            List<HashMap<String, Object>> itemsList = (List<HashMap<String, Object>>) itemOffer.get("items");
            List<Item> items1 = new ArrayList<>();
            for(int i = 0; i < itemsList.size() ; i++){
                HashMap<String, Object> item;
                item = itemsList.get(i);
                items1.add(new Item(item));
            }
            this.items = items1;
        }

        @JsonProperty("promocode")
        public String getPromocode() {
            return promocode;
        }

        @JsonProperty("promocode")
        public void setPromocode(String promocode) {
            this.promocode = promocode;
        }

        @JsonProperty("items")
        public List<Item> getItems() {
            return items;
        }

        @JsonProperty("items")
        public void setItems(List<Item> items) {
            this.items = items;
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
    public class Item {

        @JsonProperty("id")
        private String id;
        @JsonProperty("metaData")
        private List<MetaDatum> metaData;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public Item(HashMap<String, Object> item) {
            this.id = item.get("id").toString();
            List<HashMap<String, Object>> metaDataList = (List<HashMap<String, Object>>) item.get("metaData");
            List<MetaDatum> metaData1 = new ArrayList<>();
            for(int i = 0; i < metaDataList.size() ; i++){
                HashMap<String, Object> metaData;
                metaData = metaDataList.get(i);
                metaData1.add(new MetaDatum(metaData));
            }
            this.metaData = metaData1;
        }

        @JsonProperty("id")
        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        @JsonProperty("metaData")
        public List<MetaDatum> getMetaData() {
            return metaData;
        }

        @JsonProperty("metaData")
        public void setMetaData(List<MetaDatum> metaData) {
            this.metaData = metaData;
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

    public class Item__1 {

        @JsonProperty("id")
        private String id;
        @JsonProperty("promocode")
        private Object promocode;
        @JsonProperty("amount")
        private Integer amount;
        @JsonProperty("productDetail")
        private ProductDetail productDetail;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public Item__1(HashMap<String, Object> item) {
            this.id = item.get("id").toString();
            this.promocode = item.get("promocode");
            this.amount = (Integer) item.get("amount");
            this.productDetail = new ProductDetail((HashMap<String, Object>) item.get("productDetail"));
        }

        @JsonProperty("id")
        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        @JsonProperty("promocode")
        public Object getPromocode() {
            return promocode;
        }

        @JsonProperty("promocode")
        public void setPromocode(Object promocode) {
            this.promocode = promocode;
        }

        @JsonProperty("amount")
        public Integer getAmount() {
            return amount;
        }

        @JsonProperty("amount")
        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        @JsonProperty("productDetail")
        public ProductDetail getProductDetail() {
            return productDetail;
        }

        @JsonProperty("productDetail")
        public void setProductDetail(ProductDetail productDetail) {
            this.productDetail = productDetail;
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
    public class MetaDatum {

        @JsonProperty("siteId")
        private String siteId;
        @JsonProperty("userId")
        private String userId;
        @JsonProperty("promocodeId")
        private String promocodeId;
        @JsonProperty("promocode")
        private String promocode;
        @JsonProperty("campaign")
        private Object campaign;
        @JsonProperty("redemptionType")
        private String redemptionType;
        @JsonProperty("fulfillmentStatus")
        private Object fulfillmentStatus;
        @JsonProperty("status")
        private Integer status;
        @JsonProperty("fraud1")
        private Object fraud1;
        @JsonProperty("flags")
        private Object flags;
        @JsonProperty("promoGratificationData")
        private PromoGratificationData promoGratificationData;
        @JsonProperty("amount")
        private Integer amount;
        @JsonProperty("customText")
        private Object customText;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public MetaDatum(HashMap<String, Object> metaData) {
            this.siteId = metaData.get("siteId").toString();
            this.userId = metaData.get("userId").toString();
            this.promocodeId = metaData.get("promocodeId").toString();
            this.promocode = metaData.get("promocode").toString();
            this.campaign = metaData.get("campaign");
            this.redemptionType = metaData.get("redemptionType").toString();
            this.fulfillmentStatus = metaData.get("fulfillmentStatus");
            this.status = (Integer) metaData.get("status");
            this.fraud1 = metaData.get("fraud1");
            this.flags = metaData.get("flags");
            this.promoGratificationData = new PromoGratificationData((HashMap<String, Object>) metaData.get("promoGratificationData"));
            this.amount = (Integer) metaData.get("amount");
            this.customText = metaData.get("customText");
        }

        @JsonProperty("siteId")
        public String getSiteId() {
            return siteId;
        }

        @JsonProperty("siteId")
        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        @JsonProperty("userId")
        public String getUserId() {
            return userId;
        }

        @JsonProperty("userId")
        public void setUserId(String userId) {
            this.userId = userId;
        }

        @JsonProperty("promocodeId")
        public String getPromocodeId() {
            return promocodeId;
        }

        @JsonProperty("promocodeId")
        public void setPromocodeId(String promocodeId) {
            this.promocodeId = promocodeId;
        }

        @JsonProperty("promocode")
        public String getPromocode() {
            return promocode;
        }

        @JsonProperty("promocode")
        public void setPromocode(String promocode) {
            this.promocode = promocode;
        }

        @JsonProperty("campaign")
        public Object getCampaign() {
            return campaign;
        }

        @JsonProperty("campaign")
        public void setCampaign(Object campaign) {
            this.campaign = campaign;
        }

        @JsonProperty("redemptionType")
        public String getRedemptionType() {
            return redemptionType;
        }

        @JsonProperty("redemptionType")
        public void setRedemptionType(String redemptionType) {
            this.redemptionType = redemptionType;
        }

        @JsonProperty("fulfillmentStatus")
        public Object getFulfillmentStatus() {
            return fulfillmentStatus;
        }

        @JsonProperty("fulfillmentStatus")
        public void setFulfillmentStatus(Object fulfillmentStatus) {
            this.fulfillmentStatus = fulfillmentStatus;
        }

        @JsonProperty("status")
        public Integer getStatus() {
            return status;
        }

        @JsonProperty("status")
        public void setStatus(Integer status) {
            this.status = status;
        }

        @JsonProperty("fraud1")
        public Object getFraud1() {
            return fraud1;
        }

        @JsonProperty("fraud1")
        public void setFraud1(Object fraud1) {
            this.fraud1 = fraud1;
        }

        @JsonProperty("flags")
        public Object getFlags() {
            return flags;
        }

        @JsonProperty("flags")
        public void setFlags(Object flags) {
            this.flags = flags;
        }

        @JsonProperty("promoGratificationData")
        public PromoGratificationData getPromoGratificationData() {
            return promoGratificationData;
        }

        @JsonProperty("promoGratificationData")
        public void setPromoGratificationData(PromoGratificationData promoGratificationData) {
            this.promoGratificationData = promoGratificationData;
        }

        @JsonProperty("amount")
        public Integer getAmount() {
            return amount;
        }

        @JsonProperty("amount")
        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        @JsonProperty("customText")
        public Object getCustomText() {
            return customText;
        }

        @JsonProperty("customText")
        public void setCustomText(Object customText) {
            this.customText = customText;
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
    public class ProductDetail {

        @JsonProperty("id")
        private String id;
        @JsonProperty("merchantId")
        private Object merchantId;
        @JsonProperty("verticalId")
        private Object verticalId;
        @JsonProperty("categoryIds")
        private List<String> categoryIds;
        @JsonProperty("brandId")
        private String brandId;
        @JsonProperty("model")
        private Object model;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public ProductDetail(HashMap<String, Object> productDetail) {
            this.id = productDetail.get("id").toString();
            this.merchantId = productDetail.get("merchantId");
            this.verticalId = productDetail.get("verticalId");
            this.categoryIds = (List<String>) productDetail.get("categoryIds");
            this.brandId = productDetail.get("brandId").toString();
            this.model = productDetail.get("model");
        }

        @JsonProperty("id")
        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        @JsonProperty("merchantId")
        public Object getMerchantId() {
            return merchantId;
        }

        @JsonProperty("merchantId")
        public void setMerchantId(Object merchantId) {
            this.merchantId = merchantId;
        }

        @JsonProperty("verticalId")
        public Object getVerticalId() {
            return verticalId;
        }

        @JsonProperty("verticalId")
        public void setVerticalId(Object verticalId) {
            this.verticalId = verticalId;
        }

        @JsonProperty("categoryIds")
        public List<String> getCategoryIds() {
            return categoryIds;
        }

        @JsonProperty("categoryIds")
        public void setCategoryIds(List<String> categoryIds) {
            this.categoryIds = categoryIds;
        }

        @JsonProperty("brandId")
        public String getBrandId() {
            return brandId;
        }

        @JsonProperty("brandId")
        public void setBrandId(String brandId) {
            this.brandId = brandId;
        }

        @JsonProperty("model")
        public Object getModel() {
            return model;
        }

        @JsonProperty("model")
        public void setModel(Object model) {
            this.model = model;
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
    public class PromoGratificationData {

        @JsonProperty("actionType")
        private String actionType;
        @JsonProperty("subRedemptionType")
        private String subRedemptionType;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        public PromoGratificationData(HashMap<String, Object> promoGratificationData) {
            this.actionType = promoGratificationData.get("actionType").toString();
            this.subRedemptionType = promoGratificationData.get("subRedemptionType").toString();
        }

        @JsonProperty("actionType")
        public String getActionType() {
            return actionType;
        }

        @JsonProperty("actionType")
        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        @JsonProperty("subRedemptionType")
        public String getSubRedemptionType() {
            return subRedemptionType;
        }

        @JsonProperty("subRedemptionType")
        public void setSubRedemptionType(String subRedemptionType) {
            this.subRedemptionType = subRedemptionType;
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

    }