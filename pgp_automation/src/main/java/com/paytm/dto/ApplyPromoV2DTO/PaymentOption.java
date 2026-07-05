package com.paytm.dto.ApplyPromoV2DTO;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.paytm.dto.processTransactionV1.CardTokenInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "transactionAmount",
        "payMethod",
        "vpa",
        "bankCode",
        "cardNo",
        "cardTokenInfo",
        "tenure"
})
public class PaymentOption {
    @JsonProperty("transactionAmount")
    private String transactionAmount;
    @JsonProperty("payMethod")
    private String payMethod;
    @JsonProperty("vpa")
    private String vpa;
    @JsonProperty("bankCode")
    private String bankCode;
    @JsonProperty("cardNo")
    private String cardNo;
    @JsonProperty("cardTokenInfo")
    private CardTokenInfo cardTokenInfo;
    @JsonProperty("tenure")
    private String tenure;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("transactionAmount")
    public String getTransactionAmount() {
        return transactionAmount;
    }

    @JsonProperty("cardTokenInfo")
    public PaymentOption setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
        return this;
    }
    @JsonProperty("cardTokenInfo")
    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }
    @JsonProperty("transactionAmount")
    public PaymentOption setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
        return this;
    }

    @JsonProperty("payMethod")
    public String getPayMethod() {
        return payMethod;
    }

    @JsonProperty("payMethod")
    public PaymentOption setPayMethod(String payMethod) {
        this.payMethod = payMethod;
        return this;
    }

    @JsonProperty("vpa")
    public String getVpa() {
        return vpa;
    }

    @JsonProperty("vpa")
    public PaymentOption setVpa(String vpa) {
        this.vpa = vpa;
        return this;
    }

    @JsonProperty("bankCode")
    public String getBankCode() {
        return bankCode;
    }

    @JsonProperty("bankCode")
    public PaymentOption setBankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    @JsonProperty("cardNo")
    public String getCardNo() {
        return cardNo;
    }

    @JsonProperty("cardNo")
    public PaymentOption setCardNo(String cardNo) {
        this.cardNo = cardNo;
        return this;
    }

    @JsonProperty("tenure")
    public String getTenure() {
        return tenure;
    }

    @JsonProperty("tenure")
    public PaymentOption setTenure(String tenure) {
        this.tenure = tenure;
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
    public PaymentOption(String transactionAmount, String payMethod, String vpa, String bankCode, String cardNo, String tenure) {
        this.transactionAmount = transactionAmount;
        this.payMethod =payMethod;
        this.vpa = vpa;
        this.bankCode = bankCode;
        this.cardNo = cardNo;
        this.tenure = tenure;
    }
}
