package com.paytm.dto.processTransactionV1;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "paymentMode",
        "paymodeSequence",
        "hybridLevel",
        "hybridAmount"
})
public class HybridPayModeDetail implements Serializable
{

    @JsonProperty("paymentMode")
    private String paymentMode;
    @JsonProperty("paymodeSequence")
    private Integer paymodeSequence;
    @JsonProperty("hybridLevel")
    private String hybridLevel;
    @JsonProperty("hybridAmount")
    private String hybridAmount;

    public HybridPayModeDetail() {
    }

    /**
     *
     * @param hybridAmount
     * @param paymodeSequence
     * @param hybridLevel
     * @param paymentMode
     */
    public HybridPayModeDetail(String paymentMode, Integer paymodeSequence, String hybridLevel, String hybridAmount) {
        super();
        this.paymentMode = paymentMode;
        this.paymodeSequence = paymodeSequence;
        this.hybridLevel = hybridLevel;
        this.hybridAmount = hybridAmount;
    }

    @JsonProperty("paymentMode")
    public String getPaymentMode() {
        return paymentMode;
    }

    @JsonProperty("paymentMode")
    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @JsonProperty("paymodeSequence")
    public Integer getPaymodeSequence() {
        return paymodeSequence;
    }

    @JsonProperty("paymodeSequence")
    public void setPaymodeSequence(Integer paymodeSequence) {
        this.paymodeSequence = paymodeSequence;
    }

    @JsonProperty("hybridLevel")
    public String getHybridLevel() {
        return hybridLevel;
    }

    @JsonProperty("hybridLevel")
    public void setHybridLevel(String hybridLevel) {
        this.hybridLevel = hybridLevel;
    }

    @JsonProperty("hybridAmount")
    public String getHybridAmount() {
        return hybridAmount;
    }

    @JsonProperty("hybridAmount")
    public void setHybridAmount(String hybridAmount) {
        this.hybridAmount = hybridAmount;
    }

}