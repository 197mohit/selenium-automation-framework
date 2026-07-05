package com.paytm.dto.PromoDTO.ItemLevelPromoDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "transactionAmount",
            "payMethod",
            "bankCode",
            "cardNo",
            "savedCardId"

    })
    public class PaymentOptions
    {

        public PaymentOptions(String transactionAmount, String payMethod, String bankCode, String cardNo, String savedCardId, String vpa )
        {
            this.transactionAmount = transactionAmount;
            this.payMethod = payMethod;
            this.bankCode = bankCode;
            this.cardNo = cardNo;
            this.savedCardId = savedCardId;
            this.vpa = vpa;

        }

        @JsonProperty("transactionAmount")
        private String transactionAmount;
        @JsonProperty("payMethod")
        private String payMethod;
        @JsonProperty("bankCode")
        private String bankCode;
        @JsonProperty("cardNo")
        private String cardNo;
        @JsonProperty("savedCardId")
        private String savedCardId;
        @JsonProperty("vpa")
        private String vpa;

        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();


        @JsonProperty("transactionAmount")
        public String getTransactionAmount()
        {
            return transactionAmount;
        }

        @JsonProperty("transactionAmount")
        public PaymentOptions setTransactionAmount(String transactionAmount)
        {
            this.transactionAmount = transactionAmount;
            return this;
        }

        @JsonProperty("payMethod")
        public String getPayMethod()
        {
            return payMethod;
        }

        @JsonProperty("payMethod")
        public PaymentOptions setPayMethod(String payMethod)
        {
            this.payMethod = payMethod;
            return this;
        }

        @JsonProperty("bankCode")
        public String getBankCode()
        {
            return bankCode;
        }

        @JsonProperty("bankCode")
        public PaymentOptions setBankCode(String bankCode)
        {
            this.bankCode = bankCode;
            return this;
        }

        @JsonProperty("cardNo")
        public String getCardNo()
        {
            return cardNo;
        }

        @JsonProperty("cardNo")
        public PaymentOptions setCardNo(String cardNo)
        {
            this.cardNo = cardNo;
            return this;
        }

        @JsonProperty("savedCardId")
        public String getSavedCardId()
        {
            return savedCardId;
        }

        @JsonProperty("savedCardId")
        public PaymentOptions setSavedCardId(String savedCardId)
        {
            this.savedCardId = savedCardId;
            return this;
        }

        @JsonProperty("vpa")
        public String getVpa()
        {
            return vpa;
        }

        @JsonProperty("vpa")
        public PaymentOptions setVpa(String vpa)
        {
            this.vpa = vpa;
            return this;
        }

       // @JsonAnyGetter
        //public Map<String, Object> getAdditionalProperties() {
        //    return this.additionalProperties;
        //}

        /*@JsonAnySetter
        public PaymentOptions setAdditionalProperty(String name, Object value)
        {
            this.additionalProperties.put(name, value);
        }*/




}
