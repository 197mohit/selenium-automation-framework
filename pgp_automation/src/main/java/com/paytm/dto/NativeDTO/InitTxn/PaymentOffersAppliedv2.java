package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "verificationCode",
        "promoCode",
        "promoContext",
        "savings",

})
public class PaymentOffersAppliedv2 {

    @JsonProperty("verificationCode")
    private String verificationCode;
    @JsonProperty("promoCode")
    private Object promoCode;
    @JsonProperty("savings")
    private List<savings> savings;
    @JsonProperty("promoContext")
    private String promoContext;

    public PaymentOffersAppliedv2(HashMap<String, Object> paymentOffersAppliedResponse){
        List<HashMap<String, Object>> offerbreakuplist = (List<HashMap<String, Object>>) paymentOffersAppliedResponse.get("savings");
        for(int i = 0; i < offerbreakuplist.size() ; i++){
            HashMap<String, Object> offer;
            List<savings> offerBreakupList1 = new ArrayList<>();
            offer = offerbreakuplist.get(i);
            offerBreakupList1.add(new savings(offer));
            this.savings = offerBreakupList1;
        }
        this.verificationCode = (String) paymentOffersAppliedResponse.get("verificationCode");
        this.promoCode = (String) paymentOffersAppliedResponse.get("promoCode");

    }

    @JsonProperty("verificationCode")
    public String getverificationCode() {
        return verificationCode;
    }

    @JsonProperty("verificationCode")
    public PaymentOffersAppliedv2 setverificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
        return this;
    }

    @JsonProperty("promoCode")
    public Object getpromoCode() {
        return promoCode;
    }

    @JsonProperty("promoCode")
    public PaymentOffersAppliedv2 setpromoCode(Object promoCode) {
        this.promoCode = promoCode;
        return this;
    }

    @JsonProperty("savings")
    public List<savings> getOfferBreakupList() {
        return savings;
    }

    @JsonProperty("savings")
    public PaymentOffersAppliedv2 setOfferBreakupList(List<savings> offerBreakup) {
        this.savings = savings;
        return this;
    }

    public static class savings{
        @JsonProperty("savings")
        private String savings;
        @JsonProperty("redemptionType")
        private String redemptionType;

        public savings(HashMap<String, Object> offer){
            this.savings = offer.get("savings").toString();
            this.redemptionType = offer.get("redemptionType").toString();

        }

        @JsonProperty("savings")
        public String getsavings() {
            return savings;
        }

        @JsonProperty("savings")
        public savings setsavings(String savings) {
            this.savings = savings;
            return this;
        }

        @JsonProperty("redemptionType")
        public String getredemptionType() {
            return redemptionType;
        }

        @JsonProperty("redemptionType")
        public savings setPromotext(String redemptionType) {
            this.redemptionType = redemptionType;
            return this;
        }


    }

}