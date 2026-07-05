package com.paytm.dto.NativeDTO.fetchPcfDetail;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class feeRateFactors {
    private String instId;
    private boolean prepaidCard;
    private boolean oneClickSupported;
    private boolean internationalCardPayment;

    private String issuerBank;
    private String cardNetwork;


    public feeRateFactors() {
    }

    public String getInstId() {
        return this.instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public boolean isPrepaidCard() {
        return this.prepaidCard;
    }

    public void setPrepaidCard(boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public boolean isOneClickSupported() {
        return this.oneClickSupported;
    }

    public void setOneClickSupported(boolean oneClickSupported) {
        this.oneClickSupported = oneClickSupported;
    }

    public boolean isInternationalCardPayment() {
        return this.internationalCardPayment;
    }

    public void setInternationalCardPayment(boolean internationalCardPayment) {
        this.internationalCardPayment = internationalCardPayment;
    }

    public Map<String, String> getFeeRateFactorsMap() {
        Map<String, String> feeRateFactors = new HashMap();
        if (StringUtils.isNotBlank(this.instId)) {
            feeRateFactors.put("instId", this.instId);
        }

        if (this.prepaidCard) {
            feeRateFactors.put("prepaidCard", "TRUE");
        }

        if (this.oneClickSupported) {
            feeRateFactors.put("oneClickSupported", "TRUE");
        }

        if (this.internationalCardPayment) {
            feeRateFactors.put("internationalCardPayment", "TRUE");
        }

        return !feeRateFactors.isEmpty() ? feeRateFactors : null;
    }

    public String getIssuerBank() {
        return this.issuerBank;
    }

    public void setIssuerBank(String issuerBank) {
        this.issuerBank = issuerBank;
    }

    public String getCardNetwork() {
        return this.cardNetwork;
    }

    public void setCardNetwork(String cardNetwork) {
        this.cardNetwork = cardNetwork;
    }
}
